package com.zchd.screen.record.utils

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Service
import android.content.Context
import android.graphics.PixelFormat
import android.media.MediaScannerConnection
import android.os.Build
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.*
import android.view.animation.RotateAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.zchd.screen.record.R
import com.zchd.screen.record.interfaces.RecorderCallback
import java.io.File

/**
 * Copyright (C), 2020-2021, 中传互动（湖北）信息技术有限公司
 * @Author: pym
 * @Date: 2022/2/16:18:22
 * @Description:
 */
object FloatLayout {
    private var mLayoutParams: WindowManager.LayoutParams? = null
    private var mFloatBallView: View? = null
    private var mFloatCenterView: View? = null
    private var status: Int = 0
    private var mMode: Int = 0//0正常录制，1回录
    private var mIvMenu: ImageView? = null
    private var tvRecordingTime: TextView? = null
    private var ivRecordingStatus: ImageView? = null

    private var reverseRecording: FrameLayout? = null
    private var normalRecording: FrameLayout? = null
    private var countDownTimer: CountDownTimer? = null
    private var mMaxRecordingTime = 10 * 60 * 1000L


    init {
        status = 0
        var layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_TOAST
        }
        mLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        countDownTimer = object : CountDownTimer(mMaxRecordingTime, 1000) {
            override fun onTick(p0: Long) {
                tvRecordingTime?.text = DateUtils.getSurplusMS(mMaxRecordingTime - p0)
            }

            override fun onFinish() {
            }
        }
    }

    fun initUi(context: Context) {
        var metrics = context.resources.displayMetrics
        var width = metrics.widthPixels
        var height = metrics.heightPixels
        val windowManager = context.getSystemService(Service.WINDOW_SERVICE) as WindowManager
        var inflater = context.getSystemService(Service.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mFloatBallView = inflater.inflate(R.layout.float_ball_layout, null)
        mFloatCenterView = inflater.inflate(R.layout.float_center_layout, null)
        mLayoutParams?.gravity = Gravity.TOP or Gravity.START
        mFloatBallView?.visibility = View.GONE
        windowManager.addView(mFloatBallView, mLayoutParams)
        mLayoutParams?.gravity = Gravity.CENTER
        mLayoutParams?.width = WindowManager.LayoutParams.MATCH_PARENT
        mLayoutParams?.height = WindowManager.LayoutParams.MATCH_PARENT
        mFloatCenterView?.visibility = View.VISIBLE
        windowManager.addView(mFloatCenterView, mLayoutParams)
        mIvMenu = mFloatBallView?.findViewById<ImageView>(R.id.iv_menu)
        ivRecordingStatus = mFloatBallView?.findViewById<ImageView>(R.id.iv_recording_status)
        tvRecordingTime = mFloatBallView?.findViewById<TextView>(R.id.tv_recording_time)


        mIvMenu?.setOnClickListener {
            showFloatCenter()
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
                                file?.absolutePath, FileUtils.recordingPath
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
                                file?.absolutePath, FileUtils.recordingPath
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
        reverseRecording = mFloatCenterView?.findViewById<FrameLayout>(R.id.card_reverse_recording)
        normalRecording = mFloatCenterView?.findViewById<FrameLayout>(R.id.card_normal_recording)
        var centerRootView = mFloatCenterView?.findViewById<FrameLayout>(R.id.root)
        centerRootView?.setOnClickListener {
            showFloatBall()
        }
        reverseRecording?.setOnClickListener {
            mMode = 1
            showFloatBall()
            ivRecordingStatus?.performClick()
        }
        normalRecording?.setOnClickListener {
            mMode = 0
            showFloatBall()
            ivRecordingStatus?.performClick()
        }
    }

    private fun showFloatBall() {
        mFloatBallView?.visibility = View.VISIBLE
        mFloatCenterView?.visibility = View.GONE
    }

    private fun showFloatCenter() {
        mFloatBallView?.visibility = View.GONE
        mFloatCenterView?.visibility = View.VISIBLE
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
}