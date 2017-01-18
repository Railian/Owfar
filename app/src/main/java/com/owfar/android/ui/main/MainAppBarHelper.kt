package com.owfar.android.ui.main

import android.graphics.Color
import android.net.NetworkInfo
import android.net.Uri
import android.support.annotation.StringRes
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import com.balysv.materialmenu.MaterialMenuDrawable
import com.miguelcatalan.materialsearchview.MaterialSearchView
import com.owfar.android.R
import com.owfar.android.connectivity.ConnectivityManager
import com.owfar.android.connectivity.NetworkInfoListener
import com.owfar.android.helpers.KeyboardHelper
import com.owfar.android.media.MediaHelper
import com.owfar.android.models.api.classes.Media
import com.owfar.android.models.api.enums.MediaSize
import com.owfar.android.models.api.enums.MediaStorageType
import com.squareup.picasso.Callback

class MainAppBarHelper(private val activity: MainActivity) : NetworkInfoListener {

    //region widgets
    private val appBar: AppBarLayout
    private val collapsingLayout: CollapsingToolbarLayout
    private val gradient: View
    private val toolbar: Toolbar
    private val search: MaterialSearchView
    private val image: ImageView
    private val imageProgress: View
    private val tabs: TabLayout
    //endregion

    //region fields
    private val materialMenu: MaterialMenuDrawable
    var isImageLoaded: Boolean = false
        private set
    var isExpanded: Boolean = false
        private set
    private var subtitle: CharSequence? = null
    private var networkStatus: CharSequence? = null
    //endregion

    init {
        appBar = activity.findViewById(R.id.app_bar_main_appBar) as AppBarLayout
        collapsingLayout = activity.findViewById(R.id.app_bar_main_ctlCollapsingLayout) as CollapsingToolbarLayout
        gradient = activity.findViewById(R.id.app_bar_main_gradient)
        toolbar = activity.findViewById(R.id.app_bar_main_toolbar) as Toolbar
        search = activity.findViewById(R.id.app_bar_main_search) as MaterialSearchView
        image = activity.findViewById(R.id.app_bar_main_image) as ImageView
        imageProgress = activity.findViewById(R.id.app_bar_main_imageProgress)
        tabs = activity.findViewById(R.id.app_bar_main_tabs) as TabLayout

        materialMenu = MaterialMenuDrawable(activity, Color.WHITE, MaterialMenuDrawable.Stroke.THIN)
        activity.setSupportActionBar(toolbar)
        toolbar.navigationIcon = materialMenu
        toolbar.setSubtitleTextAppearance(activity, android.R.style.TextAppearance_DeviceDefault_Small)
        image.visibility = View.GONE

        ConnectivityManager.setNetworkInfoListener(this)
        configureWithNetworkStatus(ConnectivityManager.isNetworkConnected)
    }

    private fun configureWithNetworkStatus(networkConnected: Boolean) {
        if (networkConnected) {
            appBar.setBackgroundResource(R.color.colorPrimary)
            collapsingLayout.setContentScrimResource(R.color.colorPrimary)
            gradient.setBackgroundResource(R.drawable.bg_app_bar_primary)
            networkStatus = null
            updateSubtitle()
        } else {
            appBar.setBackgroundResource(R.color.colorPrimaryDark)
            collapsingLayout.setContentScrimResource(R.color.colorPrimaryDark)
            gradient.setBackgroundResource(R.drawable.bg_app_bar_primary_dark)
            networkStatus = "without connection to the internet"
            updateSubtitle()
        }
    }
    //endregion

    //region Public Tools
    fun expand(animated: Boolean): MainAppBarHelper {
        appBar.setExpanded(true, animated)
        isExpanded = true
        return this
    }

    fun collapse(animated: Boolean): MainAppBarHelper {
        appBar.setExpanded(false, animated)
        isExpanded = false
        return this
    }

    fun setExpanded(expanded: Boolean, animated: Boolean): MainAppBarHelper {
        if (expanded) expand(animated)
        else collapse(animated)
        return this
    }

    fun showMaterialMenu(): MainAppBarHelper {
        materialMenu.isVisible = true
        return this
    }

    fun hideMaterialMenu(): MainAppBarHelper {
        materialMenu.isVisible = false
        return this
    }

    fun setMaterialMenuVisible(visible: Boolean): MainAppBarHelper {
        materialMenu.isVisible = visible
        return this
    }

