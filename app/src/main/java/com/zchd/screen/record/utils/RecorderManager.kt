package com.zchd.screen.record.utils

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import com.zchd.screen.record.interfaces.RecorderCallback
import com.zchd.screen.record.service.RecorderService

/**
 * Copyright (C), 2020-2021, 中传互动（湖北）信息技术有限公司
 * @Author: pym
 * @Date: 2022/2/16:10:52
 * @Description:
 */
class RecorderManager {
    private var serviceConnection: RecorderServiceProxy? = null

    companion object {
        val INSTANCE by lazy { RecorderManager() }
        const val REQUEST_CODE = 10000
    }

    fun startService(activity: Activity) {
        serviceConnection = RecorderServiceProxy()
        val intent = Intent(activity, RecorderService::class.java)
        activity.bindService(intent, serviceConnection!!, Service.BIND_AUTO_CREATE)
    }

    /**
     * 停止媒体投影服务
     *
     * @param context context
     */
    fun stopService(context: Context?) {
        if (serviceConnection != null) {
            context?.unbindService(serviceConnection!!)
            serviceConnection = null
        }
    }

    /**
     * 创建VirtualDisplay(onActivityResult中调用)
     */
    fun createVirtualDisplay(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (requestCode != REQUEST_CODE || resultCode != Activity.RESULT_OK) {
            return
        }
        serviceConnection?.recorderBinder?.create(resultCode, data)

    }

    /**
     * 开始 屏幕录制
     */
    fun startMediaRecorder(activity: Activity, callback: RecorderCallback) {
        serviceConnection?.recorderBinder?.start(activity, callback)
    }

    /**
     * 暂停屏幕录制
     */
    fun pauseMediaRecorder() {
        serviceConnection?.recorderBinder?.pause()
    }

    /**
     * 继续屏幕录制
     */
    fun resumeMediaRecorder() {
        serviceConnection?.recorderBinder?.resume()

    }

    /**
     * 停止 屏幕录制
     */
    fun stopMediaRecorder() {
        serviceConnection?.recorderBinder?.stop()

    }

}
