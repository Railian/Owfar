package com.owfar.android.ui.contacts;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ContactsPageAdapter extends FragmentPagerAdapter {

    //region Constructors
    public ContactsPageAdapter(FragmentManager fm) {
        super(fm);
    }
    //endregion

    //region DataSource Methods
    private ContactsPageDataSource getContactsPageDataSource(int adapterPosition) {
        return ContactsPageDataSource.values()[adapterPosition];
    }

    public int getContactsPageCount() {
        return ContactsPageDataSource.values().length;
    }
    //endregion

    //region Adapter Methods
    @Override
    public Fragment getItem(int position) {
        return ContactsPageFragment.getInstance(getContactsPageDataSource(position));
    }

    @Override
    public int getCount() {
        return getContactsPageCount();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return getContactsPageDataSource(position).getTitle();
    }
    //endregion
}