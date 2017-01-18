package com.owfar.android.utils;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.owfar.android.models.api.classes.Profile;
import com.owfar.android.models.api.classes.User;

import java.util.ArrayList;

import io.realm.RealmList;

public class ContactsUtils {

    private int permissionRequestCode;
    private ContentResolver contentResolver;
    private Callback callback;

    public void generateUsersFromContacts(Fragment fragment, int permissionRequestCode, @NonNull Callback callback) {
        // Check the SDK version and whether the permission is already granted or not.
        contentResolver = fragment.getActivity().getContentResolver();
        this.callback = callback;
        this.permissionRequestCode = permissionRequestCode;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && fragment.getActivity().checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
            fragment.requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, permissionRequestCode);
        else generateUsersFromContacts();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == permissionRequestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                // Permission is granted
                generateUsersFromContacts();
            else
                callback.onNeedPermission("Until you grant the permission, we canot display the names");
        }
    }

    private void generateUsersFromContacts() {
        ArrayList<User> users = new ArrayList<>();
        User user;
        Profile profile;

        long contactId;
        String fullName;
        String phone;
        String phoneLast7;

        Cursor phonesCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        if (phonesCursor != null)
            try {
                int iContactId = phonesCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
                int iDisplayName = phonesCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int iNumber = phonesCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                while (phonesCursor.moveToNext()) {
                    contactId = phonesCursor.getLong(iContactId);
                    fullName = phonesCursor.getString(iDisplayName);
                    phone = phonesCursor.getString(iNumber);
                    phoneLast7 = phone.replaceAll("[^0-9]", "");
                    if (phoneLast7.length() > 7)
                        phoneLast7 = phoneLast7.substring(phoneLast7.length() - 7);
                    user = new User();
                    user.setHasOwfar(false);
                    user.setId(contactId);
                    profile = new Profile();
                    profile.setFullName(fullName);
                    user.setProfile(profile);
                    user.setPhone(phone);
                    user.setPhoneLast7(phoneLast7);
                    user.setFullNameFromContacts(fullName);
                    user.setPhotoFromContacts(getPhotoUri(contactId));
                    users.add(user);
                }
            } finally {
                phonesCursor.close();
            }

        RealmList<User> uniqueUsers = new RealmList<>();
        boolean hasUser;
        for (User someUser : users) {
            hasUser = false;
            for (User uniqueUser : uniqueUsers)
                if (uniqueUser.getPhoneLast7().equals(someUser.getPhoneLast7())) {
                    hasUser = true;
                    break;
                }
            if (!hasUser) uniqueUsers.add(someUser);
        }
        callback.onGeneratedUsersFromContacts(uniqueUsers);
    }

    public Uri getPhotoUri(long contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        return Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
    }

    public interface Callback {

        void onGeneratedUsersFromContacts(RealmList<User> users);

        void onNeedPermission(String message);
    }
}
