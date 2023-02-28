package com.hyphenate.easeim.common.interfaceOrImplement

/**
 * 用于解析Resource<T>，减少重复代码
 * hideErrorMsg默认为false，即默认情况是会展示错误信息
 * @param <T>
</T></T> */
abstract class OnResourceParseCallback<T> {
    var hideErrorMsg = false

    constructor() {}

    /**
     * 是否展示错误信息
     * @param hideErrorMsg
     */
    constructor(hideErrorMsg: Boolean) {
        this.hideErrorMsg = hideErrorMsg
    }

    /**
     * 成功
     * @param data
     */
    abstract fun onSuccess(data: T?)

    /**
     * 失败
     * @param code
     * @param message
     */
    open fun onError(code: Int, message: String) {}

    /**
     * 加载中
     */
    open fun onLoading(data: T?) {}

    /**
     * 隐藏加载
     */
    open fun hideLoading() {}
}