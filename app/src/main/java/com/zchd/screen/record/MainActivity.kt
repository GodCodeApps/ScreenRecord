package com.zchd.screen.record

import android.Manifest
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import com.zchd.screen.record.interfaces.RecorderCallback
import com.zchd.screen.record.utils.FloatLayout
import com.zchd.screen.record.utils.RecorderManager
import java.io.File


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FloatDragView(this)



        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS
            ),
            1
        )
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.SYSTEM_ALERT_WINDOW),
            1
        )
        RecorderManager.INSTANCE.startService(this)

        findViewById<AppCompatButton>(R.id.btn_recording).setOnClickListener {
            FloatLayout.initUi(this)
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