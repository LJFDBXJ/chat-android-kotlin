package com.hyphenate.easeim.section.ui.group.vm

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hyphenate.EMCallBack
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMMucSharedFile
import com.hyphenate.easeim.common.repositories.EMGroupManagerRepository
import com.hyphenate.easeui.utils.EaseFileUtils
import com.hyphenate.util.PathUtil
import java.io.File

class SharedFilesVm() : ViewModel() {
    private val repository = EMGroupManagerRepository()

    val filesObservable: LiveData<List<EMMucSharedFile>?> get() = _filesObservable
    private val _filesObservable = MutableLiveData<List<EMMucSharedFile>?>()
    val showFileObservable: LiveData<File?> get() = _showFileObservable
    private val _showFileObservable = MutableLiveData<File?>()

    val refreshFiles: LiveData<Boolean> get() = _refreshFiles
    private val _refreshFiles = MutableLiveData<Boolean>()


    fun getSharedFiles(groupId: String?, pageNum: Int, pageSize: Int) {
        repository.getSharedFiles(groupId!!, pageNum, pageSize,
            object : EMValueCallBack<List<EMMucSharedFile>> {
                override fun onSuccess(value: List<EMMucSharedFile>) {
                    _filesObservable.postValue(value)

                }

                override fun onError(error: Int, errorMsg: String) {
                    _filesObservable.postValue(null)
                }
            })
    }

    /**
     * 展示文件
     * @param groupId
     * @param file
     */
    fun showFile(groupId: String?, file: EMMucSharedFile) {
        val localFile = File(PathUtil.getInstance().filePath, file.fileName)
        if (localFile.exists()) {
            _showFileObservable.postValue(localFile)
            return
        }
        repository.downloadFile(groupId, file.fileId, localFile,
            object : EMValueCallBack<File> {
                override fun onSuccess(value: File?) {
                    _showFileObservable.postValue(value)
                }

                override fun onError(error: Int, errorMsg: String?) {
                    _showFileObservable.postValue(null)
                }

            }
        )

    }

    /**
     * 删除文件
     * @param groupId
     * @param file
     */
    fun deleteFile(groupId: String?, file: EMMucSharedFile) {
        //先判断是否有本地文件，如果有的话，先进行删除
        val local = File(PathUtil.getInstance().filePath, file.fileName)
        if (local.exists()) {
            try {
                local.delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        repository.deleteFile(groupId, file.fileId,object : EMCallBack {
            override fun onSuccess() {
                _refreshFiles.postValue(true)
            }

            override fun onError(code: Int, error: String) {
                _refreshFiles.postValue(false)

            }

            override fun onProgress(progress: Int, status: String) {}
        })

    }

    /**
     * 上传文件
     * @param groupId
     * @param uri
     */
    fun uploadFileByUri(context:Context,groupId: String?, uri: Uri) {
        if (!EaseFileUtils.isFileExistByUri(context, uri)) {
//            ErrorCode.EM_ERR_FILE_NOT_EXIST
            _refreshFiles.postValue(false)
            return
        }
        repository.uploadFile(groupId, uri.toString(),object : EMCallBack {
            override fun onSuccess() {
                _refreshFiles.postValue(true)
            }

            override fun onError(code: Int, error: String) {
                _refreshFiles.postValue(false)

            }

            override fun onProgress(progress: Int, status: String) {}
        })

    }

}