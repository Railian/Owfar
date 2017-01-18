package com.owfar.android.api.users

interface ProgressListener {

    fun onStarted()
    fun onUpdated(bytesRead: Long, contentLength: Long)
    fun onCompleted()
    fun onCancelled()
    fun onFinished()
    fun onError(t: Throwable)

    open class Simple : ProgressListener {

        override fun onStarted() {
        }

        override fun onUpdated(bytesRead: Long, contentLength: Long) {
        }

        override fun onFinished() {
        }

        override fun onCompleted() {
        }

        override fun onCancelled() {
        }

        override fun onError(t: Throwable) {
        }
    }
}