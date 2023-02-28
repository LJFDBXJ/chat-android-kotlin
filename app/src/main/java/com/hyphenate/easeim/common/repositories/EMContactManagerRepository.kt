package com.hyphenate.easeim.common.repositories

import com.hyphenate.EMCallBack
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMUserInfo
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.common.db.entity.EmUserEntity
import com.hyphenate.easeim.common.interfaceOrImplement.ResultCallBack
import com.hyphenate.easeim.common.utils.SpDbModel
import com.hyphenate.easeim.common.net.ErrorCode
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.manager.EaseThreadManager
import com.hyphenate.easeui.utils.EaseCommonUtils
import com.hyphenate.exceptions.HyphenateException
import com.hyphenate.util.EMLog
import java.util.*
import kotlin.collections.ArrayList

class EMContactManagerRepository : BaseEMRepository() {

    fun addContact(userName: String, reason: String?, callBack: EMValueCallBack<Boolean>) {
        if (currentUser.equals(userName, ignoreCase = true)) {
            callBack.onError(ErrorCode.EM_ADD_SELF_ERROR, "")
            return
        }
        val users = userDao?.loadContactUsers()
        if (users?.contains(userName) == true) {
            if (contactManager.blackListUsernames.contains(userName)) {
                callBack.onError(ErrorCode.EM_FRIEND_BLACK_ERROR, "")
                return
            }
            callBack.onError(ErrorCode.EM_FRIEND_ERROR, "")
            return
        }
        contactManager.aysncAddContact(userName, reason, object : EMCallBack {
            override fun onSuccess() {
                callBack.onSuccess(true)
            }

            override fun onError(code: Int, error: String) {
                callBack.onError(code, error)
            }

            override fun onProgress(progress: Int, status: String) {}
        })
    }

    fun getContactList(fetchServer: Boolean, callBack: ResultCallBack<List<EaseUser>>) {
        if (fetchServer) {
            if (!isLoggedIn) {
                callBack.onError(ErrorCode.EM_NOT_LOGIN)
                return
            }
            runOnIOThread {
                try {
                    var usernames = contactManager.allContactsFromServer
                    val ids = contactManager.selfIdsOnOtherPlatform
                    val blackListFromServer = contactManager.blackListFromServer
                    var hasSelfOtherPlatform = false
                    if (usernames == null) {
                        usernames = ArrayList()
                    }
                    if (!ids.isNullOrEmpty()) {
                        usernames.addAll(ids)
                        hasSelfOtherPlatform = true
                    }

                    //回调返回的数据
                    val easeUsers = ArrayList<EaseUser>()
                    val notRequestUsers = ArrayList<EaseUser>()
                    var exitUsers: List<String>? = null
                    if (usernames.isNotEmpty()) {
                        val updateUsers: MutableList<String>
                        if (userDao != null) {
                            exitUsers = userDao?.loadContactUsers()
                            //删除之前的多端在线
                            exitUsers?.forEach { userId ->
                                if (SdkHelper.instance.isCurrentUserFromOtherDevice(userName = userId)) {
                                    userDao?.deleteUser(arg0 = userId)
                                }
                            }
                            exitUsers = userDao?.loadContactUsers()
                        }

                        //本地没有存储任何数据
                        if (exitUsers.isNullOrEmpty()) {
                            updateUsers = usernames
                        } else {
                            //用户属性过期的好友
                            val timeOutUsers: List<String?> = userDao!!.loadTimeOutFriendUser(
                                SpDbModel.userInfoTimeOut,
                                System.currentTimeMillis()
                            )
                            updateUsers = ArrayList()
                            val timeOut = timeOutUsers.isNotEmpty()

                            usernames.forEach { userId ->
                                if (!exitUsers.contains(userId)) {
                                    updateUsers.add(userId)
                                } else {
                                    if (timeOut && timeOutUsers.contains(userId)) {
                                        updateUsers.add(userId)
                                    } else {
                                        notRequestUsers.addAll(userDao!!.loadUserByUserId(userId))
                                    }
                                }
                            }
                        }

                        //是否有多端登录
                        if (hasSelfOtherPlatform) {
                            updateUsers.add(EMClient.getInstance().currentUser)
                        }
                        var size = updateUsers.size
                        if (size > 0) {
                            var index = 0
                            val tagNumber = 100
                            while (size > 100) {
                                val userList =
                                    updateUsers.subList(index, index + tagNumber)
                                val userArray = ArrayList(userList)
                                size -= tagNumber
                                index += tagNumber
                                if (size == 0) {
                                    fetchUserInfoByIds(
                                        users = userArray,
                                        blackList = blackListFromServer,
                                        easeUsers = easeUsers,
                                        exitUsers = notRequestUsers,
                                        callBack = callBack,
                                        callback = true
                                    )
                                } else {
                                    fetchUserInfoByIds(
                                        users = userArray,
                                        blackList = blackListFromServer,
                                        easeUsers = easeUsers,
                                        exitUsers = null,
                                        callBack = callBack,
                                        callback = false
                                    )
                                }
                            }
                            if (size > 0) {
                                val userList = updateUsers.subList(index, index + size)
                                val userArray = ArrayList(userList)
                                fetchUserInfoByIds(
                                    users = userArray,
                                    blackList = blackListFromServer,
                                    easeUsers = easeUsers,
                                    exitUsers = notRequestUsers,
                                    callBack = callBack,
                                    callback = true
                                )
                            }
                        } else {
                            if (!exitUsers.isNullOrEmpty()) {
                                exitUsers.forEach {
                                    easeUsers.addAll(userDao!!.loadUserByUserId(it))
                                }
                            }
                            callBack.onSuccess(easeUsers)
                        }
                    } else {
                        callBack.onSuccess(easeUsers)
                    }
                    if (userDao != null) {
                        userDao?.clearUsers()
                        userDao?.insert(EmUserEntity.parseList(easeUsers))
                    }
                } catch (e: HyphenateException) {
                    e.printStackTrace()
                    callBack.onError(e.errorCode, e.description)
                }

            }

        } else {
            userDao?.loadUsers()
        }

    }

