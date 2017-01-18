package com.owfar.android.api.users

import com.owfar.android.models.api.StickerList
import com.owfar.android.models.api.UserCreation
import com.owfar.android.models.api.classes.*
import com.owfar.android.models.api.enums.MessageStatus
import com.owfar.android.models.api.interfaces.Message
import com.owfar.android.models.errors.Error
import io.realm.RealmList

interface UsersDelegate {

    fun onMessageReceived(requestCode: Int?, message: ReceivedMessage)
    fun onRequestForVerificationCodeSent(requestCode: Int?)
    fun onCreateUser(requestCode: Int?, userCreation: UserCreation)
    fun onNewContactAdded(requestCode: Int?)
    fun onMobileContactsConvertedToOwfar(requestCode: Int?, contacts: RealmList<User>)
    fun onContactFound(requestCode: Int?, user: User)
    fun onContactNotFound(requestCode: Int?, phone: String)
    fun onContactListReceived(requestCode: Int?, users: RealmList<User>?)
    fun onContactDeleted(requestCode: Int?, contact: User)
    fun onProfileUpdated(requestCode: Int?, profile: Profile)
    fun onProfileReceived(requestCode: Int?, profile: Profile)
    fun onUserPhotoUploaded(requestCode: Int?)
    fun onStreamReceived(requestCode: Int?, stream: Stream)
    fun onStreamsReceived(requestCode: Int?, streams: RealmList<Stream>)
    fun onConversationStarted(requestCode: Int?, conversation: Stream)
    fun onConversationDeleted(requestCode: Int?, conversation: Stream)
    fun onChatCreated(requestCode: Int?, chat: Stream)
    fun onChatDeleted(requestCode: Int?, chat: Stream)
    fun onChatUpdated(requestCode: Int?, chat: Stream)
    fun onPhotoForChatUploaded(requestCode: Int?)
    fun onUsersOutsideChatReceived(requestCode: Int?, users: RealmList<User>?)
    fun onMessagesFromStreamLoaded(requestCode: Int?, messages: RealmList<Message>?)
    fun onUsersInvitedToChat(requestCode: Int?, users: MutableList<User>?)
    fun onUserKickedFromChat(requestCode: Int?, user: User)
    fun onChatLeft(requestCode: Int?)
    fun onMessageStatusChanged(requestCode: Int?, messageId: Long, messageStatus: MessageStatus)
    fun onPhotoUploaded(requestCode: Int?)
    fun onVideoUploaded(requestCode: Int?)
    fun onStickerListReceived(requestCode: Int?, stickerLists: StickerList)
    fun onMessageDeleted(requestCode: Int?, messageId: Long)
    fun onInterestsGroupsReceived(requestCode: Int?, interestsGroups: RealmList<InterestsGroup>)
    fun onInterestsGroupSubscribed(requestCode: Int?, interestsGroup: InterestsGroup)
    fun onInterestsGroupUnsubscribed(requestCode: Int?, interestsGroup: InterestsGroup)
    fun onInterestSubscribed(requestCode: Int?, stream: Stream)
    fun onInterestUnsubscribed(requestCode: Int?, stream: Stream)
    fun onInterestInfoReceived(requestCode: Int?, stream: Stream)
    fun onDeviceTokenRegistered(requestCode: Int?)

    fun onError(requestCode: Int?, error: Error)
    fun onFailure(requestCode: Int?, throwable: Throwable)

    //region SimpleUsersDelegate
    open class Simple : UsersDelegate {
        override fun onMessageReceived(requestCode: Int?, message: ReceivedMessage) = Unit
        override fun onRequestForVerificationCodeSent(requestCode: Int?) = Unit
        override fun onCreateUser(requestCode: Int?, userCreation: UserCreation)  = Unit
        override fun onNewContactAdded(requestCode: Int?)  = Unit
        override fun onMobileContactsConvertedToOwfar(requestCode: Int?, contacts: RealmList<User>)  = Unit
        override fun onContactFound(requestCode: Int?, user: User)  = Unit
        override fun onContactNotFound(requestCode: Int?, phone: String)  = Unit
        override fun onContactListReceived(requestCode: Int?, users: RealmList<User>?)  = Unit
        override fun onContactDeleted(requestCode: Int?, contact: User)  = Unit
        override fun onProfileUpdated(requestCode: Int?, profile: Profile)  = Unit
        override fun onProfileReceived(requestCode: Int?, profile: Profile)  = Unit
        override fun onUserPhotoUploaded(requestCode: Int?)  = Unit
        override fun onStreamReceived(requestCode: Int?, stream: Stream)  = Unit
        override fun onStreamsReceived(requestCode: Int?, streams: RealmList<Stream>)  = Unit
        override fun onConversationStarted(requestCode: Int?,  conversation: Stream)  = Unit
        override fun onConversationDeleted(requestCode: Int?, conversation: Stream) = Unit
        override fun onChatCreated(requestCode: Int?, chat: Stream)  = Unit
        override fun onChatDeleted(requestCode: Int?, chat: Stream)  = Unit
        override fun onChatUpdated(requestCode: Int?, chat: Stream)  = Unit
        override fun onPhotoForChatUploaded(requestCode: Int?)  = Unit
        override fun onUsersOutsideChatReceived(requestCode: Int?, users: RealmList<User>?)  = Unit
        override fun onMessagesFromStreamLoaded(requestCode: Int?, messages: RealmList<Message>?)  = Unit
        override fun onUsersInvitedToChat(requestCode: Int?, users: MutableList<User>?)  = Unit
        override fun onUserKickedFromChat(requestCode: Int?, user: User)  = Unit
        override fun onChatLeft(requestCode: Int?)  = Unit
        override fun onMessageStatusChanged(requestCode: Int?, messageId: Long, messageStatus: MessageStatus) = Unit
        override fun onPhotoUploaded(requestCode: Int?)  = Unit
        override fun onVideoUploaded(requestCode: Int?)  = Unit
        override fun onStickerListReceived(requestCode: Int?, stickerLists: StickerList)  = Unit
        override fun onMessageDeleted(requestCode: Int?, messageId: Long)  = Unit
        override fun onInterestsGroupsReceived(requestCode: Int?, interestsGroups: RealmList<InterestsGroup>) = Unit
        override fun onInterestsGroupSubscribed(requestCode: Int?, interestsGroup: InterestsGroup)  = Unit
        override fun onInterestsGroupUnsubscribed(requestCode: Int?, interestsGroup: InterestsGroup)  = Unit
        override fun onInterestSubscribed(requestCode: Int?, stream: Stream)  = Unit
        override fun onInterestUnsubscribed(requestCode: Int?, stream: Stream)  = Unit
        override fun onInterestInfoReceived(requestCode: Int?, stream: Stream)  = Unit
        override fun onDeviceTokenRegistered(requestCode: Int?)  = Unit

        override fun onError(requestCode: Int?, error: Error)  = Unit
        override fun onFailure(requestCode: Int?, throwable: Throwable)  = Unit
    }
    //endregion
}
