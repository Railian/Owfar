package com.owfar.android.api.users

import android.util.Log
import com.owfar.android.DelegatesSet
import com.owfar.android.InvalidTokenException
import com.owfar.android.api.ApiCallback
import com.owfar.android.api.ApiFactory
import com.owfar.android.api.file.FileUploaderData
import com.owfar.android.data.logFun
import com.owfar.android.media.Extension
import com.owfar.android.models.api.*
import com.owfar.android.models.api.classes.*
import com.owfar.android.models.api.enums.MessageBodyType
import com.owfar.android.models.api.enums.MessageStatus
import com.owfar.android.models.api.enums.StreamType
import com.owfar.android.models.api.interfaces.Message
import com.owfar.android.models.errors.Error
import com.owfar.android.settings.CurrentUserSettings
import io.realm.RealmList
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import java.io.File
import java.util.*

object UsersManager {

    //region constants
    @JvmStatic val TAG = UsersManager::class.java.simpleName

    const val MY_USER_ID = "me"
    //endregion

    //region fields
    private val usersService = ApiFactory.usersService
    val delegatesSet = DelegatesSet(UsersDelegate::class.java)
    //endregion

    //region Private Tools
    private val accessToken: String
        get() = CurrentUserSettings.accessToken ?: throw InvalidTokenException()
    //endregion

    //region Callback Creator
    private fun <T> apiCallback(responseType: Class<T>, tag: String?, requestCode: Int?,
                                onSuccess: (T) -> Unit) = object : ApiCallback<T>() {
        override fun onSuccess(body: T) {
            logFun(TAG, ApiCallback<T>::onSuccess, body)
            onSuccess(body)
        }

        override fun onError(error: Error) {
            logFun(TAG, ApiCallback<T>::onError, error)
            delegatesSet.notify(tag).onError(requestCode, error)
            if (error.code == 401 && error.error == "invalid_token")
                throw InvalidTokenException()
        }

        override fun onFailure(t: Throwable) {
            Log.d(TAG, "TAG: $tag -> onFailure: $t")
            delegatesSet.notify(tag).onFailure(requestCode, t)
        }
    }
    //endregion

    //region Requests
    fun getMessageById(tag: String? = null, requestCode: Int? = null, messageId: Long) {
        val call = usersService.getMessageById(messageId, accessToken)
        call.enqueue(apiCallback(ReceivedMessage::class.java, tag, requestCode) { message ->
            delegatesSet.notify(tag).onMessageReceived(requestCode, message)
        })
    }

    fun requestForVerificationCode(tag: String? = null, requestCode: Int? = null, phone: String) {
        val call = usersService.requestForVerificationCode(phone)
        call.enqueue(apiCallback(ResponseBody::class.java, tag, requestCode) {
            delegatesSet.notify(tag).onRequestForVerificationCodeSent(requestCode)
        })
    }

    fun createUser(tag: String? = null, requestCode: Int? = null, username: String, code: String) {
        val call = usersService.createUser(username, code)
        call.enqueue(apiCallback(UserCreation::class.java, tag, requestCode) { userCreation ->
            delegatesSet.notify(tag).onCreateUser(requestCode, userCreation)
        })
    }

    fun addNewContact(tag: String? = null, requestCode: Int? = null, contactId: Long) {
        val call = usersService.addNewContact(contactId, accessToken)
        call.enqueue(apiCallback(ResponseBody::class.java, tag, requestCode) {
            delegatesSet.notify(tag).onNewContactAdded(requestCode)
        })
    }

    fun convertMobileContactsToOwfar(tag: String? = null, requestCode: Int? = null, users: List<User>) {
        val phonesList = PhoneList().apply { phones = users.map(User::phone).requireNoNulls() }
        val call = usersService.convertMobileContactsToOwfar(accessToken, phonesList)
        call.enqueue(object : ApiCallback<RealmList<User>>() {
            override fun onSuccess(contacts: RealmList<User>) {
                delegatesSet.notify(tag).onMobileContactsConvertedToOwfar(requestCode, contacts)
            }

            override fun onError(error: com.owfar.android.models.errors.Error) {
                delegatesSet.notify(tag).onError(requestCode, error)
            }

            override fun onFailure(t: Throwable) {
                delegatesSet.notify(tag).onFailure(requestCode, t)
            }
        })
    }