    /**
     * 从服务器批量获取用户信息
     */
    private fun fetchUserInfoByIds(
        users: List<String>,
        blackList: List<String>?,
        easeUsers: MutableList<EaseUser>,
        exitUsers: List<EaseUser>?,
        callBack: ResultCallBack<List<EaseUser>>,
        callback: Boolean
    ) {
        val userData = users.toTypedArray()
        EMClient.getInstance().userInfoManager()
            .fetchUserInfoByUserId(userData, object : EMValueCallBack<Map<String, EMUserInfo>> {
                override fun onSuccess(value: Map<String, EMUserInfo>) {
                    val usersDta = EaseUser.parseUserInfo(value)
                    usersDta.forEach { user ->
                        if (!blackList.isNullOrEmpty()) {
                            if (blackList.contains(user.username)) {
                                user.contact = 1
                            } else {
                                user.contact = 0
                            }
                        } else {
                            user.contact = 0
                        }
                        if (SdkHelper.instance.isCurrentUserFromOtherDevice(user.username)) {
                            val selfInfo = value[EMClient.getInstance().currentUser]
                            if (selfInfo != null) {
                                user.nickname = selfInfo.nickname
                                user.avatar = selfInfo.avatarUrl
                                user.email = selfInfo.email
                                user.gender = selfInfo.gender
                                user.birth = selfInfo.birth
                                user.sign = selfInfo.signature
                                user.ext = selfInfo.ext
                            }
                        }
                    }

//    TODO 需要处理
//                    usersDta.remove(EMClient.getInstance().currentUser)
                    easeUsers.addAll(usersDta)
                    if (exitUsers != null) {
                        easeUsers.addAll(exitUsers)
                    }
                    sortData(easeUsers)
                    callBack.onSuccess(easeUsers)
                }

                override fun onError(error: Int, errorMsg: String) {
                    callBack.onError(error, errorMsg)
                    easeUsers.addAll(EaseUser.parse(users))
                    if (callback) {
                        easeUsers.addAll(exitUsers!!)
                        sortData(easeUsers)
                        callBack.onSuccess(easeUsers)
                    }
                }
            })
    }

