package com.owfar.android.ui.group_chat_options;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.owfar.android.R;
import com.owfar.android.api.users.UsersDelegate;
import com.owfar.android.api.users.UsersManager;
import com.owfar.android.helpers.KeyboardHelper;
import com.owfar.android.helpers.MediaIntentHelper;
import com.owfar.android.models.api.classes.Stream;
import com.owfar.android.models.api.classes.User;
import com.owfar.android.models.api.enums.MediaStorageType;
import com.owfar.android.models.errors.Error;
import com.owfar.android.settings.CurrentUserSettings;
import com.owfar.android.ui.choose_contact.ChooseContactFragment;
import com.owfar.android.ui.contacts.ContactItemAdapter;
import com.owfar.android.ui.contacts.ContactItemHolder;
import com.owfar.android.ui.main.MainAppBarHelper;
import com.owfar.android.ui.main.MainBaseFragment;
import com.owfar.android.ui.main.MainFabHelper;
import com.owfar.android.ui.profile.UserProfileFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;


public class GroupChatOptionsFragment extends MainBaseFragment implements View.OnClickListener, MainAppBarHelper.OnImageShownListener, TextWatcher, KeyboardHelper.KeyboardListener, MediaIntentHelper.OnImageTakenListener, ContactItemHolder.OnContactClickListener, ContactItemHolder.ContactContextMenuListener, MainFabHelper.OnFabClickListener {

    //region constants
    private static final String TAG = GroupChatOptionsFragment.class.getSimpleName();

    private static final String ARG_STREAM = "ARG_STREAM";
    private static final int API_REQUEST_UPLOAD_CHAT_PHOTO = 1;
    private static final int API_REQUEST_UPDATE_CHAT = 2;
    private static final int API_REQUEST_KICK_USER_FROM_CHAT = 3;
    private static final int API_REQUEST_INVITE_USERS_TO_CHAT = 4;

    private static final int REQUEST_ADD_MEMBERS = 10;
    //endregion

    //region widgets
    private View vOwner;
    private TextView tvGroupName;
    private EditText etGroupName;
    private Button btSave;
    private RecyclerView rvParticipants;
    //endregion

    private ContactItemAdapter adapter;
    private MediaIntentHelper mediaIntentHelper;

    //region arguments
    private Stream stream;
    private User chatOwner;
    //endregion

