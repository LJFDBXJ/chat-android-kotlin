package com.hyphenate.easeim.section.ui.message.delegates

import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.hyphenate.chat.EMMessage
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.db.entity.InviteMessageStatus
import com.hyphenate.easeui.adapter.EaseBaseDelegate
import com.hyphenate.easeui.adapter.EaseBaseRecyclerViewAdapter
import com.hyphenate.easeui.utils.EaseDateUtils
import com.hyphenate.easeui.widget.EaseImageView
import com.hyphenate.exceptions.HyphenateException
import java.util.*

class AgreeMsgDelegate : EaseBaseDelegate<EMMessage, AgreeMsgDelegate.ViewHolder>() {
    override fun isForViewType(msg: EMMessage?, position: Int): Boolean {
        var statusParams: String? = null
        try {
            statusParams = msg?.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS)
        } catch (e: HyphenateException) {
            e.printStackTrace()
        }
        val status = InviteMessageStatus.valueOf(statusParams!!)
        return (status === InviteMessageStatus.BEAGREED
                || status === InviteMessageStatus.AGREED)
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
            var reason: String? = null
            try {
                name!!.text = msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM)
                reason = msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_REASON)
            } catch (e: HyphenateException) {
                e.printStackTrace()
            }
            if (TextUtils.isEmpty(reason)) {
                var statusParams: String? = null
                try {
                    statusParams = msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS)
                    val status = InviteMessageStatus.valueOf(statusParams)
                    if (status === InviteMessageStatus.AGREED) {
                        reason = name!!.context.getString(
                            InviteMessageStatus.AGREED.msgContent,
                            msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM)
                        )
                    } else if (status === InviteMessageStatus.BEAGREED) {
                        reason = name!!.context.getString(InviteMessageStatus.BEAGREED.msgContent)
                    }
                } catch (e: HyphenateException) {
                    e.printStackTrace()
                }
            }
            message!!.text = reason
            time!!.text = EaseDateUtils.getTimestampString(itemView.context, Date(msg.msgTime))
        }
    }
}