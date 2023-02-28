package com.hyphenate.easeim.section.ui.chat

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.hyphenate.chat.EMCustomMessageBody
import com.hyphenate.chat.EMMessage
import com.hyphenate.easeim.R
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.ktx.toast
import com.hyphenate.easeim.common.livedatas.LiveDataBus
import com.hyphenate.easeim.common.livedatas.obs
import com.hyphenate.easeim.common.utils.EmoIconExampleGroupData
import com.hyphenate.easeim.common.utils.SpDbModel
import com.hyphenate.easeim.databinding.LayoutProgressRecallBinding
import com.hyphenate.easeim.section.ui.chat.activity.ForwardMessageActivity
import com.hyphenate.easeim.section.ui.chat.activity.PickAtUserActivity
import com.hyphenate.easeim.section.ui.chat.activity.SelectUserCardActivity
import com.hyphenate.easeim.section.ui.chat.vm.MessageVm
import com.hyphenate.easeim.section.conference.ConferenceInviteActivity
import com.hyphenate.easeim.section.ui.contact.activity.ContactDetailActivity
import com.hyphenate.easeim.section.ui.me.activity.UserDetailActivity
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.easeui.modules.chat.EaseChatFragment
import com.hyphenate.easeui.modules.chat.EaseChatMessageListLayout
import com.hyphenate.easeui.modules.chat.interfaces.OnRecallMessageResultListener
import com.hyphenate.easeui.modules.menu.EasePopupWindowHelper
import com.hyphenate.easeui.modules.menu.MenuItemBean
import com.hyphenate.easeui.utils.EaseCommonUtils

class ChatFragment : EaseChatFragment() {
    private val viewModel by activityViewModels<MessageVm>()
    private var dialog: Dialog? = null
    private var result: ActivityResultLauncher<Intent>? = null

    override fun initView() {
        super.initView()
        chatLayout.setTargetLanguageCode(SpDbModel.instance.getTargetLanguage())

        //获取到聊天列表控件
        val messageListLayout = chatLayout.chatMessageListLayout
        //设置聊天列表背景
        messageListLayout.background = ColorDrawable(Color.parseColor("#FFFFFF"))
        //设置默认头像
        messageListLayout.setAvatarDefaultSrc(
            ContextCompat.getDrawable(
                mContext,
                R.drawable.ease_default_avatar
            )
        )
        //设置头像形状
        messageListLayout.setAvatarShapeType(2)
        //设置文本字体大小
        messageListLayout.setItemTextSize(EaseCommonUtils.sp2px(mContext, 18f).toInt());
        //设置文本字体颜色
        messageListLayout.setItemTextColor(ContextCompat.getColor(mContext, R.color.red));
        //设置时间线的背景
        messageListLayout.setTimeBackground(
            ContextCompat.getDrawable(
                mContext,
                R.color.white
            )
        )
        messageListLayout.showNickname(true)


        //设置时间线的文本大小
        messageListLayout.setTimeTextSize(EaseCommonUtils.sp2px(mContext, 10f).toInt())
        //设置时间线的文本颜色
        messageListLayout.setTimeTextColor(ContextCompat.getColor(mContext, R.color.black))


        //设置聊天列表样式：两侧及均位于左侧
        messageListLayout.setItemShowType(EaseChatMessageListLayout.ShowType.NORMAL)
        //获取到菜单输入父控件
        val chatInputMenu = chatLayout.chatInputMenu
        //获取到菜单输入控件
        val primaryMenu = chatInputMenu.primaryMenu
        if (primaryMenu != null) {
            //设置菜单样式为不可用语音模式
//            primaryMenu.setMenuShowType(EaseInputMenuStyle.ONLY_TEXT)
        }
    }

    private fun addItemMenuAction() {
        val itemMenu = MenuItemBean(0, R.id.action_chat_forward, 11, getString(R.string.action_forward))
        itemMenu.resourceId = R.drawable.ease_chat_item_menu_forward
        chatLayout.addItemMenu(itemMenu)

        val itemMenu1 =
            MenuItemBean(0, R.id.action_chat_label, 12, getString(R.string.action_label))
        itemMenu1.resourceId = R.drawable.ease_chat_item_menu_copy
        chatLayout.addItemMenu(itemMenu1)
        //chatLayout.setReportYourSelf(false);
    }

