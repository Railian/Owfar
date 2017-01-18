package com.owfar.android.ui.choose_contact

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v4.view.ViewCompat.setNestedScrollingEnabled
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import com.balysv.materialmenu.MaterialMenuDrawable
import com.miguelcatalan.materialsearchview.MaterialSearchView
import com.owfar.android.R
import com.owfar.android.api.users.UsersDelegate
import com.owfar.android.api.users.UsersManager
import com.owfar.android.helpers.KeyboardHelper
import com.owfar.android.models.api.classes.User
import com.owfar.android.ui.contacts.ContactItemAdapter
import com.owfar.android.ui.main.MainBaseFragment
import com.owfar.android.ui.main.MainFabHelper
import io.realm.RealmList
import java.util.*

class ChooseContactFragment : MainBaseFragment(), MaterialSearchView.OnQueryTextListener,
        ContactItemAdapter.OnCheckedContactsChangeListener, MainFabHelper.OnFabClickListener {

    companion object {
        val EXTRA_CHECKED_CONTACTS = "EXTRA_CHECKED_CONTACTS"

        fun newInstance(title: String, chatId: Long? = null, targetFragment: Fragment,
                        requestCode: Int): ChooseContactFragment = ChooseContactFragment().apply {
            setTargetFragment(targetFragment, requestCode)
            arguments = Bundle().apply {
                putStringArray(ARG_TITLES, arrayOf(title))
                chatId?.let { putLong(ARG_CHAT_ID, it) }
            }
        }

        fun newInstance(title0: String, title1: String, titleX: String, chatId: Long? = null,
                        targetFragment: Fragment, requestCode: Int): ChooseContactFragment = ChooseContactFragment().apply {
            setTargetFragment(targetFragment, requestCode)
            arguments = Bundle().apply {
                putStringArray(ARG_TITLES, arrayOf(title0, title1, titleX))
                chatId?.let { putLong(ARG_CHAT_ID, it) }
            }
        }
    }

    //region constants
    val TAG = ChooseContactFragment::class.java.simpleName

    val ARG_TITLES = "ARG_TITLES"
    val ARG_CHAT_ID = "ARG_CHAT_ID"

    private val STATE_CHECKED_COUNT = "STATE_CHECKED_COUNT"

    private val API_REQUEST_GET_CONTACT_LIST = 1
    //endregion

    //region widgets
    private var rvContacts: RecyclerView? = null
    //endregion

    //region fields
    private val adapter: ContactItemAdapter?
        get() = rvContacts?.adapter as ContactItemAdapter

    private var checkedCount: Int = 0
    private val title: String?
        get() = when (checkedCount) {
            0 -> titles?.get(0)
            1 -> titles?.get(1) ?: titles?.get(0)
            else -> titles?.get(2) ?: titles?.get(0)
        }
    //endregion

    //region arguments
    private var titles: Array<out String>? = null
    private var chatId: Long? = null
    //endregion

    //region Fragment Life-Cycle Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        UsersManager.delegatesSet.addDelegate(TAG, userDelegate)

        arguments?.let {
            titles = it.getStringArray(ARG_TITLES)
            if (it.containsKey(ARG_CHAT_ID)) chatId = it.getLong(ARG_CHAT_ID)
        }
        checkedCount = savedInstanceState?.getInt(STATE_CHECKED_COUNT) ?: 0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        appBarHelper?.apply {
            setTitle(title)
            setMaterialMenuState(MaterialMenuDrawable.IconState.ARROW)
            setOnSearchTextListener(this@ChooseContactFragment)
            hideImage()
            hideSearch()
            hideTabs()
        }
        navigationMenuHelper?.lockClosed()
        fabHelper?.hide()

        return inflater.inflate(R.layout.fragment_contacts_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvContacts = view.findViewById(R.id.fragment_contacts_page_rvContacts) as RecyclerView

        setNestedScrollingEnabled(view, false)
        setNestedScrollingEnabled(rvContacts, false)

        rvContacts?.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = ContactItemAdapter(true, false).apply {
                setOnCheckedContactsChangeListener(this@ChooseContactFragment)
            }
        }

        chatId?.let {
            UsersManager.getUsersOutsideChat(TAG, API_REQUEST_GET_CONTACT_LIST, it)
        } ?: UsersManager.getContactList(TAG, API_REQUEST_GET_CONTACT_LIST)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_CHECKED_COUNT, checkedCount)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        appBarHelper?.setOnSearchTextListener(null)
        super.onDestroyView()
    }

    override fun onDestroy() {
        UsersManager.delegatesSet.removeDelegate(userDelegate)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.choose_contact, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> activity.onBackPressed()
            R.id.action_search -> appBarHelper?.apply {
                showSearch()
                setMaterialMenuState(MaterialMenuDrawable.IconState.ARROW)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
    //endregion

    //region OnQueryTextListener Implementation
    override fun onQueryTextSubmit(query: String) = false

    override fun onQueryTextChange(newText: String) = false
    //endregion

    //region OnCheckedContactsChangeListener Implementation
    override fun onCheckedContactsChange(checkedContacts: Set<User>?) {
        when (checkedContacts?.size?.apply { checkedCount = this } ?: let { checkedCount = 0 }) {
            0 -> fabHelper?.hide()
            else -> fabHelper?.show(MainFabHelper.FabIcon.CHECK, this)
        }
        appBarHelper?.setTitle(title)
    }
    //endregion

    //region OnFabClickListener Implementation
    override fun onFabClick() {
        targetFragment?.let {
            appBarHelper?.hideSearch()
            KeyboardHelper.hideKeyboard()
            activity.onBackPressed()
            val data = Intent().apply {
                putParcelableArrayListExtra(EXTRA_CHECKED_CONTACTS,
                        ArrayList<Parcelable>(adapter?.checkedContacts))
            }
            it.onActivityResult(targetRequestCode, Activity.RESULT_OK, data)
        }
    }
    //endregion

    //region userDelegate
    private val userDelegate = object : UsersDelegate.Simple() {

        override fun onContactListReceived(requestCode: Int?, users: RealmList<User>?) {
            adapter?.allContacts = users
        }

        override fun onUsersOutsideChatReceived(requestCode: Int?, users: RealmList<User>?) {
            adapter?.allContacts = users
        }
    }
    //endregion
}
