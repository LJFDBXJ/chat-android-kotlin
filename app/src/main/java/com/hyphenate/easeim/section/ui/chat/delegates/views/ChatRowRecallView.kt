package com.hyphenate.easeim.section.ui.chat.delegates.views

import android.content.Context
import android.text.TextUtils
import android.view.View
import com.hyphenate.easeui.widget.chatrow.EaseChatRow
import android.widget.TextView
import com.hyphenate.easeim.R
import com.hyphenate.easeui.constants.EaseConstant
import com.hyphenate.chat.EMMessage

class ChatRowRecallView : EaseChatRow {

    private var contentView: TextView? = null

    constructor(context: Context?) : super(context, false)
    constructor(context: Context?, isSender: Boolean) : super(context, isSender)


    override fun onInflateView() {
        inflater.inflate(R.layout.row_recall_message, this)
    }

    override fun onFindViewById() {
        contentView = findViewById<View>(R.id.text_content) as TextView
    }

    override fun onSetUpView() {
        // 设置显示内容
        val messageStr: String?
        val recaller = message.getStringAttribute(EaseConstant.MESSAGE_TYPE_RECALLER, "")
        val from = message.from
        messageStr = if (message.direct() == EMMessage.Direct.SEND &&
            (recaller.isEmpty() || !recaller.isNullOrEmpty()
                    && recaller == from)
        ) {
            String.format(context.getString(R.string.msg_recall_by_self))
        } else if (recaller.isNotEmpty() && recaller != from) {
            String.format(context.getString(R.string.msg_recall_by_another), recaller, from)
        } else {
            String.format(context.getString(R.string.msg_recall_by_user), from)
        }
        contentView?.text = messageStr
    }
}