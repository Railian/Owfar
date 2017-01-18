package com.owfar.android.api.file;

import android.text.TextUtils;

import com.owfar.android.DelegatesSet;
import com.owfar.android.api.users.ProgressListener;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

public class DownloadProgressInterceptor implements Interceptor {

    private static final String DOWNLOAD_WITH_PROGRESS = "DOWNLOAD_WITH_PROGRESS";
    public static final String HEADER_ANNOTATION = "@: " + DOWNLOAD_WITH_PROGRESS;

    private static DelegatesSet<ProgressListener> delegatesSet = new DelegatesSet<>(ProgressListener.class);

//    public static DelegatesSet<ProgressListener> getDelegatesSet() {
//        return delegatesSet;
//    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Set<String> annotations = new HashSet<>(request.headers("@"));
        boolean containsRequiredAnnotation = annotations.contains(DOWNLOAD_WITH_PROGRESS);
        request = request.newBuilder().removeHeader("@").build();

        String key = request.url().queryParameter("key");

        Response response = chain.proceed(request);
        if (containsRequiredAnnotation) {
            ResponseBody newBody = new DownloadProgressResponseBody(response.body(), key);
            response = response.newBuilder().body(newBody).build();
        }

        return response;
    }

    private static class DownloadProgressResponseBody extends ResponseBody {

        private final ResponseBody responseBody;
        private final String key;
        private BufferedSource bufferedSource;

        DownloadProgressResponseBody(ResponseBody responseBody, String key) {
            this.responseBody = responseBody;
            this.key = key;
        }

        @Override
        public MediaType contentType() {
            return responseBody.contentType();
        }

        @Override
        public long contentLength() {
            return responseBody.contentLength();
        }

        @Override
        public BufferedSource source() {
            if (bufferedSource == null) bufferedSource = Okio.buffer(source(responseBody.source()));
            return bufferedSource;
        }

        private Source source(Source source) {
            return new ForwardingSource(source) {
                long totalBytesRead = 0L;

                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    // read() returns the number of bytes read, or -1 if this source is exhausted.
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                    if (!TextUtils.isEmpty(key) && bytesRead != -1)
                        FileManager.get().getDownloadDelegatesSet().notify(key).onUpdated(totalBytesRead, contentLength());
                    return bytesRead;
                }
            };
        }
    }
}