package com.hyphenate.easeim.section.ui.message.delegates

import android.view.View
import com.hyphenate.easeim.common.manager.PushAndMessageHelper.getSystemMessage
import com.hyphenate.easeui.adapter.EaseBaseDelegate
import com.hyphenate.chat.EMMessage
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.exceptions.HyphenateException
import com.hyphenate.easeim.common.db.entity.InviteMessageStatus
import com.hyphenate.easeim.R
import com.hyphenate.easeui.adapter.EaseBaseRecyclerViewAdapter
import android.widget.TextView
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeui.utils.EaseDateUtils
import com.hyphenate.easeui.widget.EaseImageView
import java.util.*

class OtherMsgDelegate : EaseBaseDelegate<EMMessage, OtherMsgDelegate.ViewHolder>() {
    override fun isForViewType(msg: EMMessage, position: Int): Boolean {
        var statusParams: String? = null
        try {
            statusParams = msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS)
        } catch (e: HyphenateException) {
            e.printStackTrace()
        }
        val status = InviteMessageStatus.valueOf(statusParams!!)
        return status !== InviteMessageStatus.BEINVITEED && status !== InviteMessageStatus.BEAPPLYED && status !== InviteMessageStatus.GROUPINVITATION && status !== InviteMessageStatus.BEAGREED
    }

    override fun getLayoutId(): Int {
        return R.layout.layout_item_invite_msg_agree
    }

    override fun createViewHolder(view: View): ViewHolder {
        return ViewHolder(view)
    }

    inner class ViewHolder(itemView: View) :
        EaseBaseRecyclerViewAdapter.ViewHolder<EMMessage>(itemView) {
        private var name: TextView? = null
        private var message: TextView? = null
        private var avatar: EaseImageView? = null
        private var time: TextView? = null
        override fun initView(itemView: View) {
            name = findViewById(R.id.name)
            message = findViewById(R.id.message)
            avatar = findViewById(R.id.avatar)
            time = findViewById(R.id.time)
            avatar?.setShapeType(SdkHelper.instance.easeAvatarOptions.avatarShape)
        }

        override fun setData(msg: EMMessage, position: Int) {
            try {
                name?.text = msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM)
                val str = getSystemMessage(msg)
                message!!.text = str
            } catch (e: HyphenateException) {
                e.printStackTrace()
            }
            time?.text = EaseDateUtils.getTimestampString(itemView.context, Date(msg.msgTime))
        }
    }
}