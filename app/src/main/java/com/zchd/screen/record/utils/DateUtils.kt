package com.zchd.screen.record.utils

/**
 * Copyright (C), 2020-2021, 中传互动（湖北）信息技术有限公司
 * @Author: pym
 * @Date: 2022/2/17:17:02
 * @Description:
 */
object DateUtils {
    fun getSurplusMS(time: Long): String {
        val minute = (time / 1000 / 60 % 60).toInt()
        val second = (time / 1000 % 60).toInt()
        return "${if (minute < 10) "0${minute}" else minute}:${if (second < 10) "0${second}" else second}"
    }
}