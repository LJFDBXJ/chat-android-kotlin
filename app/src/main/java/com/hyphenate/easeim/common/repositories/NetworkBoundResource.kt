package com.hyphenate.easeim.common.repositories

//import androidx.annotation.MainThread
//import androidx.annotation.WorkerThread
//import com.hyphenate.easeim.common.net.Resource.Companion.loading
//import com.hyphenate.easeim.common.net.Resource.Companion.success
//import com.hyphenate.easeim.common.net.Resource.Companion.error
//import com.hyphenate.easeui.manager.EaseThreadManager
//import androidx.lifecycle.MediatorLiveData
//import androidx.lifecycle.LiveData
//import com.hyphenate.easeim.common.interfaceOrImplement.ResultCallBack
//import com.hyphenate.util.EMLog
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.Observer
//import com.hyphenate.easeim.common.net.ErrorCode
//import com.hyphenate.easeim.common.net.Resource
//import com.hyphenate.easeim.common.net.Result
//import java.lang.Exception

/**
 * 作为服务器拉取数据和本地数据融合类
 *
 * @param <DbType> 本地数据库中拉取的数据
 * @param <RequestType> 服务器中拉取的数据
</RequestType></DbType> */
//abstract class NetworkBoundResource<DbType, RequestType> {
//    private val mThreadManager: EaseThreadManager
//    private val result = MediatorLiveData<Resource<DbType?>>()
//    private val lastFailSource: LiveData<DbType>? = null
//
//    /**
//     * work on main thread
//     */
//    private fun init() {
//        // 通知UI开始加载
//        result.value = loading(null)
//        val dbSource = safeLoadFromDb()
//        result.addSource(dbSource, Observer<S> { data: S? ->
//            result.removeSource(dbSource)
//            if (shouldFetch(data)) {
//                fetchFromNetwork(dbSource)
//            } else {
//                result.addSource(
//                    dbSource,
//                    Observer<S> { newData: S? -> setValue(success(newData)) })
//            }
//        })
//    }
//
//    /**
//     * work on main thread
//     * @param dbSource
//     */
//    private fun fetchFromNetwork(dbSource: DbType) {
//        // 先展示数据库中的数据，处理完网络请求数据后，再从数据库中取出一次进行展示
//        result.addSource(dbSource, Observer<S> { newData: S? -> setValue(loading(newData)) })
//        createCall(object : ResultCallBack<RequestType>() {
//            override fun onSuccess(apiResponse: RequestType) {
//                // 保证回调后在主线程
//                mThreadManager.runOnMainThread {
//                    result.addSource(apiResponse, Observer<S> { response: S? ->
//                        result.removeSource(apiResponse)
//                        result.removeSource(dbSource)
//                        if (response != null) {
//                            // 如果结果是EmResult结构，需要判断code，是否请求成功
//                            if (response is Result<*>) {
//                                val code = (response as Result<*>).code
//                                if (code != ErrorCode.EM_NO_ERROR) {
//                                    fetchFailed(code, dbSource, null)
//                                }
//                            }
//                            // 在异步线程中处理保存到数据库的逻辑
//                            mThreadManager.runOnIOThread {
//                                try {
//                                    saveCallResult(processResponse(response))
//                                } catch (e: Exception) {
//                                    EMLog.e(TAG, "save call result failed: $e")
//                                }
//                                //为了获取最新的数据，需要从数据库重新取一次数据，保证页面与数据的一致性
//                                mThreadManager.runOnMainThread {
//                                    result.addSource(safeLoadFromDb(), Observer<S> { newData: S? ->
//                                        setValue(
//                                            success(newData)
//                                        )
//                                    })
//                                }
//                            }
//                        } else {
//                            fetchFailed(ErrorCode.EM_ERR_UNKNOWN, dbSource, null)
//                        }
//                    })
//                }
//            }
//
//            override fun onError(error: Int, errorMsg: String) {
//                mThreadManager.runOnMainThread { fetchFailed(error, dbSource, errorMsg) }
//            }
//        })
//    }
//
//    /**
//     * 安全从数据库加载数据，如果加载失败，则数据返回null。
//     * @return
//     */
//    private fun safeLoadFromDb(): DbType {
//        var dbSource: DbType
//        try {
//            dbSource = loadFromDb()
//        } catch (e: Exception) {
//            EMLog.e(TAG, "safe load from db failed: $e")
//            dbSource = MutableLiveData<Any?>(null)
//        }
//        return dbSource
//    }
//
//    @MainThread
//    private fun fetchFailed(code: Int, dbSource: DbType, message: String?) {
//        onFetchFailed()
//        try {
//            result.addSource(
//                dbSource,
//                Observer<S> { newData: S? -> setValue(error(code, message, newData)) })
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    @MainThread
//    private fun setValue(newValue: Resource<DbType?>) {
//        if (result.value !== newValue) {
//            result.value = newValue
//        }
//    }
//
//    /**
//     * Process request response
//     * @param response
//     * @return
//     */
//    @WorkerThread
//    protected fun processResponse(response: RequestType): RequestType {
//        return response
//    }
//
//    /**
//     * Called to get the cached data from the database.
//     * @return
//     */
//    @MainThread
//    protected abstract fun loadFromDb(): DbType
//
//    /**
//     * 此处设计为回调模式，方便在此方法中进行异步操作
//     * @return
//     */
//    @MainThread
//    protected abstract fun createCall(callBack: ResultCallBack<LiveData<RequestType>?>?)
//
//    /**
//     * Called to save the result of the API response into the database
//     * @param item
//     */
//    @WorkerThread
//    protected abstract fun saveCallResult(item: RequestType)
//
//    /**
//     * Returns a LiveData object that represents the resource that's implemented
//     * in the base class.
//     * @return
//     */
//    fun asLiveData(): LiveData<Resource<DbType?>> {
//        return result
//    }
//
//    companion object {
//        private const val TAG = "NetworkBoundResource"
//    }
//
//    init {
//        mThreadManager = EaseThreadManager.getInstance()
//        if (mThreadManager.isMainThread) {
//            init()
//        } else {
//            mThreadManager.runOnMainThread { init() }
//        }
//    }
//}