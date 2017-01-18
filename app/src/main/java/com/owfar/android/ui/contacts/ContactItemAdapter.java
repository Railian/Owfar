package com.owfar.android.ui.contacts;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.owfar.android.R;
import com.owfar.android.models.api.classes.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.realm.RealmList;


public class ContactItemAdapter extends RecyclerView.Adapter<ContactItemHolder>
        implements ContactItemHolder.OnContactClickListener,
        ContactItemHolder.ContactContextMenuListener, ContactItemHolder.OnContactCheckedChangeListener {

    private enum ViewType {
        ITEM,
        FOOTER;

        public static ViewType getViewType(int itemViewType) {
            return ViewType.values()[itemViewType];
        }

        public int toItemViewType() {
            return ordinal();
        }
    }

    //region fields
    private boolean checkable;
    private boolean hasLabel;
    private HashSet<User> checkedContacts;
    private RealmList<User> allContacts;
    private RealmList<User> displayingContacts;
    private String searchText;
    //endregion

    //region Constructors
    public ContactItemAdapter(boolean checkable, boolean hasLabel) {
        this.checkable = checkable;
        this.hasLabel = hasLabel;
        allContacts = new RealmList<>();
        displayingContacts = new RealmList<>();
        for (User user : allContacts) displayingContacts.add(user);
    }
    //endregion

    //region DataSource Methods
    public RealmList<User> getCheckedContacts() {
        RealmList<User> list = new RealmList<>();
        for (User user : checkedContacts) list.add(user);
        return list;
    }

    private void addCheckedContact(User user) {
        if (checkedContacts == null) checkedContacts = new HashSet<>();
        checkedContacts.add(user);
    }

    private void removeCheckedContact(User user) {
        if (checkedContacts == null) return;
        checkedContacts.remove(user);
    }

    public RealmList<User> getAllContacts() {
        return allContacts;
    }

    public void setAllContacts(RealmList<User> allContacts) {
        if (this.allContacts != null) this.allContacts.clear();
        else this.allContacts = new RealmList<>();
        this.allContacts.addAll(allContacts);
        setSearchText(searchText);
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
        if (TextUtils.isEmpty(searchText)) {
            displayingContacts.clear();
            displayingContacts.addAll(allContacts);
        } else if (allContacts != null) {
            String[] searchTexts = searchText.split(" ");
            ArrayList searchContacts = new ArrayList();
            for (User contact : allContacts) {
                boolean ok = true;
                for (String text : searchTexts) {
                    if (TextUtils.isEmpty(text)) continue;
                    if (!contact.getProfile().getFullName().toLowerCase().contains(text.toLowerCase()) &&
                            !contact.getPhone().toLowerCase().contains(text.toLowerCase())) {
                        ok = false;
                        break;
                    }
                }
                if (ok) searchContacts.add(contact);
            }
            displayingContacts.clear();
            displayingContacts.addAll(searchContacts);
        }
        notifyDataSetChanged();
    }

    public void add(List<User> users) {
        int startPosition = allContacts != null ? allContacts.size() : 0;
        int itemCount = users != null ? users.size() : 0;
        allContacts.addAll(users);
        displayingContacts.addAll(users);
        notifyItemRangeInserted(startPosition, itemCount);
    }

//    public void remove(User contact) {
//        allContacts.remove(contact);
//        setSearchText(searchText);
//    }

    public void remove(User contact) {
        int position = allContacts.indexOf(contact);
        if (position >= 0) {
            allContacts.remove(position);
            displayingContacts.remove(position);
            notifyItemRemoved(position);
        }
    }

    private User getDisplayingContact(int adapterPosition) {
        return displayingContacts.get(adapterPosition);
    }

    public int getDisplayingContactsCount() {
        return displayingContacts == null ? 0 : displayingContacts.size();
    }
    //endregion

    //region Adapter Methods
    @Override
    public int getItemViewType(int position) {
        return position >= displayingContacts.size() ?
                ViewType.FOOTER.toItemViewType() : ViewType.ITEM.toItemViewType();
    }

    @Override
    public ContactItemHolder onCreateViewHolder(ViewGroup parent, int itemViewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_contact, parent, false);
        ContactItemHolder holder = new ContactItemHolder(itemView, checkable, hasLabel);
        switch (ViewType.getViewType(itemViewType)) {
            case ITEM:
                holder.setOnContactClickListener(this);
                holder.setOnContactCheckedChangeListener(this);
                holder.setContactContextMenuListener(this);
                break;
            case FOOTER:
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(ContactItemHolder holder, int position) {
        switch (ViewType.getViewType(getItemViewType(position))) {
            case ITEM:
                User user = getDisplayingContact(position);
                boolean isChecked = checkedContacts != null && checkedContacts.contains(user);
                holder.configureWith(getDisplayingContact(position), searchText, isChecked);
                break;
            case FOOTER:
                holder.configureAsFooter();
                break;
        }
    }

    @Override
    public int getItemCount() {
        return getDisplayingContactsCount() + 1/*footer*/;
    }
    //endregion

    //region OnContactClickListener
    private ContactItemHolder.OnContactClickListener onContactClickListener;

    public void setOnContactClickListener(ContactItemHolder.OnContactClickListener listener) {
        onContactClickListener = listener;
    }

    @Override
    public void onContactClick(int adapterPosition, User user) {
        if (onContactClickListener != null)
            onContactClickListener.onContactClick(adapterPosition, getDisplayingContact(adapterPosition));
    }
    //endregion

    //region ContactContextMenuListener
    private ContactItemHolder.ContactContextMenuListener contactContextMenuListener;

    public void setContactContextMenuListener(ContactItemHolder.ContactContextMenuListener listener) {
        contactContextMenuListener = listener;
    }

    @Override
    public void onPrepareContactContextMenu(int adapterPosition, User contact) {
        if (contactContextMenuListener != null)
            contactContextMenuListener.onPrepareContactContextMenu(adapterPosition,
                    getDisplayingContact(adapterPosition));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (contactContextMenuListener != null)
            contactContextMenuListener.onCreateContextMenu(menu, v, menuInfo);
    }
    //endregion

    @Override
    public void onContactCheckedChange(boolean isChecked, int adapterPosition) {
        User user = getDisplayingContact(adapterPosition);
        if (isChecked) addCheckedContact(user);
        else removeCheckedContact(user);
        if (onCheckedContactsChangeListener != null)
            onCheckedContactsChangeListener.onCheckedContactsChange(checkedContacts);
    }

    //region OnCheckedContactsChangeListener
    private OnCheckedContactsChangeListener onCheckedContactsChangeListener;

    public void setOnCheckedContactsChangeListener(OnCheckedContactsChangeListener listener) {
        onCheckedContactsChangeListener = listener;
    }

    public interface OnCheckedContactsChangeListener {
        void onCheckedContactsChange(Set<User> checkedContacts);
    }
//endregion
}