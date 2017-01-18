package com.owfar.android.ui.messenger

import android.app.Activity
import android.app.ProgressDialog
import android.content.*
import android.os.Bundle
import android.support.v4.view.ViewCompat.setNestedScrollingEnabled
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.ImageView
import com.balysv.materialmenu.MaterialMenuDrawable
import com.owfar.android.R
import com.owfar.android.api.file.FileManager
import com.owfar.android.api.users.ProgressListener
import com.owfar.android.api.users.UsersDelegate
import com.owfar.android.api.users.UsersManager
import com.owfar.android.data.DataDelegate
import com.owfar.android.data.DataManager
import com.owfar.android.data.logFun
import com.owfar.android.extensions.orNullIfBlank
import com.owfar.android.helpers.KeyboardHelper
import com.owfar.android.helpers.MediaIntentHelper
import com.owfar.android.media.BlurTransform
import com.owfar.android.media.MediaHelper
import com.owfar.android.models.api.StickerList
import com.owfar.android.models.api.classes.ReceivedMessage
import com.owfar.android.models.api.classes.Sticker
import com.owfar.android.models.api.classes.Stream
import com.owfar.android.models.api.classes.User
import com.owfar.android.models.api.enums.MediaSize
import com.owfar.android.models.api.enums.MessageBodyType
import com.owfar.android.models.api.enums.MessageStatus
import com.owfar.android.models.api.enums.StreamType
import com.owfar.android.models.api.interfaces.Message
import com.owfar.android.settings.CurrentUserSettings
import com.owfar.android.socket.SocketManager
import com.owfar.android.ui.broadcasts.BroadcastInfoFragment
import com.owfar.android.ui.choose_contact.ChooseContactFragment
import com.owfar.android.ui.dialogs.LeaveChatDialog
import com.owfar.android.ui.group_chat_options.GroupChatOptionsFragment
import com.owfar.android.ui.main.MainBaseFragment
import com.owfar.android.ui.main.MessengerHelper
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration
import io.realm.RealmList
import java.io.File

