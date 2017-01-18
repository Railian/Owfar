package com.owfar.android.ui.registration

import android.graphics.Color
import android.support.annotation.StringRes
import android.support.design.widget.AppBarLayout
import android.support.v7.widget.Toolbar
import android.view.View

import com.balysv.materialmenu.MaterialMenuDrawable
import com.owfar.android.R

class RegistrationAppBarHelper(private val activity: RegistrationActivity) {

    //region widgets
    private val appBar: AppBarLayout
    private val toolbar: Toolbar
    private val materialMenu: MaterialMenuDrawable
    //endregion

    //region Initialization
    init {
        appBar = activity.findViewById(R.id.app_bar_registration_appBar) as AppBarLayout
        toolbar = activity.findViewById(R.id.app_bar_registration_toolbar) as Toolbar

        activity.setSupportActionBar(toolbar)
        materialMenu = MaterialMenuDrawable(activity, Color.WHITE, MaterialMenuDrawable.Stroke.THIN)
        toolbar.navigationIcon = materialMenu
    }
    //endregion

    //region Public Tools
    fun showToolbar()
            = let { toolbar.visibility = View.VISIBLE }

    fun hideToolbar()
            = let { toolbar.visibility = View.GONE }

    val isToolbarVisible: Boolean
        get() = toolbar.visibility == View.VISIBLE

    fun showCloseApp(): RegistrationAppBarHelper {
        materialMenu.animateIconState(MaterialMenuDrawable.IconState.X)
        return this
    }

    fun showBackArrow(): RegistrationAppBarHelper {
        materialMenu.animateIconState(MaterialMenuDrawable.IconState.ARROW)
        return this
    }

    fun setTitle(title: CharSequence): RegistrationAppBarHelper {
        activity.supportActionBar?.title = title
        return this
    }

    fun setTitle(@StringRes resId: Int): RegistrationAppBarHelper {
        activity.supportActionBar?.setTitle(resId)
        return this
    }
    //endregion
}