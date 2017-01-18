package com.owfar.android.ui.contacts;

import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.owfar.android.R;
import com.owfar.android.ui.main.MainBaseFragment;


public class ContactsPageFragment extends MainBaseFragment {

    //region constants
    private static final String ARG_CONTACTS_PAGE_TYPE = "ARG_CONTACTS_PAGE_TYPE";
    //endregion

    //region widgets
    private RecyclerView rvContacts;
    //endregion

    //region fields
    private ContactsPageDataSource contactsPageDataSource;
    //endregion

    //region Getting Instances
    private static ContactsPageFragment[] instances = new ContactsPageFragment[ContactsPageDataSource.values().length];

    public static ContactsPageFragment getInstance(ContactsPageDataSource contactsPageDataSource) {
        if (instances[contactsPageDataSource.ordinal()] == null) {
            ContactsPageFragment fragment = new ContactsPageFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_CONTACTS_PAGE_TYPE, contactsPageDataSource.ordinal());
            fragment.setArguments(args);
            instances[contactsPageDataSource.ordinal()] = fragment;
        }
        return instances[contactsPageDataSource.ordinal()];
    }
    //endregion

    //region Fragment Life-Cycle Methods
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contactsPageDataSource = ContactsPageDataSource.values()[getArguments().getInt(ARG_CONTACTS_PAGE_TYPE)];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contacts_page, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvContacts = (RecyclerView) view.findViewById(R.id.fragment_contacts_page_rvContacts);

        ViewCompat.setNestedScrollingEnabled(view, false);
        ViewCompat.setNestedScrollingEnabled(rvContacts, false);

        rvContacts.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvContacts.setAdapter(contactsPageDataSource.getItemAdapter());
    }
    //endregion
}