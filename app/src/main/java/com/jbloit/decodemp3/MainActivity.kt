package com.jbloit.decodemp3

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Resources
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    val TAG = "MAIN"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initPermissions()

        decodeMp3(assets, "sinusoid.mp3")
        sample_text.text = stringFromJNI()
    }


    fun initPermissions() {

        val read = Manifest.permission.READ_EXTERNAL_STORAGE
        val write = Manifest.permission.WRITE_EXTERNAL_STORAGE
        val recordPermission = Manifest.permission.RECORD_AUDIO

        if (!(hasPermission(read) && hasPermission(write) && hasPermission(recordPermission)))
        {
            val permissions = arrayOf(read, write, recordPermission)
            ActivityCompat.requestPermissions(this, permissions, 0)
            return
        }
    }
    private fun hasPermission(permission: String): Boolean {
        val check = ActivityCompat.checkSelfPermission(this, permission)
        return check == PackageManager.PERMISSION_GRANTED
    }

    external fun stringFromJNI(): String
    external fun decodeMp3(assetManager: AssetManager,  audioFilePath: String)

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }


}
