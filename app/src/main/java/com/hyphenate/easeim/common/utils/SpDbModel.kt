package com.hyphenate.easeim.common.utils

import android.content.Context
import com.hyphenate.chat.EMChatRoom
import com.hyphenate.chat.EMClient
import com.hyphenate.easeim.AppClient
import com.hyphenate.easeim.common.db.DbHelper
import com.hyphenate.easeim.common.db.DbHelper.Companion.dbHelper
import com.hyphenate.easeim.common.db.dao.EmUserDao
import com.hyphenate.easeim.common.db.entity.AppKeyEntity
import com.hyphenate.easeim.common.db.entity.EmUserEntity
import com.hyphenate.easeim.common.db.entity.InviteMessage
import com.hyphenate.easeim.common.db.entity.MsgTypeManageEntity
import com.hyphenate.easeim.common.manager.OptionsHelper
import com.hyphenate.easeim.common.entity.ServerSetEntity
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.manager.EasePreferenceManager

/**
 * SpDbModel主要用于SP存取及一些数据库的存取
 */
class SpDbModel() {
    var dao: EmUserDao? = null
    var valueCache = HashMap<Key, Any>()
    var chatRooms: List<EMChatRoom>? = null
    var userInfoTimeOut: Long = 0
        get() = Companion.userInfoTimeOut
        set(userInfoTimeOut) {
            if (userInfoTimeOut > 0) {
                field = userInfoTimeOut
            }
        }

    fun updateContactList(contactList: List<EaseUser>): Boolean {
        val userEntities: List<EmUserEntity?> = EmUserEntity.parseList(contactList)
        val dao = dbHelper().userDao
        if (dao != null) {
            dao.insert(userEntities)
            return true
        }
        return false
    }

    val contactList: Map<String, EaseUser>
        get() {
            val dao = dbHelper().userDao ?: return HashMap()
            val map = HashMap<String, EaseUser>()
            val users = dao.loadAllContactUsers()
            if (users.isNotEmpty()) {
                users.forEach { user ->
                    map[user.username] = user
                }
            }
            return map
        }
    val allUserList: Map<String, EaseUser>
        get() {
            val dao = dbHelper().userDao ?: return HashMap()
            val map = HashMap<String, EaseUser>()
            val users = dao.loadAllEaseUsers()
            if (users.isNotEmpty()) {
                users.forEach { user ->
                    map[user.username] = user
                }
            }
            return map
        }
    val friendContactList: Map<String, EaseUser?>
        get() {
            val dao = dbHelper().userDao
                ?: return HashMap()
            val map = HashMap<String, EaseUser?>()
            val users = dao.loadContacts()
            if (users.isNotEmpty()) {
                users.forEach { user ->
                    map[user.username] = user
                }
            }
            return map
        }

    /**
     * 判断是否是联系人
     * @param userId
     * @return
     */
    fun isContact(userId: String): Boolean {
        val contactList = friendContactList
        return contactList.keys.contains(userId)
    }

    fun saveContact(user: EaseUser?) {
        val dao = dbHelper().userDao
        dao?.insert(EmUserEntity.parseParent(user!!))
    }

    val appKeys: List<AppKeyEntity>?
        get() {
            val appKeyDao = dbHelper().appKeyDao ?: return ArrayList()
            val defAppKey = OptionsHelper.get().defAppKey
            val appKey = EMClient.getInstance().options.appKey
            if (defAppKey != appKey) {
                val appKeys = appKeyDao.queryKey(appKey)
                if (appKeys.isNullOrEmpty()) {
                    appKeyDao.insert(AppKeyEntity(appKey))
                }
            }
            return appKeyDao.loadAllAppKeys()
        }

    /**
     * 保存appKey
     * @param appKey
     */
    fun saveAppKey(appKey: String) {
        val appKeyDao = dbHelper().appKeyDao
        appKeyDao?.insert(AppKeyEntity(appKey))
    }

    fun deleteAppKey(appKey: String) {
        val appKeyDao = dbHelper().appKeyDao
        appKeyDao?.deleteAppKey(arg0 = appKey)
    }

    /**
     * get DemoDbHelper
     * @return
     */
    val dbHelper: DbHelper
        get() = dbHelper()

    /**
     * 向数据库中插入数据
     * @param object
     */
    fun insert(objectData: Any) {
        when (objectData) {
            is InviteMessage -> {
                dbHelper.inviteMessageDao?.insert(objectData)
            }
            is MsgTypeManageEntity -> {
                dbHelper.msgTypeManageDao?.insert(objectData)
            }
            is EmUserEntity -> {
                dbHelper.userDao?.insert(objectData)
            }
        }
    }

