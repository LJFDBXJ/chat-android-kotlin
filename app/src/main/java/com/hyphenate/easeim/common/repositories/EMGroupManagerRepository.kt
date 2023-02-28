package com.hyphenate.easeim.common.repositories

import android.text.TextUtils
import androidx.lifecycle.LiveData
import com.hyphenate.EMCallBack
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.*
import com.hyphenate.easeim.AppClient
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.interfaceOrImplement.ResultCallBack
import com.hyphenate.easeim.common.net.ErrorCode
import com.hyphenate.easeim.common.net.Resource
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.manager.EaseThreadManager
import com.hyphenate.easeui.utils.EaseCommonUtils
import com.hyphenate.exceptions.HyphenateException
import kotlinx.coroutines.flow.flow
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class EMGroupManagerRepository : BaseEMRepository() {
    /**
     * 获取所有的群组列表
     * @return
     */
    fun allGroups(callBack: EMValueCallBack<MutableList<EMGroup>>) {
        if (!isLoggedIn) {
            callBack.onError(ErrorCode.EM_NOT_LOGIN, "未登录")
            return
        }
        groupManager.asyncGetJoinedGroupsFromServer(callBack)
    }


    /**
     * 获取所有群组列表
     * @param callBack
     */
    fun getAllGroups(callBack: ResultCallBack<List<EMGroup>>) {
        if (!isLoggedIn) {
            callBack.onError(ErrorCode.EM_NOT_LOGIN)
            return
        }
        groupManager.asyncGetJoinedGroupsFromServer(object :
            EMValueCallBack<List<EMGroup>> {
            override fun onSuccess(value: List<EMGroup>) {
                callBack.onSuccess(value)
            }

            override fun onError(error: Int, errorMsg: String) {
                callBack.onError(error, errorMsg)
            }
        })
    }


    /**
     * 从服务器分页获取加入的群组
     * @param pageIndex
     * @param pageSize
     * @return
     */
    fun getGroupListFromServer(
        pageIndex: Int,
        pageSize: Int,
        callBack: EMValueCallBack<List<EMGroup>>
    ) {
        groupManager.asyncGetJoinedGroupsFromServer(
            pageIndex,
            pageSize, callBack
        )
    }

    /**
     * 获取公开群
     * @param pageSize
     * @param cursor
     * @return
     */
    fun getPublicGroupFromServer(
        pageSize: Int,
        cursor: String?,
        callBack: EMValueCallBack<EMCursorResult<EMGroupInfo>>
    ) {
        SdkHelper.instance.groupManager.asyncGetPublicGroupsFromServer(
            pageSize,
            cursor, callBack
        )
    }

    /**
     * 获取群组信息
     * @param groupId
     * @return
     */
    fun getGroupFromServer(
        groupId: String?,
        callBack: EMValueCallBack<EMGroup>
    ) {
        if (!isLoggedIn) {
            callBack.onError(ErrorCode.EM_NOT_LOGIN, "未登录")
            return
        }
        SdkHelper.instance.groupManager
            .asyncGetGroupFromServer(groupId, callBack)
    }

    /**
     * 加入群组
     * @param group
     * @param reason
     * @return
     */
    fun joinGroup(
        group: EMGroup,
        reason: String?,
        callBack: EMValueCallBack<Boolean>
    ) {
        if (group.isMemberOnly) {
            groupManager.asyncApplyJoinToGroup(group.groupId, reason, object : EMCallBack {
                override fun onSuccess() {
                    callBack.onSuccess(true)
                }

                override fun onError(code: Int, error: String) {
                    callBack.onError(code, error)
                }

                override fun onProgress(progress: Int, status: String) {}
            })
        } else {
            groupManager.asyncJoinGroup(group.groupId, object : EMCallBack {
                override fun onSuccess() {
                    callBack.onSuccess(true)
                }

                override fun onError(code: Int, error: String) {
                    callBack.onError(code, error)
                }

                override fun onProgress(progress: Int, status: String) {}
            })
        }
    }

    fun getGroupMembersByName(groupId: String?, callBack: EMValueCallBack<List<String>>) {
        if (!isLoggedIn) {
            callBack.onError(ErrorCode.EM_NOT_LOGIN, "未登录")
            return
        }
        SdkHelper.instance.groupManager
            .asyncGetGroupFromServer(groupId, object : EMValueCallBack<EMGroup> {
                override fun onSuccess(value: EMGroup) {
                    var members = value.members
                    if (members.size < value.memberCount - value.adminList.size - 1) {
                        members = getAllGroupMemberByServer(groupId)
                    }
                    members.addAll(value.adminList)
                    members.add(value.owner)
                    if (members.isNotEmpty()) {
                        callBack.onSuccess(members)
                    } else {
                        callBack.onError(ErrorCode.EM_ERR_GROUP_NO_MEMBERS, "")
                    }
                }

                override fun onError(error: Int, errorMsg: String) {
                    callBack.onError(error, errorMsg)
                }
            })
    }

    /**
     * 获取群组成员列表(包含管理员和群主)
     * @param groupId
     * @return
     */
    fun getGroupAllMembers(groupId: String?, callBack: EMValueCallBack<List<EaseUser>>) {
        if (!isLoggedIn) {
            callBack.onError(ErrorCode.EM_NOT_LOGIN, "")
            return
        }
        SdkHelper.instance.groupManager
            .asyncGetGroupFromServer(groupId, object : EMValueCallBack<EMGroup> {
                override fun onSuccess(value: EMGroup) {
                    var members = value.members
                    if (members.size < value.memberCount - value.adminList.size - 1) {
                        members = getAllGroupMemberByServer(groupId)
                    }
                    members.addAll(value.adminList)
                    members.add(value.owner)
                    if (members.isNotEmpty()) {
                        val users: List<EaseUser> = EaseUser.parse(members)
                        sortUserData(users)
                        callBack.onSuccess(users)
                    } else {
                        callBack.onError(ErrorCode.EM_ERR_GROUP_NO_MEMBERS, "")
                    }
                }

                override fun onError(error: Int, errorMsg: String) {
                    callBack.onError(error, errorMsg)
                }
            })
    }

    /**
     * 获取群组成员列表(不包含管理员和群主)
     * @param groupId
     * @return
     */
    fun getGroupMembers(groupId: String?, callBack: EMValueCallBack<ArrayList<EaseUser>>) {
        if (!isLoggedIn) {
            callBack.onError(ErrorCode.EM_NOT_LOGIN, "未登录")
            return
        }
        runOnIOThread {
            val members = getAllGroupMemberByServer(groupId)
            val users = ArrayList<EaseUser>()
            if (members.isNotEmpty()) {
                for (i in members.indices) {
                    val user = SdkHelper.instance.getUserInfo(members[i])
                    if (user != null) {
                        users.add(user)
                    } else {
                        users.add(EaseUser(members[i]))
                    }
                }
            }
            sortUserData(users)
            callBack.onSuccess(users)
        }
    }

    /**
     * 获取禁言列表
     * @param groupId
     * @return
     */
    fun getGroupMuteMap(groupId: String?, callBack: ResultCallBack<Map<String, Long>>) {
        EaseThreadManager.getInstance().runOnIOThread {
            var map: Map<String, Long>?
            val result = HashMap<String, Long>()
            val pageSize = 200
            do {
                map = try {
                    groupManager.fetchGroupMuteList(groupId, 0, pageSize)
                } catch (e: HyphenateException) {
                    e.printStackTrace()
                    callBack.onError(e.errorCode, e.message)
                    break
                }
                if (map != null) {
                    result.putAll(map)
                }
            } while (map != null && map.size >= 200)
            callBack.onSuccess(result)
        }
    }

    /**
     * 获取群组黑名单列表
     * @param groupId
     * @return
     */
    fun getGroupBlackList(groupId: String, callBack: ResultCallBack<List<String>>) {
        EaseThreadManager.getInstance().runOnIOThread {
            var list = try {
                fetchGroupBlacklistFromServer(groupId = groupId)
            } catch (e: HyphenateException) {
                e.printStackTrace()
                callBack.onError(e.errorCode, e.message)
                return@runOnIOThread
            }
            if (list.isEmpty()) {
                list = ArrayList()
            }
            callBack.onSuccess(list)
        }
    }

    @Throws(HyphenateException::class)
    private fun fetchGroupBlacklistFromServer(groupId: String): List<String> {
        val pageSize = 200
        var list: List<String>?
        val result = ArrayList<String>()
        do {
            list = groupManager.fetchGroupBlackList(groupId, 0, pageSize)
            if (list != null) {
                result.addAll(list)
            }
        } while (list != null && list.size >= pageSize)
        return result
    }

    /**
     * 获取群公告
     * @param groupId
     * @return
     */
    fun getGroupAnnouncement(
        isFromDb: Boolean,
        groupId: String,
        callBack: EMValueCallBack<String>
    ) {
        if (isFromDb) {
            val result = SdkHelper.instance.groupManager.getGroup(groupId).announcement
            callBack.onSuccess(result)
        } else
            groupManager.asyncFetchGroupAnnouncement(
                groupId, callBack
            )
    }

    /**
     * 获取所有成员
     * @param groupId
     * @return
     */
    fun getAllGroupMemberByServer(groupId: String?): ArrayList<String> {
        // 根据groupId获取群组中所有成员
        val contactList = ArrayList<String>()
        var result: EMCursorResult<String>? = null
        do {
            try {
                val cursor = if (result != null) result.cursor else ""
                result = groupManager.fetchGroupMembers(
                    groupId,
                    cursor,
                    20
                )
            } catch (e: HyphenateException) {
                e.printStackTrace()
            }
            if (result != null) {
                contactList.addAll(result.data)
            }
        } while (result != null && !TextUtils.isEmpty(result.cursor))
        return contactList
    }

    private fun sortUserData(users: List<EaseUser>) {
        Collections.sort(users, Comparator { lhs, rhs ->
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
    }

    fun getAllManageGroups(allGroups: List<EMGroup>?): List<EMGroup> {
        if (!allGroups.isNullOrEmpty()) {
            val manageGroups = ArrayList<EMGroup>()
            for (group in allGroups) {
                if (TextUtils.equals(group.owner, currentUser) || group.adminList.contains(
                        currentUser
                    )
                ) {
                    manageGroups.add(group)
                }
            }
            // 对数据进行排序
            sortData(manageGroups)
            return manageGroups
        }
        return ArrayList()
    }

    /**
     * get all join groups, not contain manage groups
     * @return
     */
    fun getAllJoinGroups(allGroups: List<EMGroup>?): List<EMGroup> {
        if (!allGroups.isNullOrEmpty()) {
            val joinGroups: MutableList<EMGroup> = ArrayList()
            for (group in allGroups) {
                if (!TextUtils.equals(group.owner, currentUser) && !group.adminList.contains(
                        currentUser
                    )
                ) {
                    joinGroups.add(group)
                }
            }
            // 对数据进行排序
            sortData(joinGroups)
            return joinGroups
        }
        return ArrayList()
    }

    /**
     * 对数据进行排序
     * @param groups
     */
    private fun sortData(groups: List<EMGroup>) {
        Collections.sort(groups, Comparator { o1, o2 ->
            val name1 = EaseCommonUtils.getLetter(o1.groupName)
            val name2 = EaseCommonUtils.getLetter(o2.groupName)
            if (name1 == name2) {
                o1.groupId.compareTo(o2.groupId)
            } else {
                if ("#" == name1) {
                    return@Comparator 1
                } else if ("#" == name2) {
                    return@Comparator -1
                }
                name1.compareTo(name2)
            }
        })
    }

    /**
     * 设置群组名称
     * @param groupId
     * @param groupName
     * @return
     */
    fun setGroupName(groupId: String, groupName: String, callBack: EMCallBack) {
        groupManager.asyncChangeGroupName(groupId, groupName, callBack)
    }

    /**
     * 设置群公告
     * @param groupId
     * @param announcement
     * @return
     */
    fun setGroupAnnouncement(groupId: String, announcement: String, callBack: EMCallBack) {
        groupManager.asyncUpdateGroupAnnouncement(
            groupId,
            announcement, callBack
        )
    }

    /**
     * 设置群描述
     * @param groupId
     * @param description
     * @return
     */
    fun setGroupDescription(
        groupId: String?,
        description: String,
        callBack: EMCallBack
    ) {
        groupManager.asyncChangeGroupDescription(groupId, description, callBack)
    }

    /**
     * 获取共享文件
     * @param groupId
     * @param pageNum
     * @param pageSize
     * @return
     */
    fun getSharedFiles(
        groupId: String,
        pageNum: Int,
        pageSize: Int,
        callBack: EMValueCallBack<List<EMMucSharedFile>>
    ) {
        groupManager.asyncFetchGroupSharedFileList(
            groupId,
            pageNum,
            pageSize, callBack
        )
    }

    /**
     * 下载共享文件
     * @param groupId
     * @param fileId
     * @param localFile
     * @return
     */
    fun downloadFile(
        groupId: String?,
        fileId: String?,
        localFile: File,
        callBack: EMValueCallBack<File>
    ) {
        groupManager.asyncDownloadGroupSharedFile(
            groupId,
            fileId,
            localFile.absolutePath,
            object : EMCallBack {
                override fun onSuccess() {
                    callBack.onSuccess(localFile)
                }

                override fun onError(code: Int, error: String) {
                    callBack.onError(code, error)
                }

                override fun onProgress(progress: Int, status: String) {}
            })
    }

    /**
     * 删除服务器端的文件
     * @param groupId
     * @param fileId
     * @return
     */
    fun deleteFile(groupId: String?, fileId: String, callBack: EMCallBack) {
        groupManager.asyncDeleteGroupSharedFile(groupId, fileId, callBack)
    }

    /**
     * 上传文件
     * @param groupId
     * @param filePath
     * @return
     */
    fun uploadFile(groupId: String?, filePath: String?, callBack: EMCallBack) {
        groupManager.asyncUploadGroupSharedFile(groupId, filePath, callBack)
    }

    /**
     * 邀请群成员
     * @param isOwner
     * @param groupId
     * @param members
     * @return
     */
    fun addMembers(
        isOwner: Boolean,
        groupId: String?,
        members: Array<String>?,
        callBack: EMValueCallBack<Boolean>
    ) {
        if (isOwner) {
            groupManager.asyncAddUsersToGroup(groupId, members, object : EMCallBack {
                override fun onSuccess() {
                    callBack.onSuccess(true)
                }

                override fun onError(code: Int, error: String) {
                    callBack.onError(code, error)
                }

                override fun onProgress(progress: Int, status: String) {}
            })
        } else {
            groupManager.asyncInviteUser(groupId, members, null, object : EMCallBack {
                override fun onSuccess() {
                    callBack.onSuccess(true)
                }

                override fun onError(code: Int, error: String) {
                    callBack.onError(code, error)
                }

                override fun onProgress(progress: Int, status: String) {}
            })
        }
    }

    /**
     * 移交群主权限
     * @param groupId
     * @param username
     * @return
     */
    fun changeOwner(
        groupId: String?,
        username: String?,
        callBack: ResultCallBack<Boolean>
    ) {
        groupManager.asyncChangeOwner(
            groupId,
            username,
            object : EMValueCallBack<EMGroup?> {
                override fun onSuccess(value: EMGroup?) {
                    callBack.onSuccess(true)
                }

                override fun onError(error: Int, errorMsg: String) {
                    callBack.onError(error, errorMsg)
                }
            })
    }

    /**
     * 设为群管理员
     * @param groupId
     * @param username
     * @return
     */
    fun addGroupAdmin(groupId: String?, username: String?, callBack: ResultCallBack<String>) {
        groupManager.asyncAddGroupAdmin(
            groupId,
            username,
            object : EMValueCallBack<EMGroup?> {
                override fun onSuccess(value: EMGroup?) {
                    callBack.onSuccess(
                        AppClient.instance.getString(
                            R.string.demo_group_member_add_admin,
                            username
                        )
                    )
                }

                override fun onError(error: Int, errorMsg: String) {
                    callBack.onError(error, errorMsg)
                }
            })
    }

    /**
     * 移除群管理员
     * @param groupId
     * @param username
     * @return
     */
    fun removeGroupAdmin(groupId: String?, username: String?, callBack: ResultCallBack<String>) {
        groupManager.asyncRemoveGroupAdmin(
            groupId,
            username,
            object : EMValueCallBack<EMGroup?> {
                override fun onSuccess(value: EMGroup?) {
                    callBack.onSuccess(
                        AppClient.instance.getString(
                            R.string.demo_group_member_remove_admin,
                            username
                        )
                    )
                }

                override fun onError(error: Int, errorMsg: String) {
                    callBack.onError(error, errorMsg)
                }
            })
    }

    /**
     * 移出群
     * @param groupId
     * @param username
     * @return
     */
    fun removeUserFromGroup(groupId: String?, username: String?, callBack: ResultCallBack<String>) {
        groupManager.asyncRemoveUserFromGroup(groupId, username, object : EMCallBack {
            override fun onSuccess() {
                callBack.onSuccess(
                    AppClient.instance.getString(
                        R.string.demo_group_member_remove,
                        username
                    )
                )
            }

            override fun onError(code: Int, error: String) {
                callBack.onError(code, error)
            }

            override fun onProgress(progress: Int, status: String) {}
        })
    }

    /**
     * 添加到群黑名单
     * @param groupId
     * @param username
     * @return
     */
    fun blockUser(groupId: String?, username: String?, callBack: ResultCallBack<String>) {
        groupManager.asyncBlockUser(groupId, username, object : EMCallBack {
            override fun onSuccess() {
                callBack.onSuccess(
                    AppClient.instance.getString(
                        R.string.demo_group_member_add_black,
                        username
                    )
                )
            }

            override fun onError(code: Int, error: String) {
                callBack.onError(code, error)
            }

            override fun onProgress(progress: Int, status: String) {}
        })
    }

    /**
     * 移出群黑名单
     * @param groupId
     * @param username
     * @return
     */
    fun unblockUser(groupId: String?, username: String?, callBack: ResultCallBack<String>) {
        groupManager.asyncUnblockUser(groupId, username, object : EMCallBack {
            override fun onSuccess() {
                callBack.onSuccess(
                    AppClient.instance.getString(
                        R.string.demo_group_member_remove_black,
                        username
                    )
                )
            }

            override fun onError(code: Int, error: String) {
                callBack.onError(code, error)
            }

            override fun onProgress(progress: Int, status: String) {}
        })
    }

    /**
     * 禁言
     * @param groupId
     * @param usernames
     * @return
     */
    fun muteGroupMembers(
        groupId: String?,
        usernames: List<String?>,
        duration: Long,
        callBack: ResultCallBack<String>
    ) {
        groupManager.aysncMuteGroupMembers(
            groupId,
            usernames,
            duration,
            object : EMValueCallBack<EMGroup?> {
                override fun onSuccess(value: EMGroup?) {
                    callBack.onSuccess(
                        AppClient.instance.getString(
                            R.string.demo_group_member_mute,
                            usernames[0]
                        )
                    )
                }

                override fun onError(error: Int, errorMsg: String) {
                    callBack.onError(error, errorMsg)
                }
            })
    }

    /**
     * 禁言
     * @param groupId
     * @param usernames
     * @return
     */
    fun unMuteGroupMembers(
        groupId: String?,
        usernames: List<String>,
        callBack: ResultCallBack<String>
    ) {
        groupManager.asyncUnMuteGroupMembers(
            groupId,
            usernames,
            object : EMValueCallBack<EMGroup> {
                override fun onSuccess(value: EMGroup) {
                    callBack.onSuccess(
                        AppClient.instance.getString(
                            R.string.demo_group_member_remove_mute,
                            usernames[0]
                        )
                    )
                }

                override fun onError(error: Int, errorMsg: String) {
                    callBack.onError(error, errorMsg)
                }
            })
    }

    /**
     * 退群
     * @param groupId
     * @return
     */
    fun leaveGroup(groupId: String, callBack: EMCallBack) {
        groupManager.asyncLeaveGroup(groupId, callBack)
    }

    /**
     * 解散群
     * @param groupId
     * @return
     */
    fun destroyGroup(groupId: String, callBack: EMCallBack) {
        groupManager.asyncDestroyGroup(groupId, callBack)
    }

    /**
     * create a new group
     * @param groupName
     * @param desc
     * @param allMembers
     * @param reason
     * @param option
     * @return
     */
    fun createGroup(
        groupName: String?,
        desc: String?,
        allMembers: Array<String>?,
        reason: String?,
        option: EMGroupOptions?,
        callBack: EMValueCallBack<EMGroup>
    ) {
        groupManager.asyncCreateGroup(
            groupName,
            desc,
            allMembers,
            reason,
            option, callBack
        )
    }

    /**
     * 屏蔽群消息
     * @param groupId
     * @return
     */
    fun blockGroupMessage(groupId: String, callBack: ResultCallBack<Boolean>) {
        groupManager.asyncBlockGroupMessage(groupId, object : EMCallBack {
            override fun onSuccess() {
                callBack.onSuccess(true)
            }

            override fun onError(code: Int, error: String) {
                callBack.onError(code, error)
            }

            override fun onProgress(progress: Int, status: String) {}
        })
    }

    /**
     * 取消屏蔽群消息
     * @param groupId
     * @return
     */
    fun unblockGroupMessage(groupId: String, callBack: ResultCallBack<Boolean>) {
        groupManager.asyncUnblockGroupMessage(groupId, object : EMCallBack {
            override fun onSuccess() {
                callBack.onSuccess(true)
            }

            override fun onError(code: Int, error: String) {
                callBack.onError(code, error)
            }

            override fun onProgress(progress: Int, status: String) {}
        })
    }
}