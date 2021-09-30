package com.example.screenvideorecorderversion2.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

object Utils {

    private val TAG = javaClass.name.toString();

    private val PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO,

    )

    fun permissionToReadAndCreateFile(activity: Activity) {

        PERMISSIONS_STORAGE.toList().stream().forEach { permission ->
            val check_permission = ActivityCompat.checkSelfPermission(
                activity,
                permission
            )

            if (check_permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    1
                );
            }
        }

    }


}