    /**
     * update
     * @param `object`
     */
    fun update(objectData: Any?) {
        when (objectData) {
            is InviteMessage -> {
                dbHelper.inviteMessageDao?.update(objectData)
            }
            is MsgTypeManageEntity -> {
                dbHelper.msgTypeManageDao?.update(objectData)
            }
            is EmUserEntity -> {
                dbHelper.userDao?.insert(objectData)
            }
        }
    }

    /**
     * 查找有关用户用户属性过期的用户ID
     *
     */
    fun selectTimeOutUsers(): List<String>? {
        return dbHelper.userDao?.loadTimeOutEaseUsers(
            Companion.userInfoTimeOut,
            System.currentTimeMillis()
        )
    }

    /**
     * save current username
     * @param username
     */
    fun setCurrentUserName(username: String?) {
        PreferenceManager.getInstance().setCurrentUserName(username)
    }

    val currentLoginUser: String
        get() = PreferenceManager.getInstance().currentUsername


    /**
     * 保存是否删除联系人的状态
     * @param username
     * @param isDelete
     */
    fun deleteUsername(username: String, isDelete: Boolean) {
        val sp = AppClient.instance.getSharedPreferences(
            "save_delete_username_status",
            Context.MODE_PRIVATE
        )
        val edit = sp?.edit()
        edit?.putBoolean(username, isDelete)
        edit?.apply()
    }

    /**
     * 查看联系人是否删除
     * @param username
     * @return
     */
    fun isDeleteUsername(username: String?): Boolean {
        val sp = AppClient.instance.getSharedPreferences(
            "save_delete_username_status",
            Context.MODE_PRIVATE
        )
        return sp.getBoolean(username, false)
    }

    /**
     * 保存当前用户密码
     * 此处保存密码是为了查看多端设备登录是，调用接口不再输入用户名及密码，实际开发中，不可在本地保存密码！
     * 注：实际开发中不可进行此操作！！！
     * @param pwd
     */
    var currentUserPwd: String?
        get() = PreferenceManager.getInstance().currentUserPwd
        set(pwd) {
            PreferenceManager.getInstance().currentUserPwd = pwd
        }

    /**
     * 设置昵称
     * @param nickname
     */
    var currentUserNick: String?
        get() = PreferenceManager.getInstance().currentUserNick
        set(nickname) {
            PreferenceManager.getInstance().currentUserNick = nickname
        }

    /**
     * 设置头像
     * @param avatar
     */
    private var currentUserAvatar: String
        get() = PreferenceManager.getInstance().currentUserAvatar
        private set(avatar) {
            PreferenceManager.getInstance().currentUserAvatar = avatar
        }
    var settingMsgNotification: Boolean
        get() {
            var value = valueCache[Key.VibrateAndPlayToneOn]
            if (value == null) {
                value = PreferenceManager.getInstance().settingMsgNotification
                valueCache[Key.VibrateAndPlayToneOn] = value
            }
            return (value ?: true) as Boolean
        }
        set(paramBoolean) {
            PreferenceManager.getInstance().settingMsgNotification = paramBoolean
            valueCache[Key.VibrateAndPlayToneOn] = paramBoolean
        }
    var settingMsgSound: Boolean
        get() {
            var cache = valueCache[Key.PlayToneOn]
            if (cache == null) {
                cache = PreferenceManager.getInstance().settingMsgSound
                valueCache[Key.PlayToneOn] = cache
            }
            return cache as Boolean
        }
        set(paramBoolean) {
            PreferenceManager.getInstance().settingMsgSound = paramBoolean
            valueCache[Key.PlayToneOn] = paramBoolean
        }

    var settingMsgVibrate: Boolean
        get() {
            var cache = valueCache[Key.VibrateOn]
            if (cache == null) {
                cache = PreferenceManager.getInstance().settingMsgVibrate
                valueCache[Key.VibrateOn] = cache
            }
            return cache as Boolean
        }
        set(paramBoolean) {
            PreferenceManager.getInstance().settingMsgVibrate = paramBoolean
            valueCache[Key.VibrateOn] = paramBoolean
        }

