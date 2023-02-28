package com.hyphenate.easeim.section.ui.group.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import androidx.activity.viewModels
import com.hyphenate.chat.EMChatRoom
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.interfaceOrImplement.OnResourceParseCallback
import com.hyphenate.easeim.common.livedatas.LiveDataBus
import com.hyphenate.easeim.common.livedatas.obs
import com.hyphenate.easeim.section.ui.group.GroupHelper
import com.hyphenate.easeim.section.ui.group.vm.ChatRoomMemberVm
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.model.EaseEvent
import java.util.*

open class ChatRoomMemberAuthorityActivity : GroupMemberAuthorityActivity() {
    protected var chatRoom: EMChatRoom? = null
    protected var roomId: String = ""
    val roomViewModel by viewModels<ChatRoomMemberVm>()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        roomId = intent.getStringExtra("roomId") ?: ""
        binding.titleBar.setTitle(getString(R.string.em_chat_room_detail_members))
    }

    override fun onSubPrepareOptionsMenu(menu: Menu) {
        super.onSubPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_group_add).isVisible = false
        if (!isOwner && !isInAdminList(SdkHelper.instance.currentUser)) {
            menu.findItem(R.id.action_group_black).isVisible = false
            menu.findItem(R.id.action_group_mute).isVisible = false
        }
    }

    override fun initData() {
        chatRoom = SdkHelper.instance.chatroomManager.getChatRoom(roomId)
    }

    override fun initListener() {
        super.initListener()
        roomViewModel.chatRoomObservable.observe(this) { response ->
            chatRoom = response
            refreshData()
        }
        roomViewModel.members.observe(this) { response ->
            parseResource(
                response = response,
                object : OnResourceParseCallback<List<String>?>() {
                    override fun onSuccess(data: List<String>?) {
                        //List<EaseUser> parse = EmUserEntity.parse(data);
                        val users: MutableList<EaseUser> = ArrayList()
                        if (!data.isNullOrEmpty()) {
                            for (i in data.indices) {
                                val user = SdkHelper.instance.getUserInfo(data[i])
                                if (user != null) {
                                    users.add(user)
                                } else {
                                    users.add(EaseUser(data[i]))
                                }
                            }
                        }
                        sortUserData(users)
                        adapter.setList(users)
                    }

                    override fun hideLoading() {
                        super.hideLoading()
                        finishRefresh()
                    }
                })
        }
        roomViewModel.black.observe(this) { response ->
            if (!response.isNullOrEmpty()) {
                blackMembers.clear()
                blackMembers.addAll(response)
                if (flag == TYPE_BLACK) {
                    val parse = EaseUser.parse(response)
                    sortUserData(parse)
                    adapter.setList(parse)
                }
            }
            if (flag == TYPE_BLACK) {
                finishRefresh()
            }
        }
        roomViewModel.muteMap.observe(this) { response ->
            parseResource(
                response,
                object : OnResourceParseCallback<Map<String, Long>?>() {
                    override fun onSuccess(data: Map<String, Long>?) {
                        if (!data.isNullOrEmpty()) {
                            muteMembers = ArrayList(data.keys)
                            if (flag == TYPE_MUTE) {
                                val parse = EaseUser.parse(muteMembers)
                                sortUserData(parse)
                                adapter.setList(parse)
                            }
                        }

                    }

                    override fun hideLoading() {
                        super.hideLoading()
                        if (flag == TYPE_MUTE) {
                            finishRefresh()
                        }
                    }
                })
        }
        roomViewModel.destroyGroup.observe(this) {

        }
        arrayOf(
            DemoConstant.CHAT_ROOM_CHANGE,
            DemoConstant.CONTACT_CHANGE,
            DemoConstant.CONTACT_UPDATE,
            DemoConstant.CONTACT_ADD,
        ).obs<EaseEvent>(this) { key, event ->
            event ?: return@obs
            if (event.isChatRoomLeave && roomId == event.message) {
                finish()
                return@obs
            } else
                when (key) {
                    DemoConstant.CHAT_ROOM_CHANGE -> {
                        roomViewModel.getChatRoom(roomId)
                    }
                    else -> {
                        refreshData()
                    }
                }

        }

        refreshData()
    }

    //List<EaseUser> parse = EmUserEntity.parse(data);
    override val data: Unit
        //监听有关用户属性的事件
        get() {

        }

    override fun refreshData() {
        if (flag == TYPE_MEMBER) {
            roomViewModel.getMembersList(roomId)
        }
        if (!isMember) {
            roomViewModel.getGroupBlackList(roomId)
            roomViewModel.getGroupMuteMap(roomId)
        }
        when (flag) {
            TYPE_MEMBER -> {
                binding.titleBar.setTitle(getString(R.string.em_authority_menu_member_list))
            }
            TYPE_BLACK -> {
                binding.titleBar.setTitle(getString(R.string.em_authority_menu_black_list))
            }
            else -> {
                binding.titleBar.setTitle(getString(R.string.em_authority_menu_mute_list))
            }
        }
    }

    override val isMember: Boolean
        get() {
            val currentUser = SdkHelper.instance.currentUser
            return (!TextUtils.equals(currentUser, chatRoom?.owner)
                    && chatRoom?.adminList?.contains(currentUser) != true)
        }

    override fun isInAdminList(username: String): Boolean {
        return GroupHelper.isInAdminList(username = username, adminList = chatRoom?.adminList)
    }

    override val isOwner: Boolean
        get() = GroupHelper.isOwner(chatRoom)

    fun update() {
        LiveDataBus.get().use(DemoConstant.CHAT_ROOM_CHANGE)
            .postValue(EaseEvent.create(DemoConstant.CHAT_ROOM_CHANGE, EaseEvent.TYPE.CHAT_ROOM))
    }

    override fun addToAdmins(username: String) {
        roomViewModel.addGroupAdmin(roomId, username)
        update()
    }

    override fun removeFromAdmins(username: String) {
        roomViewModel.removeGroupAdmin(roomId, username)
        update()
    }

    override fun transferOwner(username: String) {
        roomViewModel.changeOwner(roomId, username)
        update()
    }

    override fun removeFromGroup(username: String) {
        update()
        val usernames = ArrayList<String>()
        usernames.add(username)
        roomViewModel.removeUserFromGroup(roomId, usernames)
    }

    override fun addToBlack(username: String) {
        val usernames = ArrayList<String>()
        usernames.add(username)
        roomViewModel.blockUser(roomId, usernames)
        update()
    }

    override fun removeFromBlacks(username: String) {
        val usernames = ArrayList<String>()
        usernames.add(username)
        roomViewModel.unblockUser(roomId, usernames)
        update()
    }

    override fun addToMuteMembers(username: String) {
        val mutes: MutableList<String> = ArrayList()
        mutes.add(username)
        roomViewModel.muteGroupMembers(roomId, mutes, (20 * 60 * 1000).toLong())
    }

    override fun removeFromMuteMembers(username: String) {
        val unMutes: MutableList<String> = ArrayList()
        unMutes.add(username)
        roomViewModel.unMuteGroupMembers(roomId, unMutes)
    }

    protected fun sortUserData(users: List<EaseUser>) {
        Collections.sort(users, Comparator { lhs, rhs ->
            if (lhs.initialLetter == rhs.initialLetter) {
                lhs.nickname.compareTo(rhs.nickname)
            } else {
                if ("#" == lhs.initialLetter) {
                    return@Comparator 1
                } else if ("#" == rhs.initialLetter) {
                    return@Comparator -1
                }
                lhs.initialLetter.compareTo(rhs.initialLetter)
            }
        })
    }

    companion object {
        @JvmStatic
        fun actionStart(context: Context, roomId: String?) {
            val starter = Intent(context, ChatRoomMemberAuthorityActivity::class.java)
            starter.putExtra("roomId", roomId)
            context.startActivity(starter)
        }
    }
}