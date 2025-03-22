package com.example.RQWL001;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * 作用：解决Android 6.0以上系统的权限问题
 */

public class PermissionUtils {

    private static final String[] PERMISSIONS_CAMERA_AND_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};

    public static boolean isGrantExternalRW(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int storagePermission = activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (storagePermission != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(PERMISSIONS_CAMERA_AND_STORAGE, requestCode);
                return false;
            }
            int iloop = 1;
            while (iloop < 10000) {
                storagePermission = activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (storagePermission == PackageManager.PERMISSION_GRANTED) {
                    return true;
                }
                iloop++;
            }
        }
        return true;

    }


}

