package com.hyphenate.easeim.section.ui.chat.delegates.views

import android.content.Context
import android.view.View
import com.hyphenate.easeui.widget.chatrow.EaseChatRow
import android.widget.TextView
import com.hyphenate.easeim.R
import com.hyphenate.chat.EMTextMessageBody

class ChatRowVoiceCallView : EaseChatRow {


    private var contentView: TextView? = null

    constructor(context: Context?) : super(context, false)
    constructor(context: Context?, isSender: Boolean) : super(context, isSender)

    override fun onInflateView() {
        inflater.inflate(
            if (showSenderType)
                R.layout.ease_row_sent_voice_call
            else
                R.layout.ease_row_received_voice_call,
            this
        )
    }

    override fun onFindViewById() {
        contentView = findViewById<View>(com.hyphenate.easeui.R.id.tv_chatcontent) as TextView
    }

    override fun onSetUpView() {
        val txtBody = message.body as EMTextMessageBody
        contentView?.text = txtBody.message
    }
}