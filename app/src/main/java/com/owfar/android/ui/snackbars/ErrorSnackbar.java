package com.owfar.android.ui.snackbars;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import com.owfar.android.R;
import com.owfar.android.models.errors.Error;


public class ErrorSnackbar {

    public static Snackbar make(View view, Error error) {
        String message = String.format("Error %d: %s\n%s",
                error.getCode(), error.getError(), error.getErrorDescription());
        Snackbar snackbar = Snackbar
                .make(view, message, Snackbar.LENGTH_INDEFINITE)
                .setAction("HIDE", new SimpleOnClickListener());
        snackbar.setActionTextColor(view.getResources().getColor(R.color.colorPrimary));

        View vSnackbar = snackbar.getView();
        TextView tvMessage = (TextView) vSnackbar.findViewById(android.support.design.R.id.snackbar_text);

        vSnackbar.setBackgroundColor(view.getResources().getColor(R.color.colorPrimaryDark));
        tvMessage.setTextColor(Color.WHITE);
        tvMessage.setMaxLines(10);
        return snackbar;
    }


    private static class SimpleOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

        }
    }
}
