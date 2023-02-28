package com.hyphenate.easeim.common.repositories

import com.hyphenate.EMCallBack
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMPushConfigs
import com.hyphenate.chat.EMPushManager.DisplayStyle
import com.hyphenate.chat.EMSilentModeParam
import com.hyphenate.chat.EMSilentModeResult
import com.hyphenate.easeui.manager.EaseThreadManager
import com.hyphenate.exceptions.HyphenateException

class EMPushManagerRepository : BaseEMRepository() {
    /**
     * 获取推送配置
     * @return
     */
    fun pushConfigsFromServer(
        isFromServer: Boolean = true,
        callBack: EMValueCallBack<EMPushConfigs>
    ) {
        if (!isFromServer) {
            pushManager.pushConfigs
        } else {
            EaseThreadManager.getInstance().runOnIOThread {
                val configs: EMPushConfigs?
                try {
                    configs = pushManager.pushConfigsFromServer
                    callBack.onSuccess(configs)
                } catch (e: HyphenateException) {
                    e.printStackTrace()
                    callBack.onError(e.errorCode, e.message)
                }
            }
        }
    }


    /**
     * 获取推送配置
     * @return
     */
    fun fetchPushConfigsFromServer(): EMPushConfigs? {
        try {
            return pushManager.pushConfigsFromServer
        } catch (e: HyphenateException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 设置免打扰时间段
     * 如果end小于start,则end为第二天的hour
     * @param start
     * @param end
     * @return
     */
    fun disableOfflinePush(start: Int, end: Int, callBack: EMValueCallBack<EMSilentModeResult>) {
        EaseThreadManager.getInstance().runOnIOThread {
            try {
//                EMClient.getInstance().pushManager().disableOfflinePush(start, end)
                val param=EMSilentModeParam(EMSilentModeParam.EMSilentModeParamType.REMIND_TYPE)
                EMClient.getInstance().pushManager().setSilentModeForAll(param, callBack)

//                callBack.onSuccess(true)
            } catch (e: HyphenateException) {
                e.printStackTrace()
                callBack.onError(e.errorCode, e.description)
            }
        }
    }

    /**
     * 允许离线推送
     * @return
     */
    fun enableOfflinePush(callBack:EMValueCallBack<EMSilentModeResult>){
        EaseThreadManager.getInstance().runOnIOThread {
            try {
//                EMClient.getInstance().pushManager().enableOfflinePush()
                val param= EMSilentModeParam(EMSilentModeParam.EMSilentModeParamType.REMIND_TYPE)
                EMClient.getInstance().pushManager(). setSilentModeForAll(param, callBack)
            } catch (e: HyphenateException) {
                e.printStackTrace()
                callBack.onError(e.errorCode, e.description)
            }
        }
    }

    /**
     * 更新推送昵称
     * @param nickname
     * @return
     */
    fun updatePushNickname(nickname: String?,callBack: EMValueCallBack<Boolean>){
        runOnIOThread{
            pushManager.asyncUpdatePushNickname(nickname, object : EMCallBack {
                override fun onSuccess() {
                    callBack.onSuccess(true)
                }

                override fun onError(code: Int, error: String) {
                    callBack.onError(code, error)
                }

                override fun onProgress(progress: Int, status: String) {}
            })
        }

    }

    /**
     * 设置推送消息样式
     * @param style
     * @return
     */
    fun updatePushStyle(style: DisplayStyle?,callBack: EMValueCallBack<Boolean>){
        pushManager.asyncUpdatePushDisplayStyle(style, object : EMCallBack {
            override fun onSuccess() {
                callBack.onSuccess(true)
            }

            override fun onError(code: Int, error: String) {
                callBack.onError(code, error)
            }

            override fun onProgress(progress: Int, status: String) {}
        })
    }
}