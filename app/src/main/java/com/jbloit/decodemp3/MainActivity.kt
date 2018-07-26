package com.jbloit.decodemp3

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    val TAG = "MAIN"

    var ffmpeg : FFmpeg? = null

    lateinit var  mp3File: File
    lateinit var  wavFile: File

    var countMS: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initPermissions()

        val baseDir =  Environment.getExternalStorageDirectory()
        val testDir = File(baseDir.absolutePath + "/test")
        mp3File = File(testDir, "sinusoid.mp3")
        wavFile = File(testDir, "sinusoid.wav")

        // Example of a call to a native method
//        sample_text.text = stringFromJNI()


        ffmpeg = FFmpeg.getInstance(this)
        try {
            ffmpeg?.loadBinary(object : LoadBinaryResponseHandler(){

                override fun onStart() {
                    super.onStart()
                }

                override fun onFailure() {
                    super.onFailure()
                }

                override fun onSuccess() {
                    super.onSuccess()
                }

                override fun onFinish() {
                    super.onFinish()
                }
            })
        } catch (e: FFmpegNotSupportedException) {
            Log.d(TAG, "load ffmpeg failed")
            e.printStackTrace()

        }

        runFFmpegCommand()
    }


    fun runFFmpegCommand(){
        val command = arrayOf("-i", "$mp3File", "$wavFile")
        try {
            ffmpeg?.execute(command, object : ExecuteBinaryResponseHandler(){
                override fun onStart() {
                    super.onStart()
                    countMS = SystemClock.uptimeMillis()
                }

                override fun onProgress(message: String?) {
                    super.onProgress(message)
                    Log.d(TAG, "progress : ${message}")
                }

                override fun onFailure(message: String?) {
                    super.onFailure(message)
                    Log.d(TAG, "failure : ${message}")
                }

                override fun onSuccess(message: String?) {
                    super.onSuccess(message)
                    Log.d(TAG, "success : ${message}")
                    sample_text.text = message
                }

                override fun onFinish() {
                    super.onFinish()
                    countMS = SystemClock.uptimeMillis() - countMS
                    sample_text.text = "ffmpeg command executed in $countMS msec"
                }
            })
        } catch (e: FFmpegCommandAlreadyRunningException) {
            Log.w(TAG, "load ffmpeg failed")
            e.printStackTrace()
        }
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

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String


    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }


}
