package com.hyphenate.easeim.section.ui.chat.vm

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.hyphenate.easeim.common.interfaceOrImplement.ResultCallBack
import com.hyphenate.easeim.common.net.Resource
import com.hyphenate.easeim.common.repositories.MediaManagerRepository
import com.hyphenate.easeui.model.VideoEntity

class VideoListVm(application: Application) : AndroidViewModel(application) {
    private val repository = MediaManagerRepository()


    val videoList get() = _videoList
    private val _videoList = MutableLiveData<Resource<List<VideoEntity>?>>()


    fun getVideoList(context: Context?) {
        repository.getVideoListFromMediaAndSelfFolder(context!!, object :

            ResultCallBack<List<VideoEntity>>() {
            override fun onError(error: Int, errorMsg: String?) {
                _videoList.postValue(Resource.error(error, errorMsg, null))
            }

            override fun onSuccess(value: List<VideoEntity>) {
                _videoList.postValue(Resource.success(value))
            }

        })
    }

}