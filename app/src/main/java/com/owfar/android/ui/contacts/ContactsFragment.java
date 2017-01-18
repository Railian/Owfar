package com.owfar.android.ui.contacts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.owfar.android.R;
import com.owfar.android.api.users.UsersDelegate;
import com.owfar.android.api.users.UsersManager;
import com.owfar.android.models.api.classes.User;
import com.owfar.android.ui.main.MainBaseFragment;
import com.owfar.android.ui.main.MainFabHelper;
import com.owfar.android.ui.new_contact.NewContactFragment;
import com.owfar.android.ui.profile.UserProfileFragment;
import com.owfar.android.utils.ContactsUtils;
import com.owfar.android.utils.IntentUtils;

import java.util.Collections;

import io.realm.RealmList;

public class ContactsFragment extends MainBaseFragment
        implements MaterialSearchView.OnQueryTextListener, MaterialSearchView.SearchViewListener,
        ContactItemHolder.OnContactClickListener, ContactItemHolder.ContactContextMenuListener, MainFabHelper.OnFabClickListener {

    //region constants
    private static final String TAG = ContactsFragment.class.getSimpleName();
    private static final String TITLE = "Contacts";
    private static final int REQUEST_NEW_CONTACT = 1;
    private static final int API_REQUEST_GET_CONTACTS_LIST = 1;
    private static final int API_REQUEST_ADD_NEW_CONTACT = 2;
    private static final int API_REQUEST_CONVERT_MOBILE_CONTACTS_TO_OWFAR = 3;
    private static final int API_REQUEST_DELETE_CONTACT = 4;
    //endregion

    //region widgets
    private ViewPager viewPager;
    //endregion

    //region fields
    private ContactsPageAdapter contactsPageAdapter;
    private ContactsUtils contactsUtils = new ContactsUtils();
    //endregion

    //region Creating New Instances
    public static ContactsFragment newInstance() {
        return new ContactsFragment();
    }
    //endregion

    //region Fragment Life-Cycle Methods
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        UsersManager.INSTANCE.getDelegatesSet().addDelegate(TAG, localUsersDelegate);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getAppBarHelper()
                .setTitle(TITLE)
                .showMaterialMenu()
                .setMaterialMenuState(MaterialMenuDrawable.IconState.BURGER)
                .setSearchViewListener(this)
                .setOnSearchTextListener(this)
                .hideSearch()
                .hideImage()
                .showTabs();
        getNavigationMenuHelper().unlock();
        getFabHelper().show(MainFabHelper.FabIcon.ADD, this);

        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager = (ViewPager) view.findViewById(R.id.container);

        ViewCompat.setNestedScrollingEnabled(view, false);

        contactsPageAdapter = new ContactsPageAdapter(getChildFragmentManager());
        viewPager.setAdapter(contactsPageAdapter);

        ContactsPageDataSource.setOnContactClickListener(this);
        getAppBarHelper().setupTabsWithViewPager(viewPager);

        ContactsPageDataSource.OWFAR.getItemAdapter().setContactContextMenuListener(this);
        UsersManager.INSTANCE.getContactList(TAG, API_REQUEST_GET_CONTACTS_LIST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_NEW_CONTACT && resultCode == Activity.RESULT_OK) {
            // TODO: 27.04.16 update contacts list
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        contactsUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onDestroyView() {
        getAppBarHelper()
                .setSearchViewListener(null)
                .setOnSearchTextListener(null);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        UsersManager.INSTANCE.getDelegatesSet().removeDelegate(localUsersDelegate);
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.contacts, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                getNavigationMenuHelper().open();
                return true;

            case R.id.action_search:
                getAppBarHelper().showSearch().setMaterialMenuState(MaterialMenuDrawable.IconState.ARROW);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private ContactContextMenuInfo contactContextMenuInfo;

    @Override
    public void onPrepareContactContextMenu(int adapterPosition, User contact) {
        contactContextMenuInfo = new ContactContextMenuInfo(adapterPosition, contact);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        Log.d(TAG, "onCreateContextMenu() called with: " + "menu = [" + menu + "], v = [" + v + "], menuInfo = [" + menuInfo + "]");
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = new MenuInflater(getActivity());
        menuInflater.inflate(R.menu.context_contacts, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_from_contacts:
                User user = contactContextMenuInfo.getContact();
                UsersManager.INSTANCE.deleteContact(TAG, API_REQUEST_DELETE_CONTACT, user);
                contactContextMenuInfo = null;
                return true;
        }
        return super.onContextItemSelected(item);
    }

    //endregion

    //region MaterialSearchView.OnQueryTextListener implementation
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        ContactsPageDataSource.setSearchText(newText);
        return true;
    }
    //endregion

    //region MaterialSearchView.SearchViewListener implementation
    @Override
    public void onSearchViewShown() {
        getNavigationMenuHelper().lockClosed();
        getFabHelper().hide();
    }

    @Override
    public void onSearchViewClosed() {
        getAppBarHelper().setMaterialMenuState(MaterialMenuDrawable.IconState.BURGER);
        getNavigationMenuHelper().unlock();
        getFabHelper().show(MainFabHelper.FabIcon.ADD, this);
    }
    //endregion

    //region OnContactClickListener Implementation
    @Override
    public void onContactClick(int adapterPosition, User user) {
        if (user.getHasOwfar()) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, UserProfileFragment.newInstance(user))
                    .addToBackStack(null)
                    .commit();
            getFragmentManager().executePendingTransactions();
            ContactsPageDataSource.setSearchText(null);
        } else IntentUtils.Companion.actionSendSMS(getActivity(), user.getPhone(), "Join Owfar!");
    }
    //endregion

    //region localUsersDelegate
    private UsersDelegate localUsersDelegate = new UsersDelegate.Simple() {
        @Override
        public void onContactListReceived(Integer requestCode, RealmList<User> users) {
            if (users != null) Collections.sort(users);
            ContactsPageDataSource.OWFAR.getItemAdapter().setAllContacts(users);

            contactsUtils.generateUsersFromContacts(ContactsFragment.this, 1, new ContactsUtils.Callback() {
                        @Override
                        public void onGeneratedUsersFromContacts(RealmList<User> users) {
                            RealmList<User> myContacts = ContactsPageDataSource.OWFAR.getItemAdapter().getAllContacts();
                            RealmList<User> usersToAdd = new RealmList<>();
                            if (myContacts != null && myContacts.size() != 0)
                                for (User myContact : myContacts) {
                                    boolean hasInMobileContact = false;
                                    for (User user : users)
                                        if (user.getPhoneLast7().equals(myContact.getPhoneLast7())) {
                                            user.setProfile(myContact.getProfile());
                                            user.setHasOwfar(true);
                                            hasInMobileContact = true;
                                            break;
                                        }
                                    if (!hasInMobileContact) usersToAdd.add(myContact);
                                }
                            users.addAll(usersToAdd);
                            Collections.sort(users);
                            ContactsPageDataSource.ALL.getItemAdapter().setAllContacts(users);
                            UsersManager.INSTANCE.convertMobileContactsToOwfar(TAG, API_REQUEST_CONVERT_MOBILE_CONTACTS_TO_OWFAR, users);
                        }

                        @Override
                        public void onNeedPermission(String message) {

                        }
                    }
            );
        }

        @Override
        public void onMobileContactsConvertedToOwfar(Integer requestCode, RealmList<User> owfarUsers) {
            RealmList<User> allContacts = ContactsPageDataSource.ALL.getItemAdapter().getAllContacts();
            if (allContacts != null && allContacts.size() != 0 &&
                    owfarUsers != null && owfarUsers.size() != 0) {
                for (User owfarUser : owfarUsers)
                    for (User contact : allContacts)
                        if (owfarUser.getPhoneLast7().equals(contact.getPhoneLast7())) {
                            contact.setProfile(owfarUser.getProfile());
                            contact.setHasOwfar(true);
                            break;
                        }
                ContactsPageDataSource.ALL.getItemAdapter().notifyDataSetChanged();
            }
        }

        @Override
        public void onNewContactAdded(Integer requestCode) {
            if (requestCode == API_REQUEST_ADD_NEW_CONTACT) {
                getFragmentManager()
                        .beginTransaction()
//                        .replace(R.id.fragment_container, MessengerFragment.newInstance())
                        .addToBackStack(null)
                        .commit();
                getFragmentManager().executePendingTransactions();
            }
        }

        @Override
        public void onContactDeleted(Integer requestCode, User contact) {
            if (requestCode == API_REQUEST_DELETE_CONTACT) {
                ContactsPageDataSource.OWFAR.getItemAdapter().remove(contact);
            }
        }
    };
    //endregion

    @Override
    public void onFabClick() {
        getFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.fragment_container, NewContactFragment.newInstance(this, REQUEST_NEW_CONTACT))
                .addToBackStack(null)
                .commit();
        getFragmentManager().executePendingTransactions();
    }
}