package com.hyphenate.easeim.section.base_ktx

import android.os.Bundle
import android.graphics.drawable.ColorDrawable
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import java.lang.Exception

/**
 * 作为dialog fragment的基类
 */
abstract class BaseDialogKtxFragment<D : ViewDataBinding> : DialogFragment() {
    var mContext: BaseActivityKtx? = null
    lateinit var bind: D
    abstract val layoutId: Int

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context as BaseActivityKtx
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initArgument()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bind = DataBindingUtil.inflate(inflater, layoutId, container, false)
        setChildView()
        setDialogAttrs()
        return bind.root
    }

   open fun setChildView() {}
    private fun setDialogAttrs() {
        try {
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(savedInstanceState)
        initListener()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initData()
    }

    open fun initArgument() {}
    open fun initView(savedInstanceState: Bundle?) {}
    open fun initListener() {}
    open fun initData() {}

    /**
     * dialog宽度占满，高度自定义
     */
    fun setDialogParams() {
        try {
            val dialogWindow = dialog!!.window
            val lp = dialogWindow!!.attributes
            lp.dimAmount = 0.6f
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
            lp.gravity = Gravity.BOTTOM
            setDialogParams(lp)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * dialog全屏
     */
    fun setDialogFullParams() {
        val dialogHeight = getContextRect(mContext)
        val height = if (dialogHeight == 0) ViewGroup.LayoutParams.MATCH_PARENT else dialogHeight
        setDialogParams(ViewGroup.LayoutParams.MATCH_PARENT, height, 0.0f)
    }

    fun setDialogParams(width: Int, height: Int, dimAmount: Float) {
        try {
            val dialogWindow = dialog!!.window
            val lp = dialogWindow!!.attributes
            lp.dimAmount = dimAmount
            lp.width = width
            lp.height = height
            dialogWindow.attributes = lp
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setDialogParams(layoutParams: WindowManager.LayoutParams?) {
        try {
            val dialogWindow = dialog?.window
            dialogWindow?.attributes = layoutParams
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //获取内容区域
    private fun getContextRect(activity: Activity?): Int {
        //应用区域
        val outRect1 = Rect()
        activity?.window?.decorView?.getWindowVisibleDisplayFrame(outRect1)
        return outRect1.height()
    }
}