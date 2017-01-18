package com.owfar.android.ui.profile;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.owfar.android.R;
import com.owfar.android.api.users.UsersDelegate;
import com.owfar.android.api.users.UsersManager;
import com.owfar.android.models.api.classes.Profile;
import com.owfar.android.models.api.classes.Stream;
import com.owfar.android.models.api.classes.User;
import com.owfar.android.models.api.enums.MediaStorageType;
import com.owfar.android.models.errors.Error;
import com.owfar.android.ui.main.MainAppBarHelper;
import com.owfar.android.ui.main.MainBaseFragment;
import com.owfar.android.ui.messenger.MessengerFragment;

public class UserProfileFragment extends MainBaseFragment implements View.OnClickListener, MainAppBarHelper.OnImageShownListener {

    //region constants
    private static final String TAG = UserProfileFragment.class.getSimpleName();

    private static final int API_REQUEST_START_CONVERSATION = 1;

    private static final String ARG_USER = "ARG_USER";
    //endregion

    //region widgets
    private View vFullName;
    private TextView tvFullName;
    private View vPhone;
    private TextView tvPhone;
    private View vGender;
    private TextView tvGender;
    private Button btSendMessage;
    //endregion

    //region arguments
    private User user;
    //endregion

    //region Creating Instances
    public static UserProfileFragment newInstance(@NonNull User user) {
        UserProfileFragment fragment = new UserProfileFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_USER, user);
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

        user = getArguments().getParcelable(ARG_USER);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getAppBarHelper()
                .setTitle(user.getDisplayName())
                .showMaterialMenu()
                .setMaterialMenuState(MaterialMenuDrawable.IconState.ARROW)
                .hideSearch()
                .hideImage()
                .setOnImageShownListener(this)
                .hideTabs();
        getNavigationMenuHelper()
                .lockClosed();
        getFabHelper().hide();

        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vFullName = view.findViewById(R.id.fragment_user_profile_vFullName);
        tvFullName = (TextView) view.findViewById(R.id.fragment_user_profile_tvFullName);
        vPhone = view.findViewById(R.id.fragment_user_profile_vPhone);
        tvPhone = (TextView) view.findViewById(R.id.fragment_user_profile_tvPhone);
        vGender = view.findViewById(R.id.fragment_user_profile_vGender);
        tvGender = (TextView) view.findViewById(R.id.fragment_user_profile_tvGender);
        btSendMessage = (Button) view.findViewById(R.id.fragment_user_profile_btSendMessage);

        ViewCompat.setNestedScrollingEnabled(view, false);

        if (user != null && user.getProfile() != null && user.getProfile().getPhoto() != null)
            getAppBarHelper().showImage(MediaStorageType.USERS_PHOTOS, user.getProfile().getPhoto());

        configureView();

        btSendMessage.setOnClickListener(this);
    }

    @Override
    public void onDestroyView() {
        getAppBarHelper().setOnImageShownListener(null);
        getNavigationMenuHelper().unlock();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        UsersManager.INSTANCE.getDelegatesSet().removeDelegate(localUsersDelegate);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                getActivity().onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //endregion

    //region View Configuration
    private void configureView() {
        if (TextUtils.isEmpty(user.getPhone())) vPhone.setVisibility(View.GONE);
        else tvPhone.setText(user.getPhone());
        if (user.getProfile() == null) {
            vFullName.setVisibility(View.GONE);
            vGender.setVisibility(View.GONE);
        } else {
            Profile profile = user.getProfile();
            if (TextUtils.isEmpty(profile.getFullName())) vFullName.setVisibility(View.GONE);
            else tvFullName.setText(profile.getFullName());
            if (TextUtils.isEmpty(profile.getGender())) vGender.setVisibility(View.GONE);
            else tvGender.setText(profile.getGender());
        }
    }
    //endregion

    //region UI Listeners Implementation
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_user_profile_btSendMessage:
                UsersManager.INSTANCE.startConversation(TAG, API_REQUEST_START_CONVERSATION, user);
                break;
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
        public void onConversationStarted(Integer requestCode, Stream stream) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, MessengerFragment.Companion.newInstance(stream))
                    .addToBackStack(null)
                    .commit();
            getFragmentManager().executePendingTransactions();
        }

        @Override
        public void onError(Integer requestCode, Error error) {
            super.onError(requestCode, error);
        }
    };
    //endregion
}
