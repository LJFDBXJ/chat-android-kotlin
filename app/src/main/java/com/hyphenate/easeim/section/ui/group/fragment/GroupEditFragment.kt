package com.hyphenate.easeim.section.ui.group.fragment

import com.hyphenate.easeui.widget.EaseTitleBar.OnRightClickListener
import com.hyphenate.easeui.widget.EaseTitleBar.OnBackPressListener
import com.hyphenate.easeim.R
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.FragmentTransaction
import com.hyphenate.easeim.databinding.FragmentGroupEditBinding
import com.hyphenate.easeui.utils.StatusBarCompat
import com.hyphenate.easeim.section.base_ktx.BaseActivityKtx
import com.hyphenate.easeim.section.base_ktx.BaseDialogKtxFragment

/**
 * @author LXJDBXJ
 * @Date 2022/9/17 明天是我生日 很遗憾没人。。。
 */
typealias  OnSaveClickListener = Function2<View?, String?, Unit>

class GroupEditFragment(override val layoutId: Int = R.layout.fragment_group_edit) :
    BaseDialogKtxFragment<FragmentGroupEditBinding>(), OnRightClickListener,
    OnBackPressListener {
    private var content: String? = null
    private var hint: String? = null
    private var listener: OnSaveClickListener? = null
    private var title: String? = null
    private var canEdit = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.AppTheme)
        StatusBarCompat.setLightStatusBar(mContext, true)
    }

    override fun onStart() {
        super.onStart()
        setDialogFullParams()
    }

    override fun initArgument() {
        super.initArgument()
        val bundle = arguments
        if (bundle != null) {
            title = bundle.getString("title")
            content = bundle.getString("content")
            hint = bundle.getString("hint")
            canEdit = bundle.getBoolean("canEdit")
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        if (TextUtils.isEmpty(content)) {
            bind.etContent.hint = hint
        } else {
            bind.etContent.setText(content)
        }
        bind.etContent.isEnabled = canEdit
        bind.titleBar.setRightLayoutVisibility(if (canEdit) View.VISIBLE else View.GONE)
        bind.titleBar.setTitle(title)
    }

    override fun initListener() {
        super.initListener()
        bind.titleBar.setOnBackPressListener(this)
        bind.titleBar.setOnRightClickListener(this)
    }

    fun setOnSaveClickListener(listener: OnSaveClickListener?) {
        this.listener = listener
    }

    override fun onRightClick(view: View) {
        val content = bind.etContent.text.toString().trim { it <= ' ' }
        listener?.invoke(view, content)
        dismiss()
    }

    override fun onBackPress(view: View) {
        dismiss()
    }

    companion object {
        fun showDialog(
            activity: BaseActivityKtx,
            title: String?,
            content: String?,
            hint: String?,
            listener: OnSaveClickListener?
        ) {
            showDialog(activity, title, content, hint, true, listener)
        }

        @JvmStatic
        fun showDialog(
            activity: BaseActivityKtx,
            title: String?,
            content: String?,
            hint: String?,
            canEdit: Boolean,
            listener: OnSaveClickListener?
        ) {
            val fragment = GroupEditFragment()
            fragment.setOnSaveClickListener(listener)
            val bundle = Bundle()
            bundle.putString("title", title)
            bundle.putString("content", content)
            bundle.putString("hint", hint)
            bundle.putBoolean("canEdit", canEdit)
            fragment.arguments = bundle
            val transaction = activity.supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            fragment.show(transaction, null)
        }
    }
}