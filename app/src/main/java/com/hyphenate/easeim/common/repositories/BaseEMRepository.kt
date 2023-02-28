package com.hyphenate.easeim.common.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hyphenate.chat.*
import com.hyphenate.easeim.AppClient.Companion.instance
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.common.db.DbHelper
import com.hyphenate.easeim.common.db.dao.EmUserDao
import com.hyphenate.easeim.common.db.dao.InviteMessageDao
import com.hyphenate.easeim.common.db.dao.MsgTypeManageDao
import com.hyphenate.easeui.manager.EaseThreadManager

open class BaseEMRepository {

    /**
     * login before
     * @return
     */
    val isLoggedIn: Boolean
        get() = EMClient.getInstance().isLoggedInBefore

    /**
     * 获取本地标记，是否自动登录
     * @return
     */
    val isAutoLogin: Boolean
        get() = SdkHelper.instance.autoLogin

    /**
     * 获取当前用户
     * @return
     */
    val currentUser: String
        get() = SdkHelper.instance.currentUser

    /**
     * EMChatManager
     * @return
     */
    val chatManager: EMChatManager
        get() = SdkHelper.instance.eMClient.chatManager()

    /**
     * EMContactManager
     * @return
     */
    val contactManager: EMContactManager
        get() = SdkHelper.instance.contactManager

    /**
     * EMGroupManager
     * @return
     */
    val groupManager: EMGroupManager
        get() = SdkHelper.instance.eMClient.groupManager()

    /**
     * EMChatRoomManager
     * @return
     */
    val chatRoomManager: EMChatRoomManager
        get() = SdkHelper.instance.chatroomManager

    /**
     * EMPushManager
     * @return
     */
    val pushManager: EMPushManager
        get() = SdkHelper.instance.pushManager

    /**
     * init room
     */
    fun initDb() {
        DbHelper.dbHelper().initDb(currentUser)
    }

    /**
     * EmUserDao
     * @return
     */
    val userDao: EmUserDao?
        get() = DbHelper.dbHelper().userDao

    /**
     * get MsgTypeManageDao
     * @return
     */
    val msgTypeManageDao: MsgTypeManageDao?
        get() = DbHelper.dbHelper().msgTypeManageDao

    /**
     * get invite message dao
     * @return
     */
    val inviteMessageDao: InviteMessageDao?
        get() = DbHelper.dbHelper().inviteMessageDao

    /**
     * 在主线程执行
     * @param runnable
     */
    fun runOnMainThread(runnable: Runnable?) {
        EaseThreadManager.getInstance().runOnMainThread(runnable)
    }

    /**
     * 在异步线程
     * @param runnable
     */
    fun runOnIOThread(runnable: Runnable?) {
        EaseThreadManager.getInstance().runOnIOThread(runnable)
    }

}