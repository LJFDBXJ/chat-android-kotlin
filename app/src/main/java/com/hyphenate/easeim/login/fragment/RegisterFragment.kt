package com.hyphenate.easeim.login.fragment

import android.graphics.Color
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import com.hyphenate.EMError
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.enums.Status
import com.hyphenate.easeim.common.ktx.toast
import com.hyphenate.easeim.databinding.FragmentRegisterBinding
import com.hyphenate.easeim.section.base_ktx.WebViewActivity
import com.hyphenate.easeim.section.base_ktx.BaseInitFragmentKtx
import com.hyphenate.easeim.login.viewmodels.LoginVm
import com.hyphenate.easeui.utils.EaseEditTextUtils

class RegisterFragment(override val layoutId: Int = R.layout.fragment_register) :
    BaseInitFragmentKtx<FragmentRegisterBinding>(),
    View.OnClickListener {
    private val mViewModel by activityViewModels<LoginVm>()

    override fun initListener() {
        super.initListener()
        val editChange = { _: Editable? ->
            checkEditContent()
        }
        binding.etLoginName.doAfterTextChanged(editChange)
        binding.etLoginPwd.doAfterTextChanged(editChange)
        binding.etLoginPwdConfirm.doAfterTextChanged(editChange)
        binding.toolbarRegister.setOnBackPressListener {
            onBackPress()
        }
        binding.cbSelect.setOnCheckedChangeListener { _, _ ->
            checkEditContent()
        }
        EaseEditTextUtils.clearEditTextListener(binding.etLoginName)
    }

    override fun initData() {
        super.initData()
        binding.tvAgreement.text = spannable
        binding.tvAgreement.movementMethod = LinkMovementMethod.getInstance()

        mViewModel.register.observe(this) {
            if (it.status == Status.SUCCESS) {
                toast(R.string.em_register_success)
                onBackPress()
            } else {
                if (it.errorCode == EMError.USER_ALREADY_EXIST) {
                    toast(R.string.demo_error_user_already_exist)
                } else {
                    toast(it.message)
                }
            }
        }
        //切换密码可见不可见的两张图片
    }

    private fun checkEditContent() {
        val mUserName = binding.etLoginName.text.toString()
        val mPwd = binding.etLoginPwd.text.toString()
        val mPwdConfirm = binding.etLoginPwdConfirm.text.toString()
        val result = mUserName.isNotEmpty()
                && mPwd.isNotEmpty()
                && mPwdConfirm.isNotEmpty()
                && binding.cbSelect.isChecked
        setButtonEnable(enable = result)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnLogin -> registerToHx()
        }
    }

    private fun registerToHx() {
        val mUserName = binding.etLoginName.text.toString()
        val mPwd = binding.etLoginPwd.text.toString()
        val mPwdConfirm = binding.etLoginPwdConfirm.text.toString()
        when {
            mUserName.isEmpty() -> toast("名字不能为空")
            mPwd.isEmpty() -> {
                toast("密码不能为空")
            }
            mPwdConfirm.isEmpty() -> {
                toast("确认密码不能为空")
            }
            mPwd != mPwdConfirm -> {
                toast(R.string.em_password_confirm_error)
            }
            else -> {
                mViewModel.register(userName = mUserName, pwd = mPwd)
            }
        }
    }

    private fun setButtonEnable(enable: Boolean) {
        binding.btnLogin.isEnabled = enable
        //同时需要修改右侧drawableRight对应的资源
        val drawableRes = if (enable) {
            R.drawable.demo_login_btn_right_enable
        } else {
            R.drawable.demo_login_btn_right_unable
        }
        val rightDrawable = ContextCompat.getDrawable(requireContext(), drawableRes)
        binding.btnLogin.setCompoundDrawablesWithIntrinsicBounds(null, null, rightDrawable, null);
    }

    //spanStr.setSpan(new UnderlineSpan(), 10, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    private val spannable: SpannableString
        get() {
            val spanStr = SpannableString(getString(R.string.em_login_agreement))
            //设置下划线
            //spanStr.setSpan(new UnderlineSpan(), 3, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanStr.setSpan(object : MyClickableSpan() {
                override fun onClick(widget: View) {
                    WebViewActivity.actionStart(
                        mContext,
                        getString(R.string.em_register_service_agreement_url)
                    )
                }
            }, 2, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            //spanStr.setSpan(new UnderlineSpan(), 10, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanStr.setSpan(object : MyClickableSpan() {
                override fun onClick(widget: View) {
                    WebViewActivity.actionStart(
                        mContext,
                        getString(R.string.em_register_privacy_agreement_url)
                    )
                }
            }, 11, spanStr.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            return spanStr
        }

    private abstract inner class MyClickableSpan : ClickableSpan() {
        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.bgColor = Color.TRANSPARENT
            ds.color = ContextCompat.getColor(mContext, R.color.white)
        }
    }
}