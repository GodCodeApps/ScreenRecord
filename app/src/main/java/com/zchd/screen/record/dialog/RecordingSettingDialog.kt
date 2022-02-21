package com.zchd.screen.record.dialog

import android.content.Context
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import com.zchd.screen.record.FloatDragView
import com.zchd.screen.record.R

/**
 * Copyright (C), 2020-2021, 中传互动（湖北）信息技术有限公司
 * @Author: pym
 * @Date: 2022/2/21:11:22
 * @Description:
 */
internal class RecordingSettingDialog(mContext: Context) : BaseDialog(mContext) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_recording_setting
    }

    override fun isShowAppBackground(): Boolean = true
    override fun initViews() {
        super.initViews()
        findViewById<ImageView>(R.id.iv_close)?.setOnClickListener { dismiss() }
        findViewById<CardView>(R.id.card_reverse_recording)?.setOnClickListener {
            FloatDragView.getInstance(mContext).setMode(1)
            dismiss()
        }
        findViewById<CardView>(R.id.card_normal_recording)?.setOnClickListener {
            FloatDragView.getInstance(mContext).setMode(0)
            dismiss()
        }
        findViewById<LinearLayout>(R.id.ll_setting)?.setOnClickListener { dismiss() }


    }
}