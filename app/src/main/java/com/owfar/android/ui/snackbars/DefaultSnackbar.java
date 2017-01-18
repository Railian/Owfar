package com.owfar.android.ui.snackbars;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import com.owfar.android.R;

public class DefaultSnackbar {

    public static Snackbar make(View view, CharSequence message) {
        return make(view, message, Snackbar.LENGTH_INDEFINITE);
    }

    public static Snackbar make(View view, CharSequence message, @Snackbar.Duration int duration) {
        Snackbar snackbar = Snackbar.make(view, message, duration);
        snackbar.setActionTextColor(view.getResources().getColor(R.color.colorPrimary));

        final View vSnackbar = snackbar.getView();
        TextView tvMessage = (TextView) vSnackbar.findViewById(android.support.design.R.id.snackbar_text);

        vSnackbar.setBackgroundColor(view.getResources().getColor(R.color.colorPrimaryDark));
        tvMessage.setTextColor(Color.WHITE);
        tvMessage.setMaxLines(10);

        snackbar.setActionTextColor(view.getResources().getColor(R.color.colorPrimary));
        return snackbar;
    }
}
