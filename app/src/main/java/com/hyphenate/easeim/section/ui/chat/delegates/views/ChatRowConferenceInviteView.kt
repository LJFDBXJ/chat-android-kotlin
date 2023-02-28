package com.hyphenate.easeim.section.ui.chat.delegates.views

import android.content.Context
import android.text.TextUtils
import com.hyphenate.easeui.widget.chatrow.EaseChatRow
import android.widget.TextView
import com.hyphenate.easeim.R
import com.hyphenate.chat.EMTextMessageBody

class ChatRowConferenceInviteView : EaseChatRow {

    private var contentView: TextView? = null

    constructor(context: Context) : super(context, false)
    constructor(context: Context?, isSender: Boolean) : super(context, isSender)


    override fun onInflateView() {
        inflater.inflate(
            if (showSenderType)
                R.layout.row_sent_conference_invite
            else
                R.layout.row_received_conference_invite,
            this
        )
    }

    override fun onFindViewById() {
        contentView = findViewById(R.id.tv_chatcontent)
    }

    override fun onSetUpView() {
        val txtBody = message.body as EMTextMessageBody
        var message = txtBody.message
        if (!TextUtils.isEmpty(message) && message.contains("-")) {
            message = """${message.substring(0, message.indexOf("-") + 1)}
                ${message.substring(message.indexOf("-") + 1)}
                """.trimIndent()
        }
        contentView!!.text = message
    }
}