package com.owfar.android.ui.broadcasts;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.buildware.widget.indeterm.IndeterminateCheckBox;
import com.owfar.android.R;
import com.owfar.android.models.api.classes.InterestsGroup;
import com.owfar.android.models.api.enums.MediaSize;
import com.owfar.android.models.api.enums.SubscribedState;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class BroadcastGroupHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    //region widgets
    private View vSubscribed;
    private IndeterminateCheckBox icbSubscribed;
    private ImageView ivIcon;
    private TextView tvName;
    private ImageView ivArrow;
    //endregion

    //region Constructors
    public BroadcastGroupHolder(View itemView) {
        super(itemView);
        vSubscribed = itemView.findViewById(R.id.item_broadcast_group_vSubscribed);
        icbSubscribed = (IndeterminateCheckBox) itemView.findViewById(R.id.item_broadcast_group_icbSubscribed);
        ivIcon = (ImageView) itemView.findViewById(R.id.item_broadcast_group_ivIcon);
        tvName = (TextView) itemView.findViewById(R.id.item_broadcast_group_tvName);
        ivArrow = (ImageView) itemView.findViewById(R.id.item_broadcast_group_ivArrow);
        vSubscribed.setOnClickListener(this);
    }
    //endregion

    //region View Configuration
    public void configureWith(InterestsGroup interestsGroup, boolean isExpanded) {
        configureIcbSubscribed(interestsGroup.getSubscribedState());
        configureIvIcon(interestsGroup);
        tvName.setText(interestsGroup.getName());
        ivArrow.setRotation(isExpanded ? 90 : 0);
    }

    public void configureIcbSubscribed(SubscribedState state) {
        switch (state) {
            case SUBSCRIBED_ALL:
                icbSubscribed.setChecked(true);
                break;
            case SUBSCRIBED_NOTHING:
                icbSubscribed.setChecked(false);
                break;
            case INDETERMINATE:
                icbSubscribed.setChecked(false);
                icbSubscribed.setIndeterminate(true);
                break;
        }
    }

    private void configureIvIcon(InterestsGroup interestsGroup) {
        Picasso.with(itemView.getContext()).cancelRequest(ivIcon);
        ivIcon.setVisibility(View.GONE);
        if (interestsGroup.getPhoto() != null)
            Picasso.with(itemView.getContext())
                    .load(interestsGroup.getPhoto().getPath(MediaSize._1X))
                    .into(ivIcon, onLoadIconCallback);
    }

    private Callback onLoadIconCallback = new Callback() {
        @Override
        public void onSuccess() {
            ivIcon.setVisibility(View.VISIBLE);
        }

        @Override
        public void onError() {

        }
    };
    //endregion

    //region Public Tools
    public void collapse() {
        ivArrow.animate().rotation(0).setDuration(300).start();
    }

    public void expand() {
        ivArrow.animate().rotation(90).setDuration(300).start();
    }
    //endregion

    //region UI Listeners Implementation
    @Override
    public void onClick(View v) {
        if (v == vSubscribed) icbSubscribed.performClick();
        else if (listener != null)
            if (v == itemView)
                listener.onInterestsGroupClick(itemView, getAdapterPosition());
            else if (v == icbSubscribed)
                listener.onInterestsGroupCheckedChanged(getAdapterPosition(), icbSubscribed.isChecked());
    }
    //endregion

    //region InterestsGroupListener
    private InterestsGroupListener listener;

    public void setInterestsGroupListener(InterestsGroupListener listener) {
        this.listener = listener;
        itemView.setOnClickListener(listener != null ? this : null);
        icbSubscribed.setOnClickListener(listener != null ? this : null);
    }

    public interface InterestsGroupListener {
        void onInterestsGroupClick(View groupView, int adapterPosition);

        void onInterestsGroupCheckedChanged(int adapterPosition, boolean isChecked);
    }

    public static interface InterestListener {
    }
    //endregion
}
