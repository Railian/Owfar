package com.owfar.android.ui.registration

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.MenuItem

import com.owfar.android.helpers.KeyboardHelper

open class RegistrationBaseFragment : Fragment() {

    //region Fragment Life-Cycle Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KeyboardHelper.hideKeyboard()
        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> activity.onBackPressed()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
    //endregion

    //region Tools
    val registrationActivity: RegistrationActivity
        get() = activity as RegistrationActivity

    val appBarHelper: RegistrationAppBarHelper
        get() = registrationActivity.appBarHelper!!
    //endregion
}