    fun findContact(tag: String? = null, requestCode: Int? = null, phone: String) {
        val call = usersService.findContact(accessToken, phone)
        call.enqueue(apiCallback(ContactSearching::class.java, tag, requestCode) { contactSearching ->
            contactSearching.user?.let { user ->
                delegatesSet.notify(tag).onContactFound(requestCode, user)
            } ?: delegatesSet.notify(tag).onContactNotFound(requestCode, phone)
        })
    }

    fun getContactList(tag: String? = null, requestCode: Int? = null) {
        val call = usersService.getContactList(accessToken)
        call.enqueue(apiCallback(ContactList::class.java, tag, requestCode) { contactList ->
            val users = contactList.users?.apply { forEach { it.hasOwfar = true } }
            delegatesSet.notify(tag).onContactListReceived(requestCode, users)
        })
    }

    fun deleteContact(tag: String? = null, requestCode: Int? = null, contact: User) {
        val call = usersService.deleteContact(contact.id, accessToken)
        call.enqueue(apiCallback(ResponseBody::class.java, tag, requestCode) {
            delegatesSet.notify(tag).onContactDeleted(requestCode, contact)
        })
    }

    fun updateProfile(tag: String? = null, requestCode: Int? = null, userId: String, firstName: String, lastName: String, gender: String) {
        val call = usersService.updateProfile(userId, accessToken, firstName, lastName, gender)
        call.enqueue(apiCallback(Profile::class.java, tag, requestCode) { profile ->
            delegatesSet.notify(tag).onProfileUpdated(requestCode, profile)
        })
    }

    fun getProfile(tag: String? = null, requestCode: Int? = null, userId: String) {
        val call = usersService.getProfile(userId, accessToken)
        call.enqueue(apiCallback(Profile::class.java, tag, requestCode) { profile ->
            delegatesSet.notify(tag).onProfileReceived(requestCode, profile)
        })
    }

    //region upload user photo
    private var uploadUserPhotoData: FileUploaderData? = null

    val isUserPhotoUploading: Boolean
        get() = uploadUserPhotoData != null

    val uploadingUserPhoto: File?
        get() = if (isUserPhotoUploading) uploadUserPhotoData!!.file else null

    fun uploadUserPhoto(tag: String? = null, requestCode: Int? = null, image: File) {
        cancelUploadUserPhoto()

        val mediaType: MediaType? = when (Extension.getFromFile(image)) {
            Extension.JPEG -> MediaType.parse("image/jpeg")
            Extension.PNG -> MediaType.parse("image/png")
            else -> null
        }
        val requestFile = RequestBody.create(mediaType, image)
        val filePart = MultipartBody.Part.createFormData("file", image.name, requestFile)

        val accessToken = CurrentUserSettings.accessToken ?: ""
        uploadUserPhotoData = FileUploaderData(image, usersService.uploadUserPhoto(UsersManager.MY_USER_ID, accessToken, filePart))
        uploadUserPhotoData!!.call.enqueue(object : ApiCallback<ResponseBody>() {
            override fun onSuccess(body: ResponseBody) {
                delegatesSet.notify(tag).onUserPhotoUploaded(requestCode)
                uploadUserPhotoData = null
            }

            override fun onError(error: com.owfar.android.models.errors.Error) {
                delegatesSet.notify(tag).onError(requestCode, error)
                uploadUserPhotoData = null
            }

            override fun onFailure(throwable: Throwable) {
                delegatesSet.notify(tag).onFailure(requestCode, throwable)
                uploadUserPhotoData = null
            }
        })
    }

    fun cancelUploadUserPhoto(): Boolean {
        if (isUserPhotoUploading) {
            uploadUserPhotoData!!.call.cancel()
            uploadUserPhotoData = null
            return true
        } else
            return false
    }
    //endregion

