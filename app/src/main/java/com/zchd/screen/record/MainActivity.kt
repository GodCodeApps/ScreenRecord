package com.zchd.screen.record

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.zchd.screen.record.interfaces.MediaRecorderCallback
import com.zchd.screen.record.utils.MediaProjectionHelper
import java.io.File


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                1
            )
        }
        MediaProjectionHelper.instance.startService(this)
        findViewById<AppCompatButton>(R.id.btn_start).setOnClickListener {
            MediaProjectionHelper.instance
                .startMediaRecorder(object : MediaRecorderCallback {

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
            MediaProjectionHelper.instance.pauseMediaRecorder()

        }
        findViewById<AppCompatButton>(R.id.btn_resume).setOnClickListener {
            MediaProjectionHelper.instance.resumeMediaRecorder()

        }
        findViewById<AppCompatButton>(R.id.btn_close).setOnClickListener {
            MediaProjectionHelper.instance.stopMediaRecorder()

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        MediaProjectionHelper.instance
            .createVirtualDisplay(requestCode, resultCode, data, true, true)
    }

    override fun onDestroy() {
        MediaProjectionHelper.instance.stopService(this)
        super.onDestroy()
    }

}