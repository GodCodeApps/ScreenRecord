package com.zchd.screen.record

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.media.MediaScannerConnection
import android.os.Build
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.*
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.zchd.screen.record.dialog.RecordingSettingDialog
import com.zchd.screen.record.interfaces.RecorderCallback
import com.zchd.screen.record.utils.DateUtils
import com.zchd.screen.record.utils.FileUtils
import com.zchd.screen.record.utils.RecorderManager
import java.io.File

/**
 * Copyright (C), 2020-2021, 中传互动（湖北）信息技术有限公司
 * @Author: pym
 * @Date: 2022/2/18:14:11
 * @Description:
 */
class FloatDragView : RelativeLayout {
    private var mCurrentX = 0
    private var mCurrentY = 0
    private var mInitialTouchX = 0f
    private var mInitialTouchY = 0f
    private var mWindowLayoutParams: WindowManager.LayoutParams? = null
    private var mWindowManager: WindowManager? = null
    private var mWindowWidth: Int = 0
    private var mWindowHeight: Int = 0
    private var mViewWith: Int = 0
    private var mViewHeight: Int = 0
    private var mNotchHeight = 0

    private var mMode: Int = 0//0正常录制，1回录
    private var status: Int = 0
    private var mIvMenu: ImageView? = null
    private var tvRecordingTime: TextView? = null
    private var ivRecordingStatus: ImageView? = null
    private var mMaxRecordingTime = 10 * 60 * 1000L
    private var countDownTimer = object : CountDownTimer(mMaxRecordingTime, 1000) {
        override fun onTick(p0: Long) {
            tvRecordingTime?.text = DateUtils.getSurplusMS(mMaxRecordingTime - p0)
        }

        override fun onFinish() {
        }
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    companion object {
        private var INSTANCE: FloatDragView? = null
        fun getInstance(context: Context?): FloatDragView = INSTANCE ?: synchronized(this) {
            INSTANCE ?: FloatDragView(context).also {
                INSTANCE = it
            }
        }
    }

    fun setMode(mode: Int) {
        mMode = mode
        ivRecordingStatus?.performClick()
    }

    init {
        screenOrientation()
        windowsLayoutParams()
        mWindowLayoutParams?.gravity = Gravity.LEFT or Gravity.TOP
        mWindowManager = context.getSystemService(Service.WINDOW_SERVICE) as WindowManager
        mWindowManager?.addView(this, mWindowLayoutParams)
        mWindowWidth = context.resources.displayMetrics.widthPixels
        mWindowHeight = context.resources.displayMetrics.heightPixels
        LayoutInflater.from(context).inflate(R.layout.float_ball_layout, this)
        post {
            mViewWith = this.width
            mViewHeight = this.height
            mWindowLayoutParams?.y = mViewHeight + mNotchHeight
            mWindowManager?.updateViewLayout(this@FloatDragView, mWindowLayoutParams)
        }
        mIvMenu = findViewById<ImageView>(R.id.iv_menu)
        ivRecordingStatus = findViewById<ImageView>(R.id.iv_recording_status)
        tvRecordingTime = findViewById<TextView>(R.id.tv_recording_time)
        setListener()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x: Int
        var y: Int
        var isClick: Boolean
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mCurrentX = mWindowLayoutParams?.x ?: 0
                mCurrentY = mWindowLayoutParams?.y ?: 0
                mInitialTouchX = event.rawX
                mInitialTouchY = event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                x = mCurrentX + (event.rawX - mInitialTouchX).toInt()
                y = mCurrentY + (event.rawY - mInitialTouchY).toInt()
                mWindowLayoutParams?.x = x
                mWindowLayoutParams?.y = y
                mWindowManager?.updateViewLayout(this, mWindowLayoutParams)

            }
            MotionEvent.ACTION_UP -> {
                var dx = event.rawX - mInitialTouchX
                var dy = event.rawY - mInitialTouchY
                val sqrt = Math.sqrt((dx * dx + dy * dy).toDouble())
                isClick = sqrt <= 2
                if (!isClick) {
                    post {
                        val rawX = event.rawX
                        val rawY = event.rawY.toInt()
                        if (rawX <= mWindowWidth / 2) {
                            mWindowLayoutParams?.x = 0
                            mWindowLayoutParams?.y =
                                if (rawY < mViewHeight + mNotchHeight) mViewHeight + mNotchHeight else if (rawY > mWindowHeight - mViewHeight - mNotchHeight) mWindowHeight - mViewHeight - mNotchHeight else rawY
                            mWindowManager?.updateViewLayout(
                                this@FloatDragView,
                                mWindowLayoutParams
                            )
                        } else {
                            mWindowLayoutParams?.x = mWindowWidth - mViewWith
                            mWindowLayoutParams?.y =
                                if (rawY < mViewHeight + mNotchHeight) mViewHeight + mNotchHeight else if (rawY > mWindowHeight - mViewHeight - mNotchHeight) mWindowHeight - mViewHeight - mNotchHeight else rawY
                            mWindowManager?.updateViewLayout(
                                this@FloatDragView,
                                mWindowLayoutParams
                            )
                        }
                    }
                }
            }
        }
        return true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val size = getScreenSize()
        mWindowWidth = if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            size[0] - mNotchHeight
        } else {
            size[0]
        }
        mWindowHeight = size[1]
    }

    private fun setListener() {
        mIvMenu?.setOnClickListener {
            RecordingSettingDialog(context).show()
        }
        ivRecordingStatus?.setOnClickListener {
            ivRecordingStatus?.clearAnimation()
            if (mMode == 1) {
                ivRecordingStatus?.setImageResource(R.mipmap.ic_float_reverse_recording)
                val rotateAnimation = RotateAnimation(
                    0f, 360f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5F,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5F
                )
                rotateAnimation.duration = 1000
                rotateAnimation.repeatCount = 10000
                ivRecordingStatus?.startAnimation(rotateAnimation)
                RecorderManager.INSTANCE.stopMediaRecorder()
                RecorderManager.INSTANCE
                    .startMediaRecorder(context, object : RecorderCallback {
                        override fun onSuccess(file: File?) {
                            super.onSuccess(file)
                            var target = FileUtils.fileCopy(
                                file?.absolutePath,
                                FileUtils.recordingPath + System.currentTimeMillis() + ".mp4"
                            )
                            MediaScannerConnection.scanFile(
                                context,
                                arrayOf(target),
                                null
                            ) { p0, p1 -> }
                            Toast.makeText(context, "回录成功", Toast.LENGTH_LONG).show()
                        }

                        override fun onFail() {
                            super.onFail()
                        }
                    })
                return@setOnClickListener
            }
            if (status == 0) {
                //录制
                status = 1
                ivRecordingStatus?.setImageResource(R.mipmap.ic_float_recording_stop)
                startTimer()
                RecorderManager.INSTANCE
                    .startMediaRecorder(context, object : RecorderCallback {
                        override fun onSuccess(file: File?) {
                            super.onSuccess(file)
                            var target = FileUtils.fileCopy(
                                file?.absolutePath, FileUtils.recordingPath+ System.currentTimeMillis() + ".mp4"
                            )
                            MediaScannerConnection.scanFile(
                                context,
                                arrayOf(target),
                                null
                            ) { p0, p1 -> }
                            Toast.makeText(context, "录制成功", Toast.LENGTH_LONG).show()
                        }

                        override fun onFail() {
                            super.onFail()
                        }
                    })
            } else if (status == 1) {
                //暂停
                ivRecordingStatus?.setImageResource(R.mipmap.ic_float_recording_finish)
                RecorderManager.INSTANCE.stopMediaRecorder()
                status = 0
                cancelTimer()
            }

        }
    }

    private fun screenOrientation() {
        try {
            val configuration = resources.configuration
            if (context is Activity) {
                if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mNotchHeight = NotchUtil.getNotchHeight(context)
                    (context as Activity).requestedOrientation =
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                } else {
                    (context as Activity).requestedOrientation =
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startTimer() {
        countDownTimer?.start()
        mIvMenu?.visibility = View.GONE
        tvRecordingTime?.visibility = View.VISIBLE
    }

    private fun cancelTimer() {
        countDownTimer?.cancel()
        tvRecordingTime?.visibility = View.GONE
        mIvMenu?.visibility = View.VISIBLE
    }

    private fun windowsLayoutParams() {
        var layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG
        }
        mWindowLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.RGBA_8888
        )
    }

    private fun getScreenSize(): IntArray {
        var dm = resources.displayMetrics
        return intArrayOf(dm.widthPixels, dm.heightPixels)
    }

}