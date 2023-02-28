package com.hyphenate.easeim.common.manager

import android.content.Context
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMClient
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.SdkHelper.DataSyncListener
import com.hyphenate.easeim.common.manager.ParseManager.Companion.instance
import com.hyphenate.easeim.common.utils.PreferenceManager
import com.hyphenate.easeui.domain.EaseUser

class UserProfileManager {
    /**
     * application context
     */
    var appContext: Context? = null

    /**
     * init flag: test if the sdk has been inited before, we don't need to init
     * again
     */
    private var sdkInited = false

    /**
     * HuanXin sync contact nick and avatar listener
     */
    private var syncContactInfosListeners: MutableList<DataSyncListener>? = null
    var isSyncingContactInfoWithServer = false
        private set
    private var currentUser: EaseUser? = null

    @Synchronized
    fun init(context: Context?): Boolean {
        if (sdkInited) {
            return true
        }
        instance.onInit(context!!)
        syncContactInfosListeners = ArrayList()
        sdkInited = true
        return true
    }

    fun addSyncContactInfoListener(listener: DataSyncListener?) {
        if (listener == null) {
            return
        }
        if (!syncContactInfosListeners!!.contains(listener)) {
            syncContactInfosListeners?.add(listener)
        }
    }

    fun removeSyncContactInfoListener(listener: DataSyncListener?) {
        if (listener == null) {
            return
        }
        syncContactInfosListeners?.remove(listener)
    }

    fun asyncFetchContactInfosFromServer(
        usernames: List<String>,
        callback: EMValueCallBack<List<EaseUser?>?>?
    ) {
        if (isSyncingContactInfoWithServer) {
            return
        }
        isSyncingContactInfoWithServer = true
        instance.getContactInfos(usernames, object : EMValueCallBack<List<EaseUser>> {
            override fun onSuccess(value: List<EaseUser>?) {
                isSyncingContactInfoWithServer = false
                // in case that logout already before server returns,we should
                // return immediately
                if (!SdkHelper.instance.isLoggedIn) {
                    return
                }
                callback?.onSuccess(value)
            }

            override fun onError(error: Int, errorMsg: String) {
                isSyncingContactInfoWithServer = false
                callback?.onError(error, errorMsg)
            }
        })
    }

    fun notifyContactInfosSyncListener(success: Boolean) {
        for (listener in syncContactInfosListeners!!) {
            listener.onSyncComplete(success)
        }
    }

    @Synchronized
    fun reset() {
        isSyncingContactInfoWithServer = false
        currentUser = null
        PreferenceManager.getInstance().removeCurrentUserInfo()
    }

    @get:Synchronized
    val currentUserInfo: EaseUser
        get() {
            if (currentUser == null) {
                val username = EMClient.getInstance().currentUser
                currentUser = EaseUser(username)
                val nick = currentUserNick
                currentUser!!.nickname = nick ?: username
                currentUser!!.avatar = currentUserAvatar
            }
            return currentUser!!
        }

    fun updateCurrentUserNickName(nickname: String?): Boolean {
        val isSuccess = instance.updateParseNickName(nickname)
        if (isSuccess) {
            currentUserNick = nickname
        }
        return isSuccess
    }

    fun uploadUserAvatar(data: ByteArray?): String? {
        val avatarUrl = instance.uploadParseAvatar(data)
        if (avatarUrl != null) {
            currentUserAvatar = avatarUrl
        }
        return avatarUrl
    }

    fun updateUserAvatar(avatarUrl: String?): String? {
        if (avatarUrl != null) {
            currentUserAvatar = avatarUrl
        }
        return avatarUrl
    }

    fun asyncGetCurrentUserInfo() {
        instance.asyncGetCurrentUserInfo(object : EMValueCallBack<EaseUser?> {
            override fun onSuccess(value: EaseUser?) {
                if (value != null) {
                    currentUserNick = value.nickname
                    currentUserAvatar = value.avatar
                }
            }

            override fun onError(error: Int, errorMsg: String) {}
        })
    }

    fun asyncGetUserInfo(username: String?, callback: EMValueCallBack<EaseUser?>?) {
        instance.asyncGetUserInfo(username, callback)
    }

    private var currentUserNick: String?
        get() = PreferenceManager.getInstance().currentUserNick
        private set(nickname) {
            currentUserInfo.nickname = nickname
            PreferenceManager.getInstance().currentUserNick = nickname
        }
    private var currentUserAvatar: String?
        get() = PreferenceManager.getInstance().currentUserAvatar
        private set(avatar) {
            currentUserInfo.avatar = avatar
            PreferenceManager.getInstance().currentUserAvatar = avatar
        }
}