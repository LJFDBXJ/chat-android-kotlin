package com.hyphenate.easeim.section.ui.me.vm

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMPushConfigs
import com.hyphenate.chat.EMSilentModeResult
import com.hyphenate.chat.EMUserInfo
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.ktx.toast
import com.hyphenate.easeim.common.livedatas.LiveDataBus
import com.hyphenate.easeim.common.repositories.EMPushManagerRepository
import com.hyphenate.easeim.common.utils.PreferenceManager
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.util.EMLog

class OfflinePushSetVm(application: Application) : AndroidViewModel(application) {
    private val repository: EMPushManagerRepository = EMPushManagerRepository()

    val configs: LiveData<EMPushConfigs?> get() = _configs
    private val _configs = MutableLiveData<EMPushConfigs?>()

    val disable: LiveData<Boolean> get() = _disable
    private val _disable = MutableLiveData<Boolean>()

    val enable: LiveData<Boolean> get() = _enable
    private val _enable = MutableLiveData<Boolean>()

    val updatePushNickname: LiveData<Boolean> get() = _updatePushNickname
    private val _updatePushNickname = MutableLiveData<Boolean>()


    fun pushConfigs() {
        repository.pushConfigsFromServer(
            callBack = object : EMValueCallBack<EMPushConfigs> {
                override fun onSuccess(value: EMPushConfigs?) {
                    _configs.postValue(value)
                }

                override fun onError(error: Int, errorMsg: String?) {
                    _configs.postValue(null)

                }

            })
    }

    fun disableOfflinePush(start: Int, end: Int) {
        repository.disableOfflinePush(
            start = start,
            end = end,
            callBack = object : EMValueCallBack<EMSilentModeResult> {
                override fun onSuccess(value: EMSilentModeResult) {
                    _disable.postValue(true)
                }

                override fun onError(error: Int, errorMsg: String?) {
                    _disable.postValue(false)
                }
            })
    }

    //TODO 需要复查
    fun enableOfflinePush() {
        repository.enableOfflinePush(
            callBack = object : EMValueCallBack<EMSilentModeResult> {
                override fun onSuccess(value: EMSilentModeResult) {
                    _enable.postValue(true)

                }

                override fun onError(error: Int, errorMsg: String?) {
                    _enable.postValue(false)
                }

            })
    }

    //同时更新推送昵称
    fun updatePushNickname(nickname: String?) {
        repository.updatePushNickname(
            nickname = nickname,
            callBack = object : EMValueCallBack<Boolean> {
                override fun onSuccess(value: Boolean) {
                    _updatePushNickname.postValue(value)
                }

                override fun onError(error: Int, errorMsg: String?) {
                    _updatePushNickname.postValue(false)
                }

            })
    }

    //发送联系人更新事件
    fun sendMessageEvent(name: String) {
        PreferenceManager.getInstance().currentUserNick = name
        val event = EaseEvent.create(
            DemoConstant.NICK_NAME_CHANGE,
            EaseEvent.TYPE.CONTACT,
        )
        //发送联系人更新事件
        event.message = name
        LiveDataBus.get().use(DemoConstant.NICK_NAME_CHANGE).postValue(event)
    }

    fun update(context: Context, nick: String) {
        EMClient.getInstance().userInfoManager().updateOwnInfoByAttribute(
            EMUserInfo.EMUserInfoType.NICKNAME,
            nick,
            object : EMValueCallBack<String> {
                override fun onSuccess(value: String) {
                    EMLog.d("update", "fetchUserInfoById :$value")
                    context.toast(R.string.demo_offline_nickname_update_success)
                    sendMessageEvent(nick)
                    updatePushNickname(nick)

                }

                override fun onError(error: Int, errorMsg: String) {
                    EMLog.d(
                        "update",
                        "fetchUserInfoById  error:$error errorMsg:$errorMsg"
                    )
                    context.toast(R.string.demo_offline_nickname_update_failed)
                }
            })
    }

}