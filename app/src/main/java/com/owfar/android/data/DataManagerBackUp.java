package com.owfar.android.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.owfar.android.DelegatesSet;
import com.owfar.android.api.users.UsersDelegate;
import com.owfar.android.api.users.UsersManager;
import com.owfar.android.models.api.classes.Media;
import com.owfar.android.models.api.classes.ReceivedMessage;
import com.owfar.android.models.api.classes.Stream;
import com.owfar.android.models.api.enums.MessageBodyType;
import com.owfar.android.models.api.enums.MessageClassType;
import com.owfar.android.models.api.enums.MessageStatus;
import com.owfar.android.models.api.enums.StreamType;
import com.owfar.android.models.api.interfaces.Message;
import com.owfar.android.models.socket.DeliveredInfo;
import com.owfar.android.models.socket.SeenInfo;
import com.owfar.android.models.socket.SubscribeData;
import com.owfar.android.settings.CurrentUserSettings;
import com.owfar.android.socket.SocketListener;
import com.owfar.android.socket.SocketManager;

import java.util.HashMap;
import java.util.Map;

import io.realm.RealmList;

public class DataManagerBackUp {

    public static final String TAG = DataManagerBackUp.class.getSimpleName();

    private static final int API_REQUEST_GET_STREAMS = 1;
    private static final int API_REQUEST_GET_STREAM = 2;
    private static final int API_REQUEST_GET_MESSAGE = 3;
    private static final int API_REQUEST_SET_MESSAGE_STATUS = 4;

    private static final int DB_REQUEST_GET_STREAMS = 1;

    private Context context;
    private Map<String, Stream> streamsMap;
    private Stream activeStream;

    //region Singleton Implementation
    private static DataManagerBackUp instance;

    private DataManagerBackUp(Context context) {
        this.context = context;
        UsersManager.INSTANCE.getDelegatesSet().addDelegate(TAG, apiDelegate);
        SocketManager.INSTANCE.setListener(socketListener);
        initStreams(true);
    }

    public static boolean isInitialized() {
        return instance != null && instance.context != null;
    }

    public static void init(Context context) {
        instance = new DataManagerBackUp(context);
    }

    public static DataManagerBackUp get() {
        if (instance == null) {
            String message = "DataManager first must be initialized by calling method init(Context context)";
            throw new NullPointerException(message);
        } else return instance;
    }
    //endregion

    //region DelegatesSet
    private DelegatesSet<DataDelegate> delegatesSet = new DelegatesSet<>(DataDelegate.class);

    public DelegatesSet<DataDelegate> getDelegatesSet() {
        return delegatesSet;
    }
    //endregion

    public void initStreams(boolean force) {
        if (force || streamsMap == null || streamsMap.isEmpty()) {
//            DatabaseService.getStreams(context, TAG, DB_REQUEST_GET_STREAMS);
            UsersManager.INSTANCE.getStreams(TAG, API_REQUEST_GET_STREAMS);
        } else delegatesSet.notify(TAG).onStreamsUpdated(null);
    }

    public RealmList<Stream> getSortedStreams() {
        if (streamsMap == null) return null;
        RealmList<Stream> sortedStreams = new RealmList<>();
        for (Stream stream : sortedStreams)
            sortedStreams.add(stream);
//        Collections.sort(sortedStreams);
        return sortedStreams;
    }

    public void setActiveStream(Stream activeStream) {
        this.activeStream = activeStream;
    }

    public void setStreamAsRead(Stream stream) {
        if (streamsMap == null) return;
        Stream readStream = streamsMap.get(stream.getSid());
        if (readStream != null) {
            readStream.setUnreadCount(0);
            delegatesSet.notify(TAG).onStreamsUpdated(null);
        }
        ReceivedMessage lastReceivedMessage = stream.getLastReceivedMessage();
        if (lastReceivedMessage != null)
            UsersManager.INSTANCE.setMessageStatus(TAG, API_REQUEST_SET_MESSAGE_STATUS, lastReceivedMessage.getId(), MessageStatus.SEEN);
//        NotificationUtils.updateUnreadMessages(context, true, false);
    }

    public void addNewMessage(@NonNull Message message) {
        UsersManager.INSTANCE.setMessageStatus(TAG, 1, message.asReceived().getId(), MessageStatus.DELIVERED);
        boolean isYouOwnMessage = message.getUser() != null &&
                message.getUser().getId() == CurrentUserSettings.INSTANCE.getCurrentUser().getId();
        boolean isMessageFromActiveStream = activeStream != null
                && activeStream.getType() == message.getStreamType()
                && activeStream.getId() == message.getStreamId();
        boolean seen = isYouOwnMessage || isMessageFromActiveStream;
        if (seen)
            UsersManager.INSTANCE.setMessageStatus(TAG, 1, message.asReceived().getId(), MessageStatus.SEEN);
        else incrementUnreadCount(message);

        Stream stream = null;
        if (streamsMap != null)
            stream = streamsMap.get(Stream.Companion.generateSid(StreamType.Companion.find(message.getStreamType()), message.getStreamId()));
        if (stream == null)
            UsersManager.INSTANCE.getStreamById(TAG, API_REQUEST_GET_STREAM, StreamType.Companion.find(message.getStreamType()), message.getStreamId());
//        else
//            stream.addNewMessage(message);
        delegatesSet.notify(TAG).onNewMessageAdded(stream, message);
//        NotificationUtils.updateUnreadMessages(context, false, !seen);
    }