    /**
     * 获取联系人列表
     * @param callBack
     */
    fun getContactList(callBack: ResultCallBack<List<EaseUser>>?) {
        if (!isLoggedIn) {
            callBack?.onError(ErrorCode.EM_NOT_LOGIN)
            return
        }
        try {
            runOnIOThread {
                var usernames = contactManager.allContactsFromServer
                val ids = contactManager.selfIdsOnOtherPlatform
                if (usernames == null) {
                    usernames = ArrayList()
                }
                if (!ids.isNullOrEmpty()) {
                    usernames.addAll(ids)
                }
                val easeUsers = EaseUser.parse(usernames)
                if (!usernames.isNullOrEmpty()) {
                    val blackListFromServer = contactManager.blackListFromServer
                    for (user in easeUsers) {
                        if (!blackListFromServer.isNullOrEmpty()) {
                            if (blackListFromServer.contains(user.username)) {
                                user.contact = 1
                            }
                        }
                    }
                }
                sortData(easeUsers)
                callBack?.onSuccess(easeUsers)
            }
        } catch (e: HyphenateException) {
            e.printStackTrace()
            callBack?.onError(e.errorCode, e.description)
        }
    }

    private fun sortData(data: List<EaseUser>?) {
        if (data.isNullOrEmpty()) {
            return
        }
        Collections.sort(data, Comparator { lhs, rhs ->
            if (lhs.initialLetter == rhs.initialLetter) {
                lhs.nickname.compareTo(rhs.nickname)
            } else {
                if ("#" == lhs.initialLetter) {
                    return@Comparator 1
                } else if ("#" == rhs.initialLetter) {
                    return@Comparator -1
                }
                lhs.initialLetter.compareTo(rhs.initialLetter)
            }
        })
    }//回调返回的数据

    /**
     * 获取黑名单
     * @return
     */
    fun getBlackUserList(isFromServer: Boolean, callBack: ResultCallBack<List<EaseUser>>) {
        if (!isFromServer) {
            userDao?.loadBlackUsers()
        }
        if (!isLoggedIn) {
            callBack.onError(ErrorCode.EM_NOT_LOGIN)
            return
        }
        contactManager.aysncGetBlackListFromServer(object : EMValueCallBack<List<String>> {
            override fun onSuccess(value: List<String>) {
                if (value.isNotEmpty()) {
                    //回调返回的数据
                    val easeUsers = ArrayList<EaseUser>()
                    var size = value.size
                    var index = 0
                    val tagNumber = 100
                    while (size > 100) {
                        val userList = value.subList(index, index + tagNumber)
                        val userArray = ArrayList(userList)
                        size -= tagNumber
                        index += tagNumber
                        if (size == 0) {
                            fetchUserInfoByIds(
                                users = userArray,
                                blackList = null,
                                easeUsers = easeUsers,
                                exitUsers = null,
                                callBack = callBack,
                                callback = true
                            )
                        } else {
                            fetchUserInfoByIds(
                                users = userArray,
                                blackList = null,
                                easeUsers = easeUsers,
                                exitUsers = null,
                                callBack = callBack,
                                callback = false
                            )
                        }
                    }
                    if (size > 0) {
                        val userList = value.subList(index, index + size)
                        val userArray = ArrayList(userList)
                        fetchUserInfoByIds(
                            users = userArray,
                            blackList = value,
                            easeUsers = easeUsers,
                            exitUsers = null,
                            callBack = callBack,
                            callback = true
                        )
                    }
                    if (userDao != null) {
                        userDao?.clearBlackUsers()
                        userDao?.insert(EmUserEntity.parseList(easeUsers))
                    }
                } else {
                    EMLog.e("EMContactManagerRepository", "getBlackList is null")
                    val users = EaseUser.parse(value)
                    callBack.onSuccess(users)
                }
            }

            override fun onError(error: Int, errorMsg: String) {
                callBack.onError(error, errorMsg)
            }
        })
    }


    /**
     * 获取黑名单用户列表
     * @param callBack
     */
    fun getBlackUserList(callBack: ResultCallBack<List<EaseUser>?>?) {
        if (!isLoggedIn) {
            callBack?.onError(ErrorCode.EM_NOT_LOGIN)
            return
        }
        contactManager.aysncGetBlackListFromServer(object : EMValueCallBack<List<String>?> {
            override fun onSuccess(value: List<String>?) {
                val users = EaseUser.parse(value)
                if (users.isNotEmpty()) {
                    for (user in users) {
                        user.contact = 1
                    }
                }
                callBack?.onSuccess(users)
            }

            override fun onError(error: Int, errorMsg: String) {
                callBack?.onError(error, errorMsg)
            }
        })
    }