    fun getSettings(tag: String? = null, requestCode: Int? = null) {
        val call = usersService.getSettings(MY_USER_ID, accessToken)
        call.enqueue(apiCallback(Settings::class.java, tag, requestCode) { settings ->
//            delegatesSet.notify(tag).onConversationStarted(requestCode, stream)
        })
    }

    fun getStreams(tag: String? = null, requestCode: Int? = null) {
        val call = usersService.getStreams(accessToken)
        call.enqueue(object : ApiCallback<RealmList<Stream>>() {
            override fun onSuccess(streams: RealmList<Stream>) {
                streams.forEach { it.sid = Stream.generateSid(StreamType.find(it.type), it.id) }
                delegatesSet.notify(tag).onStreamsReceived(requestCode, streams)
            }

            override fun onError(error: com.owfar.android.models.errors.Error) {
                delegatesSet.notify(tag).onError(requestCode, error)
            }

            override fun onFailure(throwable: Throwable) {
                delegatesSet.notify(tag).onFailure(requestCode, throwable)
            }
        })
    }

    fun startConversation(tag: String? = null, requestCode: Int? = null, user: User) {
        val call = usersService.startConversation(user.id, accessToken)
        call.enqueue(apiCallback(Stream::class.java, tag, requestCode) { stream ->
            stream.apply {
                type = StreamType.CONVERSATIONS.jsonName
                sid = Stream.generateSid(StreamType.CONVERSATIONS, id)
            }
            delegatesSet.notify(tag).onConversationStarted(requestCode, stream)
        })
    }

    fun deleteConversation(tag: String? = null, requestCode: Int? = null, conversation: Stream) {
        val call = usersService.deleteConversation(conversation.id, accessToken)
        call.enqueue(apiCallback(ResponseBody::class.java, tag, requestCode) {
            conversation.apply {
                type = StreamType.CONVERSATIONS.jsonName
                sid = Stream.generateSid(StreamType.CONVERSATIONS, id)
            }
            delegatesSet.notify(tag).onConversationDeleted(requestCode, conversation)
        })
    }

    fun createChat(tag: String? = null, requestCode: Int? = null, users: RealmList<User>?) {
        val body = ChatUsersBody().apply { usersIds = users?.map(User::id) }
        val call = usersService.createChat(accessToken, body)
        call.enqueue(apiCallback(Stream::class.java, tag, requestCode) { stream ->
            stream.apply {
                type = StreamType.CHATS.jsonName
                sid = Stream.generateSid(StreamType.CHATS, id)
            }
            delegatesSet.notify(tag).onChatCreated(requestCode, stream)
        })
    }

    fun deleteChat(tag: String? = null, requestCode: Int? = null, chat: Stream) {
        val call = usersService.deleteChat(chat.id, accessToken)
        call.enqueue(apiCallback(ResponseBody::class.java, tag, requestCode) {
            chat.apply {
                type = StreamType.CHATS.jsonName
                sid = Stream.generateSid(StreamType.CHATS, id)
            }
            delegatesSet.notify(tag).onChatDeleted(requestCode, chat)
        })
    }

    fun getStreamById(tag: String? = null, requestCode: Int? = null, streamType: StreamType, streamId: Long) {
        val call = usersService.getStreamById(streamType.jsonName, streamId, accessToken)
        call.enqueue(apiCallback(Stream::class.java, tag, requestCode) { stream ->
            stream.apply {
                type = streamType.jsonName
                sid = Stream.generateSid(streamType, streamId)
            }
            delegatesSet.notify(tag).onStreamReceived(requestCode, stream)
        })
    }

    fun updateChat(tag: String? = null, requestCode: Int? = null, chatId: Long, chatName: String) {
        val call = usersService.updateChat(chatId, accessToken, chatName)
        call.enqueue(apiCallback(Stream::class.java, tag, requestCode) { chat ->
            chat.type = StreamType.CHATS.jsonName
            chat.sid = Stream.generateSid(StreamType.CHATS, chat.id)
            delegatesSet.notify(tag).onChatUpdated(requestCode, chat)
        })
    }

    //region upload photo for chat
    private var uploadPhotoForChatData: MutableMap<Long, FileUploaderData>? = null

