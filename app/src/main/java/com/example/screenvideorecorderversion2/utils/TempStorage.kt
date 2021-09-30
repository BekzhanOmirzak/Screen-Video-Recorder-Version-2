package com.example.screenvideorecorderversion2.utils

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

object TempStorage {

    private lateinit var sharedPreferences: SharedPreferences;


    fun initSharedPreferences(activity: Activity) {
        sharedPreferences = activity.getPreferences(MODE_PRIVATE);
    }

    fun saveValue(isRecording:Boolean){
        sharedPreferences.edit().putBoolean("isRecording",isRecording).apply();
    }

    fun getValue(): Boolean{
        return sharedPreferences.getBoolean("isRecording",false);
    }


}