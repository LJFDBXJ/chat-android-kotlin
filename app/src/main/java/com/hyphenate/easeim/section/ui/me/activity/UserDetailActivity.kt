package com.hyphenate.easeim.section.ui.me.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.bumptech.glide.Glide
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMUserInfo
import com.hyphenate.chat.EMUserInfo.EMUserInfoType
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.livedatas.obs
import com.hyphenate.easeim.common.utils.PreferenceManager
import com.hyphenate.easeim.databinding.ActivityUserDetailBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.util.EMLog

class UserDetailActivity(override val layoutId: Int = R.layout.activity_user_detail) :
    BaseInitActivityKtx<ActivityUserDetailBinding>() {
    private var headImageUrl: String? = null
    private var nickName: String? = null


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        headImageUrl = intent.getStringExtra("imageUrl")
        nickName = intent.getStringExtra("nickName")
    }

    override fun initListener() {
        super.initListener()
        binding.titleBar.setOnBackPressListener { onBackPressed() }
        binding.itemNickname.setOnClickListener { v ->
            val intent = Intent(v.context, OfflinePushNickActivity::class.java)
            intent.putExtra("nickName", nickName)
            startActivityForResult(intent, 2)
        }
        binding.tvHeadImageView.setOnClickListener { v ->
            val intent = Intent(v.context, ChooseHeadImageActivity::class.java)
            intent.putExtra("headUrl", headImageUrl)
            startActivityForResult(intent, 1)
        }
    }

    override fun initData() {
        super.initData()
        if (!headImageUrl.isNullOrEmpty()) {
            Glide.with(this).load(headImageUrl).placeholder(R.drawable.em_login_logo).into(
                binding.tvHeadImageView
            )
        }
        if (headImageUrl == null || nickName == null) {
            intSelfDate()
        }

        //增加数据变化监听
        addLiveDataObserver()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null) {
                headImageUrl = data.getStringExtra("headImage")
                Glide.with(this).load(headImageUrl).placeholder(R.drawable.em_login_logo).into(
                    binding.tvHeadImageView
                )
            }
        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            if (data != null) {
                nickName = data.getStringExtra("nickName")
            }
        }
    }

    private fun intSelfDate() {
        val userId = arrayOfNulls<String>(1)
        userId[0] = EMClient.getInstance().currentUser
        val userInfoTypes = arrayOfNulls<EMUserInfoType>(2)
        userInfoTypes[0] = EMUserInfoType.NICKNAME
        userInfoTypes[1] = EMUserInfoType.AVATAR_URL
        EMClient.getInstance().userInfoManager().fetchUserInfoByAttribute(
            userId,
            userInfoTypes,
            object : EMValueCallBack<Map<String?, EMUserInfo?>> {
                override fun onSuccess(userInfos: Map<String?, EMUserInfo?>) {
                    runOnUiThread {
                        val userInfo = userInfos[EMClient.getInstance().currentUser]

                        //昵称
                        if (userInfo != null && userInfo.nickName != null && userInfo.nickName.length > 0) {
                            nickName = userInfo.nickName
                            PreferenceManager.getInstance().currentUserNick = nickName
                        }
                        //头像
                        if (userInfo != null && userInfo.avatarUrl != null && userInfo.avatarUrl.length > 0) {
                            headImageUrl = userInfo.avatarUrl
                            Glide.with(binding.tvHeadImageView).load(headImageUrl)
                                .placeholder(R.drawable.em_login_logo).into(
                                    binding.tvHeadImageView
                                )
                            PreferenceManager.getInstance().currentUserAvatar = headImageUrl
                        }
                    }
                }

                override fun onError(error: Int, errorMsg: String) {
                    EMLog.e(TAG, "fetchUserInfoByIds error:$error errorMsg:$errorMsg")
                }
            })
    }

    fun addLiveDataObserver() {
        arrayOf(
            DemoConstant.AVATAR_CHANGE,
            DemoConstant.NICK_NAME_CHANGE
        ).obs<EaseEvent>(this) { key, event ->
            event ?: return@obs
            when (key) {
                DemoConstant.AVATAR_CHANGE -> {
                    Glide.with(this)
                        .load(event.message)
                        .placeholder(R.drawable.em_login_logo).into(
                            binding.tvHeadImageView
                        )
                }
                DemoConstant.NICK_NAME_CHANGE -> {
                    nickName = event.message

                }
            }
        }
    }

    companion object {
        private const val TAG = "UserDetailActivity"
        fun actionStart(context: Context, nickName: String?, url: String?) {
            val intent = Intent(context, UserDetailActivity::class.java)
            intent.putExtra("imageUrl", url)
            intent.putExtra("nickName", nickName)
            context.startActivity(intent)
        }
    }
}