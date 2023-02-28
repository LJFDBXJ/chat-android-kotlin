package com.hyphenate.easeim.common.interfaceOrImplement

import android.content.DialogInterface

interface DialogCallBack {
    /**
     * 点击事件，一般指点击确定按钮
     * @param dialog
     * @param which
     */
    fun onClick(dialog: DialogInterface?, which: Int)
}