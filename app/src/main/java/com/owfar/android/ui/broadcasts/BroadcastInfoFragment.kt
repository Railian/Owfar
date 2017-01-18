package com.owfar.android.ui.broadcasts

import android.os.Bundle
import android.support.v4.view.ViewCompat.setNestedScrollingEnabled
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.balysv.materialmenu.MaterialMenuDrawable
import com.owfar.android.R
import com.owfar.android.api.users.UsersDelegate
import com.owfar.android.api.users.UsersManager
import com.owfar.android.models.api.classes.InterestsGroup
import com.owfar.android.models.api.classes.Stream
import com.owfar.android.models.api.enums.MediaSize
import com.owfar.android.models.api.enums.MediaStorageType
import com.owfar.android.ui.main.MainAppBarHelper
import com.owfar.android.ui.main.MainBaseFragment
import com.squareup.picasso.Picasso

class BroadcastInfoFragment : MainBaseFragment(), MainAppBarHelper.OnImageShownListener,
        View.OnClickListener {

    //region enum ButtonState
    private enum class ButtonState(val title: String) {
        FOLLOW("Follow"),
        UNFOLLOW("Unfollow")
    }
    //endregion

    companion object {

        //region constants
        @JvmStatic private val TAG = BroadcastInfoFragment::class.java.simpleName

        const private val ARG_INTERESTS_GROUP = "ARG_INTERESTS_GROUP"
        const private val ARG_STREAM = "ARG_STREAM"

        const private val API_REQUEST_GET_INTEREST_INFO = 1
        const private val API_REQUEST_SUBSCRIBE_INTEREST = 2
        const private val API_REQUEST_UNSUBSCRIBE_INTEREST = 3
        //endregion

        //region Creating New Instances
        fun newInstance(interestsGroup: InterestsGroup?, stream: Stream) = BroadcastInfoFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_INTERESTS_GROUP, interestsGroup)
                putParcelable(ARG_STREAM, stream)
            }
        }
        //endregion
    }

    //region widgets
    private var vGroup: View? = null
    private var ivGroupIcon: ImageView? = null
    private var tvGroupName: TextView? = null
    private var tvName: TextView? = null
    private var tvFollowers: TextView? = null
    private var tvDescription: TextView? = null
    private var btFollowUnfollow: Button? = null
    //endregion

    //region arguments
    private var interestsGroup: InterestsGroup? = null
    private var stream: Stream? = null
    //endregion

    //region Fragment Life-Cycle Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        UsersManager.delegatesSet.addDelegate(TAG, userDelegate)

        interestsGroup = arguments.getParcelable<InterestsGroup>(ARG_INTERESTS_GROUP)
        stream = arguments.getParcelable<Stream>(ARG_STREAM)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        appBarHelper?.apply {
            setTitle(stream?.asInterest?.name)
            setMaterialMenuState(MaterialMenuDrawable.IconState.ARROW)
            hideSearch()
            hideTabs()
        }
        navigationMenuHelper?.lockClosed()
        fabHelper?.hide()

        return inflater.inflate(R.layout.fragment_broadcast_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vGroup = view.findViewById(R.id.fragment_broadcast_info_vGroup)
        ivGroupIcon = view.findViewById(R.id.fragment_broadcast_info_ivGroupIcon) as ImageView
        tvGroupName = view.findViewById(R.id.fragment_broadcast_info_tvGroupName) as TextView
        tvName = view.findViewById(R.id.fragment_broadcast_info_tvName) as TextView
        tvFollowers = view.findViewById(R.id.fragment_broadcast_info_tvFollowers) as TextView
        tvDescription = view.findViewById(R.id.fragment_broadcast_info_tvDescription) as TextView
        btFollowUnfollow = view.findViewById(R.id.fragment_broadcast_info_btFollowUnfollow) as Button

        setNestedScrollingEnabled(view, false)

        configureWith(stream)
        btFollowUnfollow?.setOnClickListener(this)

        stream?.id?.let { UsersManager.getInterestInfoById(TAG, API_REQUEST_GET_INTEREST_INFO, it) }
    }

    override fun onDestroyView() {
        appBarHelper?.setOnImageShownListener(null)
        UsersManager.delegatesSet.removeDelegate(userDelegate)
        super.onDestroyView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                fragmentManager.popBackStack()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
    //endregion

    //region View Configuration
    private fun configureWith(stream: Stream?) {
        stream?.asInterest?.let {
            interestsGroup?.let {
                vGroup?.visibility = View.VISIBLE
                ivGroupIcon?.apply {
                    Picasso.with(activity).cancelRequest(this)
                    it.photo?.let {
                        Picasso.with(activity)
                                .load(it.getPath(MediaSize._3X))
                                .into(this)
                    }
                }
                tvGroupName?.hint = interestsGroup?.name
            } ?: let { vGroup?.visibility = View.GONE }
            appBarHelper?.apply {
                setOnImageShownListener(this@BroadcastInfoFragment)
                showImage(MediaStorageType.CHANNELS_PHOTOS, it.photo)
            }
            tvName?.text = it.name
            configureTvFollowers(it.followers)
            tvDescription?.text = it.description
            configureButtonFollowUnfollow(when (it.isSubscribed) {
                true -> ButtonState.UNFOLLOW
                false -> ButtonState.FOLLOW
                else -> null
            })
        }
    }

    private fun configureButtonFollowUnfollow(buttonState: ButtonState?) {
        btFollowUnfollow?.apply {
            buttonState?.let {
                visibility = View.VISIBLE
                text = it.title
                tag = it
            } ?: let { visibility = View.GONE }
        }
    }

    private fun configureTvFollowers(count: Int?) {
        tvFollowers?.apply {
            count?.let {
                visibility = View.VISIBLE

                val string = count.toString()
                val textColor: Int
                var fullString: String
                val key: String
                val from: Int
                val to: Int

                textColor = resources.getColor(R.color.colorPrimary)
                fullString = resources.getString(R.string.fragment_broadcast_info_tvFollowers)
                key = resources.getString(R.string.fragment_broadcast_info_tvFollowers_kCount)
                from = fullString.indexOf(key)
                fullString = fullString.replace(key, string)
                to = from + string.length
                if (from != -1) {
                    val spannableString = SpannableString(fullString)
                    spannableString.setSpan(ForegroundColorSpan(textColor), from, to, 0)
                    spannableString.setSpan(StyleSpan(android.graphics.Typeface.BOLD), from, to, 0)
                    text = spannableString
                }
            } ?: let { visibility = View.GONE }
        }
    }
    //endregion

    //region UI-Listeners Implementation
    override fun onClick(v: View) {
        btFollowUnfollow?.apply {
            stream?.let {
                when (tag as ButtonState) {
                    ButtonState.FOLLOW ->
                        UsersManager.subscribeInterest(TAG, API_REQUEST_SUBSCRIBE_INTEREST, it)
                    ButtonState.UNFOLLOW ->
                        UsersManager.unsubscribeInterest(TAG, API_REQUEST_UNSUBSCRIBE_INTEREST, it)
                }
                isEnabled = false
            }
        }
    }
    //endregion

    //region OnImageShownListener Implementation
    override fun onImageShown() {
        setNestedScrollingEnabled(view, true)
    }
    //endregion

    //region userDelegate
    private val userDelegate = object : UsersDelegate.Simple() {

        override fun onInterestInfoReceived(requestCode: Int?, stream: Stream) {
            configureWith(stream)
            btFollowUnfollow?.visibility = View.VISIBLE
        }

        override fun onInterestSubscribed(requestCode: Int?, stream: Stream) {
            stream.asInterest?.isSubscribed = true
            configureButtonFollowUnfollow(ButtonState.UNFOLLOW)
            btFollowUnfollow?.isEnabled = true
        }

        override fun onInterestUnsubscribed(requestCode: Int?, stream: Stream) {
            stream.asInterest?.isSubscribed = false
            configureButtonFollowUnfollow(ButtonState.FOLLOW)
            btFollowUnfollow?.isEnabled = true
        }
    }
    //endregion
}
