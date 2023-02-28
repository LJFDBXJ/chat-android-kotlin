package com.hyphenate.easeim.common.utils

import com.hyphenate.util.EMLog
import java.util.*

/**
 * author lijian
 * email: Allenlee@easemob.com
 * date: 03/28/2021
 */
class FetchUserInfoList private constructor() {
    /**
     * 初始化
     */
    init {
        clear()
    }

    fun clear() {
        synchronized(fetchUsers) {
            fetchUsers.clear()
        }
    }

    /**
     * 获取队列长度
     */
    val userSize: Int
        get() {
            synchronized(fetchUsers) { return fetchUsers.size }
        }

    /**
     * 入队列
     * @param userId
     */
    fun addUserId(userId: String) {
        synchronized(fetchUsers) {
            if (!fetchUsers.contains(userId)) {
                // 增加用户信息ID
                fetchUsers.addLast(userId)
                EMLog.i(TAG, "push addFetchUser userId:" + userId + "  size:" + fetchUsers.size)
            } else {
                EMLog.i(TAG, "current user is already in fetchUserList userId:$userId")
            }
        }
    }

    /**
     * 出队列
     * @return
     */
    val userId: String?
        get() {
            synchronized(fetchUsers) {
                return if (fetchUsers.size > 0) {
                    val userId = fetchUsers.removeFirst()
                    EMLog.i(
                        TAG, "pop fetchUsers  UserId:" + userId
                                + " size:" + fetchUsers.size
                    )
                    userId
                } else {
                    null
                }
            }
        }

    companion object {
        private val TAG = FetchUserInfoList::class.java.simpleName
        private var fetchUsers = LinkedList<String>()
        private var mInstance: FetchUserInfoList? = null

        @JvmStatic
        @get:Synchronized
        val instance: FetchUserInfoList
            get() {
                if (mInstance == null) {
                    mInstance = FetchUserInfoList()
                }
                return mInstance!!
            }
    }


}