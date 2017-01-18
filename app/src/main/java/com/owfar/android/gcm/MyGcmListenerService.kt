package com.owfar.android.gcm

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.support.v7.app.NotificationCompat
import android.util.Log

import com.google.android.gms.gcm.GcmListenerService
import com.owfar.android.R
import com.owfar.android.data.DataManager
import com.owfar.android.data.logFun
import com.owfar.android.models.api.enums.StreamType
import com.owfar.android.socket.SocketManager

class MyGcmListenerService : GcmListenerService() {

    companion object {
        @JvmStatic private val TAG = MyGcmListenerService::class.java.simpleName
    }

    override fun onMessageReceived(from: String, data: Bundle) {
        Log.d(TAG, "onMessageReceived() called with: from = [$from], data = [$data]")
        //        Bundle notification = data.getBundle("notification");

        val googleMessageId = data.getString("google.message_id")
        val googleSentTime = data.getLong("google.sent_time")
        val collapseKey = data.getString("collapse_key")
        val badge = Integer.valueOf(data.getString("badge"))
        if (!data.containsKey("stream_type")) return
        val streamType = StreamType.find(data.getString("stream_type"))
        val streamId = java.lang.Long.valueOf(data.getString("stream_id"))
        val messageId = java.lang.Long.valueOf(data.getString("message_id"))
        val message = data.getString("message")

        Log.d(TAG, "From: " + from)
        Log.d(TAG, "Message: " + message)

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        if (!SocketManager.isConnected)

            DataManager.addMessage(messageId)
    }

    //    /**
    //     * Create and show a simple notification containing the received GCM message.
    //     *
    //     * @param message GCM message received.
    //     */
    //    private void sendNotification(String title, String message) {
    //        Intent intent = new Intent(this, MainActivity.class);
    //        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    //        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
    //                PendingIntent.FLAG_ONE_SHOT);
    //
    //        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    //        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
    //                .setSmallIcon(R.drawable.ic_notifications_chats)
    //                .setContentTitle(title)
    //                .setContentText(message)
    //                .setAutoCancel(true)
    //                .setSound(defaultSoundUri)
    //                .setContentIntent(pendingIntent);
    //
    //        NotificationManager notificationManager =
    //                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    //
    //        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    //    }
    //
    //    private void sendNotification(boolean silent) {
    //        NotificationManager notificationManager =
    //                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    //
    //        RealmList<Stream> streams = DataManager.INSTANCE.getStreams();
    //        RealmList<Stream> unreadStreams = new RealmList<>();
    //        int sumUnreadMessagesCount = 0;
    //
    //        for (Stream stream : streams)
    //            if (stream != null && stream.getUnreadCount() > 0) {
    //                unreadStreams.add(stream);
    //                sumUnreadMessagesCount += stream.getUnreadCount();
    //            }
    //
    //        String title = null;
    //        String message = null;
    //
    //        if (unreadStreams.size() == 0) {
    //            notificationManager.cancel(0);
    //            return;
    //        } else if (unreadStreams.size() == 1) {
    //            Stream stream = unreadStreams.get(0);
    //            title = stream.getDisplayName();
    //            message = stream.getTicket();
    //        } else {
    //            title = String.format("%s unread messages", sumUnreadMessagesCount);
    //            StringBuilder messageBuilder = new StringBuilder();
    //            for (Stream stream : unreadStreams)
    //                messageBuilder
    //                        .append(messageBuilder.length() == 0 ? "from " : ", ")
    //                        .append(stream.getDisplayName());
    //            message = messageBuilder.toString();
    //        }
    //
    //        Intent intent = new Intent(this, MainActivity.class);
    //        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    //        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
    //                PendingIntent.FLAG_ONE_SHOT);
    //
    //        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    //        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
    //                .setSmallIcon(R.drawable.ic_notifications_chats)
    //                .setContentTitle(title)
    //                .setLights(0x21C0C0, 1000, 2500)
    //                .setContentText(message)
    //                .setContentInfo("" + sumUnreadMessagesCount)
    //                .setSound(defaultSoundUri)
    //                .setContentIntent(pendingIntent);
    //
    //
    //        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    //    }

    override fun onCreate() {
        super.onCreate()
        logFun(TAG, MyGcmListenerService::onCreate)
    }

    override fun onDestroy() {
        logFun(TAG, MyGcmListenerService::onDestroy)
        super.onDestroy()
    }
}