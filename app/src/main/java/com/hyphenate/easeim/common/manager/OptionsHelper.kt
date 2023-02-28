package com.hyphenate.easeim.common.manager

import com.hyphenate.easeim.common.utils.AppMetaDataHelper
import com.hyphenate.easeim.common.entity.ServerSetEntity
import com.hyphenate.easeim.common.utils.PreferenceManager

class OptionsHelper private constructor() {
    var defAppKey = ""
        private set
    private val defaultAppkey: Unit
        get() {
            defAppKey = AppMetaDataHelper.instance().getPlaceholderValue("EASEMOB_APPKEY")
        }

    /**
     * 自定义配置是否可用
     * @return
     */
    val isCustomSetEnable: Boolean
        get() = PreferenceManager.getInstance().isCustomSetEnable

    /**
     * 自定义配置是否可用
     * @param enable
     */
    fun enableCustomSet(enable: Boolean) {
        PreferenceManager.getInstance().enableCustomSet(enable)
    }

    /**
     * 自定义服务器是否可用
     * @return
     */
    val isCustomServerEnable: Boolean
        get() = PreferenceManager.getInstance().isCustomServerEnable

    /**
     * 这是自定义服务器是否可用
     * @param enable
     */
    fun enableCustomServer(enable: Boolean) {
        PreferenceManager.getInstance().enableCustomServer(enable)
    }
    /**
     * 获取闲置服务器
     * @return
     */
    /**
     * 设置闲置服务器
     * @param restServer
     */
    var restServer: String?
        get() = PreferenceManager.getInstance().restServer
        set(restServer) {
            PreferenceManager.getInstance().restServer = restServer
        }

    /**
     * 设置IM服务器
     * @param imServer
     */
    var iMServer: String?
        get() = PreferenceManager.getInstance().imServer
        set(imServer) {
            PreferenceManager.getInstance().imServer = imServer
        }

    /**
     * 设置端口号
     * @param port
     */
    var iMServerPort: Int
        get() = PreferenceManager.getInstance().imServerPort
        set(port) {
            PreferenceManager.getInstance().imServerPort = port
        }

    /**
     * 设置自定义appkey是否可用
     * @param enable
     */
    fun enableCustomAppkey(enable: Boolean) {
        PreferenceManager.getInstance().enableCustomAppkey(enable)
    }

    /**
     * 获取自定义appkey是否可用
     * @return
     */
    val isCustomAppkeyEnabled: Boolean
        get() = PreferenceManager.getInstance().isCustomAppkeyEnabled
    /**
     * 获取自定义appkey
     * @return
     */
    /**
     * 设置自定义appkey
     * @param appkey
     */
    var customAppkey: String?
        get() = PreferenceManager.getInstance().customAppkey
        set(appkey) {
            PreferenceManager.getInstance().customAppkey = appkey
        }
    /**
     * 获取是否只使用Https
     * @return
     */
    /**
     * 设置是否只使用Https
     * @param usingHttpsOnly
     */
    var usingHttpsOnly: Boolean
        get() = PreferenceManager.getInstance().usingHttpsOnly
        set(usingHttpsOnly) {
            PreferenceManager.getInstance().usingHttpsOnly = usingHttpsOnly
        }

    /**
     * 设置是否允许聊天室owner离开并删除会话记录，意味着owner再不会受到任何消息
     * @param value
     */
    fun allowChatroomOwnerLeave(value: Boolean) {
        PreferenceManager.getInstance().settingAllowChatroomOwnerLeave = value
    }

    /**
     * 获取聊天室owner离开时的设置
     * @return
     */
    val isChatroomOwnerLeaveAllowed: Boolean
        get() = PreferenceManager.getInstance().settingAllowChatroomOwnerLeave
    /**
     * 获取退出(主动和被动退出)群组时是否删除聊天消息
     * @return
     */
    /**
     * 设置退出(主动和被动退出)群组时是否删除聊天消息
     * @param value
     */
    var isDeleteMessagesAsExitGroup: Boolean
        get() = PreferenceManager.getInstance().isDeleteMessagesAsExitGroup
        set(value) {
            PreferenceManager.getInstance().isDeleteMessagesAsExitGroup = value
        }
    var isDeleteMessagesAsExitChatRoom: Boolean
        get() = PreferenceManager.getInstance().isDeleteMessagesAsExitChatRoom
        set(value) {
            PreferenceManager.getInstance().isDeleteMessagesAsExitChatRoom = value
        }
    /**
     * 获取是否自动接受加群邀请
     * @return
     */
    /**
     * 设置是否自动接受加群邀请
     * @param value
     */
    var isAutoAcceptGroupInvitation: Boolean
        get() = PreferenceManager.getInstance().isAutoAcceptGroupInvitation
        set(value) {
            PreferenceManager.getInstance().isAutoAcceptGroupInvitation = value
        }

    /**
     * 设置是否自动将消息附件上传到环信服务器，默认为True是使用环信服务器上传下载
     * @param value
     */
    fun setTransfeFileByUser(value: Boolean) {
        PreferenceManager.getInstance().setTransferFileByUser(value)
    }

    /**
     * 获取是否自动将消息附件上传到环信服务器，默认为True是使用环信服务器上传下载
     * @return
     */
    val isSetTransferFileByUser: Boolean
        get() = PreferenceManager.getInstance().isSetTransferFileByUser

    /**
     * 是否自动下载缩略图，默认是true为自动下载
     * @param autodownload
     */
    fun setAutodownloadThumbnail(autodownload: Boolean) {
        PreferenceManager.getInstance().setAudodownloadThumbnail(autodownload)
    }

    /**
     * 获取是否自动下载缩略图
     * @return
     */
    val isSetAutodownloadThumbnail: Boolean
        get() = PreferenceManager.getInstance().isSetAutodownloadThumbnail
    var isSortMessageByServerTime: Boolean
        get() = PreferenceManager.getInstance().isSortMessageByServerTime
        set(sortByServerTime) {
            PreferenceManager.getInstance().isSortMessageByServerTime = sortByServerTime
        }

    /**
     * 获取服务设置
     * @return
     */
    val serverSet: ServerSetEntity
        get() {
            val bean = ServerSetEntity()
            bean.appkey = customAppkey
            bean.isCustomServerEnable = isCustomServerEnable
            bean.isHttpsOnly = usingHttpsOnly
            bean.imServer = iMServer
            bean.restServer = restServer
            return bean
        }

    /**
     * 获取默认服务设置
     * @return
     */
    val defServerSet: ServerSetEntity
        get() {
            val bean = ServerSetEntity()
            bean.appkey = defAppKey
            bean.restServer = defRestServer
            bean.imServer = defImServer
            bean.imPort = defImPort
            bean.isHttpsOnly = usingHttpsOnly
            bean.isCustomServerEnable = isCustomServerEnable
            return bean
        }

    companion object {
        val defImServer = "msync-im1.sandbox.easemob.com"
        val defImPort = 6717
        val defRestServer = "a1.sdb.easemob.com"
        private var instance: OptionsHelper? = null

        fun get(): OptionsHelper {
            if (instance == null) {
                synchronized(OptionsHelper::class.java) {
                    if (instance == null) {
                        instance = OptionsHelper()
                    }
                }
            }
            return instance!!
        }
    }

    init {
        defaultAppkey
    }
}