    var settingMsgSpeaker: Boolean
        get() {
            var value = valueCache[Key.SpakerOn]
            if (value == null) {
                value = PreferenceManager.getInstance().settingMsgSpeaker
                valueCache[Key.SpakerOn] = value
            }
            return value as Boolean
        }
        set(paramBoolean) {
            PreferenceManager.getInstance().settingMsgSpeaker = paramBoolean
            valueCache[Key.SpakerOn] = paramBoolean
        }

    //        if(dao == null){
//            dao = new UserDao(context);
//        }
//
//        List<String> list = new ArrayList<String>();
//        list.addAll(groups);
//        for(int i = 0; i < list.size(); i++){
//            if(EaseAtMessageHelper.get().getAtMeGroups().contains(list.get(i))){
//                list.remove(i);
//                i--;
//            }
//        }
//
//        dao.setDisabledGroups(list);
//        valueCache.put(Key.DisabledGroups, list);
    //        if(dao == null){
//            dao = new UserDao(context);
//        }
//
//        if(val == null){
//            val = dao.getDisabledGroups();
//            valueCache.put(Key.DisabledGroups, val);
//        }
    val disabledGroups: List<String>?
        get() {
            val cache = valueCache[Key.DisabledGroups]

//        if(dao == null){
//            dao = new UserDao(context);
//        }
//
//        if(val == null){
//            val = dao.getDisabledGroups();
//            valueCache.put(Key.DisabledGroups, val);
//        }
            return cache as List<String>?
        }

    fun setDisabledIds(ids: List<String?>?) {
//        if(dao == null){
//            dao = new UserDao(context);
//        }
//
//        dao.setDisabledIds(ids);
//        valueCache.put(Key.DisabledIds, ids);
    }

    fun getDisabledIds(): List<String>? {
        val cache = valueCache[Key.DisabledIds]

//        if(dao == null){
//            dao = new UserDao(context);
//        }
//
//        if(val == null){
//            val = dao.getDisabledIds();
//            valueCache.put(Key.DisabledIds, val);
//        }
        return cache as List<String>?
    }

    fun setGroupsSynced(synced: Boolean) {
        PreferenceManager.getInstance().isGroupsSynced = synced
    }

    fun isGroupsSynced(): Boolean {
        return PreferenceManager.getInstance().isGroupsSynced
    }

    fun setContactSynced(synced: Boolean) {
        PreferenceManager.getInstance().isContactSynced = synced
    }

    fun isContactSynced(): Boolean {
        return PreferenceManager.getInstance().isContactSynced
    }

    fun setBlacklistSynced(synced: Boolean) {
        PreferenceManager.getInstance().setBlacklistSynced(synced)
    }

    fun isBacklistSynced(): Boolean {
        return PreferenceManager.getInstance().isBacklistSynced
    }

    fun setAdaptiveVideoEncode(value: Boolean) {
        PreferenceManager.getInstance().isAdaptiveVideoEncode = value
    }

    fun isAdaptiveVideoEncode(): Boolean {
        return PreferenceManager.getInstance().isAdaptiveVideoEncode
    }

    fun setPushCall(value: Boolean) {
        PreferenceManager.getInstance().isPushCall = value
    }

    fun isPushCall(): Boolean {
        return PreferenceManager.getInstance().isPushCall
    }

    fun isMsgRoaming(): Boolean {
        return PreferenceManager.getInstance().isMsgRoaming
    }

    fun setMsgRoaming(roaming: Boolean) {
        PreferenceManager.getInstance().isMsgRoaming = roaming
    }

    fun isShowMsgTyping(): Boolean {
        return PreferenceManager.getInstance().isShowMsgTyping
    }

    fun showMsgTyping(show: Boolean) {
        PreferenceManager.getInstance().showMsgTyping(show)
    }

    /**
     * 获取默认的服务器设置
     * @return
     */
    fun getDefServerSet(): ServerSetEntity {
        return OptionsHelper.get().defServerSet
    }

    /**
     * 设置是否使用google推送
     * @param useFCM
     */
    fun setUseFCM(useFCM: Boolean) {
        PreferenceManager.getInstance().isUseFCM = useFCM
    }

    /**
     * 获取设置，是否设置google推送
     * @return
     */
    fun isUseFCM(): Boolean {
        return PreferenceManager.getInstance().isUseFCM
    }

    /**
     * 自定义服务器是否可用
     * @return
     */
    fun isCustomServerEnable(): Boolean {
        return OptionsHelper.get().isCustomServerEnable
    }

    /**
     * 这是自定义服务器是否可用
     * @param enable
     */
    fun enableCustomServer(enable: Boolean) {
        OptionsHelper.get().enableCustomServer(enable)
    }

