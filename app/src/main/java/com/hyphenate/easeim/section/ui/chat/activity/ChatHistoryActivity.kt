package com.hyphenate.easeim.section.ui.chat.activity

import android.content.Context
import android.content.Intent
import com.hyphenate.easeim.section.ui.chat.ChatActivity
import com.hyphenate.easeui.constants.EaseConstant

class ChatHistoryActivity : ChatActivity() {

    companion object {
        fun actionStart(context: Context, userId: String?, chatType: Int, historyMsgId: String?) {
            val intent = Intent(context, ChatHistoryActivity::class.java)
            intent.putExtra(EaseConstant.EXTRA_CONVERSATION_ID, userId)
            intent.putExtra(EaseConstant.EXTRA_CHAT_TYPE, chatType)
            intent.putExtra(EaseConstant.HISTORY_MSG_ID, historyMsgId)
            context.startActivity(intent)
        }
    }

}