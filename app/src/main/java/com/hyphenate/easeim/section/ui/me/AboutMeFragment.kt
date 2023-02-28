package com.hyphenate.easeim.section.ui.me


import android.os.Bundle
import android.view.View
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMUserInfo
import com.hyphenate.easeim.R
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.livedatas.obs
import com.hyphenate.easeim.databinding.FragmentAboutMeBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitFragmentKtx
import com.hyphenate.easeim.section.ui.me.activity.DeveloperSetActivity
import com.hyphenate.easeim.section.ui.me.activity.SettingActivity
import com.hyphenate.easeim.section.ui.me.activity.UserDetailActivity
import com.hyphenate.easeui.model.EaseEvent

/**
 * @author LXJDBXJ
 * @des 首页 关于我
 */
class AboutMeFragment(override val layoutId: Int = R.layout.fragment_about_me) :
    BaseInitFragmentKtx<FragmentAboutMeBinding>(), View.OnClickListener {
    private val userInfo: EMUserInfo? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        val nickName = context?.getString(R.string.account) + SdkHelper.instance.currentUser
        binding.tvNickName.text = nickName
    }

    override fun initListener() {
        super.initListener()
        binding.clUser.setOnClickListener(this)
        binding.itemCommonSet.setOnClickListener(this)
        binding.itemDeveloperSet.setOnClickListener(this)
        DemoConstant.AVATAR_CHANGE.obs<EaseEvent>(this) {
            userInfo?.avatarUrl = it?.message
            binding.userAvatar = it?.message
        }
        DemoConstant.NICK_NAME_CHANGE.obs<EaseEvent>(this) {
            val nickName = mContext.getString(R.string.push_nick) + ": " + it?.message
            binding.tvNickName.text = nickName
            val userId =
                mContext.getString(R.string.account) + EMClient.getInstance().currentUser
            binding.tvUserId.text = userId
            userInfo?.nickname = it?.message
        }

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.clUser -> {
                if (userInfo != null) {
                    UserDetailActivity.actionStart(mContext, userInfo.nickname, userInfo.avatarUrl)
                } else {
                    UserDetailActivity.actionStart(mContext, null, null)
                }
            }
            R.id.itemCommonSet -> SettingActivity.actionStart(mContext)
            R.id.itemDeveloperSet -> DeveloperSetActivity.actionStart(mContext)
        }
    }

}