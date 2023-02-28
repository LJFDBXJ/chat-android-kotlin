package com.hyphenate.easeim.common.repositories

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import com.hyphenate.EMCallBack
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMChatRoom
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMCursorResult
import com.hyphenate.chat.EMPageResult
import com.hyphenate.easeim.common.interfaceOrImplement.ResultCallBack
import com.hyphenate.easeim.common.net.ErrorCode
import com.hyphenate.easeim.common.net.Resource
import com.hyphenate.easeui.manager.EaseThreadManager
import com.hyphenate.exceptions.HyphenateException

class EMChatRoomManagerRepository : BaseEMRepository() {
    fun loadChatRoomsFromServer(
        pageNum: Int,
        pageSize: Int,
        callBack: EMValueCallBack<List<EMChatRoom>>
    ) {
        chatRoomManager.asyncFetchPublicChatRoomsFromServer(
            pageNum,
            pageSize,
            object : EMValueCallBack<EMPageResult<EMChatRoom>> {
                override fun onSuccess(value: EMPageResult<EMChatRoom>) {
                    if (!value.data.isNullOrEmpty()) {
                        Log.e("TAG", "chatRooms = " + value.data.toString())
                        callBack.onSuccess(value.data)
                    } else {
                        callBack.onError(ErrorCode.EM_ERR_UNKNOWN, "")
                    }
                }

                override fun onError(error: Int, errorMsg: String) {
                    callBack.onError(error, errorMsg)
                }
            })
    }

    /**
     * get chat room from server
     * @param roomId
     * @return
     */
    fun getChatRoomById(roomId: String, callBack: EMValueCallBack<EMChatRoom>) {
        chatRoomManager.asyncFetchChatRoomFromServer(roomId, callBack)
    }

    fun loadMembers(roomId: String?, callBack: ResultCallBack<Resource<List<String>?>>) {
        EaseThreadManager.getInstance().runOnIOThread {
            val memberList: MutableList<String> = ArrayList()
            try {
                val chatRoom = chatRoomManager.fetchChatRoomFromServer(roomId)
                // page size set to 20 is convenient for testing, should be applied to big value
                var result = EMCursorResult<String>()
                memberList.clear()
                do {
                    result = EMClient.getInstance().chatroomManager()
                        .fetchChatRoomMembers(roomId, result.cursor, 20)
                    memberList.addAll(result.data)
                } while (!result.cursor.isNullOrEmpty())
                memberList.remove(chatRoom.owner)
                memberList.removeAll(chatRoom.adminList)
                if (isAdmin(chatRoom)) {
                    //Set<String> muteList = getChatRoomManager().fetchChatRoomMuteList(roomId, 0, 500).keySet();
                    val blacks = chatRoomManager.fetchChatRoomBlackList(roomId, 0, 500)
                    //memberList.removeAll(muteList);
                    memberList.removeAll(blacks)
                }
            } catch (e: HyphenateException) {
                e.printStackTrace()
                callBack.onError(e.errorCode, e.message)
            }
            callBack.onSuccess(Resource.success(memberList))
        }
    }

    /**
     * 获取聊天室公告内容
     * @param roomId
     * @return
     */
    fun fetchChatRoomAnnouncement(roomId: String?, callBack: ResultCallBack<String>) {
        chatRoomManager.asyncFetchChatRoomAnnouncement(roomId, callBack)

    }

    /**
     * update chat room announcement
     * @param roomId
     * @param announcement
     * @return
     */
    fun updateAnnouncement(
        roomId: String?,
        announcement: String,
        callBack: ResultCallBack<String>
    ) {
        chatRoomManager.asyncUpdateChatRoomAnnouncement(
            roomId,
            announcement,
            object : EMCallBack {
                override fun onSuccess() {
                    callBack.onSuccess(announcement)
                }

                override fun onError(code: Int, error: String) {
                    callBack.onError(code, error)
                }

                override fun onProgress(progress: Int, status: String) {}
            })
    }

    /**
     * change chat room subject
     * @param roomId
     * @param newSubject
     * @return
     */
    fun changeChatRoomSubject(
        roomId: String?,
        newSubject: String?,
        callBack: EMValueCallBack<EMChatRoom>
    ) {
        chatRoomManager.asyncChangeChatRoomSubject(
            roomId,
            newSubject, callBack
        )
    }

    /**
     * change chat room description
     * @param roomId
     * @param newDescription
     * @return
     */
    fun changeChatroomDescription(
        roomId: String?,
        newDescription: String?,
        callBack: EMValueCallBack<EMChatRoom>,
    ) {
        chatRoomManager.asyncChangeChatroomDescription(
            roomId,
            newDescription,
            callBack
        )
    }

    /**
     * 判断是否是管理员或者群主
     * @param room
     * @return
     */
    private fun isAdmin(room: EMChatRoom): Boolean {
        return TextUtils.equals(room.owner, currentUser) || room.adminList.contains(currentUser)
    }

    /**
     * 移交聊天室群主权限
     * @param groupId
     * @param username
     * @return
     */
    fun changeOwner(
        groupId: String?,
        username: String,
        callBack: EMValueCallBack<EMChatRoom>,
    ) {
        chatRoomManager.asyncChangeOwner(
            groupId,
            username, callBack
        )
    }

