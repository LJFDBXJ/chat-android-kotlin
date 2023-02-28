package com.hyphenate.easeim.section.ui.group.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.activity.viewModels
import com.hyphenate.chat.EMChatRoom
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.interfaceOrImplement.OnResourceParseCallback
import com.hyphenate.easeim.common.ktx.toast
import com.hyphenate.easeim.common.livedatas.LiveDataBus
import com.hyphenate.easeim.common.livedatas.obs
import com.hyphenate.easeim.databinding.ActivityChatChatRoomDetailBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.dialog_ktx.EditTextDialogFragment
import com.hyphenate.easeim.section.dialog_ktx.FullEditDialogFragment
import com.hyphenate.easeim.section.dialog_ktx.SimpleDialogFragment
import com.hyphenate.easeim.section.ui.group.vm.ChatRoomDetailVm
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.easeui.widget.EaseTitleBar.OnBackPressListener

class ChatRoomDetailActivity(override val layoutId: Int = R.layout.activity_chat_chat_room_detail) :
    BaseInitActivityKtx<ActivityChatChatRoomDetailBinding>(), OnBackPressListener,
    View.OnClickListener {
    private var roomId: String = ""
    private val viewModel by viewModels<ChatRoomDetailVm>()
    private var chatRoom: EMChatRoom? = null


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        roomId = intent.getStringExtra("roomId") ?: ""
        chatRoom = SdkHelper.instance.chatroomManager.getChatRoom(roomId)
        updateContent(chatRoom = chatRoom!!)
    }

    override fun initListener() {
        super.initListener()
        binding.titleBar.setOnBackPressListener(this)
        binding.itemChatRoomName.setOnClickListener(this)
        binding.itemChatRoomDescription.setOnClickListener(this)
        binding.itemChatRoomMembers.setOnClickListener(this)
        binding.itemChatRoomAdmins.setOnClickListener(this)
        binding.tvChatRoomRefund.setOnClickListener(this)
    }

    override fun initData() {
        super.initData()
        viewModel.chatRoom.observe(this) { response ->
            chatRoom = response
            response?.let {
                updateContent(chatRoom = response)
            }
        }
        viewModel.announcement.observe(this) { response ->
            parseResource(
                response = response,
                callback = object : OnResourceParseCallback<String>() {
                    override fun onSuccess(data: String?) {

                    }
                })
        }
        viewModel.destroyGroup.observe(this) { response ->
            if (!response) {
                return@observe
            }
            toast(R.string.demo_chat_room_destroy)

            LiveDataBus.get().use(DemoConstant.CHAT_ROOM_CHANGE).postValue(
                EaseEvent(
                    DemoConstant.CHAT_ROOM_CHANGE,
                    EaseEvent.TYPE.CHAT_ROOM_LEAVE
                ).apply {
                    message = roomId
                })
            finish()
        }
        DemoConstant.CHAT_ROOM_CHANGE.obs<EaseEvent>(this) { event ->
            event ?: return@obs
            if (event.type == EaseEvent.TYPE.CHAT_ROOM) {
                getChatRoom()
            }
            if (event.isChatRoomLeave && roomId == event.message) {
                finish()
            }
        }
        viewModel.members.observe(this) { response ->
            parseResource(
                response,
                object : OnResourceParseCallback<List<String>?>() {
                    override fun onSuccess(data: List<String>?) {
                        val code = data?.size.toString() + "人"
                        binding.itemChatRoomMembers.tvContent.text = code
                    }
                })
        }
        getChatRoom()
    }

    private fun updateContent(chatRoom: EMChatRoom) {
        binding.itemChatRoomId.tvContent.text = chatRoom.id
        binding.itemChatRoomDescription.tvContent.text = chatRoom.description
        binding.itemChatRoomName.tvContent.text = chatRoom.name
        binding.itemChatRoomOwner.tvContent.text = chatRoom.owner
        binding.itemChatRoomAdmins.tvContent.text = (chatRoom.adminList.size + 1).toString() + "人"
        binding.itemChatRoomMembers.tvContent.text = chatRoom.memberList.size.toString() + "人"
        binding.tvChatRoomRefund.visibility =
            if (isOwner) View.VISIBLE else View.GONE

        viewModel.getChatRoomMembers(roomId = chatRoom.id)
    }

    private fun getChatRoom() {
        viewModel.getChatRoomFromServer(roomId = roomId)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.item_chat_room_name ->
                showChatRoomNameDialog()
            R.id.item_chat_room_description ->
                showDescriptionDialog()
            R.id.item_chat_room_members ->
                ChatRoomMemberAuthorityActivity.actionStart(
                    context = this,
                    roomId = roomId
                )
            R.id.item_chat_room_admins ->
                ChatRoomAdminAuthorityActivity.actionStart(
                    context = this,
                    roomId = roomId
                )
            R.id.tv_chat_room_refund ->
                SimpleDialogFragment.Builder(this)
                    .setTitle(R.string.em_chat_room_detail_destroy_info)
                    .setOnConfirmClickListener {
                        viewModel.destroyGroup(roomId = roomId)
                    }
                    .showCancelButton(true)
                    .show()
        }
    }

    private fun showChatRoomNameDialog() {
        if (!isOwner) {
            return
        }
        EditTextDialogFragment.Builder(this)
            .setContent(chatRoom?.name)
            .setConfirmClickListener { _, content ->
                if (!TextUtils.isEmpty(content)) {
                    viewModel.changeChatRoomSubject(
                        roomId = roomId,
                        newSubject = content
                    )
                }
            }
            .setTitle(R.string.em_chat_room_detail_room_name)
            .show()
    }

    private fun showDescriptionDialog() {
        FullEditDialogFragment.Builder(this)
            .setTitle(R.string.em_chat_room_detail_description)
            .setContent(chatRoom?.description)
            .setHint(R.string.em_chat_room_detail_description_hint)
            .enableEdit(isOwner)
            .setOnConfirmClickListener(object : FullEditDialogFragment.OnSaveClickListener {
                override fun onSaveClick(view: View, content: String) {
                    if (content.isNotEmpty()) {
                        viewModel.changeChatroomDescription(
                            roomId = roomId,
                            newDescription = content
                        )
                    }
                }
            })
            .show()
    }

    override fun onBackPress(view: View) {
        onBackPressed()
    }

    private val isOwner: Boolean
        get() = chatRoom != null &&
                SdkHelper.instance.currentUser == chatRoom?.owner


    companion object {
        @JvmStatic
        fun actionStart(context: Context, roomId: String?) {
            val intent = Intent(context, ChatRoomDetailActivity::class.java)
            intent.putExtra("roomId", roomId)
            context.startActivity(intent)
        }
    }
}