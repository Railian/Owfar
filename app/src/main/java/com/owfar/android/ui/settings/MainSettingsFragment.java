package com.owfar.android.ui.settings;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.owfar.android.R;
import com.owfar.android.settings.CurrentUserManager;
import com.owfar.android.ui.main.SettingsBaseFragment;
import com.owfar.android.ui.registration.RegistrationActivity;


public class MainSettingsFragment extends SettingsBaseFragment implements Preference.OnPreferenceClickListener {

    private static final String TITLE = "Settings";
    private String KEY_LANGUAGE;
    private String KEY_NOTIFICATION;
    private String KEY_COMMON;
    private String KEY_LOGOUT;

    public static MainSettingsFragment newInstance() {
        return new MainSettingsFragment();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        addPreferencesFromResource(R.xml.pref_main);

        KEY_LANGUAGE = getString(R.string.pref_main_kLanguage);
        KEY_NOTIFICATION = getString(R.string.pref_main_kNotification);
        KEY_COMMON = getString(R.string.pref_main_kCommon);
        KEY_LOGOUT = getString(R.string.pref_main_kLogout);

        getPreferenceManager().findPreference(KEY_LANGUAGE).setOnPreferenceClickListener(this);
        getPreferenceManager().findPreference(KEY_NOTIFICATION).setOnPreferenceClickListener(this);
        getPreferenceManager().findPreference(KEY_COMMON).setOnPreferenceClickListener(this);
        getPreferenceManager().findPreference(KEY_LOGOUT).setOnPreferenceClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getAppBarHelper()
                .setTitle(TITLE)
                .showMaterialMenu()
                .setMaterialMenuState(MaterialMenuDrawable.IconState.BURGER)
                .hideSearch()
                .hideImage()
                .hideTabs();
        getNavigationMenuHelper().unlock();
        getFabHelper().hide();

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewCompat.setNestedScrollingEnabled(view, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getNavigationMenuHelper().open();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        final String key = preference.getKey();
        if (key == null) return false;

        if (key.equals(KEY_LANGUAGE)) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, LanguageSettingsFragment.newInstance())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit();
            return true;
        }
        if (key.equals(KEY_NOTIFICATION)) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, NotificationSettingsFragment.newInstance())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit();
            return true;
        }
        if (key.equals(KEY_COMMON)) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, CommonSettingsFragment.newInstance())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit();
            return true;
        }
        if (key.equals(KEY_LOGOUT)) {
            CurrentUserManager.INSTANCE.logout();
            getActivity().finish();
            startActivity(new Intent(getActivity(), RegistrationActivity.class));
            return true;
        }
        return false;
    }
}