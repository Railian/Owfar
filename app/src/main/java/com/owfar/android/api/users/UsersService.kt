package com.owfar.android.api.users

import com.owfar.android.api.ApiFactory
import com.owfar.android.models.api.*
import com.owfar.android.models.api.classes.*
import io.realm.RealmList
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


interface UsersService {

    companion object {
        const val USERS_PREFIX = "${ApiFactory.API_PREFIX}/users"
    }

    @GET("$USERS_PREFIX/me/messages/{message_id}")
    fun getMessageById(
            @Path("message_id") messageId: Long,
            @Query("access_token") accessToken: String
    ): Call<ReceivedMessage>

    @POST("$USERS_PREFIX/verify")
    @FormUrlEncoded
    fun requestForVerificationCode(
            @Field("phone") phone: String
    ): Call<ResponseBody>

    @POST(USERS_PREFIX)
    @FormUrlEncoded
    fun createUser(
            @Field("phone") phone: String,
            @Field("code") code: String
    ): Call<UserCreation>

    @POST("$USERS_PREFIX/me/contacts/{contact_id}")
    fun addNewContact(
            @Path("contact_id") contactId: Long,
            @Query("access_token") accessToken: String
    ): Call<ResponseBody>

    @POST("$USERS_PREFIX/me/contacts/convert")
    fun convertMobileContactsToOwfar(
            @Query("access_token") access_token: String,
            @Body phonesList: PhoneList
    ): Call<RealmList<User>>

    @GET("$USERS_PREFIX/me/available_contact")
    fun findContact(
            @Query("access_token") accessToken: String,
            @Query("phone") phone: String
    ): Call<ContactSearching>

    @GET("$USERS_PREFIX/me/contacts")
    fun getContactList(
            @Query("access_token") accessToken: String
    ): Call<ContactList>

    @DELETE("$USERS_PREFIX/me/contacts/{contact_id}")
    fun deleteContact(
            @Path("contact_id") contactId: Long,
            @Query("access_token") accessToken: String
    ): Call<ResponseBody>

    @PUT("$USERS_PREFIX/{user_id}/profile")
    @FormUrlEncoded
    fun updateProfile(
            @Path("user_id") userId: String,
            @Query("access_token") accessToken: String,
            @Field("fname") firstName: String,
            @Field("lname") lastName: String,
            @Field("gender") gender: String
    ): Call<Profile>

    @PUT("$USERS_PREFIX/{user_id}/photo")
    @Multipart
    fun uploadUserPhoto(
            @Path("user_id") userId: String,
            @Query("access_token") accessToken: String,
            @Part file: MultipartBody.Part
    ): Call<ResponseBody>

    @GET("$USERS_PREFIX/{user_id}/profile")
    fun getProfile(
            @Path("user_id") userId: String,
            @Query("access_token") accessToken: String
    ): Call<Profile>

    @PUT("$USERS_PREFIX/{user_id}/settings")
    @FormUrlEncoded
    fun updateSettings(
            @Path("user_id") userId: String,
            @Query("access_token") accessToken: String,
            @Field("language") language: String,
            @Field("notifications") notifications: Boolean
    ): Call<Void>

    @GET("$USERS_PREFIX/{user_id}/settings")
    fun getSettings(
            @Path("user_id") userId: String,
            @Query("access_token") accessToken: String
    ): Call<Settings>

    @GET("$USERS_PREFIX/me/streams")
    fun getStreams(
            @Query("access_token") accessToken: String
    ): Call<RealmList<Stream>>

    @POST("$USERS_PREFIX/me/contacts/{contact_id}/conversations")
    fun startConversation(
            @Path("contact_id") contactId: Long,
            @Query("access_token") accessToken: String
    ): Call<Stream>

    @DELETE("$USERS_PREFIX/me/conversations/{conversation_id}")
    fun deleteConversation(
            @Path("conversation_id") conversationId: Long,
            @Query("access_token") accessToken: String
    ): Call<ResponseBody>

    @POST("$USERS_PREFIX/me/chats")
    fun createChat(
            @Query("access_token") accessToken: String,
            @Body body: ChatUsersBody
    ): Call<Stream>

    @DELETE("$USERS_PREFIX/me/chats/{chat_id}")
    fun deleteChat(
            @Path("chat_id") chatId: Long,
            @Query("access_token") accessToken: String
    ): Call<ResponseBody>

    @GET("$USERS_PREFIX/me/{stream_type}/{stream_id}")
    fun getStreamById(
            @Path("stream_type") streamType: String, // chats, conversations, interests
            @Path("stream_id") streamId: Long,
            @Query("access_token") accessToken: String
    ): Call<Stream>

    @PUT("$USERS_PREFIX/me/chats/{chat_id}")
    @FormUrlEncoded
    fun updateChat(
            @Path("chat_id") chatId: Long,
            @Query("access_token") accessToken: String,
            @Field("name") name: String
    ): Call<Stream>

