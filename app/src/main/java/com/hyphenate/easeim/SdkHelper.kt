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
 * ??????hyphenate-sdk???????????????????????????sdk??????????????????????????????
 */
/**
 * ??????hyphenate-sdk???????????????????????????sdk??????????????????????????????
 */
class SdkHelper private constructor() {
    /**
     * ??????SDK???????????????
     * @param init
     */
    var isSDKInit = false//SDK???????????????
    private var contactList: Map<String, EaseUser>? = null
    val userProfileManager by lazy { UserProfileManager() }
    private var callKitListener: EaseCallKitListener? = null
    private val tokenUrl = "http://a1.easemob.com/token/rtcToken/v1"
    private val uIdUrl = "http://a1.easemob.com/channel/mapper"
    private var fetchUserRunnable: FetchUserRunnable? = null
    private var fetchUserTread: Thread? = null
    private var fetchUserInfoList: FetchUserInfoList? = null
    fun init(context: Context) {
        //?????????IM SDK
        if (initSDK(context)) {
            // debug mode, you'd better set it to false, if you want release your App officially.
            EMClient.getInstance().setDebugMode(true)
            // set Call options
            setCallHeadsetReceive(context)
            //???????????????
            initPush(context)
            //??????call Receiver
            //initReceiver(context);
            //?????????ease ui??????
            initEaseUI()
            //??????????????????
            registerConversationType()

            //callKit?????????
            initCallKit(context)

            //??????????????????????????????
            fetchUserInfoList = FetchUserInfoList.instance
            fetchUserRunnable = FetchUserRunnable()
            fetchUserTread = Thread(fetchUserRunnable)
            fetchUserTread?.start()
        }
    }

    /**
     * callKit?????????
     * @param context
     */
    private fun initCallKit(context: Context) {

        val callKitConfig = EaseCallKitConfig()
        //????????????????????????
        callKitConfig.callTimeOut = (30 * 1000).toLong()
        //????????????AgoraAppId
        callKitConfig.agoraAppId = "15cb0d28b87b425ea613fc46f7c9f974"
        callKitConfig.isEnableRTCToken = true
        EaseCallKit.getInstance().init(context, callKitConfig)
        // Register the activities which you have registered in manifest
        EaseCallKit.getInstance().registerVideoCallClass(VideoCallActivity::class.java)
        EaseCallKit.getInstance().registerMultipleVideoClass(MultipleVideoActivity::class.java)
        addCallkitListener(context)
    }

    /**
     * ?????????SDK
     * @param context
     * @return
     */
    private fun initSDK(context: Context): Boolean {
        // ?????????????????????SDK????????????
        val options = initChatOptions(context)
        //??????????????????rest server???im server
//        options.setRestServer("a1-hsb.easemob.com");
//        options.setIMServer("106.75.100.247");
//        options.setImPort(6717);

//        options.setRestServer("a41.easemob.com");
//        options.setIMServer("msync-im-41-tls-test.easemob.com");
//        options.setImPort(6717);

        // ?????????SDK
        isSDKInit = EaseIM.getInstance().init(context, options)
        //??????????????????????????????????????????
        SpDbModel.instance.userInfoTimeOut = (30 * 60 * 1000).toLong()
        //??????????????????????????????
        updateTimeoutUsers()
        return isSDKInit
    }

    /**
     * ??????????????????
     */
    private fun registerConversationType() {
        EaseMessageTypeSetManager.getInstance()
            .addMessageType(EaseExpressionAdapterDelegate::class.java) //???????????????
            .addMessageType(EaseFileAdapterDelegate::class.java) //??????
            .addMessageType(EaseImageAdapterDelegate::class.java) //??????
            .addMessageType(EaseLocationAdapterDelegate::class.java) //??????
            .addMessageType(EaseVideoAdapterDelegate::class.java) //??????
            .addMessageType(EaseVoiceAdapterDelegate::class.java) //??????
            .addMessageType(ChatConferenceInviteAdapterDelegate::class.java) //????????????
            .addMessageType(ChatRecallAdapterDelegate::class.java) //????????????
            .addMessageType(ChatVideoCallAdapterDelegate::class.java) //????????????
            .addMessageType(ChatVoiceCallAdapterDelegate::class.java) //????????????
            .addMessageType(ChatUserCardAdapterDelegate::class.java) //????????????
            .addMessageType(EaseCustomAdapterDelegate::class.java) //???????????????
            .addMessageType(ChatNotificationAdapterDelegate::class.java) //?????????????????????
            .setDefaultMessageType(EaseTextAdapterDelegate::class.java) //??????
    }

