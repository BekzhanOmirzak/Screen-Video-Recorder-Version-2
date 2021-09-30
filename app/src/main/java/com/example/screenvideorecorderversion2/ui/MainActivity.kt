package com.example.screenvideorecorderversion2.ui

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.screenvideorecorderversion2.R
import com.example.screenvideorecorderversion2.databinding.ActivityMainBinding
import com.example.screenvideorecorderversion2.service.VideoRecorderService
import com.example.screenvideorecorderversion2.utils.TempStorage
import com.example.screenvideorecorderversion2.utils.Utils
import java.util.*


class MainActivity : AppCompatActivity() {


    private var mProjectionManager: MediaProjectionManager? = null
    private var PERMISSION_CODE = 1;

    companion object {
        var staticMainActivityIntent: Intent? = null;
        val TAG = javaClass.name.toString();
    }

    private lateinit var binding: ActivityMainBinding;

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding.root);
        Utils.permissionToReadAndCreateFile(this);
        TempStorage.initSharedPreferences(this);

        binding.btnStart.isEnabled = !TempStorage.getValue();
        binding.btnStop.isEnabled = TempStorage.getValue();

        mProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager;

        binding.btnStart.setOnClickListener {
            shareScreen();
        }

        binding.btnStop.setOnClickListener {
            val stopIntent = Intent(this, VideoRecorderService::class.java);
            stopIntent.putExtra("intent", "stop");
            stopService(stopIntent);
            TempStorage.saveValue(false);
            binding.btnStart.isEnabled = true;
            binding.btnStop.isEnabled = false;
        }

    }


    private fun shareScreen() {
        startActivityForResult(mProjectionManager?.createScreenCaptureIntent(), PERMISSION_CODE);
    };


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != PERMISSION_CODE) {
            Log.e(TAG, "Unknown request code: " + requestCode);
            return;
        }

        if (resultCode != RESULT_OK) {
            Toast.makeText(
                this,
                "Screen Cast Permission Denied", Toast.LENGTH_SHORT
            ).show();
            return;
        }
        staticMainActivityIntent = data?.clone() as Intent;
        TempStorage.saveValue(true);
        binding.btnStart.isEnabled = false;
        binding.btnStop.isEnabled = true;
        val intent = Intent(this, VideoRecorderService::class.java);
        intent.putExtra("intent", "start");
        startForegroundService(intent);
        super.onActivityResult(requestCode, resultCode, data);
    }


}