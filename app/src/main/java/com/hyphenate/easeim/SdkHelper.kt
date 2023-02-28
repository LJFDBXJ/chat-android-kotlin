package com.hyphenate.easeim

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.AsyncTask
import android.os.Process
import android.text.TextUtils
import android.util.Log
import android.util.Pair
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailabilityLight
import com.heytap.msp.push.HeytapPushManager
import com.hyphenate.EMCallBack
import com.hyphenate.chat.*
import com.hyphenate.chat.EMConversation.EMConversationType
import com.hyphenate.cloud.EMHttpClient
import com.hyphenate.easecallkit.EaseCallKit
import com.hyphenate.easecallkit.EaseCallKit.EaseCallError
import com.hyphenate.easecallkit.base.*
import com.hyphenate.easecallkit.event.CallCancelEvent
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.db.DbHelper
import com.hyphenate.easeim.common.livedatas.LiveDataBus
import com.hyphenate.easeim.common.manager.UserProfileManager
import com.hyphenate.easeim.common.utils.SpDbModel
import com.hyphenate.easeim.common.utils.EmoIconExampleGroupData.emoData
import com.hyphenate.easeim.common.receiver.HeadsetReceiver
import com.hyphenate.easeim.common.utils.FetchUserInfoList
import com.hyphenate.easeim.common.utils.FetchUserRunnable
import com.hyphenate.easeim.common.utils.PreferenceManager
import com.hyphenate.easeim.section.av.MultipleVideoActivity
import com.hyphenate.easeim.section.av.VideoCallActivity
import com.hyphenate.easeim.section.ui.chat.ChatPresenter
import com.hyphenate.easeim.section.ui.chat.delegates.*
import com.hyphenate.easeim.section.conference.ConferenceInviteActivity
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.delegate.*
import com.hyphenate.easeui.domain.EaseAvatarOptions
import com.hyphenate.easeui.domain.EaseEmojicon
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.manager.EaseMessageTypeSetManager
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.easeui.model.EaseNotifier
import com.hyphenate.easeui.provider.EaseEmojiconInfoProvider
import com.hyphenate.easeui.provider.EaseSettingsProvider
import com.hyphenate.easeui.provider.EaseUserProfileProvider
import com.hyphenate.exceptions.HyphenateException
import com.hyphenate.push.EMPushConfig
import com.hyphenate.push.EMPushHelper
import com.hyphenate.push.EMPushType
import com.hyphenate.push.PushListener
import com.hyphenate.util.EMLog
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

/**
 * 作为hyphenate-sdk的入口控制类，获取sdk下的基础类均通过此类
 */
/**
 * 作为hyphenate-sdk的入口控制类，获取sdk下的基础类均通过此类
 */
class SdkHelper private constructor() {
    /**
     * 设置SDK是否初始化
     * @param init
     */
    var isSDKInit = false//SDK是否初始化
    private var contactList: Map<String, EaseUser>? = null
    val userProfileManager by lazy { UserProfileManager() }
    private var callKitListener: EaseCallKitListener? = null
    private val tokenUrl = "http://a1.easemob.com/token/rtcToken/v1"
    private val uIdUrl = "http://a1.easemob.com/channel/mapper"
    private var fetchUserRunnable: FetchUserRunnable? = null
    private var fetchUserTread: Thread? = null
    private var fetchUserInfoList: FetchUserInfoList? = null
    fun init(context: Context) {
        //初始化IM SDK
        if (initSDK(context)) {
            // debug mode, you'd better set it to false, if you want release your App officially.
            EMClient.getInstance().setDebugMode(true)
            // set Call options
            setCallHeadsetReceive(context)
            //初始化推送
            initPush(context)
            //注册call Receiver
            //initReceiver(context);
            //初始化ease ui相关
            initEaseUI()
            //注册对话类型
            registerConversationType()

            //callKit初始化
            initCallKit(context)

            //启动获取用户信息线程
            fetchUserInfoList = FetchUserInfoList.instance
            fetchUserRunnable = FetchUserRunnable()
            fetchUserTread = Thread(fetchUserRunnable)
            fetchUserTread?.start()
        }
    }

