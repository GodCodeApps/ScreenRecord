package com.zchd.screen.record.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.util.DisplayMetrics
import com.zchd.screen.record.interfaces.MediaRecorderCallback
import com.zchd.screen.record.interfaces.ScreenCaptureCallback
import com.zchd.screen.record.service.ScreenCapService
import com.zchd.screen.record.service.ScreenCapService.Companion.bindService
import com.zchd.screen.record.service.ScreenCapService.Companion.unbindService
import com.zchd.screen.record.service.ScreenCapService.MediaProjectionBinder

/**
 * Copyright (C), 2020-2021, 中传互动（湖北）信息技术有限公司
 * @Author: pym
 * @Date: 2022/2/16:10:52
 * @Description:
 */
class MediaProjectionHelper private constructor() {
    private object InstanceHolder {
        val instance = MediaProjectionHelper()
    }

    private var mediaProjectionManager: MediaProjectionManager? = null
    private var displayMetrics: DisplayMetrics? = null
    private var serviceConnection: ServiceConnection? = null
    private var mediaProjectionService: ScreenCapService? = null

    /**
     * 启动媒体投影服务
     *
     * @param activity activity
     */
    fun startService(activity: Activity) {
        if (mediaProjectionManager != null) {
            return
        }

        // 启动媒体投影服务
        mediaProjectionManager =
            activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        if (mediaProjectionManager != null) {
            activity.startActivityForResult(
                mediaProjectionManager!!.createScreenCaptureIntent(),
                REQUEST_CODE
            )
        }

        // 此处宽高需要获取屏幕完整宽高，否则截屏图片会有白/黑边
        displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getRealMetrics(displayMetrics)

        // 绑定服务
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                if (service is MediaProjectionBinder) {
                    mediaProjectionService = service.service
                }
            }

            override fun onServiceDisconnected(name: ComponentName) {
                mediaProjectionService = null
            }
        }
        bindService(activity, serviceConnection)
    }

    /**
     * 停止媒体投影服务
     *
     * @param context context
     */
    fun stopService(context: Context?) {
        mediaProjectionService = null
        if (serviceConnection != null) {
            unbindService(context!!, serviceConnection)
            serviceConnection = null
        }
        displayMetrics = null
        mediaProjectionManager = null
    }

    /**
     * 创建VirtualDisplay(onActivityResult中调用)
     *
     * @param requestCode           requestCode
     * @param resultCode            resultCode
     * @param data                  data
     * @param isScreenCaptureEnable 是否可以屏幕截图
     * @param isMediaRecorderEnable 是否可以媒体录制
     */
    fun createVirtualDisplay(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        isScreenCaptureEnable: Boolean,
        isMediaRecorderEnable: Boolean
    ) {
        if (mediaProjectionService == null) {
            return
        }
        if (requestCode != REQUEST_CODE) {
            return
        }
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        mediaProjectionService!!.createVirtualDisplay(
            resultCode,
            data,
            displayMetrics,
            isScreenCaptureEnable,
            isMediaRecorderEnable
        )
    }

    /**
     * 屏幕截图
     *
     * @param callback callback
     */
    fun capture(callback: ScreenCaptureCallback) {
        if (mediaProjectionService == null) {
            callback.onFail()
            return
        }
        mediaProjectionService!!.capture(callback)
    }

    /**
     * 开始 屏幕录制
     *
     * @param callback callback
     */
    fun startMediaRecorder(callback: MediaRecorderCallback) {
        if (mediaProjectionService == null) {
            callback.onFail()
            return
        }
        mediaProjectionService!!.startRecording(callback)
    }

    /**
     * 暂停屏幕录制
     */
    fun pauseMediaRecorder() {
        if (mediaProjectionService == null) {
            return
        }
        mediaProjectionService!!.pauseRecording()
    }

    /**
     * 继续屏幕录制
     */
    fun resumeMediaRecorder() {
        if (mediaProjectionService == null) {
            return
        }
        mediaProjectionService!!.resumeRecording()
    }

    /**
     * 停止 屏幕录制
     */
    fun stopMediaRecorder() {
        if (mediaProjectionService == null) {
            return
        }
        mediaProjectionService!!.stopRecording()
    }

    companion object {
        const val REQUEST_CODE = 10086
        val instance: MediaProjectionHelper get() = InstanceHolder.instance
    }
}
