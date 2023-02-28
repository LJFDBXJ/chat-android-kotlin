package com.hyphenate.easeim.common.net

import android.text.TextUtils
import com.hyphenate.EMError
import com.hyphenate.easeim.AppClient.Companion.instance
import com.hyphenate.easeim.common.enums.Status

class Resource<T> {
    var status: Status = Status.SUCCESS
    var data: T?
    var errorCode: Int
    var message: String = ""
        get() {
            //获取错误信息
            if (field.isNotEmpty()) {
                return field
            }
            return if (messageId > 0) {
                instance.getString(messageId)
            } else ""
        }
        private set

    private var messageId = 0

    fun isSuccess(): Boolean {
        return status == Status.SUCCESS
    }

    constructor(status: Status, data: T, errorCode: Int) {
        this.status = status
        this.data = data
        this.errorCode = errorCode
        messageId = ErrorCode.Error.parseMessage(errorCode).messageId
    }

    constructor(status: Status, data: T, errorCode: Int, message: String) {
        this.status = status
        this.data = data
        this.errorCode = errorCode
        this.message = message
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val resource = other as Resource<*>
        if (errorCode != resource.errorCode) return false
        if (status != resource.status) return false
        return if (data != resource.data) false else message == resource.message
    }

    override fun hashCode(): Int {
        var result = status.hashCode()
        result = 31 * result + if (data != null) data.hashCode() else 0
        result = 31 * result + errorCode
        result = 31 * result + if (message != null) message.hashCode() else 0
        return result
    }

    override fun toString(): String {
        return "Resource{" +
                "mStatus=" + status +
                ", data=" + data +
                ", errorCode=" + errorCode +
                ", message='" + message + '\'' +
                '}'
    }

    companion object {
        @JvmStatic
        fun <T> success(data: T): Resource<T> {
            return Resource(Status.SUCCESS, data, EMError.EM_NO_ERROR)
        }

        fun <T> error(code: Int, data: T): Resource<T> {
            return Resource(Status.ERROR, data, code)
        }

        @JvmStatic
        fun <T> error(code: Int, message: String?, data: T): Resource<T> {
            return if (TextUtils.isEmpty(message)) Resource(Status.ERROR, data, code) else Resource(
                Status.ERROR, data, code, message ?: "未知"
            )
        }

        @JvmStatic
        fun <T> loading(data: T): Resource<T> {
            return Resource(Status.LOADING, data, EMError.EM_NO_ERROR)
        }
    }
}