    /**
     * callKit初始化
     * @param context
     */
    private fun initCallKit(context: Context) {

        val callKitConfig = EaseCallKitConfig()
        //设置呼叫超时时间
        callKitConfig.callTimeOut = (30 * 1000).toLong()
        //设置声网AgoraAppId
        callKitConfig.agoraAppId = "15cb0d28b87b425ea613fc46f7c9f974"
        callKitConfig.isEnableRTCToken = true
        EaseCallKit.getInstance().init(context, callKitConfig)
        // Register the activities which you have registered in manifest
        EaseCallKit.getInstance().registerVideoCallClass(VideoCallActivity::class.java)
        EaseCallKit.getInstance().registerMultipleVideoClass(MultipleVideoActivity::class.java)
        addCallkitListener(context)
    }

    /**
     * 初始化SDK
     * @param context
     * @return
     */
    private fun initSDK(context: Context): Boolean {
        // 根据项目需求对SDK进行配置
        val options = initChatOptions(context)
        //配置自定义的rest server和im server
//        options.setRestServer("a1-hsb.easemob.com");
//        options.setIMServer("106.75.100.247");
//        options.setImPort(6717);

//        options.setRestServer("a41.easemob.com");
//        options.setIMServer("msync-im-41-tls-test.easemob.com");
//        options.setImPort(6717);

        // 初始化SDK
        isSDKInit = EaseIM.getInstance().init(context, options)
        //设置删除用户属性数据超时时间
        SpDbModel.instance.userInfoTimeOut = (30 * 60 * 1000).toLong()
        //更新过期用户属性列表
        updateTimeoutUsers()
        return isSDKInit
    }

    /**
     * 注册对话类型
     */
    private fun registerConversationType() {
        EaseMessageTypeSetManager.getInstance()
            .addMessageType(EaseExpressionAdapterDelegate::class.java) //自定义表情
            .addMessageType(EaseFileAdapterDelegate::class.java) //文件
            .addMessageType(EaseImageAdapterDelegate::class.java) //图片
            .addMessageType(EaseLocationAdapterDelegate::class.java) //定位
            .addMessageType(EaseVideoAdapterDelegate::class.java) //视频
            .addMessageType(EaseVoiceAdapterDelegate::class.java) //声音
            .addMessageType(ChatConferenceInviteAdapterDelegate::class.java) //语音邀请
            .addMessageType(ChatRecallAdapterDelegate::class.java) //消息撤回
            .addMessageType(ChatVideoCallAdapterDelegate::class.java) //视频通话
            .addMessageType(ChatVoiceCallAdapterDelegate::class.java) //语音通话
            .addMessageType(ChatUserCardAdapterDelegate::class.java) //名片消息
            .addMessageType(EaseCustomAdapterDelegate::class.java) //自定义消息
            .addMessageType(ChatNotificationAdapterDelegate::class.java) //入群等通知消息
            .setDefaultMessageType(EaseTextAdapterDelegate::class.java) //文本
    }

    /**
     * 判断是否之前登录过
     * @return
     */
    val isLoggedIn: Boolean
        get() = eMClient.isLoggedInBefore

    /**
     * 获取IM SDK的入口类
     * @return
     */
    val eMClient: EMClient
        get() = EMClient.getInstance()

    /**
     * 获取contact manager
     * @return
     */
    val contactManager: EMContactManager
        get() = eMClient.contactManager()

    /**
     * 获取group manager
     * @return
     */
    val groupManager: EMGroupManager
        get() = eMClient.groupManager()

    /**
     * 获取chatroom manager
     * @return
     */
    val chatroomManager: EMChatRoomManager
        get() = eMClient.chatroomManager()

