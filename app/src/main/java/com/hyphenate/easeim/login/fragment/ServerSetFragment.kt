package com.hyphenate.easeim.login.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.ktx.toast
import com.hyphenate.easeim.common.utils.SpDbModel
import com.hyphenate.easeim.databinding.FragmentServerSetBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitFragmentKtx
import com.hyphenate.easeim.section.dialog_ktx.SimpleDialogFragment
import com.hyphenate.easeui.widget.EaseTitleBar.OnBackPressListener

class ServerSetFragment(override val layoutId: Int = R.layout.fragment_server_set) :
    BaseInitFragmentKtx<FragmentServerSetBinding>(), OnBackPressListener,
    CompoundButton.OnCheckedChangeListener, TextWatcher, View.OnClickListener {
    private var mServerAddress: String? = null
    private var mServerPort: String? = null
    private var mRestServerAddress: String? = null
    private var mAppkey: String? = null
    private var mCustomServerEnable = false
    private var mCustomSetEnable = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        checkServerSet()
    }

    override fun initListener() {
        super.initListener()
        binding.toolbarServer.setOnBackPressListener(this)
        binding.switchServer.setOnCheckedChangeListener(this)
        binding.switchSpecifyServer.setOnCheckedChangeListener(this)
        binding.etAppkey.addTextChangedListener(this)
        binding.etServerAddress.addTextChangedListener(this)
        binding.etServerPort.addTextChangedListener(this)
        binding.etServerRest.addTextChangedListener(this)
        binding.btnReset.setOnClickListener(this)
        binding.btnServer.setOnClickListener(this)
    }

    /**
     * 检查服务器设置
     */
    private fun checkServerSet() {
        val isInited = SdkHelper.instance.isSDKInit

        //判断是否显示设置数据，及是否可以自定义设置
        mCustomSetEnable = SpDbModel.instance.isCustomSetEnable()
        mCustomServerEnable = SpDbModel.instance.isCustomServerEnable()
        binding.switchServer.isChecked = mCustomSetEnable
        binding.switchSpecifyServer.isChecked = mCustomServerEnable
        binding.switchHttpsSet.isChecked = SpDbModel.instance.getUsingHttpsOnly()
        val appkey = SpDbModel.instance.getCustomAppkey()
        binding.etAppkey.setText(
            if (!SpDbModel.instance.isCustomAppKeyEnabled() || TextUtils.isEmpty(
                    appkey
                )
            ) "" else appkey
        )
        val imServer = SpDbModel.instance.getIMServer()
        binding.etServerAddress.setText(if (TextUtils.isEmpty(imServer)) "" else imServer)
        val imServerPort = SpDbModel.instance.getIMServerPort()
        binding.etServerPort.setText(if (imServerPort == 0) "" else imServerPort.toString() + "")
        val restServer = SpDbModel.instance.getRestServer()
        binding.etServerRest.setText(if (TextUtils.isEmpty(restServer)) "" else restServer)
        binding.groupServerSet.visibility =
            if (binding.switchServer.isChecked) View.VISIBLE else View.GONE
        setResetButtonVisible(binding.switchServer.isChecked, isInited)
        //设置是否可用
        binding.etServerHint.visibility = if (isInited) View.VISIBLE else View.GONE
        binding.etAppkey.isEnabled = !isInited
        binding.switchSpecifyServer.isEnabled = !isInited
        binding.etServerAddress.isEnabled = !isInited && mCustomServerEnable
        binding.etServerPort.isEnabled = !isInited && mCustomServerEnable
        binding.etServerRest.isEnabled = !isInited && mCustomServerEnable
        binding.switchHttpsSet.isEnabled = !isInited && mCustomServerEnable
        checkButtonEnable()
    }

    /**
     * 设置恢复默认设置的button是否可见
     * @param isChecked
     * @param isInited
     */
    private fun setResetButtonVisible(isChecked: Boolean, isInited: Boolean) {
        binding.btnReset.visibility =
            if (isChecked) if (isInited) View.GONE else View.VISIBLE else View.GONE
    }

    override fun onBackPress(view: View) {
        onBackPress()
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        when (buttonView.id) {
            R.id.switch_server -> {
                mCustomSetEnable = isChecked
                SpDbModel.instance.enableCustomSet(isChecked)
                SpDbModel.instance.enableCustomAppKey(
                    !TextUtils.isEmpty(
                        binding.etAppkey.text.toString().trim { it <= ' ' }) && isChecked
                )
                binding.groupServerSet.visibility = if (isChecked) View.VISIBLE else View.GONE
                setResetButtonVisible(isChecked, SdkHelper.instance.isSDKInit)
            }
            R.id.switch_specify_server -> {
                SpDbModel.instance.enableCustomServer(isChecked)
                mCustomServerEnable = isChecked
                binding.etServerAddress.isEnabled = isChecked
                binding.etServerPort.isEnabled = isChecked
                binding.etServerRest.isEnabled = isChecked
                binding.switchHttpsSet.isEnabled = isChecked
                checkButtonEnable()
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    override fun afterTextChanged(s: Editable) {
        mAppkey = binding.etAppkey.text.toString().trim { it <= ' ' }
        SpDbModel.instance.enableCustomAppKey(!TextUtils.isEmpty(mAppkey) && binding.switchServer.isChecked)
        checkButtonEnable()
    }

    private fun checkButtonEnable() {
        mAppkey = binding.etAppkey.text.toString().trim { it <= ' ' }
        if (mCustomServerEnable) {
            mServerAddress = binding.etServerAddress.text.toString().trim { it <= ' ' }
            mServerPort = binding.etServerPort.text.toString().trim { it <= ' ' }
            mRestServerAddress = binding.etServerRest.text.toString().trim { it <= ' ' }
            setButtonEnable(
                !TextUtils.isEmpty(mServerAddress)
                        && !TextUtils.isEmpty(mAppkey)
                        && !TextUtils.isEmpty(mServerPort)
                        && !TextUtils.isEmpty(mRestServerAddress)
            )
        } else {
            setButtonEnable(!TextUtils.isEmpty(mAppkey))
        }
        //如果sdk已经初始化完成，则应该显示初始化完成后的数据
        if (SdkHelper.instance.isSDKInit) {
            binding.btnServer.isEnabled = false
        }
    }

    override fun onClick(v: View) {
        if (v.id == R.id.btn_server) {
            saveServerSet()
        } else if (v.id == R.id.btn_reset) {
            SimpleDialogFragment.Builder(baseActivity)
                .setTitle(R.string.em_server_set_dialog_reset)
                .setOnConfirmClickListener {
                    val set = SpDbModel.instance.getDefServerSet()
                    binding.etAppkey.setText(set.appkey)
                    binding.etServerAddress.setText(set.imServer)
                    binding.etServerPort.setText(set.imPort.toString() + "")
                    binding.etServerRest.setText(set.restServer)
                }
                .showCancelButton(true)
                .show()
        }
    }

    private fun saveServerSet() {
        if (mCustomServerEnable) {
            //如果要使用私有云服务器，则要求以下几项不能为空
            if (mAppkey.isNullOrEmpty()) {
                toast(R.string.em_server_set_appkey_empty_hint)
                return
            }
            if (mServerAddress.isNullOrEmpty()) {
                toast(R.string.em_server_set_im_server_empty_hint)
                return
            }
            if (mServerPort.isNullOrEmpty()) {
                toast(R.string.em_server_set_im_port_empty_hint)
                return
            }
            if (mRestServerAddress.isNullOrEmpty()) {
                toast(R.string.em_server_set_rest_server_empty_hint)
                return
            }
        }
        // 保存设置
        if (!TextUtils.isEmpty(mAppkey)) {
            SpDbModel.instance.enableCustomAppKey(binding.switchServer.isChecked)
            SpDbModel.instance.setCustomAppKey(mAppkey)
        }
        if (!TextUtils.isEmpty(mServerAddress)) {
            SpDbModel.instance.setIMServer(mServerAddress)
        }
        if (!TextUtils.isEmpty(mServerPort)) {
            SpDbModel.instance.setIMServer(mServerPort)
        }
        if (!TextUtils.isEmpty(mRestServerAddress)) {
            SpDbModel.instance.setRestServer(mRestServerAddress)
        }
        SpDbModel.instance.enableCustomServer(mCustomServerEnable)
        SpDbModel.instance.setUsingHttpsOnly(binding.switchHttpsSet.isChecked)

        //保存成功后，回退到生一个页面
        onBackPress()
    }

    private fun setButtonEnable(enable: Boolean) {
        Log.e("TAG", "setButtonEnable = $enable")
        binding.btnServer.isEnabled = enable
        //同时需要修改右侧drawalbeRight对应的资源
//        Drawable rightDrawable;
//        if(enable) {
//            rightDrawable = ContextCompat.getDrawable(mContext, R.drawable.demo_login_btn_right_enable);
//        }else {
//            rightDrawable = ContextCompat.getDrawable(mContext, R.drawable.demo_login_btn_right_unable);
//        }
//         binding.btnServer.setCompoundDrawablesWithIntrinsicBounds(null, null, rightDrawable, null);
    }
}