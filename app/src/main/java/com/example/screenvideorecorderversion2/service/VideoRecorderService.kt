package com.example.screenvideorecorderversion2.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.screenvideorecorderversion2.R
import com.example.screenvideorecorderversion2.ui.MainActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class VideoRecorderService : Service() {

    private val CHANNEL_ID = "First Channel";
    private val NOTIFICATION_ID = 101;
    private val TAG = javaClass.name.toString();

    private val PERMISSION_CODE = 1
    private var mScreenDensity = 420;
    private var mProjectionManager: MediaProjectionManager? = null
    private val DISPLAY_WIDTH = 480
    private val DISPLAY_HEIGHT = 640
    private var mMediaProjection: MediaProjection? = null
    private var mVirtualDisplay: VirtualDisplay? = null
    private var mMediaProjectionCallback: MediaProjectionCallbackService? = null;
    private var mMediaRecorder: MediaRecorder? = null
    private var myDataIntent: Intent? = null;


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.getStringExtra("intent").equals("start")) {
            createNotification();
            initRecorder();
            prepareRecorder();
            mProjectionManager =
                getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager;
            mMediaProjectionCallback = MediaProjectionCallbackService();
            myDataIntent = MainActivity.staticMainActivityIntent!!
            shareScreen();
        } else if (intent != null && intent.getStringExtra("intent").equals("stop")) {
            Log.e(TAG, "onStartCommand: RecorderService has just been stopped")
            mMediaRecorder?.stop();
            mMediaRecorder?.reset();
            stopScreenSharing();

        }
        return START_NOT_STICKY;
    }

    private fun shareScreen() {
        mMediaProjection =
            mProjectionManager?.getMediaProjection(Activity.RESULT_OK, myDataIntent!!);
        mMediaProjection?.registerCallback(mMediaProjectionCallback, null);
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder?.start();
    }

    private fun createNotification() {

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Видео запись channel",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Запись в процессе")
            .setContentText("Будьте осторожны, ваш экран записывается")
            .setSmallIcon(R.drawable.ic_service)
            .setDefaults(Notification.DEFAULT_SOUND)
            .build();

        startForeground(NOTIFICATION_ID, notification);

    }

    private fun prepareRecorder() {
        try {
            mMediaRecorder?.prepare();
        } catch (ex: Exception) {
            Log.e(TAG, "prepareRecorder: ", ex)
        }
    }


    inner class MediaProjectionCallbackService : MediaProjection.Callback() {

        override fun onStop() {
            mMediaRecorder?.stop()
            mMediaRecorder?.reset()
            Log.v(TAG, "Recording Stopped")
            mMediaProjection = null
            stopScreenSharing()
            Log.i(TAG, "MediaProjection Stopped")
        }
    }

    private fun stopScreenSharing() {
        if (mVirtualDisplay == null)
            return;
        mVirtualDisplay?.release();
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initRecorder() {
        if (mMediaRecorder == null) {
            mMediaRecorder = MediaRecorder()
            with(mMediaRecorder!!) {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setVideoEncodingBitRate(512 * 1000)
                setVideoFrameRate(30)
                setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT)
                setOutputFile(getOutPutMediaFile())
            }
        }
    }

    private fun getOutPutMediaFile(): File? {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            "Screen Recorder"
        )
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e(TAG, "Failed to create directory")
                return null
            }
        }
        val formatter_date = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

        return File(file.absoluteFile.toString() + File.separator + "VID" + formatter_date + ".mp4")

    }

    private fun createVirtualDisplay(): VirtualDisplay? {
        return mMediaProjection?.createVirtualDisplay(
            "MainActivity",
            DISPLAY_WIDTH,
            DISPLAY_HEIGHT,
            mScreenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mMediaRecorder?.surface,
            null,
            null
        );
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null;
    }
}