    fun setMaterialMenuState(state: MaterialMenuDrawable.IconState): MainAppBarHelper {
        materialMenu.animateIconState(state)
        return this
    }

    fun setTitle(title: CharSequence?): MainAppBarHelper {
        activity.supportActionBar?.title = title
        return this
    }

    fun setTitle(@StringRes resId: Int): MainAppBarHelper {
        activity.supportActionBar?.setTitle(resId)
        return this
    }

    fun setSubtitle(subtitle: CharSequence?): MainAppBarHelper {
        this.subtitle = subtitle
        updateSubtitle()
        return this
    }

    private fun updateSubtitle() {
        toolbar.subtitle = networkStatus?.let { it } ?: subtitle
    }

    val isSearchVisible: Boolean
        get() = search.isSearchOpen

    fun showSearch(): MainAppBarHelper {
        search.showSearch(true)
        return this
    }

    fun hideSearch(): MainAppBarHelper {
        search.closeSearch()
        return this
    }

    fun setSearchVisible(visible: Boolean): MainAppBarHelper {
        if (visible) showSearch()
        else hideSearch()
        return this
    }

    fun setSearchMenuItem(menuItem: MenuItem) {
        search.setMenuItem(menuItem)
    }

    fun showImage(type: MediaStorageType, photo: Media?): MainAppBarHelper {
        hideImageProgress()
        photo?.let {
            MediaHelper
                    .load(it)
                    .withOptions(type, MediaSize._DEFAULT)
                    .into(image, onLoadImageCallback)
        } ?: hideImage()
        return this
    }

    fun showImageWithProgress(imageUri: Uri?, text: String?): MainAppBarHelper {
        showImageProgress(text)
        imageUri?.let {
            MediaHelper
                    .load(imageUri)
                    .into(image, onLoadImageCallback)
        } ?: hideImage()
        return this
    }

    fun hideImage(): MainAppBarHelper {
        MediaHelper.cancelRequest(image)
        collapse(!activity.isActivityJustCreated)
        activity.isActivityJustCreated = false
        return this
    }

    private fun showImageProgress(text: String?): MainAppBarHelper {
        imageProgress.visibility = View.VISIBLE
        return this
    }

    fun hideImageProgress(): MainAppBarHelper {
        imageProgress.visibility = View.GONE
        return this
    }

    fun showTabs(): MainAppBarHelper {
        tabs.visibility = View.VISIBLE
        return this
    }

    fun hideTabs(): MainAppBarHelper {
        tabs.visibility = View.GONE
        return this
    }

    fun setTabsVisible(visible: Boolean): MainAppBarHelper {
        if (visible) showTabs()
        else hideTabs()
        return this
    }

    fun setupTabsWithViewPager(viewPager: ViewPager): MainAppBarHelper {
        tabs.setupWithViewPager(viewPager)
        return this
    }

    fun setSearchViewListener(listener: MaterialSearchView.SearchViewListener?): MainAppBarHelper {
        search.setOnSearchViewListener(listener)
        return this
    }

    fun setOnSearchTextListener(listener: MaterialSearchView.OnQueryTextListener?): MainAppBarHelper {
        search.setOnQueryTextListener(listener)
        return this
    }
    //endregion

    //region onLoadImageCallback
    private val onLoadImageCallback = object : Callback {
        override fun onSuccess() {
            isImageLoaded = true
            expandImage(true)
            listener?.onImageShown()
        }

        override fun onError() {
            isImageLoaded = false
        }
    }
    //endregion

    fun expandImage(animated: Boolean): MainAppBarHelper {
        if (!KeyboardHelper.keyboardVisible && isImageLoaded) {
            image.visibility = View.VISIBLE
            expand(animated)
        }
        return this
    }

    fun eraseImage(animated: Boolean): MainAppBarHelper {
        isImageLoaded = false
        return this
    }

    //region OnImageShownListener
    private var listener: OnImageShownListener? = null

    fun setOnImageShownListener(listener: OnImageShownListener?): MainAppBarHelper {
        this.listener = listener
        return this
    }

    interface OnImageShownListener {
        fun onImageShown()
    }
    //endregion

    override fun onReceiveNetworkInfo(networkConnected: Boolean, activeNetworkInfo: NetworkInfo?) {
        configureWithNetworkStatus(networkConnected)
    }
}