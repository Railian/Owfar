package com.owfar.android.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionHelper {

    //region fields
    private Activity activity;
    private Fragment fragment;
    private PermissionCallback callback;
    //endregion

    //region Constructors
    public PermissionHelper(@NonNull Activity activity) {
        this.activity = activity;
    }

    public PermissionHelper(@NonNull Fragment fragment) {
        this.fragment = fragment;
    }
    //endregion

    //region Public Tools
    public void verifyPermission(int requestCode, String[] permissions, PermissionCallback callback) {
        List<String> deniedPermissions = new ArrayList<>();
        for (String permission : permissions)
            if (ContextCompat.checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_DENIED)
                deniedPermissions.add(permission);
        if (deniedPermissions.size() == 0) {
            if (callback != null) callback.grantedAllPermissions(requestCode, permissions);
        } else {
            this.callback = callback;
            String[] denied = new String[deniedPermissions.size()];
            deniedPermissions.toArray(denied);
            if (activity == null) fragment.requestPermissions(denied, requestCode);
            else ActivityCompat.requestPermissions(activity, denied, requestCode);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        List<String> deniedPermissions = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++)
            if (grantResults[i] == PackageManager.PERMISSION_DENIED)
                deniedPermissions.add(permissions[i]);
        if (callback != null)
            if (deniedPermissions.size() == 0)
                callback.grantedAllPermissions(requestCode, permissions);
            else {
                String[] denied = new String[deniedPermissions.size()];
                deniedPermissions.toArray(denied);
                callback.deniedPermissions(requestCode, denied);
            }
        this.callback = null;
    }
    //endregion

    //region Private Tools
    private Context getContext() {
        return activity != null ? activity : fragment.getActivity();
    }
    //endregion

    //region Interface PermissionCallback
    public interface PermissionCallback {
        void grantedAllPermissions(int requestCode, String[] permissions);

        void deniedPermissions(int requestCode, String[] permissions);
    }
//endregion
}