    /**
     * get EMChatManager
     * @return
     */
    val chatManager: EMChatManager
        get() = eMClient.chatManager()

    /**
     * get push manager
     * @return
     */
    val pushManager: EMPushManager
        get() = eMClient.pushManager()

    /**
     * get conversation
     * @param username
     * @param type
     * @param createIfNotExists
     * @return
     */
    fun getConversation(
        username: String?,
        type: EMConversationType?,
        createIfNotExists: Boolean
    ): EMConversation {
        return chatManager.getConversation(username, type, createIfNotExists)
    }

    val currentUser: String
        get() = eMClient.currentUser

    /**
     * ChatPresenter中添加了网络连接状态监听，多端登录监听，群组监听，联系人监听，聊天室监听
     * @param context
     */
    private fun initEaseUI() {
        //添加ChatPresenter,ChatPresenter中添加了网络连接状态监听，
        EaseIM.getInstance().addChatPresenter(ChatPresenter.getInstance())
        EaseIM.getInstance()
            .setSettingsProvider(object : EaseSettingsProvider {
                override fun isMsgNotifyAllowed(message: EMMessage): Boolean {
                    return if (!SpDbModel.instance.settingMsgNotification) {
                        false
                    } else {
                        var chatUseName: String? = null
                        var notNotifyIds: List<String>? = null
                        // get user or group id which was blocked to show message notifications
                        if (message.chatType == EMMessage.ChatType.Chat) {
                            chatUseName = message.from
                            notNotifyIds = SpDbModel.instance.getDisabledIds()
                        } else {
                            chatUseName = message.to
                            notNotifyIds = SpDbModel.instance.disabledGroups
                        }
                        notNotifyIds == null || !notNotifyIds.contains(chatUseName)
                    }
                }

                override fun isMsgSoundAllowed(message: EMMessage): Boolean {
                    return SpDbModel.instance.settingMsgSound
                }

                override fun isMsgVibrateAllowed(message: EMMessage): Boolean {
                    return SpDbModel.instance.settingMsgVibrate
                }

                override fun isSpeakerOpened(): Boolean {
                    return SpDbModel.instance.settingMsgSpeaker
                }
            })
            .setEmojiconInfoProvider(object : EaseEmojiconInfoProvider {
                override fun getEmojiconInfo(emojiconIdentityCode: String): EaseEmojicon? {
                    for (emojicon in emoData.emojiconList) {
                        if (emojicon.identityCode == emojiconIdentityCode) {
                            return emojicon
                        }
                    }
                    return null
                }

                override fun getTextEmojiconMapping(): Map<String, Any>? {
                    return null
                }
            })
            .setAvatarOptions(avatarOptions).userProvider =
            EaseUserProfileProvider { username -> getUserInfo(username) }
    }

    //Translation Manager 初始化
    fun initTranslationManager() {
        val params = EMTranslateParams(
            "46c34219512d4f09ae6f8e04c083b7a3",
            "https://api.cognitive.microsofttranslator.com",
            500
        )
        EMClient.getInstance().translationManager().init(params)
    }

    /**
     * 统一配置头像
     * @return
     */
    private val avatarOptions: EaseAvatarOptions
        get() {
            val avatarOptions = EaseAvatarOptions()
            avatarOptions.avatarShape = 1
            return avatarOptions
        }

    fun getUserInfo(username: String): EaseUser? {
        // To get instance of EaseUser, here we get it from the user list in memory
        // You'd better cache it if you get it from your server
        var user: EaseUser? = null
        if (username == EMClient.getInstance().currentUser) return userProfileManager.currentUserInfo
        user = getContactList()[username]
        if (user == null) {
            //找不到更新会话列表 继续查找
            updateContactList()
            user = getContactList()[username]
            //如果还找不到从服务端异步拉取 然后通知UI刷新列表
            if (user == null) {
                fetchUserInfoList?.addUserId(username)
            }
        }
        return user
    }

