package com.zchd.screen.record

import android.Manifest
import android.Manifest.permission.SYSTEM_ALERT_WINDOW
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import com.zchd.screen.record.dialog.RecordingSettingDialog
import com.zchd.screen.record.interfaces.RecorderCallback
import com.zchd.screen.record.utils.FloatLayout
import com.zchd.screen.record.utils.RecorderManager
import com.zchd.screen.record.utils.XPermission
import java.io.File


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestOverlayPermission(this, object : XPermission.SimpleCallback {
            override fun onGranted() {
//                FloatDragView.getInstance(this@MainActivity)
                RecordingSettingDialog(this@MainActivity).show()
            }

            override fun onDenied() {
            }
        })
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                SYSTEM_ALERT_WINDOW
            ),
            1
        )
        RecorderManager.INSTANCE.startService(this)

        findViewById<AppCompatButton>(R.id.btn_recording).setOnClickListener {
            RecordingSettingDialog(this@MainActivity).show()

        }
        findViewById<AppCompatButton>(R.id.btn_start).setOnClickListener {
            RecorderManager.INSTANCE
                .startMediaRecorder(this, object : RecorderCallback {
                    override fun onSuccess(file: File?) {
                        super.onSuccess(file)
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(
                            Uri.parse(
                                file?.absolutePath
                            ), "video/*"
                        )
                        startActivity(intent)
                    }

                    override fun onFail() {
                        super.onFail()
                    }
                })
        }
        findViewById<AppCompatButton>(R.id.btn_pause).setOnClickListener {
            RecorderManager.INSTANCE.pauseMediaRecorder()

        }
        findViewById<AppCompatButton>(R.id.btn_resume).setOnClickListener {
            RecorderManager.INSTANCE.resumeMediaRecorder()

        }
        findViewById<AppCompatButton>(R.id.btn_close).setOnClickListener {
            RecorderManager.INSTANCE.stopMediaRecorder()

        }
    }

    private fun requestOverlayPermission(context: Context?, callback: XPermission.SimpleCallback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            XPermission.create(context).requestDrawOverlays(callback)
        } else {
            callback.onGranted()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        RecorderManager.INSTANCE
            .createVirtualDisplay(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        RecorderManager.INSTANCE.stopService(this)
        super.onDestroy()
    }

}