class MessengerFragment : MainBaseFragment(), SwipeRefreshLayout.OnRefreshListener,
        MediaIntentHelper.OnVideoTakenListener, KeyboardHelper.KeyboardListener,
        MessengerHelper.MessengerHelperListener {

    companion object {
        //region constants
        @JvmStatic private val TAG = MessengerFragment::class.java.simpleName

        const private val ARG_STREAM = "ARG_STREAM"
        const private val STATE_STREAM = "STATE_STREAM"

        const private val REQUEST_ADD_MEMBERS = 10

        const private val API_REQUEST_GET_STICKERS = 1
        const private val API_REQUEST_SEND_PHOTO = 2
        const private val API_REQUEST_SEND_AUDIO = 3
        const private val API_REQUEST_SEND_VIDEO = 4
        const private val API_REQUEST_LOAD_MESSAGES_FROM_STREAM = 5
        const private val API_REQUEST_INVITE_USERS_TO_CHAT = 6
        const private val API_REQUEST_LEAVE_CHAT = 7
        const private val API_REQUEST_SET_MESSAGE_STATUS = 8
        const private val REQUEST_DIALOG_LEAVE_CHAT = 9
        const private val API_REQUEST_DELETE_MESSAGE = 10
        //endregion

        fun newInstance(stream: Stream) = MessengerFragment().apply {
            arguments = Bundle().apply { putParcelable(ARG_STREAM, stream) }
        }
    }

    //region widgets
    private var ivBackground: ImageView? = null
    private var vSwipeRefresh: SwipeRefreshLayout? = null
    private var rvMessenger: RecyclerView? = null
    private var vMessenger: View? = null
    //endregion

    //region fields
    private val adapter: MessengerAdapter?
        get() = rvMessenger?.adapter as? MessengerAdapter
    private var messengerHelper: MessengerHelper? = null
    private var isRefreshing: Boolean = false
    //endregion

    //region args
    private var stream: Stream? = null
    //endregion

    //region Fragment Life-Cycle Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        stream = savedInstanceState?.getParcelable<Stream>(STATE_STREAM)
                ?: arguments.getParcelable<Stream>(ARG_STREAM)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        appBarHelper?.apply {
            setTitle(stream?.displayName ?: "Owfar")
            showMaterialMenu()
            setMaterialMenuState(MaterialMenuDrawable.IconState.ARROW)
            hideSearch()
            hideImage()
            hideTabs()
        }
        navigationMenuHelper?.lockClosed()
        fabHelper?.hide()

        return inflater.inflate(R.layout.fragment_messenger, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivBackground = view.findViewById(R.id.fragment_messenger_ivBackground) as? ImageView
        vSwipeRefresh = view.findViewById(R.id.fragment_messenger_vSwipeRefresh) as? SwipeRefreshLayout
        rvMessenger = view.findViewById(R.id.fragment_messenger_rvMessenger) as? RecyclerView
        vMessenger = view.findViewById(R.id.merge_messenger_vMessenger)

        setNestedScrollingEnabled(view, false)
        setNestedScrollingEnabled(rvMessenger, false)

        stream?.let {
            ivBackground?.apply {
                MediaHelper
                        .load(it.image)
                        .withOptions(it.imageMediaType, MediaSize._3X)
                        .transform(BlurTransform(activity, 15))
                        .into(this)
            }

            vSwipeRefresh?.setOnRefreshListener(this)

            streamType?.let { type ->

                messengerHelper = MessengerHelper(view, this, boardsHelper, type, streamId).apply {
                    text = it.sid?.let { DataManager.getDraft(it)?.text }
                    setMessengerHelperListener(this@MessengerFragment)
                    if (type == StreamType.INTERESTS) hideMessenger()
                    else showMessenger()
                }

                rvMessenger?.apply {
                    layoutManager = LinearLayoutManager(activity).apply {
                        reverseLayout = true
                        stackFromEnd = true
                        scrollToPosition(0) // TODO: 29.04.16 scroll to unread messages
                    }
                    val users = when (type) {
                        StreamType.CONVERSATIONS -> it.asConversation?.users
                        StreamType.CHATS -> it.asChat?.users
                        else -> null
                    }
                    adapter = MessengerAdapter(type, users).apply {
                        messages = it.allMessages
                    }
                    addItemDecoration(StickyRecyclerHeadersDecoration(adapter as MessengerAdapter))
                    registerForContextMenu(this)
                }
            }
        }

        KeyboardHelper.setKeyboardListener(this)

        UsersManager.delegatesSet.addDelegate(TAG, userDelegate)
        DataManager.delegatesSet.addDelegate(DataManager.TAG, dataDelegate)

        UsersManager.getStickersForUser(TAG, API_REQUEST_GET_STICKERS)
    }

    override fun onResume() {
        super.onResume()
        DataManager.activeStream = stream
        stream?.let { DataManager.setStreamAsRead(it) }
        appBarHelper?.apply {
            setSubtitle(when (streamType) {
                StreamType.CONVERSATIONS -> stream?.asConversation?.opponent?.let {
                    when {
                        !it.profile?.synopsis.isNullOrBlank() -> it.profile?.synopsis
                        !it.profile?.fullName.isNullOrBlank() -> it.phone
                        else -> null
                    }
                }
                StreamType.CHATS -> stream?.asChat?.getFirstUsersNamesWithoutYourself(5)?.orNullIfBlank()
                StreamType.INTERESTS -> stream?.asInterest?.description
                else -> null
            } ?: "Owfar")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        messengerHelper?.onActivityResult(requestCode, resultCode, data)
        stream?.let {
            when {
                requestCode == REQUEST_ADD_MEMBERS && resultCode == Activity.RESULT_OK -> {
                    val checkedContacts = data?.getParcelableArrayListExtra<User>(ChooseContactFragment.EXTRA_CHECKED_CONTACTS)
                    UsersManager.inviteUsersToChat(TAG, API_REQUEST_INVITE_USERS_TO_CHAT, it, checkedContacts)
                }
                requestCode == REQUEST_DIALOG_LEAVE_CHAT && resultCode == DialogInterface.BUTTON_POSITIVE ->
                    UsersManager.leaveFromChat(TAG, API_REQUEST_LEAVE_CHAT, it)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(STATE_STREAM, stream)
    }

    override fun onPause() {
        appBarHelper?.setSubtitle(null)
        DataManager.activeStream = null
        super.onPause()
    }

    override fun onDestroyView() {
        stream?.sid?.let { DataManager.setDraft(it, messengerHelper?.text) }
        UsersManager.delegatesSet.removeDelegate(userDelegate)
        DataManager.delegatesSet.removeDelegate(dataDelegate)
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        with(inflater) {
            when (streamType) {
                StreamType.CONVERSATIONS -> inflate(R.menu.messenger_conversation, menu)
                StreamType.CHATS -> inflate(R.menu.messenger_chat, menu)
                StreamType.INTERESTS -> inflate(R.menu.messenger_interest, menu)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (KeyboardHelper.keyboardVisible) KeyboardHelper.hideKeyboard()
        else when (item?.itemId) {
            android.R.id.home -> activity.onBackPressed()
            R.id.action_opponent ->
                stream?.asConversation?.opponent?.let { transactionHelper?.showProfile(it) }
            R.id.action_add_members ->
                stream?.id?.let { transactionHelper?.addMembers(it, this, REQUEST_ADD_MEMBERS) }
            R.id.action_leave_chat ->
                LeaveChatDialog.newInstance(this, REQUEST_DIALOG_LEAVE_CHAT).show(fragmentManager, null)
            R.id.action_options -> stream?.let {
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_container, GroupChatOptionsFragment.newInstance(it))
                        .addToBackStack(null)
                        .commit()
                fragmentManager.executePendingTransactions()
            }
            R.id.action_details -> stream?.let {
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_container, BroadcastInfoFragment.newInstance(null, it))
                        .addToBackStack(null)
                        .commit()
                fragmentManager.executePendingTransactions()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)

        val info = menuInfo as ContextMenuRecyclerView.RecyclerContextMenuInfo
        adapter?.getMessage(info.position)?.let {
            activity.menuInflater.inflate(R.menu.messenger_context, menu)

            val isMyOwnMessage = it.user?.id == CurrentUserSettings.currentUser?.id
            val isDeleted = MessageBodyType.find(it.bodyType) == MessageBodyType.DELETED
            val isComment = MessageBodyType.find(it.bodyType) == MessageBodyType.COMMENT

            menu.findItem(R.id.action_message_delete).isVisible = isMyOwnMessage && !isDeleted
            menu.findItem(R.id.action_message_copy).isVisible = isComment
        }
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        return (item?.menuInfo as? ContextMenuRecyclerView.RecyclerContextMenuInfo)?.let { info ->
            when (item?.itemId) {
                R.id.action_message_share -> false // Not Implemented
                R.id.action_message_resend -> false // Not Implemented
                R.id.action_message_copy -> {
                    val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("message", adapter?.getMessage(info.position)?.content)
                    clipboard.primaryClip = clip
                    true
                }
                R.id.action_message_info -> false // Not Implemented
                R.id.action_message_delete -> {
                    UsersManager.deleteMessage(TAG, API_REQUEST_DELETE_MESSAGE, info.id)
                    true
                }
                else -> false
            }
        } ?: false
    }
    //endregion

    //region OnRefreshListener Implementation
    override fun onRefresh() {
        val receivedMessagesCount = adapter?.messages?.count { it is ReceivedMessage } ?: 0
        stream?.let {
            if (!isRefreshing)
                UsersManager.loadMessagesFromStream(TAG, API_REQUEST_LOAD_MESSAGES_FROM_STREAM,
                        it, receivedMessagesCount, 10)
        }
        isRefreshing = true
    }
    //endregion

    //region OnVideoTakenListener Implementation
    override fun onVideoTaken(videoPath: String) = Unit
    //endregion

    //region KeyboardListener Implementation
    override fun onShowKeyboard() {
        messengerHelper?.hideBoards()
    }

    override fun onHideKeyboard() = Unit
    //endregion

    //region userDelegate
    private val userDelegate = object : UsersDelegate.Simple() {

        override fun onMessagesFromStreamLoaded(requestCode: Int?, messages: RealmList<Message>?) {
            DataManager.addOldMessages(messages)
            vSwipeRefresh?.isRefreshing = false
            isRefreshing = false
        }

        override fun onMessageDeleted(requestCode: Int?, messageId: Long) {
            DataManager.setMessageAsDeleted(messageId)
        }

        override fun onStickerListReceived(requestCode: Int?, stickerList: StickerList) {
            messengerHelper?.setStickerList(stickerList)
        }

        override fun onChatLeft(requestCode: Int?) {
            fragmentManager.popBackStack()
        }
    }
    //endregion

    //region dataDelegate
    private val dataDelegate = object : DataDelegate {

        override fun onStreamsUpdated(streams: MutableList<Stream>) = Unit

        override fun onNewMessageAdded(stream: Stream, message: Message) {
            logFun(TAG, DataDelegate::onNewMessageAdded, stream, message)
            activity.runOnUiThread {
                if (this@MessengerFragment.stream?.sid == stream.sid) {
                    adapter?.addNewMessage(message)
                    rvMessenger?.smoothScrollToPosition(0)
                }
            }
        }

        override fun onOldMessagesAdded(stream: Stream, messages: MutableList<Message>) {
            logFun(TAG, DataDelegate::onOldMessagesAdded, stream, messages)
            activity.runOnUiThread {
                if (this@MessengerFragment.stream?.sid == stream.sid) {
                    adapter?.addOldMessages(messages)
                }
            }
        }

        override fun onMessageStatusUpdated(messageId: Long, userId: Long, status: MessageStatus) {
            logFun(TAG, DataDelegate::onMessageStatusUpdated, messageId, userId, status)
            activity?.runOnUiThread {
                (adapter?.messages?.find { it.asReceived()?.id == messageId } as? ReceivedMessage)
                        ?.setMessageStatus(userId, status)
                adapter?.notifyDataSetChanged()
            }
        }

        override fun onMessageDeleted(messageId: Long) {
            logFun(TAG, DataDelegate::onMessageDeleted, messageId)
            activity?.runOnUiThread {
                adapter?.messages?.find { it.asReceived()?.id == messageId }
                        ?.apply { bodyType = MessageBodyType.DELETED.jsonName }
                        ?.let { adapter?.notifyDataSetChanged() }
            }
        }
    }
    //endregion

    //region MessengerHelperListener Implementation
    override fun onSendComment(comment: String) {
        logFun(TAG, MessengerFragment::onSendComment, comment)
        if (streamType != null && streamId != null)
            SocketManager.sendComment(streamType!!, streamId!!, comment)?.let {
                DataManager.addNewMessage(it)
            }
    }

    override fun onSendSticker(sticker: Sticker) {
        logFun(TAG, MessengerFragment::onSendSticker, sticker)
        if (streamType != null && streamId != null)
            SocketManager.sendSticker(streamType!!, streamId!!, sticker)?.let {
                DataManager.addNewMessage(it)
            }
    }

    override fun onSendPhoto(photoPath: String, header: String?, content: String?) {
        logFun(TAG, MessengerFragment::onSendPhoto, photoPath, header, content)
        if (streamType != null && streamId != null) {
//            FileManager.get().uploadDelegatesSet.addDelegate("loading_file_to_stream", uploadingFileDelegate)
//            FileManager.get().uploadFile(streamId!!, streamType, File(photoPath), "loading_file_to_stream", null)
            UsersManager.uploadPhoto(TAG, API_REQUEST_SEND_PHOTO, streamId!!, streamType!!, File(photoPath), header, content).let {
                DataManager.addNewMessage(it)
            }
        }
    }

    override fun onSendAudio(audioPath: String, header: String?, content: String?) {
        logFun(TAG, MessengerFragment::onSendAudio, audioPath, header, content)
        if (streamType != null && streamId != null) {
//            FileManager.get().uploadDelegatesSet.addDelegate("loading_file_to_stream", uploadingFileDelegate)
//            FileManager.get().uploadFile(streamId!!, streamType, File(audioPath), "loading_file_to_stream", null)
            UsersManager.uploadAudio(TAG, API_REQUEST_SEND_AUDIO, streamId!!, streamType!!, File(audioPath), header, content).let {
                DataManager.addNewMessage(it)
            }
        }
    }

    override fun onSendVideo(videoPath: String, header: String?, content: String?) {
        logFun(TAG, MessengerFragment::onSendVideo, videoPath, header, content)
        if (streamType != null && streamId != null) {
//            FileManager.get().uploadDelegatesSet.addDelegate("loading_file_to_stream", uploadingFileDelegate)
//            FileManager.get().uploadFile(streamId!!, streamType, File(videoPath), "loading_file_to_stream", null)
            UsersManager.uploadVideo(TAG, API_REQUEST_SEND_VIDEO, streamId!!, streamType!!, File(videoPath), header, content).let {
                DataManager.addNewMessage(it)
            }
        }
    }
    //endregion

    //region Private Tools
    private val streamType: StreamType?
            by lazy { StreamType.find(stream?.type) }

    private val streamId: Long?
            by lazy { stream?.id }
    //endregion

    private val uploadingFileDelegate = object : ProgressListener {

        private var dialog: ProgressDialog? = null

        override fun onStarted() {
            Log.d("TAG", "onStarted() called")
            dialog = ProgressDialog(activity).apply {
                setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                setTitle("Uploading file")
                setCancelable(false)
                setMessage("Please wait...")
                show()
            }
        }

        override fun onUpdated(bytesRead: Long, contentLength: Long) {
            Log.d("TAG", "onUpdated() called with: bytesRead = [$bytesRead], contentLength = [$contentLength]")
            dialog?.progress = (100 * bytesRead / contentLength).toInt()
        }

        override fun onCompleted() {
            Log.d("TAG", "onCompleted() called")
        }

        override fun onCancelled() {
            Log.d("TAG", "onCancelled() called")
        }

        override fun onFinished() {
            Log.d("TAG", "onFinished() called")
            dialog?.dismiss()
            FileManager.get().uploadDelegatesSet.removeDelegate(this)
        }

        override fun onError(t: Throwable) {
            Log.d("TAG", "onError() called with: t = [$t]")
        }
    }
}
