package com.hyphenate.easeim.section.ui.chat.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.hyphenate.easeim.common.repositories.EMConferenceManagerRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hyphenate.EMValueCallBack
import com.hyphenate.easeim.section.conference.ContactState

class ConferenceInviteVm(application: Application) : AndroidViewModel(application) {
    var repository = EMConferenceManagerRepository()


    val conferenceInvite: LiveData<List<ContactState>?> get() = _conferenceInvite
    private val _conferenceInvite = MutableLiveData<List<ContactState>?>()

    fun getConferenceMembers(groupId: String?, existMember: Array<String>?) {
        repository.getConferenceMembers(
            groupId = groupId,
            existMember = existMember,
            callBack = object : EMValueCallBack<ArrayList<ContactState>> {
                override fun onSuccess(value: ArrayList<ContactState>?) {
                    _conferenceInvite.postValue(value)
                }

                override fun onError(error: Int, errorMsg: String?) {
                    _conferenceInvite.postValue(null)
                }

            })
    }


}