    /**
     * 自定义配置是否可用
     * @return
     */
    fun isCustomSetEnable(): Boolean {
        return OptionsHelper.get().isCustomSetEnable
    }

    /**
     * 自定义配置是否可用
     * @param enable
     */
    fun enableCustomSet(enable: Boolean) {
        OptionsHelper.get().enableCustomSet(enable)
    }

    /**
     * 设置闲置服务器
     * @param restServer
     */
    fun setRestServer(restServer: String?) {
        OptionsHelper.get().restServer = restServer
    }

    /**
     * 获取闲置服务器
     * @return
     */
    fun getRestServer(): String? {
        return OptionsHelper.get().restServer
    }

    /**
     * 设置IM服务器
     * @param imServer
     */
    fun setIMServer(imServer: String?) {
        OptionsHelper.get().iMServer = imServer
    }

    /**
     * 获取IM服务器
     * @return
     */
    fun getIMServer(): String? {
        return OptionsHelper.get().iMServer
    }

    /**
     * 设置端口号
     * @param port
     */
    fun setIMServerPort(port: Int) {
        OptionsHelper.get().iMServerPort = port
    }

    fun getIMServerPort(): Int {
        return OptionsHelper.get().iMServerPort
    }

    /**
     * 设置自定义appkey是否可用
     * @param enable
     */
    fun enableCustomAppKey(enable: Boolean) {
        OptionsHelper.get().enableCustomAppkey(enable)
    }

    /**
     * 获取自定义appkey是否可用
     * @return
     */
    fun isCustomAppKeyEnabled(): Boolean {
        return OptionsHelper.get().isCustomAppkeyEnabled
    }

    /**
     * 设置自定义appkey
     * @param appKey
     */
    fun setCustomAppKey(appKey: String?) {
        OptionsHelper.get().customAppkey = appKey
    }

    /**
     * 获取自定义appkey
     * @return
     */
    fun getCustomAppkey(): String? {
        return OptionsHelper.get().customAppkey
    }

    /**
     * 设置是否允许聊天室owner离开并删除会话记录，意味着owner再不会受到任何消息
     * @param value
     */
    fun allowChatroomOwnerLeave(value: Boolean) {
        OptionsHelper.get().allowChatroomOwnerLeave(value)
    }

    /**
     * 获取聊天室owner离开时的设置
     * @return
     */
    fun isChatroomOwnerLeaveAllowed(): Boolean {
        return OptionsHelper.get().isChatroomOwnerLeaveAllowed
    }

    /**
     * 设置退出(主动和被动退出)群组时是否删除聊天消息
     * @param value
     */
    fun setDeleteMessagesAsExitGroup(value: Boolean) {
        OptionsHelper.get().isDeleteMessagesAsExitGroup = value
    }

    /**
     * 获取退出(主动和被动退出)群组时是否删除聊天消息
     * @return
     */
    fun isDeleteMessagesAsExitGroup(): Boolean {
        return OptionsHelper.get().isDeleteMessagesAsExitGroup
    }

    /**
     * 设置退出（主动和被动）聊天室时是否删除聊天信息
     * @param value
     */
    fun setDeleteMessagesAsExitChatRoom(value: Boolean) {
        OptionsHelper.get().isDeleteMessagesAsExitChatRoom = value
    }

    /**
     * 获取退出(主动和被动退出)聊天室时是否删除聊天消息
     * @return
     */
    fun isDeleteMessagesAsExitChatRoom(): Boolean {
        return OptionsHelper.get().isDeleteMessagesAsExitChatRoom
    }

    /**
     * 设置是否自动接受加群邀请
     * @param value
     */
    fun setAutoAcceptGroupInvitation(value: Boolean) {
        OptionsHelper.get().isAutoAcceptGroupInvitation = value
    }

    /**
     * 获取是否自动接受加群邀请
     * @return
     */
    fun isAutoAcceptGroupInvitation(): Boolean {
        return OptionsHelper.get().isAutoAcceptGroupInvitation
    }

    /**
     * 设置是否自动将消息附件上传到环信服务器，默认为True是使用环信服务器上传下载
     * @param value
     */
    fun setTransfeFileByUser(value: Boolean) {
        OptionsHelper.get().setTransfeFileByUser(value)
    }

    /**
     * 获取是否自动将消息附件上传到环信服务器，默认为True是使用环信服务器上传下载
     * @return
     */
    fun isSetTransferFileByUser(): Boolean {
        return OptionsHelper.get().isSetTransferFileByUser
    }

