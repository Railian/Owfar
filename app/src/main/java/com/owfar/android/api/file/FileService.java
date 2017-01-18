package com.owfar.android.api.file;

import com.owfar.android.api.ApiFactory;
import com.owfar.android.models.api.enums.MediaSize;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface FileService {

    String USERS_PREFIX = ApiFactory.API_PREFIX + "/users";

    @Streaming
    @GET
    @Headers(DownloadProgressInterceptor.HEADER_ANNOTATION)
    Call<ResponseBody> downloadFile(
            @Url String fileUrl,
            @Query("key") String notificationNey);

    @Streaming
    @GET(USERS_PREFIX + "/me/messages/{message_id}/media")
    @Headers(DownloadProgressInterceptor.HEADER_ANNOTATION)
    Call<ResponseBody> downloadMessageFile(
            @Path("message_id") String messageId,
            @Query("access_token") String accessToken,
            @Query("size") MediaSize size,
            @Query("key") String notificationNey);

//    @Streaming
//    @POST
//    @Headers(UploadProgressInterceptor.HEADER_ANNOTATION)
//    Call<Progress.ProgressResponseBody> uploadFile(
//            @Url String fileUrl,
//            @Query("key") String notificationNey);

    @Streaming
    @Multipart
    @POST(USERS_PREFIX + "/me/{stream_type}/{stream_id}/message/file")
    Call<ResponseBody> uploadFile2(
            @Path("stream_id") long streamId,
            @Path("stream_type") String streamTypeTitle,
            @Query("access_token") String accessToken,
            @Query("sid") String sid,
            @Query("header") String header,
            @Query("content") String content,
//            @Part MultipartBody.Part sid,
            @Part MultipartBody.Part file
    );

}