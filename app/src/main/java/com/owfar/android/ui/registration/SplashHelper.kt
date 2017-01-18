package com.owfar.android.ui.registration

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View

class SplashHelper(
        private val vSplash: View,
        private val listener: SplashHelper.SplashListener?
) {

    companion object {

        //region constants
        @JvmStatic private val TAG = SplashHelper::class.java.simpleName
        private val STATE_SPLASH_VISIBILITY = "$TAG.STATE_SPLASH_VISIBILITY"
        private val STATE_TIMER_DURATION = "$TAG.STATE_TIMER_DURATION"
        private val STATE_TIMER_STARTED_AT = "$TAG.STATE_TIMER_STARTED_AT"
        private val STATE_TIMER_FINISHED = "$TAG.STATE_TIMER_FINISHED"
        private val STATE_CAN_HIDE_SPLASH = "$TAG.STATE_CAN_HIDE_SPLASH"
        //endregion
    }

    //region fields
    private var timerDuration: Long = 0
    private var timerStartedAt: Long = 0
    private var timerFinished: Boolean = false
    private var canHideSplash: Boolean = false
    private var timer: CountDownTimer? = null
    //endregion

    //region Initialization
    init {
        vSplash.visibility = View.GONE
    }
    //endregion

    //region Public Tools
    fun showSplash(durationInMillis: Long) {
        vSplash.visibility = View.VISIBLE
        timerDuration = durationInMillis
        timerStartedAt = System.currentTimeMillis()
        timerFinished = false
        canHideSplash = false
        timer?.cancel()
        timer = createHidingTimer(timerDuration).apply { start() }
        listener?.onSplashShown()
    }

    fun canHideSplash() {
        canHideSplash = true
        tryToHideSplash()
    }

    fun saveInstanceState(outState: Bundle) {
        with(outState) {
            putInt(STATE_SPLASH_VISIBILITY, vSplash.visibility)
            putLong(STATE_TIMER_DURATION, timerDuration)
            putLong(STATE_TIMER_STARTED_AT, timerStartedAt)
            putBoolean(STATE_TIMER_FINISHED, timerFinished)
            putBoolean(STATE_CAN_HIDE_SPLASH, canHideSplash)
        }
        timer?.cancel()
    }

    fun restoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.apply {
            vSplash.visibility = getInt(STATE_SPLASH_VISIBILITY)
            timerDuration = getLong(STATE_TIMER_DURATION)
            timerStartedAt = getLong(STATE_TIMER_STARTED_AT)
            timerFinished = getBoolean(STATE_TIMER_FINISHED)
            canHideSplash = getBoolean(STATE_CAN_HIDE_SPLASH)
        }

        val now = System.currentTimeMillis()
        val spendTime = now - timerStartedAt
        if (spendTime < timerDuration) {
            timerDuration -= spendTime
            timerStartedAt = now
            timer = createHidingTimer(timerDuration).apply { start() }
            listener?.onSplashRestored(true)
        } else {
            timerDuration = 0
            timerStartedAt = 0
            timer = null
            timerFinished = true
            if (vSplash.visibility == View.VISIBLE) tryToHideSplash()
            else listener?.onSplashRestored(false)
        }
    }
    //endregion

    //region Private Tools
    private fun tryToHideSplash() {
        if (canHideSplash && timerFinished) {
            vSplash.visibility = View.GONE
            timerFinished = false
            canHideSplash = false
            listener?.onSplashHidden()
        }
    }

    private fun createHidingTimer(durationInMillis: Long) = object : CountDownTimer(durationInMillis, durationInMillis) {
        override fun onTick(millisUntilFinished: Long) = Unit
        override fun onFinish() {
            timerDuration = 0
            timerStartedAt = 0
            timer = null
            timerFinished = true
            tryToHideSplash()
        }
    }
    //endregion

    //region Interface SplashListener
    interface SplashListener {
        fun onSplashShown()
        fun onSplashRestored(visible: Boolean)
        fun onSplashHidden()
    }
    //endregion
}










