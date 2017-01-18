package com.owfar.android.ui.main;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

import com.owfar.android.R;


public class MainFabHelper implements View.OnClickListener {

    //region widgets
    private FloatingActionButton fab;
    //endregion

    //region Constructors
    public MainFabHelper(MainActivity activity) {
        fab = (FloatingActionButton) activity.findViewById(R.id.fab);
        fab.setOnClickListener(this);
    }
    //endregion

    public enum FabIcon {

        ADD(R.drawable.ic_fab_add),
        CHECK(R.drawable.ic_fab_check);

        private int imageResource;

        FabIcon(@DrawableRes int imageResource) {
            this.imageResource = imageResource;
        }

        public int getImageResource() {
            return imageResource;
        }
    }

    private OnFabClickListener listener;

    public interface OnFabClickListener {
        void onFabClick();
    }

    @Override
    public void onClick(View v) {
        if (listener != null) listener.onFabClick();
    }

    //region Public Tools
    public MainFabHelper show(@NonNull FabIcon fabIcon, @NonNull OnFabClickListener listener) {
        fab.setImageResource(fabIcon.getImageResource());
        this.listener = listener;
        fab.show();
        return this;
    }

    public MainFabHelper hide() {
        listener = null;
        fab.hide();
        return this;
    }
    //endregion
}