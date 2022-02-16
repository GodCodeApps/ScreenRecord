package com.zchd.screen.record.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import com.zchd.screen.record.interfaces.MediaRecorderCallback
import com.zchd.screen.record.interfaces.ScreenCaptureCallback
import com.zchd.screen.record.utils.FileUtils
import com.zchd.screen.record.utils.MediaProjectionHelper
import com.zchd.screen.record.utils.NotificationHelper
import java.io.File

/**
 * Copyright (C), 2020-2021, 中传互动（湖北）信息技术有限公司
 * @Author: pym
 * @Date: 2022/2/16:09:13
 * @Description:录屏服务
 */
class ScreenCapService : Service() {
    private var displayMetrics: DisplayMetrics? = null
    private var isScreenCaptureEnable // 是否可以屏幕截图
            = false
    private var isMediaRecorderEnable // 是否可以媒体录制
            = false
    private var mediaProjectionManager: MediaProjectionManager? = null
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplayImageReader: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var isImageAvailable = false
    private var virtualDisplayMediaRecorder: VirtualDisplay? = null
    private var mediaRecorder: MediaRecorder? = null
    private var mediaFile: File? = null
    private var isMediaRecording = false
    private var mediaRecorderCallback: MediaRecorderCallback? = null

    companion object {
        private const val ID_MEDIA_PROJECTION = MediaProjectionHelper.REQUEST_CODE
        fun bindService(context: Context, serviceConnection: ServiceConnection?) {
            val intent = Intent(context, ScreenCapService::class.java)
            context.bindService(intent, serviceConnection!!, BIND_AUTO_CREATE)
        }

        fun unbindService(context: Context, serviceConnection: ServiceConnection?) {
            context.unbindService(serviceConnection!!)
        }
    }

    inner class MediaProjectionBinder : Binder() {
        val service: ScreenCapService get() = this@ScreenCapService
    }

    override fun onBind(intent: Intent): IBinder? {
        return MediaProjectionBinder()
    }

    override fun onDestroy() {
        destroy()
        super.onDestroy()
    }

    /**
     * 销毁
     */
    private fun destroy() {
        stopImageReader()
        stopMediaRecorder()
        if (mediaProjection != null) {
            mediaProjection!!.stop()
            mediaProjection = null
        }
        if (mediaProjectionManager != null) {
            mediaProjectionManager = null
        }
        stopForeground(true)
    }

    /**
     * 结束 屏幕截图
     */
    private fun stopImageReader() {
        isImageAvailable = false
        if (imageReader != null) {
            imageReader!!.close()
            imageReader = null
        }
        if (virtualDisplayImageReader != null) {
            virtualDisplayImageReader!!.release()
            virtualDisplayImageReader = null
        }
    }