    /**
     * 根据自己的需要进行配置
     * @param context
     * @return
     */
    private fun initChatOptions(context: Context): EMOptions {
        Log.d(TAG, "init HuanXin Options")
        val options = EMOptions()
        // 设置是否自动接受加好友邀请,默认是true
        options.acceptInvitationAlways = false
        // 设置是否需要接受方已读确认
        options.requireAck = true
        // 设置是否需要接受方送达确认,默认false
        options.requireDeliveryAck = false
        //设置fpa开关，默认false
        options.fpaEnable = true
        /**
         * NOTE:你需要设置自己申请的账号来使用三方推送功能，详见集成文档
         */
        val builder = EMPushConfig.Builder(context)
        builder.enableVivoPush() // 需要在AndroidManifest.xml中配置appId和appKey
            .enableMeiZuPush("134952", "f00e7e8499a549e09731a60a4da399e3")
            .enableMiPush("2882303761517426801", "5381742660801")
            .enableOppoPush(
                "0bb597c5e9234f3ab9f821adbeceecdb",
                "cd93056d03e1418eaa6c3faf10fd7537"
            )
            .enableHWPush() // 需要在AndroidManifest.xml中配置appId
            .enableFCM("782795210914")
        options.pushConfig = builder.build()

        //set custom servers, commonly used in private deployment
        if (SpDbModel.instance.isCustomSetEnable()) {
            if (SpDbModel.instance.isCustomServerEnable() && SpDbModel.instance.getRestServer() != null && SpDbModel.instance.getIMServer() != null) {
                // 设置rest server地址
                options.restServer = SpDbModel.instance.getRestServer()
                // 设置im server地址
                options.setIMServer(SpDbModel.instance.getIMServer())
                //如果im server地址中包含端口号
                if (SpDbModel.instance.getIMServer()!!.contains(":")) {
                    options.setIMServer(
                        SpDbModel.instance.getIMServer()!!.split(":".toRegex()).toTypedArray()[0]
                    )
                    // 设置im server 端口号，默认443
                    options.imPort = Integer.valueOf(
                        SpDbModel.instance.getIMServer()!!.split(":".toRegex()).toTypedArray()[1]
                    )
                } else {
                    //如果不包含端口号
                    if (SpDbModel.instance.getIMServerPort() != 0) {
                        options.imPort = SpDbModel.instance.getIMServerPort()
                    }
                }
            }
        }
        if (SpDbModel.instance.isCustomAppKeyEnabled() && !TextUtils.isEmpty(SpDbModel.instance.getCustomAppkey())) {
            // 设置appkey
            options.appKey = SpDbModel.instance.getCustomAppkey()
        }
        val imServer = options.imServer
        val restServer = options.restServer

        // 设置是否允许聊天室owner离开并删除会话记录，意味着owner再不会受到任何消息
        options.allowChatroomOwnerLeave(SpDbModel.instance.isChatroomOwnerLeaveAllowed())
        // 设置退出(主动和被动退出)群组时是否删除聊天消息
        options.isDeleteMessagesAsExitGroup = SpDbModel.instance.isDeleteMessagesAsExitGroup()
        // 设置是否自动接受加群邀请
        options.isAutoAcceptGroupInvitation = SpDbModel.instance.isAutoAcceptGroupInvitation()
        // 是否自动将消息附件上传到环信服务器，默认为True是使用环信服务器上传下载
        options.autoTransferMessageAttachments = SpDbModel.instance.isSetTransferFileByUser()
        // 是否自动下载缩略图，默认是true为自动下载
        options.setAutoDownloadThumbnail(SpDbModel.instance.isSetAutodownloadThumbnail())
        return options
    }

    private fun setCallHeadsetReceive(context: Context) {
        val headsetReceiver = HeadsetReceiver()
        val headsetFilter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        context.registerReceiver(headsetReceiver, headsetFilter)
    }

