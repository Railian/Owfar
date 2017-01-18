package com.owfar.android.ui.registration

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.transition.TransitionInflater
import android.view.View
import com.owfar.android.R
import com.owfar.android.helpers.KeyboardHelper
import com.owfar.android.models.CountryCode
import com.owfar.android.models.errors.Error
import com.owfar.android.settings.CurrentUserDelegate
import com.owfar.android.settings.CurrentUserManager
import com.owfar.android.settings.CurrentUserSettings
import com.owfar.android.ui.main.MainActivity


class RegistrationActivity : AppCompatActivity(), SplashHelper.SplashListener {

    companion object {

        //region constants
        @JvmStatic private val TAG = RegistrationActivity::class.java.simpleName
        //endregion

        fun start(context: Context) {
            context.startActivity(Intent(context, RegistrationActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
    }

    //region fields
    private var splashHelper: SplashHelper? = null
    var appBarHelper: RegistrationAppBarHelper? = null
    //endregion

    //region Activity Life-Cycle Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        val vSplash = findViewById(R.id.merge_registration_splash_vContent)

        splashHelper = SplashHelper(vSplash, this)
        appBarHelper = RegistrationAppBarHelper(this)

        splashHelper?.apply {
            if (savedInstanceState == null) {
                showSplash(4000)
                canHideSplash()
            } else restoreInstanceState(savedInstanceState)
        }

        if (savedInstanceState == null)
            showPhoneNumberFragment(CurrentUserSettings.lastCountryCode, CurrentUserSettings.lastPhoneNumber)

        CurrentUserManager.delegatesSet.addDelegate(TAG, currentUserDelegate)
        KeyboardHelper.init(this)
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        splashHelper?.saveInstanceState(outState)
    }

    override fun onDestroy() {
        CurrentUserManager.delegatesSet.removeDelegate(currentUserDelegate)
        super.onDestroy()
    }
    //endregion

    //region Transactions
    fun showPhoneNumberFragment(lastCountryCode: CountryCode?, lastPhoneNumber: String?) {
        supportFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.fragment_container, RegistrationPhoneNumberFragment.newInstance(lastCountryCode, lastPhoneNumber)).commit()
        supportFragmentManager.executePendingTransactions()
    }

    fun showCountryCodeFragment(targetFragment: android.support.v4.app.Fragment, requestCode: Int, sharedElement: View?, countryCode: CountryCode?) {
        val fragment = RegistrationCountryCodesFragment.newInstance(countryCode)
        fragment.setTargetFragment(targetFragment, requestCode)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            targetFragment.sharedElementReturnTransition = TransitionInflater.from(this).inflateTransition(R.transition.change_transform)
            fragment.sharedElementEnterTransition = TransitionInflater.from(this).inflateTransition(R.transition.change_transform)
        }
        supportFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addSharedElement(sharedElement, "frame")
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        supportFragmentManager.executePendingTransactions()
    }

    fun showNumberVerificationFragment(countryCode: CountryCode?, phoneNumber: String) {
        supportFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.fragment_container, RegistrationNumberVerificationFragment.newInstance(countryCode, phoneNumber))
                .addToBackStack(null)
                .commit()
        supportFragmentManager.executePendingTransactions()
    }

    fun showSignUpFragment(countryCode: CountryCode, phoneNumber: String) {
        supportFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.fragment_container, RegistrationSignUpFragment.newInstance(countryCode, phoneNumber))
                .addToBackStack(null)
                .commit()
        supportFragmentManager.executePendingTransactions()
    }
    //endregion

    //region SplashListener Implementation
    override fun onSplashShown() = appBarHelper?.hideToolbar() ?: Unit

    override fun onSplashRestored(visible: Boolean) {
        appBarHelper?.apply { if (visible) hideToolbar() else showToolbar() } ?: Unit
    }

    override fun onSplashHidden() = appBarHelper?.showToolbar() ?: Unit
    //endregion

    //region localCurrentUserDelegate
    private val currentUserDelegate = object : CurrentUserDelegate.Simple() {

        override fun onAuthorized() {
            MainActivity.start(this@RegistrationActivity)
            finish()
        }

        override fun onErrorDuringAuthorization(error: Error) = AlertDialog.Builder(this@RegistrationActivity)
                .setTitle(error.error)
                .setMessage(error.errorDescription)
                .setPositiveButton("Hide") { dialog, which -> finish() }
                .create().show()

        override fun onTimeoutException() = AlertDialog.Builder(this@RegistrationActivity)
                .setTitle("Problem with connection")
                .create().show()
    }
    //endregion
}
























