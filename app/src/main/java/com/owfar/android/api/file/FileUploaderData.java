package com.owfar.android.api.file;

import java.io.File;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class FileUploaderData {

    private File file;
    private Call<ResponseBody> call;

    public FileUploaderData(File file, Call<ResponseBody> call) {
        this.file = file;
        this.call = call;
    }

    public File getFile() {
        return file;
    }

    public Call<ResponseBody> getCall() {
        return call;
    }
}
