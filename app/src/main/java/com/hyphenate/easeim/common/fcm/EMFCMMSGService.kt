package com.hyphenate.easeim.common.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.chat.EMClient

/**
 * @author LXJDBXJ
 * @date 2022/10/9
 */
class EMFCMMSGService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (remoteMessage.data.isNotEmpty()) {
            val message = remoteMessage.data["alert"]
            Log.d(TAG, "onMessageReceived: $message")
            SdkHelper.instance.notifier.notify(message)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "onNewToken: $token")
        EMClient.getInstance().sendFCMTokenToServer(token)
    }

    companion object {
        private const val TAG = "EMFCMMSGService"
    }
}