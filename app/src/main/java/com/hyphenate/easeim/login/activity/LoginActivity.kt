package com.hyphenate.easeim.login.activity

import android.content.Context
import com.hyphenate.easeim.R
import android.os.Bundle
import com.hyphenate.easeim.login.fragment.LoginFragment
import com.hyphenate.easeim.login.viewmodels.LoginVm
import com.hyphenate.easeim.login.fragment.RegisterFragment
import com.hyphenate.easeim.login.fragment.ServerSetFragment
import androidx.activity.viewModels
import com.hyphenate.easeim.common.ktx.jump
import com.hyphenate.easeim.databinding.ActivityLoginBinding
import com.hyphenate.easeim.section.base_ktx.BaseFragmentKtx
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx

/**
 * 登录
 */
class LoginActivity(override val layoutId: Int = R.layout.activity_login) :
    BaseInitActivityKtx<ActivityLoginBinding>() {
    private val viewModel by viewModels<LoginVm>()

    override fun initSystemFit() {
        setFitSystemForTheme(false, R.color.transparent)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        supportFragmentManager.beginTransaction().replace(R.id.fl_fragment, LoginFragment())
            .commit()
    }

    override fun initData() {
        super.initData()
        viewModel.pageSelect.observe(this) { page: Int ->
            when (page) {
                PAGE_REGISTER -> {
                    //注册
                    replace(RegisterFragment())
                }
                PAGE_SERVER_SET -> {
                    replace(ServerSetFragment())
                }
            }
        }
    }

    private fun replace(fragment: BaseFragmentKtx) {
        supportFragmentManager.beginTransaction().setCustomAnimations(
            R.anim.slide_in_from_right,
            R.anim.slide_out_to_left,
            R.anim.slide_in_from_left,
            R.anim.slide_out_to_right
        ).replace(R.id.fl_fragment, fragment).addToBackStack(null).commit()
    }

    companion object {
        const val PAGE_REGISTER = 1
        const val PAGE_SERVER_SET = 2
        fun startAction(context: Context) {
            context.jump<LoginActivity>()
        }
    }
}