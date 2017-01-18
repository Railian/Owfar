package com.owfar.android.helpers

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager

import com.owfar.android.utils.MetricsUtil

object KeyboardHelper : ViewTreeObserver.OnGlobalLayoutListener {

    //region constants
    const private val SETTINGS_KEYBOARD = "SETTINGS_KEYBOARD"

    const private val PREF_KEYBOARD_HEIGHT = "PREF_KEYBOARD_HEIGHT"
    const private val DEFAULT_KEYBOARD_HEIGHT_IN_DP = 300
    //endregion

    //region fields
    private var activity: Activity? = null
    private var windowView: View? = null
    private val settings: SharedPreferences?
        get() = activity?.getSharedPreferences(SETTINGS_KEYBOARD, Context.MODE_PRIVATE)
    //endregion

    //region Initialization
    fun init(activity: Activity) {
        this.activity = activity
        windowView?.viewTreeObserver?.let {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
                it.removeGlobalOnLayoutListener(this)
            else it.removeOnGlobalLayoutListener(this)
        }
        windowView = activity.window?.decorView?.findViewById(android.R.id.content)?.apply {
            viewTreeObserver.addOnGlobalLayoutListener(this@KeyboardHelper)
        }
        keyboardVisible = checkKeyboardVisibility()
    }
    //endregion

    //region Public Tools
    var keyboardVisible: Boolean = false
        private set

    val keyboardHeight: Int
        get() = activity?.let {
            val defaultHeight = MetricsUtil.dp2px(it, DEFAULT_KEYBOARD_HEIGHT_IN_DP.toFloat()) ?: 0
            settings?.getInt(PREF_KEYBOARD_HEIGHT, defaultHeight) ?: defaultHeight
        } ?: 0

    fun hideKeyboard() = activity?.let {
        val inputMethodManager = it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        it.currentFocus?.let { inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0) } ?: false
    } ?: false
    //endregion

    //region Private Tools
    private fun checkKeyboardVisibility() = Rect().let {
        windowView?.getWindowVisibleDisplayFrame(it)
        val screenHeight = windowView?.rootView?.height ?: 0
        val heightDifference = screenHeight - (it.bottom - it.top)
        (heightDifference > screenHeight / 3).let { keyboardVisible ->
            activity?.let {
                if (keyboardVisible) MetricsUtil(it).let {
                    val keyboardHeight = heightDifference - it.navigationBarHeight - it.statusBarHeight
                    settings?.edit()?.putInt(PREF_KEYBOARD_HEIGHT, keyboardHeight)?.apply()
                }
            }
            keyboardVisible
        }
    }
    //endregion

    //region UI Listeners Implementation
    override fun onGlobalLayout() {
        checkKeyboardVisibility().let {
            if (keyboardVisible != it) {
                keyboardVisible = it
                listener?.apply { if (keyboardVisible) onShowKeyboard() else onHideKeyboard() }
            }
        }
    }
    //endregion

    //region KeyboardListener
    private var listener: KeyboardListener? = null

    fun setKeyboardListener(listener: KeyboardListener) {
        this.listener = listener
    }

    interface KeyboardListener {
        fun onShowKeyboard()
        fun onHideKeyboard()
    }
    //endregion
}
