package com.owfar.android.ui.broadcasts

import android.os.Bundle
import android.support.v4.view.ViewCompat.setNestedScrollingEnabled
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.balysv.materialmenu.MaterialMenuDrawable
import com.owfar.android.R
import com.owfar.android.api.users.UsersDelegate
import com.owfar.android.api.users.UsersManager
import com.owfar.android.models.api.classes.InterestsGroup
import com.owfar.android.models.api.classes.Stream
import com.owfar.android.ui.main.MainBaseFragment
import io.realm.RealmList

class ChannelsFragment : MainBaseFragment(), ChannelsAdapter.OnInterestClickListener,
        ChannelsAdapter.OnCheckedChangedListener {

    companion object {
        val newInstance: ChannelsFragment
            get () = ChannelsFragment()
    }

    //region constants
    private val TAG = ChannelsFragment::class.java.simpleName
    private val TITLE = "Channels"
    //endregion

    //region widgets
    private var rvInterestsGroups: RecyclerView? = null
    //endregion

    //region fields
    private val adapter: ChannelsAdapter?
        get() = rvInterestsGroups?.adapter as ChannelsAdapter?
    //endregion

    //region Fragment Life-Cycle Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        UsersManager.delegatesSet.addDelegate(TAG, userDelegate)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        appBarHelper?.apply {
            setTitle(TITLE)
            showMaterialMenu()
            setMaterialMenuState(MaterialMenuDrawable.IconState.BURGER)
            hideSearch()
            hideImage()
            hideTabs()
        }
        navigationMenuHelper?.unlock()
        fabHelper?.hide()
        return inflater.inflate(R.layout.fragment_broadcasts, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvInterestsGroups = view!!.findViewById(R.id.fragment_broadcasts_rvInterestsGroups) as RecyclerView

        setNestedScrollingEnabled(view, false)
        setNestedScrollingEnabled(rvInterestsGroups, false)

        rvInterestsGroups?.let {
            it.layoutManager = LinearLayoutManager(activity)
            it.adapter = ChannelsAdapter().apply {
                setOnInterestClickListener(this@ChannelsFragment)
                setOnCheckedChangedListener(this@ChannelsFragment)
            }
        }

        UsersManager.getInterestsGroups(TAG, null)
    }

    override fun onDestroy() {
        UsersManager.delegatesSet.removeDelegate(userDelegate)
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> navigationMenuHelper?.open()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
    //endregion

    //region OnInterestClickListener Implementation
    override fun onInterestClick(interestsGroup: InterestsGroup, stream: Stream) {
        transactionHelper?.showChannelInfo(interestsGroup, stream)
    }
    //endregion

    //region OnCheckedChangedListener Implementation
    override fun onInterestsGroupChecked(interestsGroup: InterestsGroup, isChecked: Boolean) {
        if (isChecked) UsersManager.subscribeInterestsGroup(TAG, null, interestsGroup)
        else UsersManager.unsubscribeInterestsGroup(TAG, null, interestsGroup)
    }

    override fun onInterestChecked(stream: Stream, isChecked: Boolean) {
        if (isChecked) UsersManager.subscribeInterest(TAG, null, stream)
        else UsersManager.unsubscribeInterest(TAG, null, stream)
    }
    //endregion

    //region localUserDelegate
    private val userDelegate = object : UsersDelegate.Simple() {
        override fun onInterestsGroupsReceived(requestCode: Int?, interestsGroups: RealmList<InterestsGroup>) {
            adapter?.setInterestsGroups(interestsGroups)
        }
    }
    //endregion
}