    public void addOldMessages(RealmList<Message> messages) {
        if (messages == null) return;
        for (Message message : messages) {
            Stream stream = streamsMap.get(Stream.Companion.generateSid(StreamType.Companion.find(message.getStreamType()), message.getStreamId()));
//            if (stream != null) stream.addOldMessage(message);
        }
//        delegatesSet.notify(TAG).onOldMessagesAdded(messages);
    }

    private void setStreams(RealmList<Stream> streams) {
        if (streams == null) {
            streamsMap.clear();
        } else {
            if (streamsMap != null) streamsMap.clear();
            else streamsMap = new HashMap<>();
            for (Stream stream : streams) {
//                stream.sortMessages();
                streamsMap.put(stream.getSid(), stream);
                if (stream.getUnreadCount() > 0) {
                    ReceivedMessage lastReceivedMessage = stream.getLastReceivedMessage();
                    if (lastReceivedMessage != null)
                        UsersManager.INSTANCE.setMessageStatus(TAG, 1, lastReceivedMessage.getId(), MessageStatus.DELIVERED);
                }
            }
        }
        delegatesSet.notify(TAG).onStreamsUpdated(null);
//        NotificationUtils.updateUnreadMessages(context, false, false);
    }

    private void addNewStream(StreamType streamType, Long streamId) {
        UsersManager.INSTANCE.getStreamById(TAG, API_REQUEST_GET_STREAM, streamType, streamId);
    }

    private void addNewStream(Stream stream) {
        if (streamsMap == null) streamsMap = new HashMap<>();
        streamsMap.put(stream.getSid(), stream);
        delegatesSet.notify(TAG).onStreamsUpdated(null);
        // TODO: 12.09.16      DatabaseService.saveStream(context, stream);
//        NotificationUtils.updateUnreadMessages(context, false, true);
    }

    public void setMessageAsDeleted(long messageId) {
        if (streamsMap != null)
            for (Stream stream : streamsMap.values())
                if (stream != null && stream.getAllMessages() != null)
                    for (Message message : stream.getAllMessages())
                        if (message != null && message.messageClassType() == MessageClassType.RECEIVED && message.asReceived().getId() == messageId) {
                            message.asReceived().setBodyType(MessageBodyType.DELETED.getJsonName());
                            delegatesSet.notify(TAG).onMessageDeleted( messageId);
                            delegatesSet.notify(TAG).onStreamsUpdated(null);
                            return;
                        }
    }

    public void removeStream(Stream stream) {
        if (stream != null)
            removeStream(StreamType.Companion.find(stream.getType()), stream.getId());
    }

    public void clearAll() {
        if (streamsMap != null) {
            streamsMap.clear();
            streamsMap = null;
        }
    }

    private void updateChat(long chatId, String name) {
        Stream stream = streamsMap.get(Stream.Companion.generateSid(StreamType.CHATS, chatId));
        if (stream != null) stream.getAsChat().setName(name);
        delegatesSet.notify(TAG).onStreamsUpdated(null);
    }

    private void removeStream(StreamType streamType, Long streamId) {
        if (streamType != null && streamId != null && streamId >= 0) {
            Stream stream = streamsMap.remove(Stream.Companion.generateSid(streamType, streamId));
            delegatesSet.notify(TAG).onStreamsUpdated(null);
            // TODO: 12.09.16      DatabaseService.removeStream(context, stream);
//            NotificationUtils.updateUnreadMessages(context, false, false);
        }
    }

    private void incrementUnreadCount(Message message) {
        if (streamsMap == null) return;
        Stream stream = streamsMap.get(Stream.Companion.generateSid(StreamType.Companion.find(message.getStreamType()), message.getStreamId()));
        if (stream != null) stream.setUnreadCount(stream.getUnreadCount() + 1);
        delegatesSet.notify(TAG).onStreamsUpdated(null);
    }

    private void updateMessageStatus(DeliveredInfo info) {
        if (streamsMap != null)
            for (Stream stream : streamsMap.values())
                if (stream != null && stream.getAllMessages() != null)
                    for (Message message : stream.getAllMessages())
                        if (message != null && message.messageClassType() == MessageClassType.RECEIVED && message.asReceived().getId() == info.getId())
                            message.asReceived().setMessageStatus(info.getUserId(), MessageStatus.DELIVERED);
//        delegatesSet.notify(TAG).onMessagesStatusUpdated();
    }

