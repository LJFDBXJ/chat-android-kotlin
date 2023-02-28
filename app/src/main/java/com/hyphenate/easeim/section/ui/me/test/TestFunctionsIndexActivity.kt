package com.hyphenate.easeim.section.ui.me.test

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.View
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMGroup
import com.hyphenate.chat.EMPresence
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.ktx.toast
import com.hyphenate.easeim.databinding.ActivityTestFunctionsIndexBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.ui.me.test.TestFunctionsIndexActivity
import com.hyphenate.util.EMLog

class TestFunctionsIndexActivity(override val layoutId: Int = R.layout.activity_test_functions_index) :
    BaseInitActivityKtx<ActivityTestFunctionsIndexBinding>(),
    View.OnClickListener {


    override fun initListener() {
        super.initListener()
        binding.btnMuteGroupMember.setOnClickListener(this)
        binding.btnPresenceUsername.setOnClickListener(this)
        binding.titleBar.setOnBackPressListener { view: View? -> onBackPressed() }
    }

    override fun onClick(v: View) {
        if (v.id == R.id.btn_mute_group_member) {
            muteGroupMember()
        } else if (v.id == R.id.btn_presence_username) {
            presenceUsername()
        }
    }

    private fun muteGroupMember() {
        val username = binding.etMuteGroupMember.text.toString().trim { it <= ' ' }
        val groupId = binding.etMuteGroupId.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(username)) {
            toast("Username should not be null")
            return
        }
        if (TextUtils.isEmpty(groupId)) {
            toast("GroupId should not be null")
            return
        }
        val usernames: MutableList<String> = ArrayList()
        usernames.add(username)
        EMClient.getInstance().groupManager()
            .aysncMuteGroupMembers(groupId, usernames, 1000000, object : EMValueCallBack<EMGroup?> {
                override fun onSuccess(value: EMGroup?) {
                    toast("Mute user: $username in group: $groupId success")
                }

                override fun onError(error: Int, errorMsg: String) {
               toast("Mute failed, error: $error errormsg: $errorMsg")
                    EMLog.e("TAG", "Mute failed, error: $error errormsg: $errorMsg")
                }
            })
    }

    private fun presenceUsername() {
        val username = binding.etPresenceUsername.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(username)) {
            toast("Username should not be null")
            return
        }
        val usernames: MutableList<String> = ArrayList()
        usernames.add(username)
        EMClient.getInstance().presenceManager()
            .subscribePresences(usernames, 100000, object : EMValueCallBack<List<EMPresence?>?> {
                override fun onSuccess(value: List<EMPresence?>?) {
                    toast("Presence user: $username success")
                }

                override fun onError(error: Int, errorMsg: String) {
                    toast("Presence failed, error: $error errormsg: $errorMsg")
                    EMLog.e("TAG", "Presence failed, error: $error errormsg: $errorMsg")
                }
            })
    }

    companion object {
        fun actionStart(context: Context) {
            val intent = Intent(context, TestFunctionsIndexActivity::class.java)
            context.startActivity(intent)
        }
    }
}