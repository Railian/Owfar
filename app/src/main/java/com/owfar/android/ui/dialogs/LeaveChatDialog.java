/*
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.owfar.android.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

public class LeaveChatDialog extends DialogFragment implements DialogInterface.OnClickListener {

    public static LeaveChatDialog newInstance(Fragment targetFragment, int requestCode) {
        LeaveChatDialog fragment = new LeaveChatDialog();
        fragment.setTargetFragment(targetFragment, requestCode);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle("Leave Chat")
                .setMessage("This chat will be removed from your list")
                .setNegativeButton("Cancel", this)
                .setPositiveButton("Continue", this)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        Fragment targetFragment = getTargetFragment();
        targetFragment.onActivityResult(getTargetRequestCode(), which, null);
    }
}
