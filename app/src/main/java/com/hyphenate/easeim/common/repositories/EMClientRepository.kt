package com.hyphenate.easeim.common.repositories

import com.hyphenate.EMCallBack
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMGroup
import com.hyphenate.easeim.AppClient
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.db.DbHelper.Companion.dbHelper
import com.hyphenate.easeim.common.db.entity.EmUserEntity
import com.hyphenate.easeim.common.interfaceOrImplement.DemoEmCallBack
import com.hyphenate.easeim.common.interfaceOrImplement.ResultCallBack
import com.hyphenate.easeim.common.livedatas.LiveDataBus
import com.hyphenate.easeim.common.net.ErrorCode
import com.hyphenate.easeim.common.utils.PreferenceManager
import com.hyphenate.easeim.common.utils.SpDbModel
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.exceptions.HyphenateException
import com.hyphenate.util.EMLog

/**
 * 作为EMClient的repository,处理EMClient相关的逻辑
 */
class EMClientRepository : BaseEMRepository() {
    /**
     * 登录过后需要加载的数据
     * @return
     */
    fun loadAllInfoFromHX(callBack: ResultCallBack<Boolean>) {
        if (isAutoLogin) {
            runOnIOThread {
                if (isLoggedIn) {
                    loadAllConversationsAndGroups()
                    callBack.onSuccess(true)
                } else {
                    callBack.onError(ErrorCode.EM_NOT_LOGIN)
                }
            }
        } else {
            callBack.onError(ErrorCode.EM_NOT_LOGIN)
        }
    }

    /**
     * 从本地数据库加载所有的对话及群组
     */
    private fun loadAllConversationsAndGroups() {
        // 初始化数据库
        initDb()
        // 从本地数据库加载所有的对话及群组
        chatManager.loadAllConversations()
        groupManager.loadAllGroups()
    }

    /**
     * 注册
     * @param userName
     * @param pwd
     * @return
     */
    fun registerToHx(userName: String?, pwd: String?, callBack: EMValueCallBack<String>) {
        //注册之前先判断SDK是否已经初始化，如果没有先进行SDK的初始化
        if (!SdkHelper.instance.isSDKInit) {
            SdkHelper.instance.init(AppClient.instance)
            SpDbModel.instance.setCurrentUserName(userName)
        }
        runOnIOThread {
            try {
                EMClient.getInstance().createAccount(userName, pwd)
                callBack.onSuccess(userName)
            } catch (e: HyphenateException) {
                callBack.onError(e.errorCode, e.message)
            }
        }
    }

    /**
     * 登录到服务器，可选择密码登录或者token登录
     * 登录之前先初始化数据库，如果登录失败，再关闭数据库;如果登录成功，则再次检查是否初始化数据库
     * @param userName
     * @param pwd
     * @param isTokenFlag
     * @return
     */
    fun loginToServer(
        userName: String?,
        pwd: String?,
        isTokenFlag: Boolean,
        callBack: ResultCallBack<EaseUser>
    ) {
        SdkHelper.instance.init(AppClient.instance)
        SpDbModel.instance.setCurrentUserName(userName)
        SpDbModel.instance.currentUserPwd = pwd
        if (isTokenFlag) {
            EMClient.getInstance().loginWithToken(userName, pwd, object : DemoEmCallBack() {
                override fun onSuccess() {
                    val user = getCurrentUser()
                    callBack.onSuccess(user)
                }

                override fun onError(code: Int, error: String) {
                    callBack.onError(code, error)
                    closeDb()
                }
            })
        } else {
            EMClient.getInstance().login(userName, pwd, object : DemoEmCallBack() {
                override fun onSuccess() {
                    val user = getCurrentUser()
                    callBack.onSuccess(user)

                }

                override fun onError(code: Int, error: String) {
                    callBack.onError(code, error)
                    closeDb()
                }
            })
        }
    }

    /**
     * 退出登录
     * @param unbindDeviceToken
     * @return
     */
    fun logout(unbindDeviceToken: Boolean, callBack: ResultCallBack<Boolean>) {
        EMClient.getInstance().logout(unbindDeviceToken, object : EMCallBack {
            override fun onSuccess() {
                SdkHelper.instance.logoutSuccess()
                callBack.onSuccess(true)
            }

            override fun onProgress(progress: Int, status: String) {}
            override fun onError(code: Int, error: String) {
                callBack.onError(code, error)
            }
        })
    }

    /**
     * 设置本地标记，是否自动登录
     * @param autoLogin
     */
    fun setAutoLogin(autoLogin: Boolean) {
        PreferenceManager.getInstance().autoLogin = autoLogin
    }

    private fun getCurrentUser(): EaseUser {
        preLoadData()
        val currentUser = EMClient.getInstance().currentUser
        return EaseUser(currentUser)
    }

    //预先加载一些数据
    private fun preLoadData() {
        // ** manually load all local groups and conversation
        loadAllConversationsAndGroups()
        //从服务器拉取加入的群，防止进入会话页面只显示id
        allJoinGroup()
        // get contacts from server
        contactsFromServer()
        // get current user id
    }

    private fun contactsFromServer() {
        EMContactManagerRepository().getContactList(object :
            ResultCallBack<List<EaseUser>>() {
            override fun onSuccess(value: List<EaseUser>) {
                if (userDao != null) {
                    userDao?.clearUsers()
                    userDao?.insert(EmUserEntity.parseList(value))
                }
            }

            override fun onError(error: Int, errorMsg: String) {}
        })
    }

    //加载完群组信息后，刷新会话列表页面，保证展示群组名称
    private fun allJoinGroup() {
        EMGroupManagerRepository().getAllGroups(object : ResultCallBack<List<EMGroup>>() {
            override fun onSuccess(value: List<EMGroup>) {
                //加载完群组信息后，刷新会话列表页面，保证展示群组名称
                EMLog.i("ChatPresenter", "login isGroupsSyncedWithServer success")
                val event = EaseEvent.create(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP)
                LiveDataBus.get().use(DemoConstant.GROUP_CHANGE).postValue(event)
            }

            override fun onError(error: Int, errorMsg: String) {}
        })
    }

    private fun closeDb() {
        dbHelper().closeDb()
    }
}