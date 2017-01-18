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
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import com.owfar.android.ui.main.MainActivity;

public class NeedReadWriteForCachingDialog extends DialogFragment implements DialogInterface.OnClickListener {

    private static final String ARG_REQUEST_CODE = "ARG_REQUEST_CODE";

    private int requestCode;

    public static NeedReadWriteForCachingDialog newInstance(int requestCode) {
        NeedReadWriteForCachingDialog fragment = new NeedReadWriteForCachingDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_REQUEST_CODE, requestCode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestCode = getArguments().getInt(ARG_REQUEST_CODE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle("Need permission")
                .setMessage("Read/Write permission is required for caching. Without this permission cache will be disabled.")
                .setNeutralButton("Try Again", this)
                .setPositiveButton("Continue", this)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        ((MainActivity) getActivity()).onDialogResult(requestCode, which);
    }
}