    /**
     * 是否自动下载缩略图，默认是true为自动下载
     * @param autoDownload
     */
    fun setAutoDownloadThumbnail(autoDownload: Boolean) {
        OptionsHelper.get().setAutodownloadThumbnail(autoDownload)
    }

    /**
     * 获取是否自动下载缩略图
     * @return
     */
    fun isSetAutodownloadThumbnail(): Boolean {
        return OptionsHelper.get().isSetAutodownloadThumbnail
    }

    /**
     * 设置是否只使用Https
     * @param usingHttpsOnly
     */
    fun setUsingHttpsOnly(usingHttpsOnly: Boolean) {
        OptionsHelper.get().usingHttpsOnly = usingHttpsOnly
    }

    /**
     * 获取是否只使用Https
     * @return
     */
    fun getUsingHttpsOnly(): Boolean {
        return OptionsHelper.get().usingHttpsOnly
    }

    fun setSortMessageByServerTime(sortByServerTime: Boolean) {
        OptionsHelper.get().isSortMessageByServerTime = sortByServerTime
    }

    fun isSortMessageByServerTime(): Boolean {
        return OptionsHelper.get().isSortMessageByServerTime
    }

    /**
     * 是否允许token登录
     * @param isChecked
     */
    fun setEnableTokenLogin(isChecked: Boolean) {
        PreferenceManager.getInstance().isEnableTokenLogin = isChecked
    }

    fun isEnableTokenLogin(): Boolean {
        return PreferenceManager.getInstance().isEnableTokenLogin
    }

    /**
     * 保存未发送的文本消息内容
     * @param toChatUsername
     * @param content
     */
    fun saveUnSendMsg(toChatUsername: String?, content: String?) {
        EasePreferenceManager.getInstance().saveUnSendMsgInfo(toChatUsername, content)
    }

    fun getUnSendMsg(toChatUsername: String?): String {
        return EasePreferenceManager.getInstance().getUnSendMsgInfo(toChatUsername)
    }

    /**
     * 检查是否是第一次安装登录
     * 默认值是true, 需要在用api拉取完会话列表后，就其置为false.
     * @return
     */
    fun isFirstInstall(): Boolean {
        val preferences =
            AppClient.instance.getSharedPreferences("first_install", Context.MODE_PRIVATE)
        return preferences.getBoolean("is_first_install", true)
    }

    /**
     * 将状态置为非第一次安装，在调用获取会话列表的api后调用
     * 并将会话列表是否来自服务器置为true
     */
    fun makeNotFirstInstall() {
        val preferences =
            AppClient.instance.getSharedPreferences("first_install", Context.MODE_PRIVATE)
        preferences.edit().putBoolean("is_first_install", false).apply()
        preferences.edit().putBoolean("is_conversation_come_from_server", true).apply()
    }

    /**
     * 检查会话列表是否从服务器返回数据
     * @return
     */
    fun isConComeFromServer(): Boolean {
        val preferences =
            AppClient.instance.getSharedPreferences("first_install", Context.MODE_PRIVATE)
        return preferences.getBoolean("is_conversation_come_from_server", false)
    }

    /**
     * 将会话列表从服务器取数据的状态置为false，即后面应该采用本地数据库数据。
     */
    fun modifyConComeFromStatus() {
        val preferences =
            AppClient.instance.getSharedPreferences("first_install", Context.MODE_PRIVATE)
        preferences.edit().putBoolean("is_conversation_come_from_server", false).apply()
    }

    /**
     * 获取目标翻译语言
     */
    fun getTargetLanguage(): String {
        return PreferenceManager.getInstance().targetLanguage
    }

    /**
     * 设置目标翻译语言
     */
    fun setTargetLanguage(languageCode: String?) {
        PreferenceManager.getInstance().targetLanguage = languageCode
    }

    enum class Key {
        VibrateAndPlayToneOn, VibrateOn, PlayToneOn, SpakerOn, DisabledGroups, DisabledIds
    }

    companion object {
        //用户属性数据过期时间设置
        var userInfoTimeOut = (7 * 24 * 60 * 60 * 1000).toLong()

        private var mInstance: SpDbModel? = null

        @JvmStatic
        @get:Synchronized
        val instance: SpDbModel
            get() {
                if (mInstance == null) {
                    mInstance = SpDbModel()
                }
                return mInstance!!
            }
    }

    init {
        PreferenceManager.init(AppClient.instance)
    }
}