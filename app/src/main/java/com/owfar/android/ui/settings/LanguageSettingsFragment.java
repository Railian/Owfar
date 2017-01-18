package com.owfar.android.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.owfar.android.R;
import com.owfar.android.ui.main.SettingsBaseFragment;

public class LanguageSettingsFragment extends SettingsBaseFragment {

    private static final String TITLE = "Language";

    public static LanguageSettingsFragment newInstance() {
        return new LanguageSettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        addPreferencesFromResource(R.xml.pref_language);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getAppBarHelper()
                .setTitle(TITLE)
                .showMaterialMenu()
                .setMaterialMenuState(MaterialMenuDrawable.IconState.ARROW)
                .hideSearch()
                .hideImage()
                .hideTabs();
        getNavigationMenuHelper().lockClosed();
        getFabHelper().hide();

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}