    fun initPush(context: Context?) {
        if (EaseIM.getInstance().isMainProcess(context)) {
            //OPPO SDK升级到2.1.0后需要进行初始化
            HeytapPushManager.init(context, true)
            //HMSPushHelper.getInstance().initHMSAgent(DemoApplication.getInstance());
            EMPushHelper.getInstance().setPushListener(object : PushListener() {
                override fun onError(pushType: EMPushType, errorCode: Long) {
                    // TODO: 返回的errorCode仅9xx为环信内部错误，可从EMError中查询，其他错误请根据pushType去相应第三方推送网站查询。
                    EMLog.e("PushClient", "Push client occur a error: $pushType - $errorCode")
                }

                override fun isSupportPush(
                    pushType: EMPushType,
                    pushConfig: EMPushConfig
                ): Boolean {
                    // 由外部实现代码判断设备是否支持FCM推送
                    if (pushType == EMPushType.FCM) {
                        EMLog.d(
                            "FCM",
                            "GooglePlayServiceCode:" + GoogleApiAvailabilityLight.getInstance()
                                .isGooglePlayServicesAvailable(context)
                        )
                        return SpDbModel.instance.isUseFCM() && GoogleApiAvailabilityLight.getInstance()
                            .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
                    }
                    return super.isSupportPush(pushType, pushConfig)
                }
            })
        }
    }

    /**
     * logout
     *
     * @param unbindDeviceToken
     * whether you need unbind your device token
     * @param callback
     * callback
     */
    fun logOut(unbindDeviceToken: Boolean, callback: EMCallBack?) {
        if (fetchUserTread != null && fetchUserRunnable != null) {
            fetchUserRunnable?.setStop(true)
        }
        val cancelEvent = CallCancelEvent()
        EaseCallKit.getInstance()
            .sendCmdMsg(cancelEvent, EaseCallKit.getInstance().fromUserId, object : EMCallBack {
                override fun onSuccess() {
                    EMClient.getInstance().logout(unbindDeviceToken, object : EMCallBack {
                        override fun onSuccess() {
                            logoutSuccess()
                            //reset();
                            callback?.onSuccess()
                        }

                        override fun onProgress(progress: Int, status: String) {
                            callback?.onProgress(progress, status)
                        }

                        override fun onError(code: Int, error: String) {
                            Log.d(TAG, "logout: onSuccess")
                            //reset();
                            callback?.onError(code, error)
                        }
                    })
                }

                override fun onError(code: Int, error: String) {
                    EMClient.getInstance().logout(unbindDeviceToken, object : EMCallBack {
                        override fun onSuccess() {
                            logoutSuccess()
                            //reset();
                            callback?.onSuccess()
                        }

                        override fun onProgress(progress: Int, status: String) {
                            callback?.onProgress(progress, status)
                        }

                        override fun onError(code: Int, error: String) {
                            Log.d(TAG, "logout: onSuccess")
                            //reset();
                            callback?.onError(code, error)
                        }
                    })
                }

                override fun onProgress(progress: Int, status: String) {}
            })
    }

    /**
     * 关闭当前进程
     */
    fun killApp() {
        val activities = AppClient.instance.lifecycleCallbacks.activityList
        if (activities.isNotEmpty()) {
            for (activity in activities) {
                activity.finish()
            }
        }
        Process.killProcess(Process.myPid())
        exitProcess(0)
    }

    /**
     * 退出登录后，需要处理的业务逻辑
     */
    fun logoutSuccess() {
        autoLogin = false
        DbHelper.dbHelper().closeDb()
        userProfileManager.reset()
        EMClient.getInstance().translationManager().logout()
    }

    val easeAvatarOptions: EaseAvatarOptions
        get() = EaseIM.getInstance().avatarOptions