    /**
     * ???????????????????????????
     * @return
     */
    val isLoggedIn: Boolean
        get() = eMClient.isLoggedInBefore

    /**
     * ??????IM SDK????????????
     * @return
     */
    val eMClient: EMClient
        get() = EMClient.getInstance()

    /**
     * ??????contact manager
     * @return
     */
    val contactManager: EMContactManager
        get() = eMClient.contactManager()

    /**
     * ??????group manager
     * @return
     */
    val groupManager: EMGroupManager
        get() = eMClient.groupManager()

    /**
     * ??????chatroom manager
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
     * ChatPresenter????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * @param context
     */
    private fun initEaseUI() {
        //??????ChatPresenter,ChatPresenter???????????????????????????????????????
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

    //Translation Manager ?????????
    fun initTranslationManager() {
        val params = EMTranslateParams(
            "46c34219512d4f09ae6f8e04c083b7a3",
            "https://api.cognitive.microsofttranslator.com",
            500
        )
        EMClient.getInstance().translationManager().init(params)
    }

    /**
     * ??????????????????
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
            //??????????????????????????? ????????????
            updateContactList()
            user = getContactList()[username]
            //?????????????????????????????????????????? ????????????UI????????????
            if (user == null) {
                fetchUserInfoList?.addUserId(username)
            }
        }
        return user
    }

    /**
     * ?????????????????????????????????
     * @param context
     * @return
     */
    private fun initChatOptions(context: Context): EMOptions {
        Log.d(TAG, "init HuanXin Options")
        val options = EMOptions()
        // ???????????????????????????????????????,?????????true
        options.acceptInvitationAlways = false
        // ???????????????????????????????????????
        options.requireAck = true
        // ???????????????????????????????????????,??????false
        options.requireDeliveryAck = false
        //??????fpa???????????????false
        options.fpaEnable = true
        /**
         * NOTE:????????????????????????????????????????????????????????????????????????????????????
         */
        val builder = EMPushConfig.Builder(context)
        builder.enableVivoPush() // ?????????AndroidManifest.xml?????????appId???appKey
            .enableMeiZuPush("134952", "f00e7e8499a549e09731a60a4da399e3")
            .enableMiPush("2882303761517426801", "5381742660801")
            .enableOppoPush(
                "0bb597c5e9234f3ab9f821adbeceecdb",
                "cd93056d03e1418eaa6c3faf10fd7537"
            )
            .enableHWPush() // ?????????AndroidManifest.xml?????????appId
            .enableFCM("782795210914")
        options.pushConfig = builder.build()

        //set custom servers, commonly used in private deployment
        if (SpDbModel.instance.isCustomSetEnable()) {
            if (SpDbModel.instance.isCustomServerEnable() && SpDbModel.instance.getRestServer() != null && SpDbModel.instance.getIMServer() != null) {
                // ??????rest server??????
                options.restServer = SpDbModel.instance.getRestServer()
                // ??????im server??????
                options.setIMServer(SpDbModel.instance.getIMServer())
                //??????im server????????????????????????
                if (SpDbModel.instance.getIMServer()!!.contains(":")) {
                    options.setIMServer(
                        SpDbModel.instance.getIMServer()!!.split(":".toRegex()).toTypedArray()[0]
                    )
                    // ??????im server ??????????????????443
                    options.imPort = Integer.valueOf(
                        SpDbModel.instance.getIMServer()!!.split(":".toRegex()).toTypedArray()[1]
                    )
                } else {
                    //????????????????????????
                    if (SpDbModel.instance.getIMServerPort() != 0) {
                        options.imPort = SpDbModel.instance.getIMServerPort()
                    }
                }
            }
        }
        if (SpDbModel.instance.isCustomAppKeyEnabled() && !TextUtils.isEmpty(SpDbModel.instance.getCustomAppkey())) {
            // ??????appkey
            options.appKey = SpDbModel.instance.getCustomAppkey()
        }
        val imServer = options.imServer
        val restServer = options.restServer

        // ???????????????????????????owner???????????????????????????????????????owner???????????????????????????
        options.allowChatroomOwnerLeave(SpDbModel.instance.isChatroomOwnerLeaveAllowed())
        // ????????????(?????????????????????)?????????????????????????????????
        options.isDeleteMessagesAsExitGroup = SpDbModel.instance.isDeleteMessagesAsExitGroup()
        // ????????????????????????????????????
        options.isAutoAcceptGroupInvitation = SpDbModel.instance.isAutoAcceptGroupInvitation()
        // ???????????????????????????????????????????????????????????????True????????????????????????????????????
        options.autoTransferMessageAttachments = SpDbModel.instance.isSetTransferFileByUser()
        // ???????????????????????????????????????true???????????????
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
            //OPPO SDK?????????2.1.0????????????????????????
            HeytapPushManager.init(context, true)
            //HMSPushHelper.getInstance().initHMSAgent(DemoApplication.getInstance());
            EMPushHelper.getInstance().setPushListener(object : PushListener() {
                override fun onError(pushType: EMPushType, errorCode: Long) {
                    // TODO: ?????????errorCode???9xx??????????????????????????????EMError?????????????????????????????????pushType???????????????????????????????????????
                    EMLog.e("PushClient", "Push client occur a error: $pushType - $errorCode")
                }

                override fun isSupportPush(
                    pushType: EMPushType,
                    pushConfig: EMPushConfig
                ): Boolean {
                    // ?????????????????????????????????????????????FCM??????
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
     * ??????????????????
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
     * ?????????????????????????????????????????????
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
     * ???????????????????????????????????????
     * @param autoLogin
     */
    var autoLogin: Boolean
        get() = PreferenceManager.getInstance().autoLogin
        set(autoLogin) {
            PreferenceManager.getInstance().autoLogin = autoLogin
        }

    /**
     * ???????????????????????????
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
     * ?????????????????????????????????
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
     * ????????????????????????
     */
    fun showNotificationPermissionDialog(context: Context) {
        val pushType = EMPushHelper.getInstance().pushType
        // oppo
        if (pushType == EMPushType.OPPOPUSH && HeytapPushManager.isSupportPush(context)) {
            HeytapPushManager.requestNotificationPermission()
        }
    }

    /**
     * ???????????????
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
     * ????????????????????????????????????
     * ????????????true, ????????????api???????????????????????????????????????false.
     * @return
     */
    val isFirstInstall: Boolean
        get() = SpDbModel.instance.isFirstInstall()

    /**
     * ??????????????????????????????????????????????????????????????????api?????????
     * ?????????????????????????????????????????????true
     */
    fun makeNotFirstInstall() {
        SpDbModel.instance.makeNotFirstInstall()
    }

    /**
     * ????????????????????????????????????????????????
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
     * ??????EaseCallkit??????
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

                //????????????Token
                getRtcToken(url, callback)
            }

            override fun onReceivedCall(
                callType: EaseCallType,
                fromUserId: String,
                ext: JSONObject
            ) {
                //??????????????????
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
                    //?????????????????? ??????
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
     * ????????????Token
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

                                    //????????????????????????
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
     * ??????channelName?????????uId???????????????????????????UserId
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
                                            //????????????????????????userName ???????????????????????????
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
     * ??????callKit ??????????????????
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
         * @param success true???data sync successful???false: failed to sync data
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