    fun isPhotoForChatUploading(chatId: Long): Boolean {
        return uploadPhotoForChatData != null && uploadPhotoForChatData!!.containsKey(chatId)
    }

    fun getUploadingPhotoForChat(chatId: Long): File? {
        return if (isPhotoForChatUploading(chatId)) uploadPhotoForChatData!![chatId]?.getFile() else null
    }

    fun uploadPhotoForChat(tag: String? = null, requestCode: Int? = null, chatId: Long, image: File) {
        val accessToken = CurrentUserSettings.accessToken ?: ""
        val requestFile = RequestBody.create(MediaType.parse("image/jpeg"), image)
        val file = MultipartBody.Part.createFormData("file", image.name, requestFile)

        if (uploadPhotoForChatData == null) uploadPhotoForChatData = HashMap<Long, FileUploaderData>()
        uploadPhotoForChatData!!.put(chatId, FileUploaderData(image, usersService.uploadPhotoForChat(chatId, accessToken, file)))
        uploadPhotoForChatData!![chatId]?.getCall()?.enqueue(object : ApiCallback<ResponseBody>() {
            override fun onSuccess(body: ResponseBody) {
                delegatesSet.notify(tag).onPhotoForChatUploaded(requestCode)
                uploadPhotoForChatData!!.remove(chatId)
            }

            override fun onError(error: com.owfar.android.models.errors.Error) {
                delegatesSet.notify(tag).onError(requestCode, error)
                uploadPhotoForChatData!!.remove(chatId)
            }

            override fun onFailure(throwable: Throwable) {
                delegatesSet.notify(tag).onFailure(requestCode, throwable)
                uploadPhotoForChatData!!.remove(chatId)
            }
        })
    }

    fun cancelUploadPhotoForChat(chatId: Long): Boolean {
        if (uploadPhotoForChatData != null && uploadPhotoForChatData!!.containsKey(chatId)) {
            uploadPhotoForChatData!![chatId]?.getCall()?.cancel()
            uploadPhotoForChatData!!.remove(chatId)
            return true
        } else
            return false
    }
//endregion

    fun getUsersOutsideChat(tag: String? = null, requestCode: Int? = null, chatId: Long) {
        val call = usersService.getUsersOutsideChat(chatId, accessToken)
        call.enqueue(apiCallback(ContactList::class.java, tag, requestCode) { contactList ->
            delegatesSet.notify(tag).onUsersOutsideChatReceived(requestCode, contactList.users)
        })
    }

    fun loadMessagesFromStream(tag: String? = null, requestCode: Int? = null, stream: Stream, offset: Int = 0, limit: Int = 0) {
        val call = usersService.loadMessagesFromStream(stream.id, stream.type.orEmpty(), accessToken, offset, limit)
        call.enqueue(apiCallback(MessageList::class.java, tag, requestCode) { messageList ->
            delegatesSet.notify(tag).onMessagesFromStreamLoaded(requestCode, messageList.messages?.let { RealmList<Message>().apply { addAll(it) } })
        })
    }

    fun inviteUsersToChat(tag: String? = null, requestCode: Int? = null, chat: Stream, users: MutableList<User>?) {
        val body = ChatUsersBody().apply { usersIds = users?.map(User::id) }
        val call = usersService.inviteUsersToChat(chat.id, accessToken, body)
        call.enqueue(apiCallback(ResponseBody::class.java, tag, requestCode) {
            delegatesSet.notify(tag).onUsersInvitedToChat(requestCode, users)
        })
    }

    fun kickUserFromChat(tag: String? = null, requestCode: Int? = null, chat: Stream, user: User) {
        val call = usersService.kickUserFromChat(chat.id, user.id, accessToken)
        call.enqueue(apiCallback(ResponseBody::class.java, tag, requestCode) {
            delegatesSet.notify(tag).onUserKickedFromChat(requestCode, user)
        })
    }

    fun leaveFromChat(tag: String? = null, requestCode: Int? = null, chat: Stream) {
        val call = usersService.leaveFromChat(chat.id, accessToken)
        call.enqueue(apiCallback(ResponseBody::class.java, tag, requestCode) {
            delegatesSet.notify(tag).onChatLeft(requestCode)
        })
    }