    //region Creating Instances
    public static GroupChatOptionsFragment newInstance(@NonNull Stream stream) {
        GroupChatOptionsFragment fragment = new GroupChatOptionsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_STREAM, stream);
        fragment.setArguments(args);
        return fragment;
    }
    //endregion

    //region Life-Cycle Methods
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        UsersManager.INSTANCE.getDelegatesSet().addDelegate(TAG, localUsersDelegate);

        mediaIntentHelper = new MediaIntentHelper(this, null,null);
        mediaIntentHelper.setOnImageTakenListener(this);

        if (savedInstanceState != null) mediaIntentHelper.restoreState(savedInstanceState);

        stream = getArguments().getParcelable(ARG_STREAM);
        chatOwner = stream.getAsChat().getUsers().get(0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getAppBarHelper()
                .setTitle(stream.getDisplayName())
                .showMaterialMenu()
                .setMaterialMenuState(MaterialMenuDrawable.IconState.ARROW)
                .hideSearch()
                .hideImage()
                .setOnImageShownListener(this)
                .hideTabs();
        getNavigationMenuHelper()
                .lockClosed();
        getFabHelper().show(MainFabHelper.FabIcon.ADD, this);

        return inflater.inflate(R.layout.fragment_group_chat_options, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vOwner = view.findViewById(R.id.fragment_group_chat_options_vOwner);
        tvGroupName = (TextView) view.findViewById(R.id.fragment_group_chat_options_tvGroupName);
        etGroupName = (EditText) view.findViewById(R.id.fragment_group_chat_options_etGroupName);
        btSave = (Button) view.findViewById(R.id.fragment_group_chat_options_btSave);
        rvParticipants = (RecyclerView) view.findViewById(R.id.fragment_group_chat_options_rvParticipants);

        ViewCompat.setNestedScrollingEnabled(view, false);

        if (stream != null && stream.getAsChat().getPhoto() != null && stream.getAsChat().getPhoto().getMediaFileId() >= 0)
            getAppBarHelper().showImage(MediaStorageType.CHATS_PHOTOS, stream.getAsChat().getPhoto());

        ContactItemHolder ownerHolder = new ContactItemHolder(vOwner, false, false);
        ownerHolder.configureWith(chatOwner, null);
        ownerHolder.setOnContactClickListener(this);
        if (savedInstanceState == null) etGroupName.setText(stream.getAsChat().getName());
        etGroupName.addTextChangedListener(this);
        btSave.setOnClickListener(this);
        rvParticipants.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new ContactItemAdapter(false, false);
        RealmList<User> allContacts = new RealmList<>();
        for (User user : stream.getAsChat().getUsers().subList(1, stream.getAsChat().getUsers().size()))
            allContacts.add(user);
        adapter.setAllContacts(allContacts);
        adapter.setOnContactClickListener(this);
        if (chatOwner.getId() == CurrentUserSettings.INSTANCE.getCurrentUser().getId())
            adapter.setContactContextMenuListener(this);
        rvParticipants.setAdapter(adapter);

        KeyboardHelper.INSTANCE.setKeyboardListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mediaIntentHelper.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_MEMBERS && resultCode == Activity.RESULT_OK) {
            ArrayList<User> checkedContactsArrayList = data.getParcelableArrayListExtra(ChooseContactFragment.Companion.getEXTRA_CHECKED_CONTACTS());
            RealmList<User> checkedContacts = new RealmList<>();
            if (checkedContactsArrayList != null)
                for (User user : checkedContactsArrayList) checkedContacts.add(user);
            if (checkedContacts.size() != 0)
                UsersManager.INSTANCE.inviteUsersToChat(TAG, API_REQUEST_INVITE_USERS_TO_CHAT, stream, checkedContacts);
        }
    }

    @Override
    public void onDestroyView() {
        getAppBarHelper().eraseImage(true).setOnImageShownListener(null);
        getNavigationMenuHelper().unlock();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        UsersManager.INSTANCE.getDelegatesSet().removeDelegate(localUsersDelegate);
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mediaIntentHelper.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.profile, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                getActivity().onBackPressed();
                return true;

            case R.id.action_profile_camera:
                KeyboardHelper.INSTANCE.hideKeyboard();
                return true;

            case R.id.action_profile_takePhoto:
                mediaIntentHelper.requestTakePhoto();
                return true;

            case R.id.action_profile_pickPhoto:
                mediaIntentHelper.requestPickPhoto();
                return true;

            case R.id.action_profile_pickImageContent:
                mediaIntentHelper.requestPickImageContent();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //endregion

    @Override
    public void onImageShown() {
        ViewCompat.setNestedScrollingEnabled(getView(), true);
    }

    //region Local UsersDelegate
    private UsersDelegate localUsersDelegate = new UsersDelegate.Simple() {
        @Override
        public void onPhotoForChatUploaded(Integer requestCode) {
            getAppBarHelper().hideImageProgress();
        }

        @Override
        public void onChatUpdated(Integer requestCode, Stream chat) {
            GroupChatOptionsFragment.this.stream.getAsChat().setName(chat.getAsChat().getName());
            getAppBarHelper().setTitle(chat.getDisplayName());
            btSave.setVisibility(View.GONE);
        }

        @Override
        public void onUserKickedFromChat(Integer requestCode, User user) {
            stream.getAsChat().getUsers().remove(user);
            adapter.remove(user);
        }

        @Override
        public void onUsersInvitedToChat(Integer requestCode, List<User> users) {
            stream.getAsChat().getUsers().addAll(users);
            adapter.add(users);
        }

        @Override
        public void onError(Integer requestCode, Error error) {
            super.onError(requestCode, error);
        }
    };
    //endregion

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        btSave.setVisibility(View.VISIBLE);
    }

    @Override
    public void afterTextChanged(Editable s) {
        tvGroupName.setVisibility(TextUtils.isEmpty(s) ? View.GONE : View.VISIBLE);
    }


    //region UI Listeners Implementation
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_group_chat_options_btSave:
                long chatId = stream.getId();
                String groupName = !TextUtils.isEmpty(etGroupName.getText()) ? etGroupName.getText().toString() : null;
                UsersManager.INSTANCE.updateChat(TAG, API_REQUEST_UPDATE_CHAT, chatId, groupName);
                btSave.setVisibility(View.GONE);
                break;
        }
    }
    //endregion


    //region KeyboardListener Implementation
    @Override
    public void onShowKeyboard() {
        getAppBarHelper().setMaterialMenuState(MaterialMenuDrawable.IconState.CHECK);
        if (getAppBarHelper() != null && getAppBarHelper().isExpanded()) {
            getAppBarHelper().collapse(true);
            ViewCompat.setNestedScrollingEnabled(getView(), false);
        }
        getFabHelper().hide();
    }

    @Override
    public void onHideKeyboard() {
        getAppBarHelper().setMaterialMenuState(MaterialMenuDrawable.IconState.ARROW);
        if (getAppBarHelper() != null && !getAppBarHelper().isExpanded()) {
            getAppBarHelper().expandImage(true);
            ViewCompat.setNestedScrollingEnabled(getView(), true);
        }
        getFabHelper().show(MainFabHelper.FabIcon.ADD, this);
    }
    //endregion

    @Override
    public void onImageTaken(String imagePath) {
        long chatId = stream.getId();
        File image = new File(imagePath);
        getAppBarHelper().showImageWithProgress(Uri.fromFile(image), "Uploading chat photo to server...");
        UsersManager.INSTANCE.uploadPhotoForChat(TAG, API_REQUEST_UPLOAD_CHAT_PHOTO, chatId, image);
    }

    @Override
    public void onContactClick(int adapterPosition, User user) {
        if (adapterPosition < 0) user = stream.getAsChat().getUsers().get(0);
        if (user.getId() != CurrentUserSettings.INSTANCE.getCurrentUser().getId()) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, UserProfileFragment.newInstance(user))
                    .addToBackStack(null)
                    .commit();
            getFragmentManager().executePendingTransactions();
        }
    }

    private int selectedParticipantAdapterPosition;
    private User selectedParticipant;

    @Override
    public void onPrepareContactContextMenu(int adapterPosition, User contact) {
        selectedParticipantAdapterPosition = adapterPosition;
        selectedParticipant = contact;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo
            menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = new MenuInflater(v.getContext());
        menuInflater.inflate(R.menu.participants_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_kick_user_from_chat:
                UsersManager.INSTANCE.kickUserFromChat(TAG, API_REQUEST_KICK_USER_FROM_CHAT, stream, selectedParticipant);
                selectedParticipantAdapterPosition = -1;
                selectedParticipant = null;
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onFabClick() {
        getTransactionHelper().addMembers(stream.getId(), this, REQUEST_ADD_MEMBERS);
    }
}
