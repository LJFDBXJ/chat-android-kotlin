package com.hyphenate.easeim.login.fragment

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.hyphenate.EMError
import com.hyphenate.chat.EMClient
import com.hyphenate.easeim.MainActivity
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.db.DbHelper
import com.hyphenate.easeim.common.enums.Status
import com.hyphenate.easeim.common.ktx.toast
import com.hyphenate.easeim.common.utils.SpDbModel
import com.hyphenate.easeim.databinding.FragmentLoginBinding
import com.hyphenate.easeim.login.activity.LoginActivity
import com.hyphenate.easeim.section.base_ktx.BaseInitFragmentKtx
import com.hyphenate.easeim.login.viewmodels.LoginFragmentVm
import com.hyphenate.easeim.login.viewmodels.LoginVm
import com.hyphenate.easeui.utils.EaseEditTextUtils

class LoginFragment(override val layoutId: Int = R.layout.fragment_login) :
    BaseInitFragmentKtx<FragmentLoginBinding>(), View.OnClickListener,
    OnEditorActionListener {

    private val mFragmentViewModel by activityViewModels<LoginFragmentVm>()
    private val mViewModel by activityViewModels<LoginVm>()

    private var isTokenFlag = false //是否是token登录
    private var clear: Drawable? = null
    private var eyeOpen: Drawable? = null
    private var eyeClose: Drawable? = null
    private var mUserName: String? = null
    private var mPwd: String? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        // 保证切换fragment后相关状态正确
        val enableTokenLogin = SpDbModel.instance.isEnableTokenLogin()
        binding.tvLoginToken.visibility =
            if (enableTokenLogin) View.VISIBLE else View.GONE
//        binding.etLoginName.setText(DemoHelper.instance.currentUser ?: "")
        binding.tvVersion.text = "V" + EMClient.VERSION
        if (isTokenFlag) {
            switchLogin()
        }
    }

    override fun initListener() {
        super.initListener()
        val editChange = { _: Editable? ->
            afterTextChanged()
        }
        binding.etLoginName.doAfterTextChanged(editChange)
        binding.etLoginPwd.doAfterTextChanged(editChange)
        binding.tvLoginRegister.setOnClickListener(this)
        binding.tvLoginToken.setOnClickListener(this)
        binding.tvLoginServerSet.setOnClickListener(this)
        binding.btnLogin.setOnClickListener(this)
        binding.cbSelect.setOnCheckedChangeListener { _, isChecked ->
            setButtonEnable(
                !mUserName.isNullOrEmpty() && !mPwd.isNullOrEmpty() && isChecked
            )
        }
        binding.etLoginPwd.setOnEditorActionListener(this)
        EaseEditTextUtils.clearEditTextListener(binding.etLoginName)
        mFragmentViewModel.loginObservable.observe(this) {
            if (it.status == Status.SUCCESS) {
                SdkHelper.instance.autoLogin = true
                //跳转到主页
                MainActivity.startAction(mContext)
                mContext.finish()
            } else {
                if (it.errorCode == EMError.USER_AUTHENTICATION_FAILED) {
                    toast(R.string.demo_error_user_authentication_failed)
                } else {
                    toast(it.message)
                }
            }
        }
    }


    private val dataDbObs = Observer<Boolean> {
        Log.i(
            "login",
            "本地数据库初始化完成"
        )
    }

    override fun initViewModel() {
        super.initViewModel()
        mViewModel.register.observe(this) {
            binding.etLoginName.setText(if (it?.data.isNullOrEmpty()) "" else it!!.data)
            binding.etLoginPwd.setText("")
        }
        if (!DbHelper.dbHelper().mIsDatabaseCreated.hasObservers()) {
            DbHelper.dbHelper().mIsDatabaseCreated.observe(this, dataDbObs)
        }
    }

    override fun initData() {
        super.initData()
        binding.tvAgreement.text = spannable
        binding.tvAgreement.movementMethod = LinkMovementMethod.getInstance()
        //切换密码可见不可见的两张图片
        eyeClose = ContextCompat.getDrawable(requireContext(), R.drawable.d_pwd_hide)
        eyeOpen = ContextCompat.getDrawable(requireContext(), R.drawable.d_pwd_show)
        clear = ContextCompat.getDrawable(requireContext(), R.drawable.d_clear)
        EaseEditTextUtils.showRightDrawable(binding.etLoginName, clear)
        EaseEditTextUtils.changePwdDrawableRight(
            binding.etLoginPwd,
            eyeClose,
            eyeOpen,
            null,
            null,
            null
        )
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tvLoginRegister -> {
                mViewModel.clearRegisterInfo()
                mViewModel.setPageSelect(LoginActivity.PAGE_REGISTER)
            }
            R.id.tvLoginToken -> {
                isTokenFlag = !isTokenFlag
                switchLogin()
            }
            R.id.tvLoginServerSet -> mViewModel.setPageSelect(LoginActivity.PAGE_SERVER_SET)
            R.id.btnLogin -> {
                hideKeyboard()
                loginToServer()
            }
        }
    }

    /**
     * 切换登录方式
     */
    private fun switchLogin() {
        binding.etLoginPwd.setText("")
        if (isTokenFlag) {
            binding.etLoginPwd.setHint(R.string.em_login_token_hint)
            binding.tvLoginToken.setText(R.string.em_login_tv_pwd)
            binding.etLoginPwd.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
        } else {
            binding.etLoginPwd.setHint(R.string.em_login_password_hint)
            binding.tvLoginToken.setText(R.string.em_login_tv_token)
            binding.etLoginPwd.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
    }

    private fun loginToServer() {
        if (mUserName.isNullOrEmpty() || mPwd.isNullOrEmpty()) {
            toast(R.string.em_login_btn_info_incomplete)
            return
        }
        mFragmentViewModel.login(userName = mUserName!!, pwd = mPwd!!, isTokenFlag = isTokenFlag)
    }

    private fun afterTextChanged() {
        mUserName = binding.etLoginName.text.toString().trim { it <= ' ' }
        mPwd = binding.etLoginPwd.text.toString().trim { it <= ' ' }
        EaseEditTextUtils.showRightDrawable(binding.etLoginName, clear)
        EaseEditTextUtils.showRightDrawable(binding.etLoginPwd, if (isTokenFlag) null else eyeClose)
        setButtonEnable(!mUserName.isNullOrEmpty() && !mPwd.isNullOrEmpty())
    }

    private fun setButtonEnable(enable: Boolean) {
        binding.btnLogin.isEnabled = enable
        if (binding.etLoginPwd.hasFocus()) {
            binding.etLoginPwd.imeOptions =
                if (enable)
                    EditorInfo.IME_ACTION_DONE
                else
                    EditorInfo.IME_ACTION_PREVIOUS
        } else if (binding.etLoginName.hasFocus()) {
            binding.etLoginPwd.imeOptions =
                if (enable) EditorInfo.IME_ACTION_DONE else EditorInfo.IME_ACTION_NEXT
        }
        //同时需要修改右侧drawableRight对应的资源
        val res = if (enable) {
            R.drawable.demo_login_btn_right_enable
        } else {
            R.drawable.demo_login_btn_right_unable
        }
        val rightDrawable = ContextCompat.getDrawable(mContext, res)
        binding.btnLogin.setCompoundDrawablesWithIntrinsicBounds(null, null, rightDrawable, null);
    }
    //设置下划线

    //spanStr.setSpan(new UnderlineSpan(), 10, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    private val spannable: SpannableString
        get() {
            val spanStr = SpannableString(getString(R.string.em_login_agreement))
            //设置下划线
            //spanStr.setSpan(new UnderlineSpan(), 3, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanStr.setSpan(object : MyClickableSpan() {
                override fun onClick(widget: View) {
                    toast(mContext.getString(R.string.intent_to_service))
                }
            }, 2, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            //spanStr.setSpan(new UnderlineSpan(), 10, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanStr.setSpan(object : MyClickableSpan() {
                override fun onClick(widget: View) {
                    toast(mContext.getString(R.string.intent_to_privacy_protocol))
                }
            }, 11, spanStr.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            return spanStr
        }

    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            if (!TextUtils.isEmpty(mUserName) && !TextUtils.isEmpty(mPwd)) {
                hideKeyboard()
                loginToServer()
                return true
            }
        }
        return false
    }

    private abstract inner class MyClickableSpan : ClickableSpan() {
        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.bgColor = Color.TRANSPARENT
        }
    }

    override fun onStop() {
        super.onStop()
        DbHelper.dbHelper().mIsDatabaseCreated.removeObservers(this)
    }
}