package com.owfar.android.helpers

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import com.owfar.android.R
import com.owfar.android.models.api.classes.Stream
import com.owfar.android.ui.main.MainActivity
import io.realm.Realm
import io.realm.RealmList
import java.util.*

object UnreadMessagesHelper {

    private var context: Context? = null
        get() {
            context ?: throw NullPointerException("${UnreadMessagesHelper::class.java.simpleName} " +
                    "first must be initialized by calling method init(Context context)")
            return field
        }

    //region Initialization
    fun init(context: Context) {
        this.context = context
    }
    //endregion

    fun sendNotification(context: Context, silent: Boolean, popup: Boolean) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val realm = Realm.getDefaultInstance()

        val streams = ArrayList<Stream>()
        //                DataManager.INSTANCE.getStreams();
        val unreadStreams = RealmList<Stream>()
        var sumUnreadMessagesCount = 0

        if (streams != null)
            for (stream in streams)
                if (stream != null && stream.unreadCount > 0) {
                    unreadStreams.add(stream)
                    sumUnreadMessagesCount += stream.unreadCount
                }

        var title: String? = null
        var message: String? = null
        var pendingIntent: PendingIntent? = null

        if (unreadStreams.size == 0) {
            notificationManager.cancel(0)
            return
        } else if (unreadStreams.size == 1) {
            val stream = unreadStreams[0]
            title = stream.displayName
            message = stream.ticket
            pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */,
                    MainActivity.createIntentToShowMessenger(context, stream), PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            title = String.format("%s unread messages", sumUnreadMessagesCount)
            val messageBuilder = StringBuilder()
            for (stream in unreadStreams)
                messageBuilder
                        .append(if (messageBuilder.length == 0) "from " else ", ")
                        .append(stream.displayName)
            message = messageBuilder.toString()
            pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */,
                    MainActivity.createIntentToShowChats(context), PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notifications_chats)
                .setContentTitle(title)
                .setContentText(message)
                .setContentInfo("" + sumUnreadMessagesCount)
                .setContentIntent(pendingIntent)

        if (!silent)
            notificationBuilder
                    .setLights(0x21C0C0, 1000, 2500)
                    .setSound(defaultSoundUri)

        if (popup)
            notificationBuilder.priority = Notification.PRIORITY_MAX

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())

        realm.close()
    }
}