package com.owfar.android.ui.contacts;

import android.view.ContextMenu;

import com.owfar.android.models.api.classes.User;

public class ContactContextMenuInfo implements ContextMenu.ContextMenuInfo {

    private int adapterPosition;
    private User contact;

    public ContactContextMenuInfo(int adapterPosition, User contact) {
        this.adapterPosition = adapterPosition;
        this.contact = contact;
    }

    public int getAdapterPosition() {
        return adapterPosition;
    }

    public User getContact() {
        return contact;
    }
}