    fun setMessageStatus(tag: String? = null, requestCode: Int? = null, messageId: Long, messageStatus: MessageStatus) {
        val call = usersService.setMessageStatus(messageId, messageStatus.jsonName, accessToken)
        call.enqueue(apiCallback(ResponseBody::class.java, tag, requestCode) {
            delegatesSet.notify(tag).onMessageStatusChanged(requestCode, messageId, messageStatus)
        })
    }

    fun uploadPhoto(tag: String? = null, requestCode: Int? = null,
                    streamId: Long, streamType: StreamType,
                    image: File, header: String? = null, content: String? = null): SentMessage {

        val user = CurrentUserSettings.currentUser
        val userId = user?.id
        val timestamp = System.currentTimeMillis()
        val sid = String.format("%s_%s_from_%s_at_%s", streamType, streamId, userId, timestamp)

        val requestFile = RequestBody.create(Extension.getFromFile(image).mediaType, image)
        val filePart = MultipartBody.Part.createFormData("file", image.name, requestFile)

        val call = usersService.uploadFile(streamId, streamType.jsonName, accessToken, sid, header, content, filePart)
        call.enqueue(apiCallback(ResponseBody::class.java, tag, requestCode) {
            delegatesSet.notify(tag).onPhotoUploaded(requestCode)
        })

        return SentMessage(
                sid = sid,
                streamType = streamType.jsonName,
                streamId = streamId,
                createdAt = Date(timestamp),
                sentAt = Date(timestamp),
                user = user,
                bodyType = MessageBodyType.PHOTO.jsonName,
                content = image.path,
                messageStatus = MessageStatus.SENDING.jsonName
        )
    }

    fun uploadAudio(tag: String? = null, requestCode: Int? = null,
                    streamId: Long, streamType: StreamType,
                    audio: File, header: String? = null, content: String? = null): SentMessage {

        val user = CurrentUserSettings.currentUser
        val userId = user?.id
        val timestamp = System.currentTimeMillis()
        val sid = String.format("%s_%s_from_%s_at_%s", streamType, streamId, userId, timestamp)

        val requestFile = RequestBody.create(Extension.getFromFile(audio).mediaType, audio)
        val filePart = MultipartBody.Part.createFormData("file", audio.name, requestFile)

        val call = usersService.uploadFile(streamId, streamType.jsonName, accessToken, sid, header, content, filePart)
        call.enqueue(apiCallback(ResponseBody::class.java, tag, requestCode) {
            delegatesSet.notify(tag).onVideoUploaded(requestCode)
        })

        return SentMessage(
                sid = sid,
                streamType = streamType.jsonName,
                streamId = streamId,
                createdAt = Date(timestamp),
                sentAt = Date(timestamp),
                user = user,
                bodyType = MessageBodyType.AUDIO.jsonName,
                content = audio.path,
                messageStatus = MessageStatus.SENDING.jsonName
        )
    }

    fun uploadVideo(tag: String? = null, requestCode: Int? = null,
                    streamId: Long, streamType: StreamType,
                    video: File, header: String? = null, content: String? = null): SentMessage {

        val user = CurrentUserSettings.currentUser
        val userId = user?.id
        val timestamp = System.currentTimeMillis()
        val sid = String.format("%s_%s_from_%s_at_%s", streamType, streamId, userId, timestamp)

        val requestFile = RequestBody.create(Extension.getFromFile(video).mediaType, video)
        val filePart = MultipartBody.Part.createFormData("file", video.name, requestFile)

        val call = usersService.uploadFile(streamId, streamType.jsonName, accessToken, sid, header, content, filePart)
        call.enqueue(apiCallback(ResponseBody::class.java, tag, requestCode) {
            delegatesSet.notify(tag).onVideoUploaded(requestCode)
        })

        return SentMessage(
                sid = sid,
                streamType = streamType.jsonName,
                streamId = streamId,
                createdAt = Date(timestamp),
                sentAt = Date(timestamp),
                user = user,
                bodyType = MessageBodyType.VIDEO.jsonName,
                content = video.path,
                messageStatus = MessageStatus.SENDING.jsonName
        )
    }