    @PUT("$USERS_PREFIX/me/chats/{chat_id}/photo")
    @Multipart
    fun uploadPhotoForChat(
            @Path("chat_id") chatId: Long,
            @Query("access_token") accessToken: String,
            @Part file: MultipartBody.Part
    ): Call<ResponseBody>

    @GET("$USERS_PREFIX/me/chats/{chat_id}/users")
    fun getUsersOutsideChat(
            @Path("chat_id") chatId: Long,
            @Query("access_token") access_token: String
    ): Call<ContactList>

    @GET("$USERS_PREFIX/me/{stream_type}/{stream_id}/messages")
    fun loadMessagesFromStream(
            @Path("stream_id") streamId: Long,
            @Path("stream_type") streamType: String,
            @Query("access_token") accessToken: String,
            @Query("offset") offset: Int,
            @Query("limit") limit: Int
    ): Call<MessageList>

    @POST("$USERS_PREFIX/me/chats/{chat_id}/invite")
    fun inviteUsersToChat(
            @Path("chat_id") chatId: Long,
            @Query("access_token") accessToken: String,
            @Body body: ChatUsersBody
    ): Call<ResponseBody>

    @POST("$USERS_PREFIX/me/chats/{chat_id}/users/{user_id}/kick")
    fun kickUserFromChat(
            @Path("chat_id") chatId: Long,
            @Path("user_id") userId: Long,
            @Query("access_token") accessToken: String
    ): Call<ResponseBody>

    @POST("$USERS_PREFIX/me/chats/{chat_id}/leave")
    fun leaveFromChat(
            @Path("chat_id") chatId: Long,
            @Query("access_token") accessToken: String
    ): Call<ResponseBody>

    @POST("$USERS_PREFIX/me/messages/{message_id}/{message_status}")
    fun setMessageStatus(
            @Path("message_id") messageId: Long,
            @Path("message_status") messageStatus: String /* delivered / seen */,
            @Query("access_token") accessToken: String
    ): Call<ResponseBody>

    @POST("$USERS_PREFIX/me/{stream_type}/{stream_id}/message/file")
    @Multipart
    fun uploadFile(
            @Path("stream_id") streamId: Long,
            @Path("stream_type") streamTypeTitle: String,
            @Query("access_token") accessToken: String,
            @Query("sid") sid: String,
            @Query("header") header: String?,
            @Query("content") content: String?,
            @Part file: MultipartBody.Part
    ): Call<ResponseBody>

    @GET("$USERS_PREFIX/me/stickers_groups")
    fun getStickersForUser(
            @Query("access_token") accessToken: String
    ): Call<StickerList>

    @DELETE("$USERS_PREFIX/me/messages/{message_id}")
    fun deleteMessage(
            @Path("message_id") messageId: Long,
            @Query("access_token") accessToken: String
    ): Call<ResponseBody>

    @GET("$USERS_PREFIX/me/interests_groups")
    fun getInterestsGroup(
            @Query("access_token") accessToken: String
    ): Call<RealmList<InterestsGroup>>

    @POST("$USERS_PREFIX/me/interests_groups/{interests_group_id}/subscribe")
    fun subscribeInterestsGroup(
            @Path("interests_group_id") interestsGroupId: Long,
            @Query("access_token") accessToken: String
    ): Call<ResponseBody>

    @POST("$USERS_PREFIX/me/interests_groups/{interests_group_id}/unsubscribe")
    fun unsubscribeInterestsGroup(
            @Path("interests_group_id") interestsGroupId: Long,
            @Query("access_token") accessToken: String
    ): Call<ResponseBody>

    @POST("$USERS_PREFIX/me/interests/{interest_id}/subscribe")
    fun subscribeInterest(
            @Path("interest_id") interestId: Long,
            @Query("access_token") accessToken: String
    ): Call<ResponseBody>

    @POST("$USERS_PREFIX/me/interests/{interest_id}/unsubscribe")
    fun unsubscribeInterest(
            @Path("interest_id") interestId: Long,
            @Query("access_token") accessToken: String
    ): Call<ResponseBody>

    @GET("$USERS_PREFIX/me/interests/{interest_id}/info")
    fun getInterestInfoById(
            @Path("interest_id") interestId: Long,
            @Query("access_token") accessToken: String
    ): Call<Stream>

    @POST("$USERS_PREFIX/me/devices")
    @FormUrlEncoded
    fun registerDeviceToken(
            @Query("access_token") accessToken: String,
            @Field("token") deviceToken: String,
            @Field("type") deviceType: String /* android / ios */
    ): Call<ResponseBody>

    @POST("$USERS_PREFIX/me/{stream_type}/{stream_id}/notifications")
    @FormUrlEncoded
    fun updateNotificationSettings(
            @Path("stream_id") streamId: Long,
            @Path("stream_type") streamType: String,
            @Query("access_token") accessToken: String,
            @Field("notifications") notifications: Boolean
    ): Call<ResponseBody>

}