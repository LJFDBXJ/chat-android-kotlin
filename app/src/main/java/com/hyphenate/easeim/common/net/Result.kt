package com.hyphenate.easeim.common.net

import com.google.gson.annotations.SerializedName
import com.hyphenate.EMError

/**
 * 结果基础类
 * @param <T> 请求结果的实体类
</T> */
class Result<T> {
    @SerializedName("code")
    var code = 0
    @SerializedName("result")
    var result: T? = null

    val isSuccess: Boolean
        get() = code == EMError.EM_NO_ERROR
}