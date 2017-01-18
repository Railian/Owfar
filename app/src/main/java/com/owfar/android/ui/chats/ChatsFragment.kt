package com.owfar.android.ui.chats

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.view.ViewCompat.setNestedScrollingEnabled
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.balysv.materialmenu.MaterialMenuDrawable
import com.owfar.android.R
import com.owfar.android.api.users.UsersDelegate
import com.owfar.android.api.users.UsersManager
import com.owfar.android.data.DataDelegate
import com.owfar.android.data.DataManager
import com.owfar.android.models.api.classes.Stream
import com.owfar.android.models.api.classes.User
import com.owfar.android.models.api.enums.MessageStatus
import com.owfar.android.models.api.enums.StreamType
import com.owfar.android.models.api.interfaces.Message
import com.owfar.android.models.errors.Error
import com.owfar.android.ui.choose_contact.ChooseContactFragment
import com.owfar.android.ui.main.MainBaseFragment
import com.owfar.android.ui.main.MainFabHelper
import com.owfar.android.ui.snackbars.RemoveStreamSnackbar
import io.realm.RealmList

class ChatsFragment : MainBaseFragment(), ChatItemAdapter.OnChatClickListener,
        MainFabHelper.OnFabClickListener, SwipeRefreshLayout.OnRefreshListener, OnSwipeListener {

    companion object {
        val newInstance: ChatsFragment
            get () = ChatsFragment()
    }

    //region constants
    private val TAG = ChatsFragment::class.java.simpleName

    private val REQUEST_CREATE_NEW_CHAT = 1

    private val API_REQUEST_GET_STREAMS = 1
    private val API_REQUEST_START_CONVERSATION = 2
    private val API_REQUEST_CREATE_CHAT = 3
    private val API_REQUEST_DELETE_CONVERSATION = 4
    private val API_REQUEST_LEAVE_FROM_CHAT = 5
    private val API_REQUEST_UNSUBSCRIBE_INTEREST = 6
    //endregion

    //region widgets
    private var srlRefresh: SwipeRefreshLayout? = null
    private var rvChats: RecyclerView? = null
    private var sbRemoveStream: Snackbar? = null
    //endregion

    //region fields
    private val adapter: ChatItemAdapter?
        get () = rvChats?.adapter as? ChatItemAdapter?
    //endregion

    //region Fragment Life-Cycle Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        appBarHelper?.apply {
            setTitle("Chats")
            showMaterialMenu()
            setMaterialMenuState(MaterialMenuDrawable.IconState.BURGER)
            hideSearch()
            hideImage()
            hideTabs()
        }
        navigationMenuHelper?.unlock()
        fabHelper?.show(MainFabHelper.FabIcon.ADD, this)

        return inflater.inflate(R.layout.fragment_chats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        srlRefresh = view.findViewById(R.id.fragment_chats_srlRefresh) as SwipeRefreshLayout
        rvChats = view.findViewById(R.id.fragment_chats_rvChats) as RecyclerView

        setNestedScrollingEnabled(view, false)
        setNestedScrollingEnabled(srlRefresh, false)
        setNestedScrollingEnabled(rvChats, false)

        srlRefresh?.apply { setOnRefreshListener(this@ChatsFragment) }
        rvChats?.apply {
            adapter = ChatItemAdapter().apply {
                setOnChatClickListener(this@ChatsFragment)
            }
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(onScrollListener)
            ItemTouchHelper(ChatItemTouchHelper().apply {
                setOnSwipeListener(this@ChatsFragment)
            }).attachToRecyclerView(this)
        }

        UsersManager.delegatesSet.addDelegate(TAG, userDelegate)
        DataManager.delegatesSet.addDelegate(TAG, dataDelegate)
        DataManager.initStreams()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CREATE_NEW_CHAT && resultCode == Activity.RESULT_OK)
            data?.getParcelableArrayListExtra<User>(ChooseContactFragment.Companion.EXTRA_CHECKED_CONTACTS)?.let {
                val users = RealmList<User>().apply { addAll(it) }
                when {
                    users.size == 1 -> UsersManager.startConversation(TAG, API_REQUEST_START_CONVERSATION, users.first())
                    users.size > 1 -> UsersManager.createChat(TAG, API_REQUEST_CREATE_CHAT, users)
                }
            }
    }

    override fun onDestroyView() {
        DataManager.delegatesSet.removeDelegate(dataDelegate)
        UsersManager.delegatesSet.removeDelegate(userDelegate)
        dismissRemoveStreamSnackbarIfNeed()
        super.onDestroyView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> navigationMenuHelper?.open()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
    //endregion

    override fun onChatClick(stream: Stream)
            = DataManager.getStreamForMessenger(stream)?.let { transactionHelper?.showMessenger(it) } ?: Unit

    override fun onFabClick()
            = transactionHelper?.createNewChat(this, REQUEST_CREATE_NEW_CHAT) ?: Unit

    override fun onRefresh() {
        confirmRemovingIfNeed()
        DataManager.initStreams()
    }

    override fun onSwiped(adapterPosition: Int) {
        confirmRemovingIfNeed()
        showRemoveStreamSnackbar(adapter?.remove(adapterPosition))
    }

    private val onScrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int)
                = confirmRemovingIfNeed()

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) = Unit
    }

    //region Removing Stream
    private fun confirmRemovingIfNeed() {
        dismissRemoveStreamSnackbarIfNeed()
        adapter?.onRemovingConfirmed()?.apply {
            DataManager.removeStream(this)
            when (StreamType.find(type)) {
                StreamType.CONVERSATIONS -> UsersManager.deleteConversation(TAG, API_REQUEST_DELETE_CONVERSATION, this)
                StreamType.CHATS -> UsersManager.leaveFromChat(TAG, API_REQUEST_LEAVE_FROM_CHAT, this)
                StreamType.INTERESTS -> UsersManager.unsubscribeInterest(TAG, API_REQUEST_UNSUBSCRIBE_INTEREST, this)
            }
        }
    }

    fun showRemoveStreamSnackbar(stream: Stream?) {
        dismissRemoveStreamSnackbarIfNeed()
        sbRemoveStream = RemoveStreamSnackbar.make(view, stream) {
            adapter?.undoRemoving()
        }.apply { show() }
    }

    fun dismissRemoveStreamSnackbarIfNeed() {
        sbRemoveStream?.apply {
            if (isShown) dismiss()
            sbRemoveStream = null
        }
    }
    //endregion

    //region usersDelegate
    private val userDelegate = object : UsersDelegate.Simple() {

        override fun onConversationStarted(requestCode: Int?, conversation: Stream) {
            DataManager.addOrUpdateStream(conversation)
            transactionHelper?.showMessenger(conversation)
        }

        override fun onChatCreated(requestCode: Int?, chat: Stream) {
            DataManager.addOrUpdateStream(chat)
            transactionHelper?.showMessenger(chat)
        }

        override fun onError(requestCode: Int?, error: Error) {
            showError(error)
        }
    }
    //endregion

    //region dataDelegate
    private val dataDelegate = object : DataDelegate {

        override fun onStreamsUpdated(streams: MutableList<Stream>) {
            activity.runOnUiThread {
                adapter?.streams = streams
                srlRefresh?.isRefreshing = false
            }
        }

        override fun onNewMessageAdded(stream: Stream, message: Message) = Unit
        override fun onOldMessagesAdded(stream: Stream, messages: MutableList<Message>) = Unit
        override fun onMessageStatusUpdated(messageId: Long, userId: Long, status: MessageStatus) = Unit
        override fun onMessageDeleted(messageId: Long) = Unit
    }
    //endregion
}