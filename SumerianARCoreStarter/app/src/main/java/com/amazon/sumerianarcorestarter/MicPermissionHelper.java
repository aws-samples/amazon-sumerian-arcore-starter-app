/*
 * This code has been modified. Portions copyright 2018 Amazon.com, Inc. or its affiliates. Please see LICENSE.txt for applicable license terms and NOTICE.txt for applicable notices.
 */

package com.amazon.sumerianarcorestarter;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/** Helper to ask Mic permission. */
public final class MicPermissionHelper {
    private static final int Mic_PERMISSION_CODE = 0;
    private static final String Mic_PERMISSION = Manifest.permission.RECORD_AUDIO;

    /**
     * Check to see we have the necessary permissions for this app.
     */
    public static boolean hasMicPermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, Mic_PERMISSION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check to see we have the necessary permissions for this app, and ask for them if we don't.
     */
    public static void requestMicPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Mic_PERMISSION},
                Mic_PERMISSION_CODE);
    }

    /** Check to see if we need to show the rationale for this permission. */
    public static boolean shouldShowRequestPermissionRationale(Activity activity) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, Mic_PERMISSION);
    }

    /** Launch Application Setting to grant permission. */
    public static void launchPermissionSettings(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        activity.startActivity(intent);
    }
}
