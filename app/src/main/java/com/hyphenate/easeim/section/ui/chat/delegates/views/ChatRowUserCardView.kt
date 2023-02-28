package com.hyphenate.easeim.section.ui.chat.delegates.views

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.hyphenate.easeui.widget.chatrow.EaseChatRow
import android.widget.TextView
import com.hyphenate.easeim.R
import com.hyphenate.chat.EMCustomMessageBody
import com.hyphenate.easeim.common.constant.DemoConstant
import com.bumptech.glide.Glide

class ChatRowUserCardView : EaseChatRow {

    private var nickNameView: TextView? = null
    private var userIdView: TextView? = null
    private var headImageView: ImageView? = null

    constructor(context: Context?) : super(context, false)
    constructor(context: Context?, isSender: Boolean) : super(context, isSender)

    override fun onInflateView() {
        inflater.inflate(
            if (showSenderType)
                R.layout.ease_row_sent_user_card
            else
                R.layout.ease_row_received_user_card,
            this
        )
    }

    override fun onFindViewById() {
        nickNameView = findViewById<View>(R.id.user_nick_name) as TextView
        userIdView = findViewById<View>(R.id.user_id) as TextView
        headImageView = findViewById<View>(R.id.head_Image_view) as ImageView
    }

    override fun onSetUpView() {
        val messageBody = message.body as EMCustomMessageBody
        val params = messageBody.params
        val uId = params[DemoConstant.USER_CARD_ID]
        userIdView?.text = uId
        val nickName = params[DemoConstant.USER_CARD_NICK]
        nickNameView?.text = nickName
        val headUrl = params[DemoConstant.USER_CARD_AVATAR]

        Glide.with(getContext())
            .load(headUrl)
            .placeholder(R.drawable.em_login_logo)
            .error(R.drawable.em_login_logo)
            .into(headImageView!!)
    }
}