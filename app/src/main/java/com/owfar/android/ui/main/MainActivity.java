package com.owfar.android.ui.main;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.owfar.android.R;
import com.owfar.android.data.DataDelegate;
import com.owfar.android.data.DataManager;
import com.owfar.android.gcm.QuickstartPreferences;
import com.owfar.android.gcm.RegistrationIntentService;
import com.owfar.android.helpers.KeyboardHelper;
import com.owfar.android.helpers.MainTransactionHelper;
import com.owfar.android.helpers.PermissionHelper;
import com.owfar.android.models.api.classes.Stream;
import com.owfar.android.models.api.enums.MessageStatus;
import com.owfar.android.models.api.interfaces.Message;
import com.owfar.android.socket.SocketManager;
import com.owfar.android.ui.boards.BoardsHelper;
import com.owfar.android.ui.registration.RegistrationActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        MainNavigationMenuHelper.NavigationMenuListener,
        PermissionHelper.PermissionCallback {

    //region constants
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String ACTION_SHOW_CHATS = "ACTION_SHOW_CHATS";
    private static final String ACTION_SHOW_MESSENGER = "ACTION_SHOW_MESSENGER";

    private static final String EXTRA_STREAM = "EXTRA_STREAM";

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1;
    private static final int PERMISSION_REQUEST_READ_WRITE = 2;
    private static final int DIALOG_NEED_READ_WRITE_PERMISSION = 3;
    //endregion

    //region widgets
    protected RelativeLayout fragmentContainer;
    //endregion

    //region fields
    protected MainAppBarHelper appBarHelper;
    protected MainNavigationMenuHelper navigationMenuHelper;
    protected MainFabHelper fabHelper;
    protected BoardsHelper boardsHelper;
    protected MainTransactionHelper transactionHelper;

    protected boolean isActivityJustCreated;
    private PermissionHelper permissionHelper;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private BillingProcessor billingProcessor;
    //endregion

    public static Intent createIntentToShowChats(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(ACTION_SHOW_CHATS);
        return intent;
    }

    public static Intent createIntentToShowMessenger(Context context, Stream stream) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(ACTION_SHOW_MESSENGER);
        intent.putExtra(EXTRA_STREAM, stream);
        return intent;
    }

    //region Activity Life-Cycle Methods
    public static void start(Context context) {
        Intent intent = new Intent(context, RegistrationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getAction() != null) switch (intent.getAction()) {
            default:
            case ACTION_SHOW_CHATS:
                transactionHelper.showChats();
                break;
            case ACTION_SHOW_MESSENGER:
                Stream stream = intent.getParcelableExtra(EXTRA_STREAM);
                if (stream != null) transactionHelper.showMessenger(stream);
                else transactionHelper.showChats();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called with: " + "savedInstanceState = [" + savedInstanceState + "]");

        setContentView(R.layout.activity_main);
        KeyboardHelper.INSTANCE.init(this);
        isActivityJustCreated = true;

        fragmentContainer = (RelativeLayout) findViewById(R.id.fragment_container);

        appBarHelper = new MainAppBarHelper(this);
        navigationMenuHelper = new MainNavigationMenuHelper(this);
        navigationMenuHelper.setNavigationMenuListener(this);
        fabHelper = new MainFabHelper(this);
        boardsHelper = new BoardsHelper();
        transactionHelper = new MainTransactionHelper(this);

        if (savedInstanceState == null) {
            appBarHelper.expand(false);
            navigationMenuHelper.performItemAction(R.id.nav_chats);

            String[] nestedPermissions;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                nestedPermissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE};
            else nestedPermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (permissionHelper == null) permissionHelper = new PermissionHelper(this);
            permissionHelper.verifyPermission(PERMISSION_REQUEST_READ_WRITE, nestedPermissions, this);
        }

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences.getBoolean(QuickstartPreferences.INSTANCE.getSENT_TOKEN_TO_SERVER(), false);
                if (sentToken) Log.d(TAG, getString(R.string.gcm_send_message));
                else Log.d(TAG, getString(R.string.token_error_message));
            }
        };

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }

        SocketManager.INSTANCE.initSocket();
        DataManager.INSTANCE.getDelegatesSet().addDelegate(DataManager.INSTANCE.getTAG(), dataDelegate);

        billingProcessor = new BillingProcessor(this,
                getResources().getString(R.string.billing_license_key), billingHandler);

        if (savedInstanceState == null) onNewIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        SocketManager.INSTANCE.connect();
        registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.INSTANCE.getREGISTRATION_COMPLETE()));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (billingProcessor != null)
            billingProcessor.handleActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mRegistrationBroadcastReceiver);
        SocketManager.INSTANCE.disconnect();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (billingProcessor != null) billingProcessor.release();
        DataManager.INSTANCE.getDelegatesSet().removeDelegate(dataDelegate);
        SocketManager.INSTANCE.releaseSocket();
        super.onDestroy();
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode))
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (navigationMenuHelper.isOpened()) navigationMenuHelper.close();
        else if (appBarHelper.isSearchVisible()) appBarHelper.hideSearch();
        else if (KeyboardHelper.INSTANCE.hideKeyboard()) return;
        else if (boardsHelper.hideBoards()) return;
        else if (getFragmentManager().getBackStackEntryCount() > 0)
            getFragmentManager().popBackStack();
        else if (getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStack();
        else navigationMenuHelper.open();
//            super.onBackPressed();
    }
    //endregion

    //region NavigationMenuListener Implementation
    @Override
    public void onNavigationMenuOpened() {

    }

    @Override
    public void onNavigationMenuClosed() {

    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_chats:
                transactionHelper.showChats();
                break;
            case R.id.nav_contacts:
                transactionHelper.showContacts();
                break;
            case R.id.nav_settings:
                transactionHelper.showSettings();
                break;
            case R.id.nav_profile:
                transactionHelper.showMyProfile();
                break;
            case R.id.nav_channels:
                transactionHelper.showChannels();
                break;
            case R.id.nav_developers:
                transactionHelper.showDevelopers();
//                break;
        }

        navigationMenuHelper.close();
        return true;
    }
    //endregion

    //region Private Tools

    //endregion

    @Override
    public void grantedAllPermissions(int requestCode, String[] permissions) {

    }

    @Override
    public void deniedPermissions(int requestCode, String[] permissions) {
//        if (requestCode == PERMISSION_REQUEST_READ_WRITE)
//            NeedReadWriteForCachingDialog.newInstance(DIALOG_NEED_READ_WRITE_PERMISSION)
//                    .show(getSupportFragmentManager(), TAG);
    }

    public void onDialogResult(int requestCode, int which) {
        if (requestCode == DIALOG_NEED_READ_WRITE_PERMISSION && which == DialogInterface.BUTTON_NEUTRAL) {
            String[] nestedPermissions;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                nestedPermissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE};
            else nestedPermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (permissionHelper == null) permissionHelper = new PermissionHelper(this);
            permissionHelper.verifyPermission(PERMISSION_REQUEST_READ_WRITE, nestedPermissions, this);
        }
    }

    private void updateChatsBadge() {
        int count = 0;
        List<Stream> streams =
                new ArrayList<>();
//                DataManager.INSTANCE.getStreams();
        if (streams != null) for (Stream stream : streams)
            if (stream != null) count += stream.getUnreadCount();
        String badge = count == 0 ? null : count > 99 ? "+99" : String.valueOf(count);
        navigationMenuHelper.setChatsBadge(badge);
    }

    private DataDelegate dataDelegate = new DataDelegate() {

        @Override
        public void onStreamsUpdated(@NotNull List<Stream> streams) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateChatsBadge();
                }
            });
        }

        @Override
        public void onNewMessageAdded(Stream stream, Message message) {

        }

        @Override
        public void onOldMessagesAdded(@NotNull Stream stream, @NotNull List<Message> messages) {

        }

        @Override
        public void onMessageStatusUpdated(long messageId, long userId, @NotNull MessageStatus status) {

        }

        @Override
        public void onMessageDeleted(long messageId) {

        }
    };

    private final BillingProcessor.IBillingHandler billingHandler = new BillingProcessor.IBillingHandler() {

        @Override
        public void onBillingInitialized() {
            Log.d("BILLING", "onBillingInitialized() called");
        }

        @Override
        public void onPurchaseHistoryRestored() {
            Log.d("BILLING", "onPurchaseHistoryRestored() called");
        }

        @Override
        public void onProductPurchased(String productId, TransactionDetails details) {
            Log.d("BILLING", "onProductPurchased() called with: productId = [" + productId + "], details = [" + details + "]");
        }

        @Override
        public void onBillingError(int errorCode, Throwable error) {
            Log.d("BILLING", "onBillingError() called with: errorCode = [" + errorCode + "], error = [" + error + "]");
        }
    };
}