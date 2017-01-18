package com.owfar.android.api.file;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.owfar.android.DelegatesSet;
import com.owfar.android.api.ApiFactory;
import com.owfar.android.api.users.ProgressListener;
import com.owfar.android.media.Extension;
import com.owfar.android.models.api.enums.StreamType;
import com.owfar.android.settings.CurrentUserSettings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class FileManager {

    //region fields
    private final FileService fileService;
    //endregion

    //region Singleton Implementation
    private static FileManager fileManager;

    private FileManager() {
        fileService = ApiFactory.getFileService();
        uploadDelegatesSet.addDelegate("key", new ProgressListener.Simple() {
            @Override
            public void onStarted() {
                Log.d("TAG", "onStarted() called");
            }

            @Override
            public void onUpdated(long bytesRead, long contentLength) {
                Log.d("TAG", "onUpdated() called with: bytesRead = [" + bytesRead + "], contentLength = [" + contentLength + "]");
            }

            @Override
            public void onFinished() {
                Log.d("TAG", "onFinished() called");
            }

            @Override
            public void onCompleted() {
                Log.d("TAG", "onCompleted() called");
            }

            @Override
            public void onCancelled() {
                Log.d("TAG", "onCancelled() called");
            }

            @Override
            public void onError(Throwable t) {
                Log.d("TAG", "onError() called");
            }
        });
    }

    public static FileManager get() {
        return fileManager == null ? fileManager = new FileManager() : fileManager;
    }
    //endregion

    private DelegatesSet<ProgressListener> downloadDelegatesSet = new DelegatesSet<>(ProgressListener.class);
    private DelegatesSet<ProgressListener> uploadDelegatesSet = new DelegatesSet<>(ProgressListener.class);

    public DelegatesSet<ProgressListener> getDownloadDelegatesSet() {
        return downloadDelegatesSet;
    }

    public DelegatesSet<ProgressListener> getUploadDelegatesSet() {
        return uploadDelegatesSet;
    }

    //region Requests

    //region Downloading
    private Map<String, DownloadFileTask> downloadFileTasks = new TreeMap<>();

    public void downloadFile(@NonNull String path, @NonNull final File file, String key, Callback callback) {
        cancelDownload(key);
        DownloadFileTask downloadFileTask = new DownloadFileTask(path, file, key, callback);
        downloadFileTasks.put(key, downloadFileTask);
        downloadFileTask.execute();
    }

    public void cancelDownload(@NonNull final String key) {
        DownloadFileTask downloadFileTask = downloadFileTasks.remove(key);
        if (downloadFileTask != null) downloadFileTask.cancel(true);
    }

    private static void writeToFile(File file, ResponseBody responseBody) throws IOException {
        Log.d("TAG", "writeFileToSD() called with: file = [" + file + "], responseBody = [" + responseBody + "]");
        FileOutputStream stream = new FileOutputStream(file);
        byte[] buf = responseBody.bytes();
        stream.write(buf);
        stream.close();
    }

    private class DownloadFileTask extends AsyncTask<String, Void, File> {

        private final String path;
        private final File file;
        private final String key;
        private final Callback callback;

        public DownloadFileTask(String path, File file, String key, Callback callback) {
            this.path = path;
            this.file = file;
            this.key = key;
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
            downloadDelegatesSet.notify(key).onStarted();
            super.onPreExecute();
        }

        @Override
        protected File doInBackground(String... params) {
            Call<ResponseBody> call = fileService.downloadFile(path, key);
            try {
                Response<ResponseBody> response = call.execute();
                if (response.body() != null) {
                    writeToFile(file, response.body());
                    downloadDelegatesSet.notify(key).onCompleted();
                    if (callback != null) callback.onCompleted();
                } else if (response.code() == 403)
                    downloadDelegatesSet.notify(key).onError(new UrlExpiredException());
            } catch (IOException e) {
                e.printStackTrace();
                downloadDelegatesSet.notify(key).onError(e);
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            file.delete();
            downloadDelegatesSet.notify(key).onCancelled();
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(File file) {
            downloadDelegatesSet.notify(key).onFinished();
            if (callback != null) callback.onFinished();
            super.onPostExecute(file);
        }
    }
    //endregion

    //region Uploading
    private Map<String, UploadFileTask> uploadFileTasks = new TreeMap<>();

    public void uploadFile(long streamId, StreamType streamType, @NonNull final File file, String key, Callback callback) {
        cancelUpload(key);
        UploadFileTask uploadFileTask = new UploadFileTask(streamId, streamType, file, key, callback);
        uploadFileTasks.put(key, uploadFileTask);
        uploadFileTask.execute();
    }

    public void cancelUpload(@NonNull final String key) {
        UploadFileTask uploadFileTask = uploadFileTasks.remove(key);
        if (uploadFileTask != null) uploadFileTask.cancel(true);
    }

    private class UploadFileTask extends AsyncTask<String, Void, File> {

        private final long streamId;
        private final StreamType streamType;
        private final File file;
        private final String key;
        private final Callback callback;

        public UploadFileTask(long streamId, StreamType streamType, File file, String key, Callback callback) {
            this.streamId = streamId;
            this.streamType = streamType;
            this.file = file;
            this.key = key;
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
            uploadDelegatesSet.notify(key).onStarted();
            super.onPreExecute();
        }

        @Override
        protected File doInBackground(String... params) {

            String accessToken = CurrentUserSettings.INSTANCE.getAccessToken();

            long userId = CurrentUserSettings.INSTANCE.getCurrentUser().getId();
            long timestamp = System.currentTimeMillis();
            String sid = String.format("%s_%s_from_%s_at_%s", streamType, streamId, userId, timestamp);

            String header = null;
            String content = null;

            MediaType mediaType = null;
            Extension extension = Extension.Companion.getFromFile(file);
            if (extension != null) switch (extension) {
                case JPEG:
                    mediaType = MediaType.parse("image/jpeg");
                    break;
                case PNG:
                    mediaType = MediaType.parse("image/png");
                    break;
            }

//            RequestBody fileRequest = RequestBody.create(mediaType, file);
//            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", file.getName(), fileRequest);
//            Call<ResponseBody> call = fileService.uploadFile2(streamId, streamType.getTitle(), accessToken, key, sidPart, filePart);

            RequestBody fileRequest = new UploadProgressInterceptor.UploadProgressRequestBody2(mediaType, file, key);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", file.getName(), fileRequest);
            Call<ResponseBody> call = fileService.uploadFile2(streamId, streamType.getJsonName(), accessToken, sid, header, content, filePart);
            try {
                Response<ResponseBody> response = call.execute();
                if (response.isSuccessful()) {
                    uploadDelegatesSet.notify(key).onCompleted();
                    if (callback != null) callback.onCompleted();
                }
            } catch (IOException e) {
                e.printStackTrace();
                uploadDelegatesSet.notify(key).onError(e);
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            uploadDelegatesSet.notify(key).onCancelled();
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(File file) {
            uploadDelegatesSet.notify(key).onFinished();
            if (callback != null) callback.onFinished();
            super.onPostExecute(file);
        }
    }
    //endregion

    //endregion

    public interface Callback {
        void onCompleted();

        void onFinished();
    }
}
