package com.zchd.screen.record.service

import android.app.Activity
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.DisplayMetrics
import com.zchd.screen.record.interfaces.IRecorderBinder
import com.zchd.screen.record.interfaces.RecorderCallback
import com.zchd.screen.record.utils.FileUtils
import com.zchd.screen.record.utils.NotificationHelper
import com.zchd.screen.record.utils.RecorderManager
import java.io.File

/**
 * Copyright (C), 2020-2021, 中传互动（湖北）信息技术有限公司
 * @Author: pym
 * @Date: 2022/2/16:09:13
 * @Description:录屏服务
 */
class RecorderService : Service() {
    private var displayMetrics: DisplayMetrics? = null
    private var mediaProjectionManager: MediaProjectionManager? = null
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplayMediaRecorder: VirtualDisplay? = null
    private var mMediaRecorder: MediaRecorder? = null
    private var mMediaFile: File? = null
    private var isMediaRecording = false
    private var mCallback: RecorderCallback? = null


    inner class RecorderBinder : Binder(), IRecorderBinder {
        val service: RecorderService get() = this@RecorderService
        override fun create(resultCode: Int, data: Intent?) {
            createVirtualDisplay(resultCode, data)
        }

        override fun start(context: Context, callback: RecorderCallback) {
            displayMetrics = DisplayMetrics()
            context as Activity
            context.windowManager.defaultDisplay.getRealMetrics(displayMetrics)
            if (mediaProjectionManager == null) {
                mediaProjectionManager =
                    context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                context.startActivityForResult(
                    mediaProjectionManager?.createScreenCaptureIntent(),
                    RecorderManager.REQUEST_CODE
                )
            }
            Handler().postDelayed({
                startRecording(callback)
            }, 3000)
        }

        override fun pause() {
            pauseRecording()
        }

        override fun resume() {
            resumeRecording()
        }

        override fun stop() {
            stopRecording()
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return RecorderBinder()
    }


    /**
     * 创建 媒体录制
     */
    private fun createMediaRecorder() {
        val width = displayMetrics!!.widthPixels
        val height = displayMetrics!!.heightPixels
        val densityDpi = displayMetrics!!.densityDpi
        val dirFile = FileUtils.getCacheMovieDir(this)
        val mkdirs = dirFile?.mkdirs()
        mMediaFile = File(dirFile, FileUtils.getDateName("MediaRecorder") + ".mp4")
        mMediaRecorder = MediaRecorder()
        mMediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mMediaRecorder?.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mMediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mMediaRecorder?.setOutputFile(mMediaFile?.absolutePath)
        mMediaRecorder?.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        mMediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        mMediaRecorder?.setVideoSize(width, height)
        mMediaRecorder?.setVideoFrameRate(30)
        mMediaRecorder?.setVideoEncodingBitRate(5 * width * height)
        mMediaRecorder?.setOnErrorListener { mr, what, extra ->
            mCallback?.onFail()
        }
        try {
            mMediaRecorder?.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (virtualDisplayMediaRecorder == null) {
            virtualDisplayMediaRecorder = mediaProjection?.createVirtualDisplay(
                "VoiceVideo",
                width, height, densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder?.surface, null, null
            )
        } else {
            virtualDisplayMediaRecorder?.surface = mMediaRecorder?.surface
        }
    }

    /**
     * 初始化
     */
    fun createVirtualDisplay(resultCode: Int, data: Intent?) {
        if (data == null) {
            stopSelf()
            return
        }
        showNotification()
        if (mediaProjectionManager == null) {
            stopSelf()
            return
        }
        mediaProjection = mediaProjectionManager?.getMediaProjection(resultCode, data)
        if (mediaProjection == null) {
            stopSelf()
            return
        }
    }

    /**
     * 开始录制
     */
    fun startRecording(callback: RecorderCallback?) {
        mCallback = callback
        if (isMediaRecording) {
            mCallback?.onFail()
            return
        }
        createMediaRecorder()
        mMediaRecorder?.start()
        isMediaRecording = true
    }

    /**
     * 暂停录制
     */
    fun pauseRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mMediaRecorder?.pause()
        }

    }

    /**
     * 继续录制
     */
    fun resumeRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mMediaRecorder?.resume()
        }

    }

    /**
     * 结束录制
     */
    fun stopRecording() {
        if (mMediaRecorder == null) {
            mCallback?.onFail()
            return
        }
        if (!isMediaRecording) {
            mCallback?.onFail()
            return
        }
        mMediaRecorder?.stop()
        mMediaRecorder?.reset()
        mMediaRecorder?.release()
        mMediaRecorder = null
        mCallback?.onSuccess(mMediaFile)
        mMediaFile = null
        isMediaRecording = false
        mCallback = null

    }

    override fun onDestroy() {
        destroy()
        super.onDestroy()
    }

    /**
     * 销毁
     */
    private fun destroy() {
        stopMediaRecorder()
        if (mediaProjection != null) {
            mediaProjection?.stop()
            mediaProjection = null
        }
        if (mediaProjectionManager != null) {
            mediaProjectionManager = null
        }
        stopForeground(true)
    }

    /**
     * 结束录制
     */
    private fun stopMediaRecorder() {
        stopRecording()
        if (virtualDisplayMediaRecorder != null) {
            virtualDisplayMediaRecorder?.release()
            virtualDisplayMediaRecorder = null
        }
    }

    /**
     * 显示通知栏
     */
    private fun showNotification() {
        val notification = NotificationHelper.instance.createSystem()
            .setTicker("录屏服务启动")
            .setOngoing(true)
            .setContentText("录屏服务启动")
            .setDefaults(Notification.DEFAULT_ALL)
            .build()
        startForeground(RecorderManager.REQUEST_CODE, notification)
    }
}