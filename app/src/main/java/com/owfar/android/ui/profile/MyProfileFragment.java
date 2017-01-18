package com.owfar.android.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.owfar.android.R;
import com.owfar.android.api.users.UsersDelegate;
import com.owfar.android.api.users.UsersManager;
import com.owfar.android.helpers.FacebookHelper;
import com.owfar.android.helpers.KeyboardHelper;
import com.owfar.android.helpers.MediaIntentHelper;
import com.owfar.android.models.api.classes.Profile;
import com.owfar.android.models.api.classes.User;
import com.owfar.android.models.api.enums.MediaStorageType;
import com.owfar.android.models.errors.Error;
import com.owfar.android.models.facebook.FacebookProfile;
import com.owfar.android.settings.CurrentUserSettings;
import com.owfar.android.ui.main.MainAppBarHelper;
import com.owfar.android.ui.main.MainBaseFragment;
import com.owfar.android.ui.snackbars.DefaultSnackbar;

import java.io.File;

import static android.support.v4.view.ViewCompat.setNestedScrollingEnabled;

public class MyProfileFragment extends MainBaseFragment implements View.OnClickListener,
        KeyboardHelper.KeyboardListener, MainAppBarHelper.OnImageShownListener,
        FacebookHelper.FacebookHelperListener, MediaIntentHelper.OnImageTakenListener,
        TextWatcher, CompoundButton.OnCheckedChangeListener {

    //region constants
    private static final String TAG = MyProfileFragment.class.getSimpleName();
    private static final String TITLE = "Profile";

    private static final int API_REQUEST_UPLOAD_USER_PHOTO = 1;
    private static final int API_REQUEST_GET_MY_PROFILE = 2;
    private static final int API_REQUEST_UPDATE_MY_PROFILE = 3;
    //endregion

    //region widgets
    private View vFocusable;
    private View vConnect;
    private EditText etFullName;
    private EditText etSynopsis;
    private RadioButton rbMale;
    private RadioButton rbFemale;
    private Button btSave;
    //endregion

    //region fields
    private String firstName;
    private String lastName;
    private String gender;
    //    private boolean isFieldsChanged;
    private FacebookHelper facebookHelper;
    private MediaIntentHelper mediaIntentHelper;
    //endregion

    //region Creating Instances
    public static MyProfileFragment newInstance() {
        return new MyProfileFragment();
    }
    //endregion

    //region Life-Cycle Methods
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        UsersManager.INSTANCE.getDelegatesSet().addDelegate(TAG, localUsersDelegate);

        facebookHelper = new FacebookHelper(getActivity().getApplicationContext(), this);
        mediaIntentHelper = new MediaIntentHelper(this,null,null);
        mediaIntentHelper.setOnImageTakenListener(this);

        if (savedInstanceState != null) mediaIntentHelper.restoreState(savedInstanceState);

        Profile currentProfile = CurrentUserSettings.INSTANCE.getCurrentUser().getProfile();
        if (currentProfile != null) {
            firstName = currentProfile.getFirstName();
            lastName = currentProfile.getLastName();
            gender = currentProfile.getGender();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getAppBarHelper()
                .setTitle(TITLE)
                .showMaterialMenu()
                .setMaterialMenuState(MaterialMenuDrawable.IconState.BURGER)
                .hideSearch()
                .setOnImageShownListener(this)
                .hideTabs();
        getFabHelper().hide();

        return inflater.inflate(R.layout.fragment_my_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vFocusable = view.findViewById(R.id.fragment_my_profile_vFocusable);
        vConnect = view.findViewById(R.id.fragment_my_profile_vConnect);
        etFullName = (EditText) view.findViewById(R.id.fragment_my_profile_etFullName);
        etSynopsis = (EditText) view.findViewById(R.id.fragment_my_profile_etSynopsis);
        rbMale = (RadioButton) view.findViewById(R.id.fragment_my_profile_rbMale);
        rbFemale = (RadioButton) view.findViewById(R.id.fragment_my_profile_rbFemale);
        btSave = (Button) view.findViewById(R.id.fragment_my_profile_btSave);

        setNestedScrollingEnabled(view, false);

        User user = CurrentUserSettings.INSTANCE.getCurrentUser();
        if (UsersManager.INSTANCE.isUserPhotoUploading())
            getAppBarHelper().showImageWithProgress(Uri.fromFile(UsersManager.INSTANCE.getUploadingUserPhoto()), "Uploading photo to server...");
        else if (user != null && user.getProfile() != null && user.getProfile().getPhoto() != null && user.getProfile().getPhoto().getMediaFileId() >= 0)
            getAppBarHelper().showImage(MediaStorageType.USERS_PHOTOS, user.getProfile().getPhoto());
        else getAppBarHelper().hideImage();

        configureView();

        vConnect.setOnClickListener(this);
        etFullName.addTextChangedListener(this);
        rbMale.setOnCheckedChangeListener(this);
        rbFemale.setOnCheckedChangeListener(this);
        btSave.setOnClickListener(this);

        KeyboardHelper.INSTANCE.setKeyboardListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebookHelper.onActivityResult(requestCode, resultCode, data);
        mediaIntentHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mediaIntentHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onDestroyView() {
        getAppBarHelper().eraseImage(true).setOnImageShownListener(null);
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

    //region View Configuration
    private void configureView() {
        if (firstName == null && lastName == null) etFullName.setText(null);
        else if (firstName != null && lastName != null)
            etFullName.setText(String.format("%s %s", firstName, lastName));
        else etFullName.setText(firstName != null ? firstName : lastName);

        if (gender == null) {
            rbMale.setChecked(false);
            rbFemale.setChecked(false);
        } else switch (gender) {
            case "male":
                rbFemale.setChecked(false);
                rbMale.setChecked(true);
                break;
            case "female":
                rbMale.setChecked(false);
                rbFemale.setChecked(true);
                break;
        }
    }
    //endregion

    //region UI Listeners Implementation
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_my_profile_vConnect:
                KeyboardHelper.INSTANCE.hideKeyboard();
                facebookHelper.requestFacebookProfile(this);
                break;
            case R.id.fragment_my_profile_btSave:
                KeyboardHelper.INSTANCE.hideKeyboard();
                UsersManager.INSTANCE.updateProfile(TAG, API_REQUEST_UPDATE_MY_PROFILE,
                        UsersManager.MY_USER_ID, firstName, lastName, gender);
                break;
        }
    }
    //endregion

    //region KeyboardListener Implementation
    @Override
    public void onShowKeyboard() {
        dismissErrorIfNeed();
        if (getAppBarHelper().isExpanded()) {
            getAppBarHelper().collapse(true).setMaterialMenuState(MaterialMenuDrawable.IconState.CHECK);
            setNestedScrollingEnabled(getView(), false);
            getNavigationMenuHelper().lockClosed();
        }
    }

    @Override
    public void onHideKeyboard() {
        vFocusable.requestFocus();
        if (!getAppBarHelper().isExpanded()) {
            getAppBarHelper().expandImage(true).setMaterialMenuState(MaterialMenuDrawable.IconState.BURGER);
            if (getAppBarHelper().isImageLoaded()) setNestedScrollingEnabled(getView(), true);
            getNavigationMenuHelper().unlock();
        }
    }
    //endregion

    //region OnImageShownListener Implementation
    @Override
    public void onImageShown() {
        setNestedScrollingEnabled(getView(), true);
    }
    //endregion

    //region FacebookHelperListener Implementation
    @Override
    public void onFacebookProfileReceived(FacebookProfile facebookProfile) {
        // TODO: 26.04.16 dialog to confirm that user want to use facebook photo
        firstName = facebookProfile.getFirstName();
        lastName = facebookProfile.getLastName();
        gender = facebookProfile.getGender();
        configureView();
    }
    //endregion

    //region TextWatcher Implementation
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String fullName = s.toString();
        int indexOfSpace = fullName.indexOf(' ');
        if (indexOfSpace > 0 && indexOfSpace < fullName.length() - 1) {
            firstName = fullName.substring(0, indexOfSpace);
            lastName = fullName.substring(indexOfSpace + 1, fullName.length());
        } else firstName = fullName;
        btSave.setEnabled(true);
//        isFieldsChanged = true;
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
    //endregion

    //region OnCheckedChangeListener Implementation
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        KeyboardHelper.INSTANCE.hideKeyboard();
        if (isChecked) switch (buttonView.getId()) {
            case R.id.fragment_my_profile_rbMale:
                gender = "male";
                break;
            case R.id.fragment_my_profile_rbFemale:
                gender = "female";
                break;
            default:
                gender = null;
                break;
        }
        btSave.setEnabled(true);
//        isFieldsChanged = true;
    }
    //endregion

    //region OnImageTakenListener Implementation
    @Override
    public void onImageTaken(String imagePath) {
        Log.d(TAG, "onImageTaken() called with: " + "path = [" + imagePath + "]");
        getAppBarHelper().showImageWithProgress(Uri.fromFile(new File(imagePath)), "Uploading photo to server...");
        UsersManager.INSTANCE.uploadUserPhoto(TAG, API_REQUEST_UPLOAD_USER_PHOTO, new File(imagePath));
    }
    //endregion

    //region Local UsersDelegate
    private UsersDelegate localUsersDelegate = new UsersDelegate.Simple() {
        @Override
        public void onUserPhotoUploaded(Integer requestCode) {
            getAppBarHelper().hideImageProgress();
            UsersManager.INSTANCE.getProfile(TAG, API_REQUEST_GET_MY_PROFILE, UsersManager.MY_USER_ID);
        }

        @Override
        public void onProfileUpdated(Integer requestCode, Profile profile) {
            if (requestCode == API_REQUEST_UPDATE_MY_PROFILE) {
                DefaultSnackbar.make(getView(), "Profile updated", Snackbar.LENGTH_SHORT).show();
                btSave.setEnabled(false);
                UsersManager.INSTANCE.getProfile(TAG, API_REQUEST_GET_MY_PROFILE, UsersManager.MY_USER_ID);
            }
        }

        @Override
        public void onProfileReceived(Integer requestCode, Profile profile) {
            if (requestCode == API_REQUEST_GET_MY_PROFILE) {
                User user = CurrentUserSettings.INSTANCE.getCurrentUser();
                user.setProfile(profile);
                CurrentUserSettings.INSTANCE.setCurrentUser(user);
                getNavigationMenuHelper().updateUserInfo();
            }
        }

        @Override
        public void onError(Integer requestCode, Error error) {
            showError(error);
        }
    };
    //endregion
}
