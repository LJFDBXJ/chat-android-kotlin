package com.hyphenate.easeim.section.ui.message.delegates

import android.view.View
import android.widget.Button
import android.widget.TextView
import com.hyphenate.chat.EMMessage
import com.hyphenate.easeim.SdkHelper.Companion.instance
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.db.entity.InviteMessageStatus
import com.hyphenate.easeui.adapter.EaseBaseDelegate
import com.hyphenate.easeui.adapter.EaseBaseRecyclerViewAdapter
import com.hyphenate.easeui.utils.EaseDateUtils
import com.hyphenate.easeui.widget.EaseImageView
import com.hyphenate.exceptions.HyphenateException
import java.util.*

class InviteMsgDelegate : EaseBaseDelegate<EMMessage, InviteMsgDelegate.ViewHolder>() {
    private var listener: OnInviteListener? = null
    override fun isForViewType(msg: EMMessage, position: Int): Boolean {
        var statusParams: String? = null
        try {
            statusParams = msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS)
        } catch (e: HyphenateException) {
            e.printStackTrace()
        }
        val status = InviteMessageStatus.valueOf(statusParams!!)
        return status === InviteMessageStatus.BEINVITEED || status === InviteMessageStatus.BEAPPLYED || status === InviteMessageStatus.GROUPINVITATION
    }

    override fun getLayoutId(): Int {
        return R.layout.item_invite_msg_invite_layout
    }

    override fun createViewHolder(view: View): ViewHolder {
        return ViewHolder(view)
    }

    inner class ViewHolder(itemView: View) :  EaseBaseRecyclerViewAdapter.ViewHolder<EMMessage>(itemView) {
        private var name: TextView? = null
        private var message: TextView? = null
        private var agree: Button? = null
        private var refuse: Button? = null
        private var avatar: EaseImageView? = null
        private var time: TextView? = null
        override fun initView(itemView: View) {
            name = findViewById(R.id.name)
            message = findViewById(R.id.message)
            agree = findViewById(R.id.agree)
            refuse = findViewById(R.id.refuse)
            time = findViewById(R.id.time)
            avatar = findViewById(R.id.avatar)
            avatar?.setShapeType(instance.easeAvatarOptions.avatarShape)
        }

        override fun setData(msg: EMMessage, position: Int) {
            var reason: String? = null
            try {
                name?.text = msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM)
                reason = msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_REASON)
            } catch (e: HyphenateException) {
                e.printStackTrace()
            }
            if (reason.isNullOrEmpty()) {
                val statusParams: String?
                try {
                    statusParams = msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS)
                    val status = InviteMessageStatus.valueOf(statusParams)
                    if (status === InviteMessageStatus.BEINVITEED) {
                        reason = name!!.context.getString(
                            InviteMessageStatus.BEINVITEED.msgContent,
                            msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM)
                        )
                    } else if (status === InviteMessageStatus.BEAPPLYED) { //application to join group
                        reason = name!!.context.getString(
                            InviteMessageStatus.BEAPPLYED.msgContent,
                            msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM),
                            msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_NAME)
                        )
                    } else if (status === InviteMessageStatus.GROUPINVITATION) {
                        reason = name!!.context.getString(
                            InviteMessageStatus.GROUPINVITATION.msgContent,
                            msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_INVITER),
                            msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_NAME)
                        )
                    }
                } catch (e: HyphenateException) {
                    e.printStackTrace()
                }
            }
            message?.text = reason
            time?.text = EaseDateUtils.getTimestampString(itemView.context, Date(msg.msgTime))

            agree?.setOnClickListener { view: View ->
                listener?.onInviteAgree(view, msg)
            }
            refuse?.setOnClickListener { view: View ->
                    listener?.onInviteRefuse(view, msg)
                }
        }
    }

    fun setOnInviteListener(listener: OnInviteListener?) {
        this.listener = listener
    }

    interface OnInviteListener {
        fun onInviteAgree(view: View, msg: EMMessage)
        fun onInviteRefuse(view: View, msg: EMMessage)
    }
}