    /**
     * get instance of EaseNotifier
     * @return
     */
    val notifier: EaseNotifier
        get() = EaseIM.getInstance().notifier

    /**
     * 设置本地标记，是否自动登录
     * @param autoLogin
     */
    var autoLogin: Boolean
        get() = PreferenceManager.getInstance().autoLogin
        set(autoLogin) {
            PreferenceManager.getInstance().autoLogin = autoLogin
        }

    /**
     * 向数据库中插入数据
     * @param objectData
     */
    fun insert(objectData: Any) {
        SpDbModel.instance.insert(objectData)
    }

    /**
     * update
     * @param objectData
     */
    fun update(objectData: Any?) {
        SpDbModel.instance.update(objectData)
    }

    /**
     * update user list
     * @param users
     */
    fun updateUserList(users: List<EaseUser>) {
        SpDbModel.instance.updateContactList(users)
    }

    /**
     * 更新过期的用户属性数据
     */
    fun updateTimeoutUsers() {
        val userIds = SpDbModel.instance.selectTimeOutUsers()
        if (!userIds.isNullOrEmpty()) {
            if (fetchUserInfoList != null) {
                userIds.forEach {
                    fetchUserInfoList?.addUserId(it)
                }
            }
        }
    }

    /**
     * get contact list
     *
     * @return
     */
    fun getContactList(): Map<String, EaseUser> {
        if (isLoggedIn && contactList == null) {
            updateTimeoutUsers()
            contactList = SpDbModel.instance.allUserList
        }

        // return a empty non-null object to avoid app crash
        return if (contactList == null) {
            Hashtable()
        } else contactList!!
    }

    /**
     * update contact list
     */
    fun updateContactList() {
        if (isLoggedIn) {
            updateTimeoutUsers()
            contactList = SpDbModel.instance.contactList
        }
    }


    /**
     * 展示通知设置页面
     */
    fun showNotificationPermissionDialog(context: Context) {
        val pushType = EMPushHelper.getInstance().pushType
        // oppo
        if (pushType == EMPushType.OPPOPUSH && HeytapPushManager.isSupportPush(context)) {
            HeytapPushManager.requestNotificationPermission()
        }
    }

    /**
     * 删除联系人
     * @param username
     * @return
     */
    @Synchronized
    fun deleteContact(username: String): Int {
        if (username.isEmpty()) {
            return 0
        }
        val dbHelper = DbHelper.dbHelper()
        if (dbHelper.userDao == null) {
            return 0
        }
        val num = dbHelper.userDao?.deleteUser(arg0 = username) ?: 0
        dbHelper.inviteMessageDao?.deleteByFrom(from = username)
        EMClient.getInstance().chatManager().deleteConversation(username, false)
        SpDbModel.instance.deleteUsername(username = username, false)
        Log.e(TAG, "delete num = $num")
        return num
    }

    /**
     * 检查是否是第一次安装登录
     * 默认值是true, 需要在用api拉取完会话列表后，就其置为false.
     * @return
     */
    val isFirstInstall: Boolean
        get() = SpDbModel.instance.isFirstInstall()

    /**
     * 将状态置为非第一次安装，在调用获取会话列表的api后调用
     * 并将会话列表是否来自服务器置为true
     */
    fun makeNotFirstInstall() {
        SpDbModel.instance.makeNotFirstInstall()
    }

    /**
     * 检查会话列表是否从服务器返回数据
     * @return
     */
    val isConComeFromServer: Boolean
        get() = SpDbModel.instance.isConComeFromServer()

    /**
     * Determine if it is from the current user account of another device
     * @param userName
     * @return
     */
    fun isCurrentUserFromOtherDevice(userName: String): Boolean {
        if (userName.isEmpty()) {
            return false
        }
        return userName.contains("/") && userName.contains(EMClient.getInstance().currentUser)
    }

