package com.hyphenate.easeim.section.ui.chat.delegates.views

import android.content.Context
import com.hyphenate.easeui.widget.chatrow.EaseChatRow
import android.widget.TextView
import com.hyphenate.easeim.R
import com.hyphenate.chat.EMTextMessageBody

class ChatRowNotificationView : EaseChatRow {


    private var contentView: TextView? = null
    constructor(context: Context?) : super(context,false)
    constructor(context: Context?, isSender: Boolean) : super(context, isSender)


    override fun onInflateView() {
        inflater.inflate(R.layout.row_notification, this)
    }

    override fun onFindViewById() {
        contentView = findViewById(R.id.tv_chatcontent)
    }

    override fun onSetUpView() {
        val txtBody = message.body as EMTextMessageBody
        contentView!!.text = txtBody.message
    }
}