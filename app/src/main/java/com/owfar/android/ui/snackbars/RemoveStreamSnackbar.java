package com.owfar.android.ui.snackbars;

import android.support.design.widget.Snackbar;
import android.view.View;

import com.owfar.android.R;
import com.owfar.android.models.api.classes.Stream;


public class RemoveStreamSnackbar {

    private static final String UNDO = "Undo";

    public static Snackbar make(View view, Stream stream, View.OnClickListener onUndoClickListener) {
        CharSequence text = stream.getRemovingText(view.getResources().getColor(R.color.colorPrimary));
        Snackbar snackbar = DefaultSnackbar.make(view, text);
        snackbar.setAction(UNDO.toUpperCase(), onUndoClickListener);
        return snackbar;
    }
}
