package com.hyphenate.easeim.section.base_ktx

import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class BaseInitFragmentKtx<D : ViewDataBinding> : BaseFragmentKtx() {
    lateinit var binding: D
    private var rootView: View? = null
    @get:LayoutRes
    abstract val layoutId: Int
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initArgument()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
            binding.lifecycleOwner = this
            rootView = binding.root
            initView(savedInstanceState)
            initViewModel()
            initListener()
            initData()
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }




    /**
     * 获取传递参数
     */
    open fun initArgument() {}

    /**
     * 初始化布局相关
     * @param savedInstanceState
     */
    open fun initView(savedInstanceState: Bundle?) {
        Log.e("TAG", "fragment = " + this.javaClass.simpleName)
    }

    /**
     * 初始化ViewModel相关
     */
    open  fun initViewModel() {}

    /**
     * 初始化监听等
     */
    open fun initListener() {}

    /**
     * 初始化数据相关
     */
    open fun initData() {}
}