package com.owfar.android.ui.main;

import android.support.design.widget.NavigationView;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.owfar.android.R;
import com.owfar.android.media.MediaHelper;
import com.owfar.android.models.api.classes.Profile;
import com.owfar.android.models.api.classes.User;
import com.owfar.android.models.api.enums.MediaSize;
import com.owfar.android.models.api.enums.MediaStorageType;
import com.owfar.android.settings.CurrentUserSettings;

public class MainNavigationMenuHelper implements DrawerLayout.DrawerListener,
        NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    //region widgets
    private DrawerLayout drawer;
    private View navigationContainer;
    private CircularImageView civImage;
    private TextView tvFullName;
    private ImageView ivEditProfile;
    private NavigationView navigationView;
    //endregion

    //region fields
    private MainActivity activity;
    //endregion

    //region Constructors
    public MainNavigationMenuHelper(MainActivity activity) {
        this.activity = activity;

        drawer = (DrawerLayout) activity.findViewById(R.id.activity_main_drawer);
        navigationContainer = activity.findViewById(R.id.activity_main_navigationContainer);
        navigationView = (NavigationView) activity.findViewById(R.id.activity_main_navigationView);
        View headerView = navigationView.getHeaderView(0);
        civImage = (CircularImageView) headerView.findViewById(R.id.nav_header_main_civImage);
        tvFullName = (TextView) headerView.findViewById(R.id.nav_header_main_tvFullName);
        ivEditProfile = (ImageView) headerView.findViewById(R.id.nav_header_main_ivEditProfile);

        drawer.setScrimColor(activity.getResources().getColor(R.color.colorPrimaryDark));
        drawer.addDrawerListener(this);
        navigationView.setNavigationItemSelectedListener(this);
        ivEditProfile.setOnClickListener(this);
        User currentUser = CurrentUserSettings.INSTANCE.getCurrentUser();
        configureWith(currentUser);
    }
    //endregion

    //region View Configuration
    private void configureWith(User user) {
        if (user != null) {
            tvFullName.setText(user.getDisplayName());
            Profile profile = user.getProfile();
            if (profile != null && profile.getPhoto() != null && profile.getPhoto().getMediaFileId() >= 0)
                MediaHelper.INSTANCE
                        .load(profile.getPhoto())
                        .withOptions(MediaStorageType.USERS_PHOTOS, MediaSize._2X)
                        .placeholder(R.drawable.temp_avatar)
                        .into(civImage);
        }
    }
    //endregion

    //region Public Tools
    public void setChatsBadge(String badge) {
        MenuItem item = navigationView.getMenu().findItem(R.id.nav_chats);
        TextView tvBadge = (TextView) MenuItemCompat.getActionView(item).findViewById(R.id.nav_drawer_counter_tvBadge);
        tvBadge.setText(badge);
        tvBadge.setVisibility(badge == null ? View.GONE : View.VISIBLE);
    }

    public MainNavigationMenuHelper open() {
        drawer.openDrawer(navigationContainer);
        return this;
    }

    public MainNavigationMenuHelper close() {
        drawer.closeDrawer(navigationContainer);
        return this;
    }

    public boolean isOpened() {
        return drawer.isDrawerOpen(navigationContainer);
    }

    public void lockClosed() {
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, navigationContainer);
    }

    public void unlock() {
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, navigationContainer);
    }

    public void performItemAction(int id) {
        navigationView.setCheckedItem(id);
        navigationView.getMenu().performIdentifierAction(id, 0);
    }

    public void updateUserInfo() {
        User currentUser = CurrentUserSettings.INSTANCE.getCurrentUser();
        configureWith(currentUser);
    }
    //endregion

    //region DrawerListener Implementation
    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
    }

    @Override
    public void onDrawerOpened(View drawerView) {
        if (drawerView == navigationContainer && listener != null)
            listener.onNavigationMenuOpened();
    }

    @Override
    public void onDrawerClosed(View drawerView) {
        if (drawerView == navigationContainer && listener != null)
            listener.onNavigationMenuClosed();
    }

    @Override
    public void onDrawerStateChanged(int newState) {
    }
    //endregion

    //region OnNavigationItemSelectedListener Implementation
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return listener != null && listener.onNavigationItemSelected(item);
    }
    //endregion

    //region UI Listeners Implementation
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nav_header_main_ivEditProfile:
                performItemAction(R.id.nav_profile);
                break;
        }
    }
    //endregion

    //region NavigationMenuListener
    private NavigationMenuListener listener;

    public void setNavigationMenuListener(NavigationMenuListener listener) {
        this.listener = listener;
    }

    public interface NavigationMenuListener {

        void onNavigationMenuOpened();

        void onNavigationMenuClosed();

        boolean onNavigationItemSelected(MenuItem item);
    }
    //endregion
}