package com.owfar.android.api.file;

import com.owfar.android.models.api.classes.Media;

import java.io.File;

public interface FileDelegate {

    void onProgressUpdated(int requestCode, Media mediaFile, long fileSize, long fileSizeDownloaded);

    void onFileDownloaded(int requestCode, Media mediaFile, File file);

    void onError(int requestCode, Error error);

    //region SimpleAuthDelegate
    class SimpleAuthDelegate implements FileDelegate {

        @Override
        public void onProgressUpdated(int requestCode, Media mediaFile, long fileSize, long fileSizeDownloaded) {

        }

        @Override
        public void onFileDownloaded(int requestCode, Media mediaFile, File file) {

        }

        @Override
        public void onError(int requestCode, Error error) {

        }
    }
    //endregion
}