    /**
     * 删除联系人
     * @param userName
     * @return
     */
    fun deleteContact(userName: String, callBack: EMCallBack) {
        SpDbModel.instance.deleteUsername(userName, true)
        contactManager.aysncDeleteContact(userName, callBack)
    }

    /**
     * 添加到黑名单
     * @param userName
     * @param both 把用户加入黑民单时，如果是both双方发消息时对方都收不到；如果不是，
     * 则我能给黑名单的中用户发消息，但是对方发给我时我是收不到的
     * @return
     */
    fun addUserToBlackList(userName: String?, both: Boolean, callBack: EMValueCallBack<Boolean>) {
        contactManager.aysncAddUserToBlackList(userName, both, object : EMCallBack {
            override fun onSuccess() {
                userDao?.updateContact(1, userName)
                callBack.onSuccess(true)
            }

            override fun onError(code: Int, error: String) {
                callBack.onError(code, error)
            }

            override fun onProgress(progress: Int, status: String) {}
        })
    }

    /**
     * 移出黑名单
     * @param userName
     * @return
     */
    fun removeUserFromBlackList(userName: String?, callBack: EMValueCallBack<Boolean>) {
        contactManager.aysncRemoveUserFromBlackList(userName, object : EMCallBack {
            override fun onSuccess() {
                userDao?.updateContact(0, userName)
                callBack.onSuccess(true)
            }

            override fun onError(code: Int, error: String) {
                callBack.onError(code, error)
            }

            override fun onProgress(progress: Int, status: String) {}
        })
    }

    fun getSearchContacts(keyword: String?, data: Function1<ArrayList<EaseUser>, Unit>) {
        EaseThreadManager.getInstance().runOnIOThread {
            val easeUsers = userDao?.loadContacts()
            val list = ArrayList<EaseUser>()
            if (!easeUsers.isNullOrEmpty()) {
                easeUsers.forEach { user ->
                    if (user.username.contains(keyword!!) ||
                        !user.nickname.isNullOrEmpty() &&
                        user.nickname.contains(keyword)
                    ) {
                        list.add(user)
                    }
                }

            }
            data.invoke(list)
        }
    }

    fun getUserInfoById(
        isFromServer: Boolean = true,
        userName: String,
        mIsFriend: Boolean,
        callBack: EMValueCallBack<EaseUser>
    ) {
        if (!isFromServer && userDao != null) {
            callBack.onSuccess(userDao!!.loadUserByUserId(arg0 = userName)[0])
            return
        }
        var userId = userName
        if (SdkHelper.instance.isCurrentUserFromOtherDevice(userName = userName)) {
            userId = EMClient.getInstance().currentUser
        }
        val userIds = arrayOf(userId)
        val finalUserId = userId
        EMClient.getInstance().userInfoManager().fetchUserInfoByUserId(
            userIds,
            object : EMValueCallBack<Map<String, EMUserInfo>> {
                override fun onSuccess(value: Map<String, EMUserInfo>) {
                    transformEMUserInfo(info = value[finalUserId])?.let { user ->
                        if (mIsFriend) {
                            callBack.onSuccess(user)
                        }
                        if (mIsFriend) {
                            userDao?.insert(EmUserEntity.parseParent(user))
                        }
                    }
                }

                override fun onError(error: Int, errorMsg: String) {
                    callBack.onError(error, errorMsg)
                }
            })
    }

    private fun transformEMUserInfo(info: EMUserInfo?): EaseUser? {
        if (info != null) {
            val users = userDao?.loadUserByUserId(info.userId)
            var user: EaseUser? = null
            if (!users.isNullOrEmpty()) {
                user = users[0]
            }
            val userEntity = EaseUser()
            userEntity.username = user?.username ?: info.userId
            userEntity.nickname = info.nickname
            userEntity.email = info.email
            userEntity.avatar = info.avatarUrl
            userEntity.birth = info.birth
            userEntity.gender = info.gender
            userEntity.ext = info.ext
            userEntity.sign = info.signature
            EaseCommonUtils.setUserInitialLetter(userEntity)
            userEntity.contact = user?.contact ?: 0
            return userEntity
        }
        return null
    }
}