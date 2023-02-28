package com.hyphenate.easeim.common.receiver

import android.content.Context
import com.hyphenate.push.platform.mi.EMMiMsgReceiver
import com.xiaomi.mipush.sdk.MiPushMessage
import com.hyphenate.util.EMLog
import org.json.JSONObject
import com.hyphenate.easeim.common.utils.PushUtils
import java.lang.Exception

/**
 * 获取有关小米音视频推送消息
 */
class MiMsgReceiver : EMMiMsgReceiver() {
    override fun onNotificationMessageArrived(context: Context?, message: MiPushMessage?) {
        val extStr = message?.content
        try {
            val extras = JSONObject(extStr?:"")
            val result = extras.getJSONObject("e")
            PushUtils.isRtcCall =result.getBoolean("isRtcCall")
            PushUtils.type = result.getInt("callType")
        } catch (e: Exception) {
            e.stackTrace
        }
        super.onNotificationMessageArrived(context, message)
    }

    companion object {
        private const val TAG = "MiMsgReceiver"
    }
}