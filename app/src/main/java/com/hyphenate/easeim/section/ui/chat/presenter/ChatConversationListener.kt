package com.hyphenate.easeim.section.ui.chat.presenter

import com.hyphenate.EMConversationListener
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.livedatas.LiveDataBus
import com.hyphenate.easeui.model.EaseEvent

class ChatConversationListener : EMConversationListener {
        override fun onCoversationUpdate() {}
        override fun onConversationRead(from: String, to: String) {
            val event = EaseEvent.create(DemoConstant.CONVERSATION_READ, EaseEvent.TYPE.MESSAGE)
            LiveDataBus.get().use(DemoConstant.CONVERSATION_READ).postValue(event)
        }
    }