    fun getStickersForUser(tag: String? = null, requestCode: Int? = null) {
        val call = usersService.getStickersForUser(accessToken)
        call.enqueue(apiCallback(StickerList::class.java, tag, requestCode) { stickerList ->
            delegatesSet.notify(tag).onStickerListReceived(requestCode, stickerList)
        })
    }

    fun deleteMessage(tag: String? = null, requestCode: Int? = null, messageId: Long) {
        val call = usersService.deleteMessage(messageId, accessToken)
        call.enqueue(apiCallback(ResponseBody::class.java, tag, requestCode) {
            delegatesSet.notify(tag).onMessageDeleted(requestCode, messageId)
        })
    }

    fun getInterestsGroups(tag: String? = null, requestCode: Int? = null) {
        val call = usersService.getInterestsGroup(accessToken)
        call.enqueue(object : ApiCallback<RealmList<InterestsGroup>>() {
            override fun onSuccess(interestsGroups: RealmList<InterestsGroup>) {
                interestsGroups.forEach {
                    it.interests?.forEach {
                        it.type = StreamType.INTERESTS.jsonName
                        it.sid = Stream.generateSid(StreamType.INTERESTS, it.id)
                    }
                }
                delegatesSet.notify(tag).onInterestsGroupsReceived(requestCode, interestsGroups)
            }

            override fun onError(error: Error) {
                delegatesSet.notify(tag).onError(requestCode, error)
            }

            override fun onFailure(t: Throwable) {
                delegatesSet.notify(tag).onFailure(requestCode, t)
            }
        })
    }

    fun subscribeInterestsGroup(tag: String? = null, requestCode: Int? = null, interestsGroup: InterestsGroup) {
        val call = usersService.subscribeInterestsGroup(interestsGroup.id, accessToken)
        call.enqueue(apiCallback(ResponseBody::class.java, tag, requestCode) {
            delegatesSet.notify(tag).onInterestsGroupSubscribed(requestCode, interestsGroup)
        })
    }

    fun unsubscribeInterestsGroup(tag: String? = null, requestCode: Int? = null, interestsGroup: InterestsGroup) {
        val call = usersService.unsubscribeInterestsGroup(interestsGroup.id, accessToken)
        call.enqueue(apiCallback(ResponseBody::class.java, tag, requestCode) {
            delegatesSet.notify(tag).onInterestsGroupUnsubscribed(requestCode, interestsGroup)
        })
    }

    fun subscribeInterest(tag: String? = null, requestCode: Int? = null, interest: Stream) {
        val call = usersService.subscribeInterest(interest.id, accessToken)
        call.enqueue(apiCallback(ResponseBody::class.java, tag, requestCode) {
            delegatesSet.notify(tag).onInterestSubscribed(requestCode, interest)
        })
    }

    fun unsubscribeInterest(tag: String? = null, requestCode: Int? = null, interest: Stream) {
        val call = usersService.unsubscribeInterest(interest.id, accessToken)
        call.enqueue(apiCallback(ResponseBody::class.java, tag, requestCode) {
            delegatesSet.notify(tag).onInterestUnsubscribed(requestCode, interest)
        })
    }

    fun getInterestInfoById(tag: String? = null, requestCode: Int? = null, interestId: Long) {
        val call = usersService.getInterestInfoById(interestId, accessToken)
        call.enqueue(apiCallback(Stream::class.java, tag, requestCode) { interest ->
            interest.apply { type = StreamType.INTERESTS.jsonName }
            delegatesSet.notify(tag).onInterestInfoReceived(requestCode, interest)
        })
    }

    fun registerDeviceToken(tag: String? = null, requestCode: Int? = null, deviceToken: String) {
        val call = usersService.registerDeviceToken(accessToken, deviceToken, deviceType = "android")
        call.enqueue(apiCallback(ResponseBody::class.java, tag, requestCode) {
            delegatesSet.notify(tag).onDeviceTokenRegistered(requestCode)
        })
    }
//endregion
}