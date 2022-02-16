package com.zchd.screen.record

import android.app.Application

/**
 * Copyright (C), 2020-2021, 中传互动（湖北）信息技术有限公司
 * @Author: pym
 * @Date: 2022/2/15:16:06
 * @Description:
 */
class App : Application() {
    companion object{
        var instance: App? = null
    }
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}