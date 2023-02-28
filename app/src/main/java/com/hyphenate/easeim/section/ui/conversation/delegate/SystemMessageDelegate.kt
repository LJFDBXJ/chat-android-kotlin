package com.hyphenate.easeim.section.ui.conversation.delegate

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.db.entity.InviteMessage
import com.hyphenate.easeim.common.db.entity.InviteMessageStatus
import com.hyphenate.easeim.common.db.entity.MsgTypeManageEntity
import com.hyphenate.easeim.common.manager.PushAndMessageHelper.getSystemMessage
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.adapter.EaseBaseDelegate
import com.hyphenate.easeui.adapter.EaseBaseRecyclerViewAdapter
import com.hyphenate.easeui.utils.EaseDateUtils
import com.hyphenate.easeui.widget.EaseImageView
import java.util.*

class SystemMessageDelegate :
    EaseBaseDelegate<MsgTypeManageEntity?, SystemMessageDelegate.ViewHolder>() {
    override fun isForViewType(item: MsgTypeManageEntity?, position: Int): Boolean {
        return item != null
    }

    override fun getLayoutId(): Int {
        return R.layout.ease_item_row_chat_history
    }

    override fun createViewHolder(view: View): ViewHolder {
        return ViewHolder(view)
    }

    inner class ViewHolder(itemView: View) :
        EaseBaseRecyclerViewAdapter.ViewHolder<MsgTypeManageEntity>(itemView) {
        private var listIteaseLayout: ConstraintLayout? = null
        private var avatar: EaseImageView? = null
        private var mUnreadMsgNumber: TextView? = null
        private var name: TextView? = null
        private var time: TextView? = null
        private var mMsgState: ImageView? = null
        private var mentioned: TextView? = null
        private var message: TextView? = null
        private var mContext: Context? = null
        override fun initView(itemView: View) {
            mContext = itemView.context
            listIteaseLayout = findViewById(com.hyphenate.easeui.R.id.list_itease_layout)
            avatar = findViewById(com.hyphenate.easeui.R.id.avatar)
            mUnreadMsgNumber = findViewById(com.hyphenate.easeui.R.id.unread_msg_number)
            name = findViewById(com.hyphenate.easeui.R.id.name)
            time = findViewById(com.hyphenate.easeui.R.id.time)
            mMsgState = findViewById(com.hyphenate.easeui.R.id.msg_state)
            mentioned = findViewById(com.hyphenate.easeui.R.id.mentioned)
            message = findViewById(com.hyphenate.easeui.R.id.message)
            avatar?.setShapeType(EaseIM.getInstance().avatarOptions.avatarShape)
        }

        override fun setData(`object`: MsgTypeManageEntity, position: Int) {
            val type = `object`.type
            val lastMsg = `object`.lastMsg
            if (lastMsg == null || TextUtils.isEmpty(type)) {
                return
            }
            listIteaseLayout!!.background =
                if (!TextUtils.isEmpty(`object`.extField)) ContextCompat.getDrawable(
                    mContext!!, R.drawable.ease_conversation_top_bg
                ) else null
            if (TextUtils.equals(type, MsgTypeManageEntity.msgType.NOTIFICATION.name)) {
                avatar!!.setImageResource(R.drawable.em_system_nofinication)
                name!!.text = mContext!!.getString(R.string.em_conversation_system_notification)
            }
            val unReadCount = `object`.unReadCount
            if (unReadCount > 0) {
                mUnreadMsgNumber!!.text = unReadCount.toString()
                mUnreadMsgNumber!!.visibility = View.VISIBLE
            } else {
                mUnreadMsgNumber!!.visibility = View.GONE
            }
            if (lastMsg is InviteMessage) {
                time!!.text =
                    EaseDateUtils.getTimestampString(mContext, Date(lastMsg.time))
                val status = lastMsg.statusEnum ?: return
                val reason = lastMsg.reason
                if (status === InviteMessageStatus.BEINVITEED || status === InviteMessageStatus.BEAPPLYED || status === InviteMessageStatus.GROUPINVITATION || status === InviteMessageStatus.AGREED) {
                    message!!.text =
                        if (TextUtils.isEmpty(reason)) getSystemMessage(
                            (lastMsg as InviteMessage?)!!
                        ) else reason
                } else {
                    message!!.text = getSystemMessage((lastMsg as InviteMessage?)!!)
                }
            }
        }
    }
}