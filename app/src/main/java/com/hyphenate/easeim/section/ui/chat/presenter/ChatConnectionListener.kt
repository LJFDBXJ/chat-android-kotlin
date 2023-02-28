package com.hyphenate.easeim.section.ui.chat.presenter

import com.hyphenate.EMConnectionListener
import com.hyphenate.EMError
import com.hyphenate.chat.EMGroup
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.db.DbHelper
import com.hyphenate.easeim.common.db.entity.EmUserEntity
import com.hyphenate.easeim.common.interfaceOrImplement.ResultCallBack
import com.hyphenate.easeim.common.livedatas.LiveDataBus
import com.hyphenate.easeim.common.repositories.EMContactManagerRepository
import com.hyphenate.easeim.common.repositories.EMGroupManagerRepository
import com.hyphenate.easeim.common.repositories.EMPushManagerRepository
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.model.EaseEvent

class ChatConnectionListener(
    private var isGroupsSyncedWithServer: Boolean,
    private var isContactsSyncedWithServer: Boolean,
    private var isBlackListSyncedWithServer: Boolean,
    private var isPushConfigsWithServer: Boolean,
) : EMConnectionListener {
    override fun onConnected() {
        if (!SdkHelper.instance.isLoggedIn) {
            return
        }
        if (!isGroupsSyncedWithServer) {
            EMGroupManagerRepository().getAllGroups(
                callBack = object : ResultCallBack<List<EMGroup>>() {
                    override fun onSuccess(value: List<EMGroup>) {
                        //加载完群组信息后，刷新会话列表页面，保证展示群组名称
                        val event =
                            EaseEvent.create(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP)
                        LiveDataBus.get().use(DemoConstant.GROUP_CHANGE).postValue(event)
                    }

                    override fun onError(error: Int, errorMsg: String) {}
                })
            isGroupsSyncedWithServer = true
        }
        if (!isContactsSyncedWithServer) {
            EMContactManagerRepository().getContactList(
                callBack = object : ResultCallBack<List<EaseUser>>() {
                    override fun onSuccess(value: List<EaseUser>) {
                        val userDao = DbHelper.dbHelper().userDao
                        if (userDao != null) {
                            userDao.clearUsers()
                            userDao.insert(EmUserEntity.parseList(value))
                        }
                    }

                    override fun onError(error: Int, errorMsg: String) {}
                })
            isContactsSyncedWithServer = true
        }
        if (!isBlackListSyncedWithServer) {
            EMContactManagerRepository().getBlackUserList(null)
            isBlackListSyncedWithServer = true
        }
        if (!isPushConfigsWithServer) {
            //首先获取push配置，否则获取push配置项会为空
            EMPushManagerRepository().fetchPushConfigsFromServer()
            isPushConfigsWithServer = true
        }
    }

    /**
     * 用来监听账号异常
     * @param error
     */
    override fun onDisconnected(error: Int) {
        var event: String? = null
        when (error) {
            EMError.USER_REMOVED -> {
                event = DemoConstant.ACCOUNT_REMOVED
            }
            EMError.USER_LOGIN_ANOTHER_DEVICE,
            EMError.USER_BIND_ANOTHER_DEVICE,
            EMError.USER_DEVICE_CHANGED,
            EMError.USER_LOGIN_TOO_MANY_DEVICES -> {
                event = DemoConstant.ACCOUNT_CONFLICT
            }
            EMError.SERVER_SERVICE_RESTRICTED -> {
                event = DemoConstant.ACCOUNT_FORBIDDEN
            }
            EMError.USER_KICKED_BY_CHANGE_PASSWORD -> {
                event = DemoConstant.ACCOUNT_KICKED_BY_CHANGE_PASSWORD
            }
            EMError.USER_KICKED_BY_OTHER_DEVICE -> {
                event = DemoConstant.ACCOUNT_KICKED_BY_OTHER_DEVICE
            }
        }
        if (!event.isNullOrEmpty()) {
            LiveDataBus.get().use(DemoConstant.ACCOUNT_CHANGE)
                .postValue(EaseEvent(event, EaseEvent.TYPE.ACCOUNT))
        }
    }
}