    private void updateMessageStatus(SeenInfo info) {
        if (streamsMap == null) return;
        for (long messageId : info.getIds())
            for (Stream stream : streamsMap.values())
                if (stream != null && stream.getAllMessages() != null)
                    for (Message message : stream.getAllMessages())
                        if (message != null && message.messageClassType() == MessageClassType.RECEIVED && message.asReceived().getId() == messageId)
                            message.asReceived().setMessageStatus(info.getUserId(), MessageStatus.SEEN);
//        delegatesSet.notify(TAG).onMessagesStatusUpdated();
    }

    public void onGetPushNotification(long messageId) {
        // FIXME: 12.09.16 push must not received when socket is connected
        if (SocketManager.INSTANCE.isConnected())
            UsersManager.INSTANCE.getMessageById(TAG, API_REQUEST_GET_MESSAGE, messageId);
    }

    private UsersDelegate apiDelegate = new UsersDelegate.Simple() {
        @Override
        public void onMessageReceived(Integer requestCode, ReceivedMessage message) {
            addNewMessage(message);
        }

        @Override
        public void onStreamsReceived(Integer requestCode, RealmList<Stream> streams) {
            setStreams(streams);
        }

        @Override
        public void onStreamReceived(Integer requestCode, Stream stream) {
            addNewStream(stream);
        }
    };

    private SocketListener socketListener = new SocketListener() {
        @Override
        public void onConnected() {
        }

        @Override
        public void onMessageFailed() {
            Log.d(TAG, "onMessageFailed() called with: " + "");
        }

        @Override
        public void onMessageSent() {
            Log.d(TAG, "onMessageSent() called with: " + "");
        }

        @Override
        public void onMessageReceived(Message message) {
            Log.d(TAG, "onMessageReceived() called with: " + "message = [" + message + "]");
            addNewMessage(message);
        }

        @Override
        public void onMessageDelivered(DeliveredInfo info) {
            Log.d(TAG, "onMessageDelivered() called with: " + "info = [" + info + "]");
            updateMessageStatus(info);
        }

        @Override
        public void onMessagesSeen(SeenInfo info) {
            Log.d(TAG, "onMessagesSeen() called with: " + "info = [" + info + "]");
            updateMessageStatus(info);
        }

        @Override
        public void onMessageRemoved(long messageId) {
            Log.d(TAG, "onMessageRemoved() called with: " + "messageId = [" + messageId + "]");
            setMessageAsDeleted(messageId);
        }

        @Override
        public void onSubscribed(SubscribeData data) {
            Log.d(TAG, "onSubscribed() called with: " + "data = [" + data + "]");
            if (data.getStreamId() == null) initStreams(true);
            else addNewStream(StreamType.Companion.find(data.getStreamType()), data.getStreamId());
        }

        @Override
        public void onUnsubscribed(SubscribeData data) {
            Log.d(TAG, "onUnsubscribed() called with: " + "data = [" + data + "]");
            if (data.getStreamId() == null) initStreams(true);
            else removeStream(StreamType.Companion.find(data.getStreamType()), data.getStreamId());
        }

        @Override
        public void onChatCreated(Stream chat) {
            Log.d(TAG, "onChatCreated() called with: " + "chat = [" + chat + "]");
            addNewStream(chat);
        }

        @Override
        public void onChatUpdated(long chatId, String name) {
            Log.d(TAG, "onChatUpdated() called with: " + "chatId = [" + chatId + "], name = [" + name + "]");
            updateChat(chatId, name);
        }

        @Override
        public void onInvitedToChat(Stream chat) {
            Log.d(TAG, "onInvitedToChat() called with: " + "chat = [" + chat + "]");
            addNewStream(chat);
        }

        @Override
        public void onUploadedPhotoToChat(long chatId, Media photo) {
            Log.d(TAG, "onUploadedPhotoToChat() called with: " + "chatId = [" + chatId + "], photo = [" + photo + "]");
            Stream stream = streamsMap.get(Stream.Companion.generateSid(StreamType.CHATS, chatId));
            if (stream != null) {
                stream.getAsChat().setPhoto(photo);
                delegatesSet.notify(TAG).onStreamsUpdated(null);
            }
        }

        @Override
        public void onLeftChat(long chatId, long userId) {
            Log.d(TAG, "onLeftChat() called with: " + "chatId = [" + chatId + "], userId = [" + userId + "]");
            if (CurrentUserSettings.INSTANCE.getCurrentUser().getId() == userId)
                removeStream(StreamType.CHATS, chatId);
        }

        @Override
        public void onChatDeleted(long chatId) {
            Log.d(TAG, "onChatDeleted() called with: " + "chatId = [" + chatId + "]");
            removeStream(StreamType.CHATS, chatId);
        }

        @Override
        public void onStickersGroupCreated() {

        }

        @Override
        public void onStickersGroupUpdated() {

        }

        @Override
        public void onStickersGroupDeleted() {

        }

        @Override
        public void onStickerCreated() {

        }

        @Override
        public void onStickerDeleted() {

        }
    };
}
