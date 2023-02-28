package com.hyphenate.easeim.section.ui.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.google.android.material.snackbar.Snackbar
import com.hyphenate.chat.EMClient
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.interfaceOrImplement.OnResourceParseCallback
import com.hyphenate.easeim.common.livedatas.obs
import com.hyphenate.easeim.common.utils.SpDbModel
import com.hyphenate.easeim.databinding.ActivityChatBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.ui.chat.activity.SingleChatSetActivity
import com.hyphenate.easeim.section.ui.chat.vm.ChatVm
import com.hyphenate.easeim.section.ui.chat.vm.MessageVm
import com.hyphenate.easeim.section.ui.group.GroupHelper
import com.hyphenate.easeim.section.ui.group.activity.ChatRoomDetailActivity
import com.hyphenate.easeim.section.ui.group.activity.GroupDetailActivity
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.constants.EaseConstant
import com.hyphenate.easeui.model.EaseEvent

/**
 * @author LXJDBXJ
 * @date 2022/10/10
 * @desc 聊天 activity
 */
open class ChatActivity(override val layoutId: Int = R.layout.activity_chat) :
    BaseInitActivityKtx<ActivityChatBinding>() {
    private var conversationId: String = ""
    private var chatType = 0
    private var historyMsgId: String? = null
    private val viewModel by viewModels<ChatVm>()
    private val messageViewModel by viewModels<MessageVm>()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        conversationId = intent.getStringExtra(EaseConstant.EXTRA_CONVERSATION_ID) ?: ""
        chatType = intent.getIntExtra(EaseConstant.EXTRA_CHAT_TYPE, EaseConstant.CHATTYPE_SINGLE)
        historyMsgId = intent.getStringExtra(DemoConstant.HISTORY_MSG_ID)
        initChatFragment()
        setTitleBarRight()
    }

    private fun initChatFragment() {
        val fragment = ChatFragment()
        fragment.arguments = Bundle().apply {
            putString(EaseConstant.EXTRA_CONVERSATION_ID, conversationId)
            putInt(EaseConstant.EXTRA_CHAT_TYPE, chatType)
            putString(DemoConstant.HISTORY_MSG_ID, historyMsgId)
            putBoolean(EaseConstant.EXTRA_IS_ROAM, SpDbModel.instance.isMsgRoaming())
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_fragment, fragment, "chat")
            .commit()
    }

    private fun setTitleBarRight() {
        val drawable = if (chatType == DemoConstant.CHATTYPE_SINGLE) {
            R.drawable.chat_user_info
        } else {
            R.drawable.chat_group_info
        }
        binding.titleBarMessage.setRightImageResource(drawable)

    }

    override fun initListener() {
        super.initListener()
        binding.titleBarMessage.setOnBackPressListener {
            onBackPressed()
        }
        binding.titleBarMessage.setOnRightClickListener {
            when (chatType) {
                //跳转到单聊设置页面
                DemoConstant.CHATTYPE_SINGLE -> {
                    SingleChatSetActivity.actionStart(
                        context = this,
                        toChatUsername = conversationId
                    )
                }

                // 跳转到群组设置
                DemoConstant.CHATTYPE_GROUP -> {
                    GroupDetailActivity.actionStart(
                        context = this,
                        groupId = conversationId
                    )
                }

                DemoConstant.CHATTYPE_CHATROOM -> {
                    ChatRoomDetailActivity.actionStart(
                        context = this,
                        roomId = conversationId
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        initChatFragment()
        initData()
    }

    override fun initData() {
        super.initData()
        val conversation = EMClient.getInstance().chatManager().getConversation(conversationId)
        viewModel.deleteConversation.observe(this) { resource ->
            parseResource(
                response = resource,
                callback = object : OnResourceParseCallback<Boolean>() {
                    override fun onSuccess(data: Boolean?) {
                        finish()
                        val event = EaseEvent.create(
                            DemoConstant.CONVERSATION_DELETE,
                            EaseEvent.TYPE.MESSAGE
                        )
                        messageViewModel.setMessageChange(change = event)
                    }
                }
            )

        }

        viewModel.chatRoom.observe(this) {
            setDefaultTitle()
        }

        arrayOf(
            DemoConstant.GROUP_CHANGE,
            DemoConstant.CHAT_ROOM_CHANGE,
            DemoConstant.MESSAGE_FORWARD,
        ).obs<EaseEvent>(this) { key, event ->
            event ?: return@obs
            when (key) {
                DemoConstant.GROUP_CHANGE -> {
                    if (event.isGroupLeave && conversationId == event.message) {
                        finish()
                    }
                }
                DemoConstant.CHAT_ROOM_CHANGE -> {
                    if (event.isChatRoomLeave && conversationId == event.message) {
                        finish()
                    }
                }
                DemoConstant.MESSAGE_FORWARD -> {
                    if (event.isMessageChange) {
                        showSnackBar(event.event)
                    }
                }
                DemoConstant.CONTACT_CHANGE -> {
                    if (conversation == null) {
                        finish()
                    }
                }

            }
        }
        setDefaultTitle()

        messageViewModel.action.observe(this) { action ->
            when (action) {
                "TypingBegin" -> {
                    binding.titleBarMessage.setTitle(getString(R.string.alert_during_typing))
                }
                "TypingEnd" -> {
                    setDefaultTitle()
                }
            }
        }
    }

    private fun showSnackBar(event: String) {
        Snackbar.make(binding.titleBarMessage, event, Snackbar.LENGTH_SHORT).show()
    }

    private fun setDefaultTitle() {
        val title = when (chatType) {
            DemoConstant.CHATTYPE_GROUP -> {
                GroupHelper.getGroupName(groupId = conversationId)
            }
            DemoConstant.CHATTYPE_CHATROOM -> {
                val room =
                    EMClient.getInstance().chatroomManager().getChatRoom(conversationId)
                if (room == null) {
                    viewModel.getChatRoom(roomId = conversationId)
                    return
                }
                room.name.ifEmpty { conversationId }
            }
            else -> {
                val userProvider = EaseIM.getInstance().userProvider
                if (userProvider != null) {
                    val user = userProvider.getUser(conversationId)
                    if (user != null) {
                        user.nickname
                    } else {
                        conversationId
                    }
                } else {
                    conversationId
                }
            }
        }

        binding.titleBarMessage.setTitle(title)
    }

    companion object {
        fun actionStart(context: Context, conversationId: String, chatType: Int) {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(EaseConstant.EXTRA_CONVERSATION_ID, conversationId)
            intent.putExtra(EaseConstant.EXTRA_CHAT_TYPE, chatType)
            context.startActivity(intent)
        }
    }
}