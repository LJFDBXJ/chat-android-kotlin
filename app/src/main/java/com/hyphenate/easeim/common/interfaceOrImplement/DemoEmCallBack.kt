package com.hyphenate.easeim.common.interfaceOrImplement

import com.hyphenate.EMCallBack

/**
 * 作为EMCallBack的抽象类，onError()和onProgress()根据情况进行重写
 */
abstract class DemoEmCallBack : EMCallBack {
    override fun onError(code: Int, error: String) {
        // do something for error
    }

    override fun onProgress(progress: Int, status: String) {
        // do something in progress
    }
}