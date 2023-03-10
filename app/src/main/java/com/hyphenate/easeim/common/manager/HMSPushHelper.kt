package com.hyphenate.easeim.common.manager

import android.app.Activity
import android.text.TextUtils
import com.hyphenate.chat.EMClient
import com.hyphenate.util.EMLog
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.common.ApiException
import java.lang.Exception

/**
 * Created by lzan13 on 2018/4/10.
 */
class HMSPushHelper private constructor() {
    /**
     * 申请华为Push Token
     * 1、getToken接口只有在AppGallery Connect平台开通服务后申请token才会返回成功。
     *
     * 2、EMUI10.0及以上版本的华为设备上，getToken接口直接返回token。如果当次调用失败Push会缓存申请，之后会自动重试申请，成功后则以onNewToken接口返回。
     *
     * 3、低于EMUI10.0的华为设备上，getToken接口如果返回为空，确保Push服务开通的情况下，结果后续以onNewToken接口返回。
     *
     * 4、服务端识别token过期后刷新token，以onNewToken接口返回。
     */
    fun getHMSToken(activity: Activity?) {
        // 判断是否启用FCM推送
        if (EMClient.getInstance().isFCMAvailable) {
            return
        }
        try {
            if (Class.forName("com.huawei.hms.api.HuaweiApiClient") != null) {
                val classType = Class.forName("android.os.SystemProperties")
                val getMethod = classType.getDeclaredMethod(
                    "get", *arrayOf<Class<*>>(
                        String::class.java
                    )
                )
                val buildVersion =
                    getMethod.invoke(classType, *arrayOf<Any>("ro.build.version.emui")) as String
                //在某些手机上，invoke方法不报错
                if (!TextUtils.isEmpty(buildVersion)) {
                    EMLog.d("HWHMSPush", "huawei hms push is available!")
                    object : Thread() {
                        override fun run() {
                            try {
                                // read from agconnect-services.json
                                val appId = AGConnectServicesConfig.fromContext(activity)
                                    .getString("client/app_id")

                                // 申请华为推送token
                                val token =
                                    HmsInstanceId.getInstance(activity).getToken(appId, "HCM")
                                EMLog.d("HWHMSPush", "get huawei hms push token:$token")
                                if (token != null && token != "") {
                                    //没有失败回调，假定token失败时token为null
                                    EMLog.d(
                                        "HWHMSPush",
                                        "register huawei hms push token success token:$token"
                                    )
                                    // 上传华为推送token
                                    EMClient.getInstance().sendHMSPushTokenToServer(token)
                                } else {
                                    EMLog.e("HWHMSPush", "register huawei hms push token fail!")
                                }
                            } catch (e: ApiException) {
                                EMLog.e("HWHMSPush", "get huawei hms push token failed, $e")
                            }
                        }
                    }.start()
                } else {
                    EMLog.d("HWHMSPush", "huawei hms push is unavailable!")
                }
            } else {
                EMLog.d("HWHMSPush", "no huawei hms push sdk or mobile is not a huawei phone")
            }
        } catch (e: Exception) {
            EMLog.d("HWHMSPush", "no huawei hms push sdk or mobile is not a huawei phone")
        }
    }

    companion object {
        var instance: HMSPushHelper? = null
            get() {
                if (field == null) {
                    field = HMSPushHelper()
                }
                return field
            }
            private set
    }
}