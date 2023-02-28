package com.hyphenate.easeim.common.manager

import android.content.Context
import com.hyphenate.chat.EMClient
import com.hyphenate.util.EMLog
import com.hyphenate.EMValueCallBack
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.utils.EaseCommonUtils
import com.hyphenate.easeim.SdkHelper
import com.parse.*
import java.lang.Exception
import java.util.ArrayList

class ParseManager private constructor() {
    fun onInit(context: Context) {
        val appContext = context.applicationContext
        Parse.enableLocalDatastore(appContext)
        //		Parse.initialize(context, ParseAppID, ParseClientKey);
        Parse.initialize(
            Parse.Configuration.Builder(appContext)
                .applicationId(ParseAppID)
                .server(parseServer)
                .build()
        )
    }

    fun updateParseNickName(nickname: String?): Boolean {
        val username = EMClient.getInstance().currentUser
        val pQuery = ParseQuery.getQuery<ParseObject>(CONFIG_TABLE_NAME)
        pQuery.whereEqualTo(CONFIG_USERNAME, username)
        var pUser: ParseObject? = null
        try {
            pUser = pQuery.first
            if (pUser == null) {
                return false
            }
            pUser.put(CONFIG_NICK, nickname)
            pUser.save()
            return true
        } catch (e: ParseException) {
            if (e.code == ParseException.OBJECT_NOT_FOUND) {
                pUser = ParseObject(CONFIG_TABLE_NAME)
                pUser.put(CONFIG_USERNAME, username)
                pUser.put(CONFIG_NICK, nickname)
                try {
                    pUser.save()
                    return true
                } catch (e1: ParseException) {
                    e1.printStackTrace()
                    EMLog.e(TAG, "parse error " + e1.message)
                }
            }
            e.printStackTrace()
            EMLog.e(TAG, "parse error " + e.message)
        } catch (e: Exception) {
            EMLog.e(TAG, "updateParseNickName error")
            e.printStackTrace()
        }
        return false
    }

    fun getContactInfos(usernames: List<String>, callback: EMValueCallBack<List<EaseUser>>) {
        val pQuery = ParseQuery.getQuery<ParseObject>(CONFIG_TABLE_NAME)
        pQuery.whereContainedIn(CONFIG_USERNAME, usernames)
        pQuery.findInBackground { arg0, arg1 ->
            if (arg0 != null) {
                val mList: MutableList<EaseUser> = ArrayList()
                for (pObject in arg0) {
                    val user = EaseUser(pObject.getString(CONFIG_USERNAME))
                    val parseFile = pObject.getParseFile(CONFIG_AVATAR)
                    if (parseFile != null) {
                        user.avatar = parseFile.url
                    }
                    user.nickname = pObject.getString(CONFIG_NICK)
                    EaseCommonUtils.setUserInitialLetter(user)
                    mList.add(user)
                }
                callback.onSuccess(mList)
            } else {
                callback.onError(arg1.code, arg1.message)
            }
        }
    }

    fun asyncGetCurrentUserInfo(callback: EMValueCallBack<EaseUser?>) {
        val username = EMClient.getInstance().currentUser
        asyncGetUserInfo(username, object : EMValueCallBack<EaseUser?> {
            override fun onSuccess(value: EaseUser?) {
                callback.onSuccess(value)
            }

            override fun onError(error: Int, errorMsg: String) {
                if (error == ParseException.OBJECT_NOT_FOUND) {
                    val pUser = ParseObject(CONFIG_TABLE_NAME)
                    pUser.put(CONFIG_USERNAME, username)
                    pUser.saveInBackground { arg0 ->
                        if (arg0 == null) {
                            callback.onSuccess(EaseUser(username))
                        }
                    }
                } else {
                    callback.onError(error, errorMsg)
                }
            }
        })
    }

    fun asyncGetUserInfo(username: String?, callback: EMValueCallBack<EaseUser?>?) {
        val pQuery = ParseQuery.getQuery<ParseObject>(CONFIG_TABLE_NAME)
        pQuery.whereEqualTo(CONFIG_USERNAME, username)
        pQuery.getFirstInBackground { pUser, e ->
            if (pUser != null) {
                val nick = pUser.getString(CONFIG_NICK)
                val pFile = pUser.getParseFile(CONFIG_AVATAR)
                if (callback != null) {
                    var user = SdkHelper.instance.getContactList()[username]
                    if (user != null) {
                        user.nickname = nick
                        if (pFile != null && pFile.url != null) {
                            user.avatar = pFile.url
                        }
                    } else {
                        user = EaseUser(username!!)
                        user.nickname = nick
                        if (pFile != null && pFile.url != null) {
                            user.avatar = pFile.url
                        }
                    }
                    callback.onSuccess(user)
                }
            } else {
                callback?.onError(e.code, e.message)
            }
        }
    }

    fun uploadParseAvatar(data: ByteArray?): String? {
        val username = EMClient.getInstance().currentUser
        val pQuery = ParseQuery.getQuery<ParseObject>(CONFIG_TABLE_NAME)
        pQuery.whereEqualTo(CONFIG_USERNAME, username)
        var pUser: ParseObject? = null
        try {
            pUser = pQuery.first
            if (pUser == null) {
                pUser = ParseObject(CONFIG_TABLE_NAME)
                pUser.put(CONFIG_USERNAME, username)
            }
            val pFile = ParseFile(data)
            pUser.put(CONFIG_AVATAR, pFile)
            pUser.save()
            return pFile.url
        } catch (e: ParseException) {
            if (e.code == ParseException.OBJECT_NOT_FOUND) {
                try {
                    pUser = ParseObject(CONFIG_TABLE_NAME)
                    pUser.put(CONFIG_USERNAME, username)
                    val pFile = ParseFile(data)
                    pUser.put(CONFIG_AVATAR, pFile)
                    pUser.save()
                    return pFile.url
                } catch (e1: ParseException) {
                    e1.printStackTrace()
                    EMLog.e(TAG, "parse error " + e1.message)
                }
            } else {
                e.printStackTrace()
                EMLog.e(TAG, "parse error " + e.message)
            }
        } catch (e: Exception) {
            EMLog.e(TAG, "uploadParseAvatar error")
            e.printStackTrace()
        }
        return null
    }

    companion object {
        private val TAG = ParseManager::class.java.simpleName
        private const val ParseAppID = "UUL8TxlHwKj7ZXEUr2brF3ydOxirCXdIj9LscvJs"
        private const val ParseClientKey = "B1jH9bmxuYyTcpoFfpeVslhmLYsytWTxqYqKQhBJ"

        //	private static final String ParseAppID = "task";
        //	private static final String ParseClientKey = "123456789";
        private const val CONFIG_TABLE_NAME = "hxuser"
        private const val CONFIG_USERNAME = "username"
        private const val CONFIG_NICK = "nickname"
        private const val CONFIG_AVATAR = "avatar"
        private const val parseServer = "http://parse.easemob.com/parse/"
        @JvmStatic
		val instance = ParseManager()
    }
}