package com.zchd.screen.record

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.Build
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.RelativeLayout

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
    private var mStartTime: Long = 0
    private var mEndTime: Long = 0

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
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
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x: Int
        var y: Int
        var isClick = false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mStartTime = System.currentTimeMillis()
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
            MotionEvent.ACTION_OUTSIDE -> {
                Log.e("MotionEvent", "ACTION_OUTSIDE")
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

    private fun getScreenSize(): IntArray {
        var dm = DisplayMetrics()
        dm = resources.displayMetrics
        return intArrayOf(dm.widthPixels, dm.heightPixels)
    }
}