    /**
     * 结束 媒体录制
     */
    private fun stopMediaRecorder() {
        stopRecording()
        if (virtualDisplayMediaRecorder != null) {
            virtualDisplayMediaRecorder!!.release()
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
        startForeground(ID_MEDIA_PROJECTION, notification)
    }

    /**
     * 创建 屏幕截图
     */
    @SuppressLint("WrongConstant")
    private fun createImageReader() {
        val width = displayMetrics!!.widthPixels
        val height = displayMetrics!!.heightPixels
        val densityDpi = displayMetrics!!.densityDpi
        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        imageReader!!.setOnImageAvailableListener({ isImageAvailable = true }, null)
        virtualDisplayImageReader = mediaProjection!!.createVirtualDisplay(
            "ScreenCapture",
            width, height, densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader!!.surface, null, null
        )
    }

    /**
     * 创建 媒体录制
     */
    private fun createMediaRecorder() {
        val width = displayMetrics!!.widthPixels
        val height = displayMetrics!!.heightPixels
        val densityDpi = displayMetrics!!.densityDpi

        // 创建保存路径
        val dirFile = FileUtils.getCacheMovieDir(this)
        val mkdirs = dirFile?.mkdirs()
        // 创建保存文件
        mediaFile = File(dirFile, FileUtils.getDateName("MediaRecorder") + ".mp4")
        // 调用顺序不能乱
        mediaRecorder = MediaRecorder()
        //设置音频
        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder!!.setOutputFile(mediaFile!!.absolutePath)
        mediaRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        mediaRecorder!!.setVideoSize(width, height)
        mediaRecorder!!.setVideoFrameRate(30)
        mediaRecorder!!.setVideoEncodingBitRate(5 * width * height)
        mediaRecorder!!.setOnErrorListener { mr, what, extra ->
            if (mediaRecorderCallback != null) {
                mediaRecorderCallback!!.onFail()
            }
        }
        try {
            mediaRecorder!!.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (virtualDisplayMediaRecorder == null) {
            virtualDisplayMediaRecorder = mediaProjection!!.createVirtualDisplay(
                "VoiceVideo",
                width, height, densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder!!.surface, null, null
            )
        } else {
            virtualDisplayMediaRecorder!!.surface = mediaRecorder!!.surface
        }
    }

    /**
     * 创建VirtualDisplay
     *
     * @param resultCode            resultCode
     * @param data                  data
     * @param displayMetrics        displayMetrics
     * @param isScreenCaptureEnable 是否可以屏幕截图
     * @param isMediaRecorderEnable 是否可以媒体录制
     */
    fun createVirtualDisplay(
        resultCode: Int,
        data: Intent?,
        displayMetrics: DisplayMetrics?,
        isScreenCaptureEnable: Boolean,
        isMediaRecorderEnable: Boolean
    ) {
        this.displayMetrics = displayMetrics
        this.isScreenCaptureEnable = isScreenCaptureEnable
        this.isMediaRecorderEnable = isMediaRecorderEnable
        if (data == null) {
            stopSelf()
            return
        }
        showNotification()
        mediaProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        if (mediaProjectionManager == null) {
            stopSelf()
            return
        }
        mediaProjection = mediaProjectionManager!!.getMediaProjection(resultCode, data)
        if (mediaProjection == null) {
            stopSelf()
            return
        }
        if (isScreenCaptureEnable) {
            createImageReader()
        }
    }

    /**
     * 屏幕截图
     *
     * @param callback callback
     */
    fun capture(callback: ScreenCaptureCallback) {
        if (!isScreenCaptureEnable) {
            callback.onFail()
            return
        }
        if (imageReader == null) {
            callback.onFail()
            return
        }
        if (!isImageAvailable) {
            callback.onFail()
            return
        }
        val image = imageReader!!.acquireLatestImage()
        if (image == null) {
            callback.onFail()
            return
        }

        // 获取数据
        val width = image.width
        val height = image.height
        val plane = image.planes[0]
        val buffer = plane.buffer

        // 重新计算Bitmap宽度，防止Bitmap显示错位
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * width
        val bitmapWidth = width + rowPadding / pixelStride

        // 创建Bitmap
        val bitmap = Bitmap.createBitmap(bitmapWidth, height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)

        // 释放资源
        image.close()

        // 裁剪Bitmap，因为重新计算宽度原因，会导致Bitmap宽度偏大
        val result = Bitmap.createBitmap(bitmap, 0, 0, width, height)
        bitmap.recycle()
        isImageAvailable = false
        callback.onSuccess(result)
    }

    /**
     * 开始 媒体录制
     *
     * @param callback callback
     */
    fun startRecording(callback: MediaRecorderCallback?) {
        mediaRecorderCallback = callback
        if (!isMediaRecorderEnable) {
            if (mediaRecorderCallback != null) {
                mediaRecorderCallback!!.onFail()
            }
            return
        }
        if (isMediaRecording) {
            if (mediaRecorderCallback != null) {
                mediaRecorderCallback!!.onFail()
            }
            return
        }
        createMediaRecorder()
        mediaRecorder!!.start()
        isMediaRecording = true
    }

    fun pauseRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder?.pause()
        }

    }

    fun resumeRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder?.resume()
        }

    }

    /**
     * 停止 媒体录制
     */
    fun stopRecording() {
        if (!isMediaRecorderEnable) {
            if (mediaRecorderCallback != null) {
                mediaRecorderCallback!!.onFail()
            }
        }
        if (mediaRecorder == null) {
            if (mediaRecorderCallback != null) {
                mediaRecorderCallback!!.onFail()
            }
            return
        }
        if (!isMediaRecording) {
            if (mediaRecorderCallback != null) {
                mediaRecorderCallback!!.onFail()
            }
            return
        }
        mediaRecorder!!.stop()
        mediaRecorder!!.reset()
        mediaRecorder!!.release()
        mediaRecorder = null
        if (mediaRecorderCallback != null) {
            mediaRecorderCallback!!.onSuccess(mediaFile)
        }
        mediaFile = null
        isMediaRecording = false
        mediaRecorderCallback = null
    }

}