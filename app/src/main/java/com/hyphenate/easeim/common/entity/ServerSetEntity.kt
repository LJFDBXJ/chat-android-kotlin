package com.hyphenate.easeim.common.entity

import java.lang.NumberFormatException

/**
 * 服务器设置model
 */
class ServerSetEntity {
    var appkey: String? = null
    var imServer: String? = null
        set(value) {
            field = value
            val result=field
            if (result.isNullOrEmpty())
                return
            if (result.contains(":")) {
                try {
                    field = value!!.split(":".toRegex()).toTypedArray()[0]
                    imPort = Integer.valueOf(result)
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }
            }
        }
    var imPort = 0
    var restServer: String? = null
    var isCustomServerEnable = false//是否使用自定义服务器
    var isHttpsOnly = false//是否只使用https

}