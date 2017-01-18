package com.owfar.android.api.file;


import com.owfar.android.DelegatesSet;
import com.owfar.android.api.users.ProgressListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;

public class UploadProgressInterceptor implements Interceptor {

    private static final String UPLOAD_WITH_PROGRESS = "UPLOAD_WITH_PROGRESS";
    public static final String HEADER_ANNOTATION = "@: " + UPLOAD_WITH_PROGRESS;

    private static DelegatesSet<ProgressListener> delegatesSet = new DelegatesSet<>(ProgressListener.class);

    public static DelegatesSet<ProgressListener> getDelegatesSet() {
        return delegatesSet;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Set<String> annotations = new HashSet<>(request.headers("@"));
        request = request.newBuilder().removeHeader("@").build();

        String key = request.url().queryParameter("key");

        if (annotations.contains(UPLOAD_WITH_PROGRESS)) {
            RequestBody newBody = new UploadProgressRequestBody(request.body(), key);
            request = request.newBuilder().method(request.method(), newBody).build();
        }

        return chain.proceed(request);
    }

    public static class UploadProgressRequestBody extends RequestBody {

        private final RequestBody requestBody;
        private final String key;

        UploadProgressRequestBody(RequestBody requestBody, String key) {
            this.requestBody = requestBody;
            this.key = key;
        }

        public static RequestBody create(final MediaType contentType, final File file) {
            return RequestBody.create(contentType, file);
        }

        @Override
        public MediaType contentType() {
            return requestBody.contentType();
        }

        @Override
        public long contentLength() {
            try {
                return requestBody.contentLength();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return -1;
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            BufferedSink bufferedSink;

            ForwardingSink countingSink = new ForwardingSink(sink) {
                private long bytesWritten = 0;

                @Override
                public void write(Buffer source, long byteCount) throws IOException {
                    super.write(source, byteCount);
                    bytesWritten += byteCount;
                    FileManager.get().getUploadDelegatesSet().notify(key).onUpdated(bytesWritten, contentLength());
                }
            };

            bufferedSink = Okio.buffer(countingSink);
            requestBody.writeTo(bufferedSink);
            bufferedSink.flush();
        }
    }


    public static class UploadProgressRequestBody2 extends RequestBody {

        private final MediaType contentType;
        private final File file;
        private final String key;

        UploadProgressRequestBody2(MediaType contentType, File file, String key) {
            this.contentType = contentType;
            this.file = file;
            this.key = key;
        }

        @Override
        public MediaType contentType() {
            return contentType;
        }

        @Override
        public long contentLength() {
            return file.length();
        }

        private static final int DEFAULT_BUFFER_SIZE = 2048;

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            FileInputStream in = new FileInputStream(file);
            long bytesWritten = 0;
            try {
                int read;
                while ((read = in.read(buffer)) != -1) {
                    bytesWritten += read;
                    sink.write(buffer, 0, read);
                    //use interface for updating activity
                    FileManager.get().getUploadDelegatesSet().notify(key).onUpdated(bytesWritten, contentLength());
                }
            } finally {
                in.close();
            }
        }


//            Source source = null;
//            try {
//                source = Okio.source(file);
//                 long bytesWritten = 0;
//                long byteCount;
//
//                while ((byteCount = source.read(sink.buffer(), SEGMENT_SIZE)) != -1) {
//                    bytesWritten += byteCount;
//                    sink.flush();
//                    FileManager.get().getUploadDelegatesSet().notify(key).onUpdated(bytesWritten, contentLength());
//                }
//            } finally {
//                Util.closeQuietly(source);
//            }
    }
}