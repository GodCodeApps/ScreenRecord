package com.zchd.screen.record.utils

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import com.zchd.screen.record.service.RecorderService

/**
 * Copyright (C), 2020-2021, 中传互动（湖北）信息技术有限公司
 * @Author: pym
 * @Date: 2022/2/16:15:50
 * @Description:
 */
class RecorderServiceProxy : ServiceConnection {
    var recorderBinder: RecorderService.RecorderBinder? = null
    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        recorderBinder = (p1 as RecorderService.RecorderBinder)

    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        recorderBinder = null
    }
}