    /**
     * 获取聊天室禁言列表
     * @param groupId
     * @return
     */
    fun getChatRoomMuteMap(groupId: String?, callBack: ResultCallBack<Map<String, Long>>) {
        EaseThreadManager.getInstance().runOnIOThread {
            var map: Map<String, Long>? = null
            val result: MutableMap<String, Long> = HashMap()
            val pageSize = 200
            do {
                map = try {
                    chatRoomManager.fetchChatRoomMuteList(groupId, 0, pageSize)
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
     * 获取聊天室黑名单列表
     * @param groupId
     * @return
     */
    fun getChatRoomBlackList(groupId: String?, callBack: EMValueCallBack<ArrayList<String>>) {
        EaseThreadManager.getInstance().runOnIOThread {
            var list: List<String>? = null
            val result = ArrayList<String>()
            val pageSize = 200
            do {
                list = try {
                    chatRoomManager.fetchChatRoomBlackList(groupId, 0, pageSize)
                } catch (e: HyphenateException) {
                    e.printStackTrace()
                    callBack.onError(e.errorCode, e.message)
                    break
                }
                if (list != null) {
                    result.addAll(list)
                }
            } while (list != null && list.size >= 200)
            callBack.onSuccess(result)
        }
    }

    /**
     * 设为聊天室管理员
     * @param groupId
     * @param username
     * @return
     */
    fun addChatRoomAdmin(
        groupId: String?,
        username: String?,
        callBack: EMValueCallBack<EMChatRoom>
    ) {
        chatRoomManager.asyncAddChatRoomAdmin(
            groupId,
            username, callBack
        )
    }

    /**
     * 移除聊天室管理员
     * @param groupId
     * @param username
     * @return
     */
    fun removeChatRoomAdmin(
        groupId: String?,
        username: String,
        callBack: EMValueCallBack<EMChatRoom>
    ) {
        chatRoomManager.asyncRemoveChatRoomAdmin(
            groupId,
            username, callBack
        )
    }

    /**
     * 移出聊天室
     * @param groupId
     * @param usernames
     * @return
     */
    fun removeUserFromChatRoom(
        groupId: String?,
        usernames: List<String>?,
        callBack: EMValueCallBack<EMChatRoom>
    ) {
        chatRoomManager.asyncRemoveChatRoomMembers(
            groupId,
            usernames, callBack
        )
    }

    /**
     * 添加到聊天室黑名单
     * 需要拥有者或者管理员权限
     * @param groupId
     * @param username
     * @return
     */
    fun blockUser(
        groupId: String?, username: List<String>, callBack: EMValueCallBack<EMChatRoom>
    ) {
        chatRoomManager.asyncBlockChatroomMembers(
            groupId,
            username, callBack
        )
    }

    /**
     * 移出聊天室黑名单
     * @param groupId
     * @param username
     * @return
     */
    fun unblockUser(
        groupId: String?,
        username: List<String>,
        callBack: EMValueCallBack<EMChatRoom>

    ) {
        chatRoomManager.asyncUnBlockChatRoomMembers(
            groupId,
            username, callBack
        )
    }

    /**
     * 禁言
     * 需要聊天室拥有者或者管理员权限
     * @param groupId
     * @param usernames
     * @return
     */
    fun muteChatRoomMembers(
        groupId: String?,
        usernames: List<String>,
        duration: Long,
        callBack: EMValueCallBack<EMChatRoom>

    ) {
        chatRoomManager.asyncMuteChatRoomMembers(
            groupId,
            usernames,
            duration, callBack
        )
    }

    /**
     * 禁言
     * @param groupId
     * @param usernames
     * @return
     */
    fun unMuteChatRoomMembers(
        groupId: String?,
        usernames: List<String>,
        callBack: EMValueCallBack<EMChatRoom>

    ) {
        chatRoomManager.asyncUnMuteChatRoomMembers(
            groupId,
            usernames, callBack
        )
    }

    /**
     * 退群
     * @param groupId
     * @return
     */
    fun leaveChatRoom(groupId: String, callBack: ResultCallBack<Boolean>) {
        chatRoomManager.leaveChatRoom(groupId)
        callBack.onSuccess(true)
    }

    /**
     * 解散群
     * @param groupId
     * @return
     */
    fun destroyChatRoom(groupId: String, callBack: EMCallBack) {
        chatRoomManager.asyncDestroyChatRoom(groupId, callBack)
    }

    /**
     * create new chat room
     * @param subject
     * @param description
     * @param welcomeMessage
     * @param maxUserCount
     * @param members
     * @return
     */
    fun createChatRoom(
        subject: String?, description: String?, welcomeMessage: String?,
        maxUserCount: Int, members: List<String>?,
        callBack: EMValueCallBack<EMChatRoom>
    ) {
        chatRoomManager.asyncCreateChatRoom(
            subject,
            description,
            welcomeMessage,
            maxUserCount,
            members,
            object : EMValueCallBack<EMChatRoom> {
                override fun onSuccess(value: EMChatRoom) {
                    callBack.onSuccess(value)
                }

                override fun onError(error: Int, errorMsg: String) {
                    callBack.onError(error, errorMsg)
                }
            })
    }
}