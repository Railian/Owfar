/*
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.owfar.android.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

class IntentUtils(private val context: Context) {

    //region Actions
    fun actionCall(phoneNumber: String)
            = actionCall(context, phoneNumber)

    fun actionSendSMS(phoneNumber: String, message: String)
            = actionSendSMS(context, phoneNumber, message)

    fun actionSendEmail(emailAddress: String, subject: String, message: String)
            = actionSendEmail(context, emailAddress, subject, message)

    fun actionOpenInBrowser(url: String)
            = actionOpenInBrowser(context, url)
    //endregion

    companion object {

        fun actionCall(context: Context, phoneNumber: String) {
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:${Uri.encode(phoneNumber)}")
            context.startActivity(intent)
        }

        fun actionSendSMS(context: Context, phoneNumber: String, message: String) {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("smsto:${Uri.encode(phoneNumber)}")
            intent.putExtra("sms_body", message)
            context.startActivity(intent)
        }

        fun actionSendEmail(context: Context, emailAddress: String, subject: String, message: String) {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:${Uri.encode(emailAddress)}" +
                    "?subject=${Uri.encode(subject)}&body=${Uri.encode(message)}")
            context.startActivity(intent)
        }

        fun actionOpenInBrowser(context: Context, url: String) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            context.startActivity(intent)
        }
    }
}