    /**
     * 初始化 键盘扩展菜单
     */
    private fun initKeyBordData() {
        val chatExtendMenu = chatLayout.chatInputMenu.chatExtendMenu
        chatExtendMenu.clear()
        viewModel.initKeyBordData(
            chatType = chatType,
            conversationId = conversationId,
            menu = chatExtendMenu
        )
        //添加扩展表情
        chatLayout.chatInputMenu.emojiconMenu.addEmojiconGroup(EmoIconExampleGroupData.emoData)
    }


    override fun initListener() {
        super.initListener()
        chatLayout.setOnRecallMessageResultListener(object : OnRecallMessageResultListener {
            override fun recallSuccess(message: EMMessage?) {
                dialog?.dismiss()
            }

            override fun recallFail(code: Int, errorMsg: String?) {
                dialog?.dismiss()
            }

        })
        result = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            it.data ?: return@registerForActivityResult
            if (it.resultCode == Activity.RESULT_OK) {
                when (20) {
                    REQUEST_CODE_SELECT_AT_USER -> {
                        val username = it.data?.getStringExtra("username")
                        chatLayout.inputAtUsername(username, false)
                    }
                    REQUEST_CODE_SELECT_USER_CARD -> {
                        val user = it.data?.getSerializableExtra("user") as EaseUser?
                        user?.let { sendUserCardMessage(it) }
                    }
                }
            }
        }
    }

    override fun initData() {
        super.initData()
        initKeyBordData()
        addItemMenuAction()
        val unSendMsg = SpDbModel.instance.getUnSendMsg(toChatUsername = conversationId)
        chatLayout.chatInputMenu.primaryMenu.editText.setText(unSendMsg)
        chatLayout.turnOnTypingMonitor(SpDbModel.instance.isShowMsgTyping())
        LiveDataBus.get().use(DemoConstant.MESSAGE_CHANGE_CHANGE)
            .postValue(EaseEvent(DemoConstant.MESSAGE_CHANGE_CHANGE, EaseEvent.TYPE.MESSAGE))

        DemoConstant.MESSAGE_CALL_SAVE.obs<Boolean>(this) {
            if (it == true) {
                chatLayout.chatMessageListLayout.refreshToLatest()
            }
        }
        arrayOf(
            DemoConstant.CONVERSATION_DELETE,
            DemoConstant.MESSAGE_CHANGE_CHANGE,
            DemoConstant.CONVERSATION_READ,
            DemoConstant.CONTACT_ADD,
            DemoConstant.CONTACT_UPDATE
        ).obs<EaseEvent>(this) { key, result ->
            result ?: return@obs
            if (result.isMessageChange)
                when (key) {
                    DemoConstant.CONVERSATION_DELETE,
                    DemoConstant.MESSAGE_CHANGE_CHANGE,
                    DemoConstant.CONVERSATION_READ,
                    DemoConstant.CONTACT_ADD,
                    DemoConstant.CONTACT_UPDATE -> {
                        chatLayout.chatMessageListLayout.refreshMessages()

                    }
                }
        }

    }

    override fun onUserAvatarClick(username: String) {
        if (username != SdkHelper.instance.currentUser) {
            var user = SdkHelper.instance.getUserInfo(username)
            if (user == null) {
                user = EaseUser(username)
            }
            if (SpDbModel.instance.isContact(userId = username)) {
                user.contact = 0
            } else {
                user.contact = 3
            }
            ContactDetailActivity.actionStart(context = mContext, user = user)
        } else {
            UserDetailActivity.actionStart(context = mContext, nickName = null, url = null)
        }
    }

    override fun onUserAvatarLongClick(username: String) {

    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (chatLayout.chatMessageListLayout.isGroupChat) {
            if (count == 1 && "@" == s[start].toString()) {
                PickAtUserActivity.actionStartForResult(
                    fragment = this,
                    groupId = conversationId,
                    requestCode = REQUEST_CODE_SELECT_AT_USER
                )
            }
        }
    }


    override fun onChatExtendMenuItemClick(view: View, itemId: Int) {
        super.onChatExtendMenuItemClick(view, itemId)
        when (itemId) {
            /**
             * 聊天界面 键盘弹出 音视频联系
             */
            R.id.extend_item_video_call ->
                viewModel.showSelectDialog(
                    context = requireContext(),
                    conversationId = conversationId
                )
            //邀请多人视频会议
            R.id.extend_item_conference_call -> {
                val intent = Intent(
                    context, ConferenceInviteActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra(DemoConstant.EXTRA_CONFERENCE_GROUP_ID, conversationId)
                context?.startActivity(intent)
            }
            R.id.extend_item_delivery ->
                viewModel.showDeliveryDialog(context = requireContext(), chatLayout = chatLayout)

            //个人名片卡
            R.id.extend_item_user_card -> {
                val userCardIntent = Intent(context, SelectUserCardActivity::class.java)
                userCardIntent.putExtra("toUser", conversationId)
                result?.launch(userCardIntent)
            }
        }
    }

    override fun onChatError(code: Int, errorMsg: String) {
        toast(errorMsg)
    }

    override fun onOtherTyping(action: String) {
        viewModel.updateAction(action)
    }

    /**
     * Send user card message
     * @param user
     */
    private fun sendUserCardMessage(user: EaseUser) {
        val message = EMMessage.createSendMessage(EMMessage.Type.CUSTOM)
        val body = EMCustomMessageBody(DemoConstant.USER_CARD_EVENT)
        val params = HashMap<String, String>()
        params[DemoConstant.USER_CARD_ID] = user.username
        params[DemoConstant.USER_CARD_NICK] = user.nickname
        params[DemoConstant.USER_CARD_AVATAR] = user.avatar
        body.params = params
        message.body = body
        message.to = conversationId
        chatLayout.sendMessage(message)
    }

    //================================== for video and voice start ====================================
    /**
     * 保存未发送的文本消息内容
     * @param content
     */
    private fun saveUnSendMsg(content: String) {
        SpDbModel.instance.saveUnSendMsg(toChatUsername = conversationId, content = content)
    }

    override fun onPreMenu(helper: EasePopupWindowHelper, message: EMMessage, v: View) {
        //默认两分钟后，即不可撤回
        if (System.currentTimeMillis() - message.msgTime > 2 * 60 * 1000) {
            helper.findItemVisible(R.id.action_chat_recall, false)
        }
        helper.findItemVisible(R.id.action_chat_forward, false)
        when (message.type) {
            EMMessage.Type.TXT -> {
                if (!message.getBooleanAttribute(DemoConstant.MESSAGE_ATTR_IS_VIDEO_CALL, false)
                    && !message.getBooleanAttribute(DemoConstant.MESSAGE_ATTR_IS_VOICE_CALL, false)
                ) {
                    helper.findItemVisible(R.id.action_chat_forward, true)
                }
                if (v.id == R.id.subBubble) {
                    helper.findItemVisible(R.id.action_chat_forward, false)
                }
            }
            EMMessage.Type.IMAGE ->
                helper.findItemVisible(R.id.action_chat_forward, true)
            else -> {}
        }
        if (chatType == DemoConstant.CHATTYPE_CHATROOM) {
            helper.findItemVisible(R.id.action_chat_forward, true)
        }
    }

    /**
     * 长按消息 操作
     */
    override fun onMenuItemClick(item: MenuItemBean, message: EMMessage): Boolean {
        when (item.itemId) {
            // 转发
            R.id.action_chat_forward -> {
                ForwardMessageActivity.actionStart(mContext, message.msgId)
                return true
            }
            // 删除当前聊天
            R.id.action_chat_delete -> {
                viewModel.showDeleteDialog(requireContext(), message, chatLayout)
                return true
            }
            // 撤回 消息
            R.id.action_chat_recall -> {
                showProgressBar()
                chatLayout.recallMessage(message)
                return true
            }
            //翻译
            R.id.action_chat_reTranslate -> {
                viewModel.showTranslateDialog(requireContext(), message, chatLayout)
                return true
            }
            // 标记 聊天内容类型是否友善
            R.id.action_chat_label -> {
                viewModel.showLabel(requireContext(), message)
                return true
            }
        }
        return false
    }

    private fun showProgressBar() {
        val bind = LayoutProgressRecallBinding.inflate(layoutInflater)
        dialog = Dialog(mContext, R.style.dialog_recall)
        dialog?.setContentView(
            bind.root, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.show()
    }

    override fun translateMessageFail(message: EMMessage, code: Int, error: String) {
        viewModel.translateMessageFail(requireContext(), message, code, error)
    }

    override fun onStop() {
        super.onStop()
        //保存未发送的文本消息内容
        if (mContext != null && mContext.isFinishing) {
            if (chatLayout.chatInputMenu != null) {
                saveUnSendMsg(chatLayout.inputContent)
                LiveDataBus.get().use(DemoConstant.MESSAGE_NOT_SEND).postValue(true)
            }
        }
    }
    companion object {
        private const val REQUEST_CODE_SELECT_USER_CARD = 20
        private const val REQUEST_CODE_SELECT_AT_USER = 15
    }
}
