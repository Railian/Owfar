package com.owfar.android.ui.contacts;

import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.ContextMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.owfar.android.R;
import com.owfar.android.media.CircleTransform;
import com.owfar.android.media.MediaHelper;
import com.owfar.android.models.api.classes.User;
import com.owfar.android.models.api.enums.MediaSize;
import com.owfar.android.models.api.enums.MediaStorageType;
import com.owfar.android.utils.MetricsUtil;

public class ContactItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
        View.OnCreateContextMenuListener, CompoundButton.OnCheckedChangeListener {

    //region widgets
    private ImageView ivAvatar;
    private TextView tvName;
    private TextView tvPhone;
    private CheckBox cbCheck;
    private ImageView ivLabel;
    //endregion

    //region fields
    private boolean checkable;
    private boolean hasLabel;
    //endregion

    //region Constructors
    public ContactItemHolder(View itemView, boolean checkable, boolean hasLabel) {
        super(itemView);
        this.checkable = checkable;
        this.hasLabel = hasLabel;

        ivAvatar = (ImageView) itemView.findViewById(R.id.item_contact_ivAvatar);
        tvName = (TextView) itemView.findViewById(R.id.item_contact_tvName);
        tvPhone = (TextView) itemView.findViewById(R.id.item_contact_tvPhone);
        cbCheck = (CheckBox) itemView.findViewById(R.id.item_contact_cbCheck);
        ivLabel = (ImageView) itemView.findViewById(R.id.item_contact_ivLabel);

        cbCheck.setVisibility(checkable ? View.VISIBLE : View.GONE);
        ivLabel.setVisibility(hasLabel ? View.VISIBLE : View.GONE);

        if (checkable) {
            itemView.setOnClickListener(this);
            cbCheck.setOnCheckedChangeListener(this);
        }
    }
    //endregion

    //region View Configuration
    public void configureWith(User user, String searchText, boolean checked) {
        MediaHelper.RequestCreator requestCreator;
        if (user != null && user.getProfile() != null && user.getProfile().getPhoto() != null && user.getProfile().getPhoto().getMediaFileId() >= 0)
            requestCreator = MediaHelper.INSTANCE.load(user.getProfile().getPhoto())
                    .withOptions(MediaStorageType.USERS_PHOTOS, MediaSize._1X);
        else if (user != null && user.getPhotoFromContacts() != null)
            requestCreator = MediaHelper.INSTANCE.load(user.getPhotoFromContacts());
        else requestCreator = MediaHelper.INSTANCE.load(R.drawable.temp_avatar);
        requestCreator
                .transform(new CircleTransform())
                .into(ivAvatar);

        configureTvName(user.getProfile().getFullName(), searchText);
        configureTvPhone(user.getPhone(), searchText);
        int _4dp = MetricsUtil.Companion.dp2px(itemView.getContext(), 2);

        if (checkable) {
            cbCheck.setOnCheckedChangeListener(null);
            cbCheck.setChecked(checked);
            cbCheck.setOnCheckedChangeListener(this);
        }
        if (hasLabel)
            if (user.getHasOwfar()) {
                ivLabel.setPadding(0, 0, 0, 0);
                ivLabel.setImageResource(R.drawable.ic_contacts_owfar);
            } else {
                ivLabel.setPadding(_4dp, _4dp, _4dp, _4dp);
                ivLabel.setImageResource(R.drawable.ic_contacts_add);
            }
    }

    public void configureWith(User user, String searchText) {
        configureWith(user, searchText, false);
    }

    public void configureAsFooter() {
        itemView.setVisibility(View.INVISIBLE);
        ViewGroup.LayoutParams params = itemView.getLayoutParams();
        params.height = (int) itemView.getResources().getDimension(R.dimen.contact_footer_height);
        itemView.setLayoutParams(params);
    }

    private void configureTvName(String name, String searchText) {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(searchText))
            tvName.setText(name);
        else {
            int textColor;
            int backgroundColor;
            String fullString;
            int from;
            int to;

            textColor = itemView.getResources().getColor(android.R.color.white);
            backgroundColor = itemView.getResources().getColor(R.color.colorPrimary);
            fullString = name;

            SpannableString spannableString = new SpannableString(fullString);
            for (String key : searchText.split(" ")) {
                if (TextUtils.isEmpty(key)) continue;
                from = fullString.toLowerCase().indexOf(key.toLowerCase());
                to = from + key.length();
                if (from != -1) {
                    spannableString.setSpan(new ForegroundColorSpan(textColor), from, to, 0);
                    spannableString.setSpan(new BackgroundColorSpan(backgroundColor), from, to, 0);
                }
            }
            tvName.setText(spannableString);
        }
    }

    private void configureTvPhone(String phone, String searchText) {
        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(searchText))
            tvPhone.setText(phone);
        else {
            int textColor;
            int backgroundColor;
            String fullString;
            int from;
            int to;

            textColor = itemView.getResources().getColor(android.R.color.white);
            backgroundColor = itemView.getResources().getColor(R.color.colorPrimary);
            fullString = phone;

            SpannableString spannableString = new SpannableString(fullString);
            for (String key : searchText.split(" ")) {
                if (TextUtils.isEmpty(key)) continue;
                from = fullString.toLowerCase().indexOf(key.toLowerCase());
                to = from + key.length();
                if (from != -1) {
                    spannableString.setSpan(new ForegroundColorSpan(textColor), from, to, 0);
                    spannableString.setSpan(new BackgroundColorSpan(backgroundColor), from, to, 0);
                }
            }
            tvPhone.setText(spannableString);
        }
    }
    //endregion

    //region UI Listeners Implementation
    @Override
    public void onClick(View v) {
        if (checkable) cbCheck.setChecked(!cbCheck.isChecked());
        else if (onContactClickListener != null)
            onContactClickListener.onContactClick(getAdapterPosition(), null);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (onContactCheckedChangeListener != null)
            onContactCheckedChangeListener.onContactCheckedChange(isChecked, getAdapterPosition());
    }
    //endregion

    //region OnContactClickListener
    private OnContactClickListener onContactClickListener;

    public void setOnContactClickListener(OnContactClickListener listener) {
        onContactClickListener = listener;
        itemView.setOnClickListener(listener != null ? this : null);
    }

    public interface OnContactClickListener {
        void onContactClick(int adapterPosition, User user);
    }
    //endregion

    //region OnContactCheckedChangeListener
    private OnContactCheckedChangeListener onContactCheckedChangeListener;

    public void setOnContactCheckedChangeListener(OnContactCheckedChangeListener listener) {
        onContactCheckedChangeListener = listener;
    }

    public interface OnContactCheckedChangeListener {
        void onContactCheckedChange(boolean isChecked, int adapterPosition);
    }
    //endregion

    //region ContactContextMenuListener
    private ContactContextMenuListener contactContextMenuListener;

    public void setContactContextMenuListener(ContactContextMenuListener listener) {
        contactContextMenuListener = listener;
        itemView.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        contactContextMenuListener.onPrepareContactContextMenu(getAdapterPosition(), null);
        contactContextMenuListener.onCreateContextMenu(menu, v, menuInfo);
    }

    public interface ContactContextMenuListener extends View.OnCreateContextMenuListener {
        void onPrepareContactContextMenu(int adapterPosition, User contact);
    }
    //endregion
}