    /**
     * 增加EaseCallkit监听
     *
     */
    fun addCallkitListener(context: Context) {
        callKitListener = object : EaseCallKitListener {
            override fun onInviteUsers(context: Context, userId: Array<String>, ext: JSONObject) {
                val intent = Intent(
                    context,
                    ConferenceInviteActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                var groupId: String? = null
                if (ext.length() > 0) {
                    try {
                        groupId = ext.getString("groupId")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                intent.putExtra(DemoConstant.EXTRA_CONFERENCE_GROUP_ID, groupId)
                intent.putExtra(DemoConstant.EXTRA_CONFERENCE_GROUP_EXIST_MEMBERS, userId)
                context.startActivity(intent)
            }

            override fun onEndCallWithReason(
                callType: EaseCallType,
                channelName: String,
                reason: EaseCallEndReason,
                callTime: Long
            ) {
                EMLog.d(
                    TAG,
                    "onEndCallWithReason" + (callType.name
                        ?: " callType is null ") + " reason:" + reason + " time:" + callTime
                )
                val formatter = SimpleDateFormat("mm:ss", Locale.getDefault())
                formatter.timeZone = TimeZone.getTimeZone("UTC")
                var callString: String? = context.getString(R.string.call_duration)
                callString += formatter.format(callTime)
                Toast.makeText(context, callString, Toast.LENGTH_SHORT).show()
            }

            override fun onGenerateToken(
                userId: String,
                channelName: String,
                appKey: String,
                callback: EaseCallKitTokenCallback
            ) {
                EMLog.d(
                    TAG,
                    "onGenerateToken userId:$userId channelName:$channelName appKey:$appKey"
                )
                var url = tokenUrl
                url += "?"
                url += "userAccount="
                url += userId
                url += "&channelName="
                url += channelName
                url += "&appkey="
                url += appKey

                //获取声网Token
                getRtcToken(url, callback)
            }

            override fun onReceivedCall(
                callType: EaseCallType,
                fromUserId: String,
                ext: JSONObject
            ) {
                //收到接听电话
                EMLog.d(TAG, "onRecivedCall" + callType.name + " fromUserId:" + fromUserId)
            }

            override fun onCallError(type: EaseCallError, errorCode: Int, description: String) {}
            override fun onInViteCallMessageSent() {
                LiveDataBus.get().use(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(
                    EaseEvent(
                        DemoConstant.MESSAGE_CHANGE_CHANGE,
                        EaseEvent.TYPE.MESSAGE
                    )
                )
            }

            override fun onRemoteUserJoinChannel(
                channelName: String,
                userName: String?,
                uid: Int,
                callback: EaseGetUserAccountCallback
            ) {
                if (userName.isNullOrEmpty()) {
                    var url = uIdUrl
                    url += "?"
                    url += "channelName="
                    url += channelName
                    url += "&userAccount="
                    url += EMClient.getInstance().currentUser
                    url += "&appkey="
                    url += EMClient.getInstance().options.appKey
                    getUserIdAgoraUid(uid, url, callback)
                } else {
                    //设置用户昵称 头像
                    setEaseCallKitUserInfo(userName)
                    val account = EaseUserAccount(uid, userName)
                    val accounts: MutableList<EaseUserAccount> = ArrayList()
                    accounts.add(account)
                    callback.onUserAccount(accounts)
                }
            }
        }
        EaseCallKit.getInstance().setCallKitListener(callKitListener)
    }

    /**
     * 获取声网Token
     *
     */
    private fun getRtcToken(tokenUrl: String, callback: EaseCallKitTokenCallback) {
        object : AsyncTask<String, Void, Pair<Int, String>>() {
            override fun doInBackground(vararg str: String): Pair<Int, String>? {
                try {
                    return EMHttpClient.getInstance()
                        .sendRequestWithToken(tokenUrl, null, EMHttpClient.GET)
                } catch (exception: HyphenateException) {
                    exception.printStackTrace()
                }
                return null
            }

            override fun onPostExecute(response: Pair<Int, String>?) {
                if (response != null) {
                    try {
                        val resCode = response.first
                        if (resCode == 200) {
                            val responseInfo = response.second
                            if (!responseInfo.isNullOrEmpty()) {
                                try {
                                    val result = JSONObject(responseInfo)
                                    val token = result.getString("accessToken")
                                    val uId = result.getInt("agoraUserId")

                                    //设置自己头像昵称
                                    setEaseCallKitUserInfo(EMClient.getInstance().currentUser)
                                    callback.onSetToken(token, uId)
                                } catch (e: Exception) {
                                    e.stackTrace
                                }
                            } else {
                                callback.onGetTokenError(response.first, response.second)
                            }
                        } else {
                            callback.onGetTokenError(response.first, response.second)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    callback.onSetToken(null, 0)
                }
            }
        }.execute(tokenUrl)
    }

    /**
     * 根据channelName和声网uId获取频道内所有人的UserId
     * @param uId
     * @param url
     * @param callback
     */
    private fun getUserIdAgoraUid(uId: Int, url: String, callback: EaseGetUserAccountCallback) {
        object : AsyncTask<String, Void, Pair<Int, String>?>() {
            override fun doInBackground(vararg str: String): Pair<Int, String>? {
                try {
                    return EMHttpClient.getInstance()
                        .sendRequestWithToken(url, null, EMHttpClient.GET)
                } catch (exception: HyphenateException) {
                    exception.printStackTrace()
                }
                return null
            }

            override fun onPostExecute(response: Pair<Int, String>?) {
                if (response != null) {
                    try {
                        val resCode = response.first
                        if (resCode == 200) {
                            val responseInfo = response.second
                            val userAccounts: MutableList<EaseUserAccount> = ArrayList()
                            if (!responseInfo.isNullOrEmpty()) {
                                try {
                                    val result = JSONObject(responseInfo)
                                    val resToken = result.getJSONObject("result")
                                    val it: Iterator<*> = resToken.keys()
                                    while (it.hasNext()) {
                                        val uIdStr = it.next().toString()
                                        var uid = 0
                                        uid = Integer.valueOf(uIdStr).toInt()
                                        val username = resToken.optString(uIdStr)
                                        if (uid == uId) {
                                            //获取到当前用户的userName 设置头像昵称等信息
                                            setEaseCallKitUserInfo(username)
                                        }
                                        userAccounts.add(EaseUserAccount(uid, username))
                                    }
                                    callback.onUserAccount(userAccounts)
                                } catch (e: Exception) {
                                    e.stackTrace
                                }
                            } else {
                                callback.onSetUserAccountError(response.first, response.second)
                            }
                        } else {
                            callback.onSetUserAccountError(response.first, response.second)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    callback.onSetUserAccountError(100, "response is null")
                }
            }
        }.execute(url)
    }

    /**
     * 设置callKit 用户头像昵称
     * @param userName
     */
    private fun setEaseCallKitUserInfo(userName: String) {
        val user = getUserInfo(userName)
        val userInfo = EaseCallUserInfo()
        if (user != null) {
            userInfo.nickName = user.nickname
            userInfo.headImage = user.avatar
        }
        EaseCallKit.getInstance().callKitConfig.setUserInfo(userName, userInfo)
    }

    /**
     * data sync listener
     */
    interface DataSyncListener {
        /**
         * sync complete
         * @param success true：data sync successful，false: failed to sync data
         */
        fun onSyncComplete(success: Boolean)
    }

    companion object {
        private val TAG = SdkHelper::class.java.simpleName
        private var minstance: SdkHelper? = null

        val instance: SdkHelper
            get() {
                if (minstance == null) {
                    synchronized(SdkHelper::class.java) {
                        if (minstance == null) {
                            minstance = SdkHelper()
                        }
                    }
                }
                return minstance!!
            }


    }
}