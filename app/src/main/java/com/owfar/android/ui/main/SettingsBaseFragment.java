package com.owfar.android.ui.main;

import android.preference.PreferenceFragment;

public abstract class SettingsBaseFragment extends PreferenceFragment {

    //region Tools
    protected MainNavigationMenuHelper getNavigationMenuHelper() {
        return getActivity() == null ? null : ((MainActivity) getActivity()).navigationMenuHelper;
    }

    protected MainAppBarHelper getAppBarHelper() {
        return getActivity() == null ? null : ((MainActivity) getActivity()).appBarHelper;
    }

    protected MainFabHelper getFabHelper() {
        return getActivity() == null ? null : ((MainActivity) getActivity()).fabHelper;
    }
    //endregion
}