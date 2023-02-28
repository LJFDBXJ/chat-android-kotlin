package com.hyphenate.easeim.section.ui.me.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.hyphenate.chat.EMClient
import com.hyphenate.easeim.BR
import com.hyphenate.easeim.SdkHelper
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.db.entity.AppKeyEntity
import com.hyphenate.easeim.common.interfaceOrImplement.OnResourceParseCallback
import com.hyphenate.easeim.common.manager.OptionsHelper
import com.hyphenate.easeim.common.utils.SpDbModel
import com.hyphenate.easeim.databinding.ActivityAppkeyManageBinding
import com.hyphenate.easeim.databinding.ItemAppkeyManageBinding
import com.hyphenate.easeim.section.base_ktx.BaseBindAdapter
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.dialog_ktx.SimpleDialogFragment
import com.hyphenate.easeim.section.ui.me.vm.AppKeyManagerVm
import com.hyphenate.easeui.widget.EaseTitleBar.OnBackPressListener
import com.hyphenate.easeui.widget.EaseTitleBar.OnRightClickListener
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshListener

class AppKeyManageActivity(override val layoutId: Int = R.layout.activity_appkey_manage) :
    BaseInitActivityKtx<ActivityAppkeyManageBinding>(),
    OnBackPressListener, OnRefreshListener, OnRightClickListener {
    private val adapter by lazy { RvAdapter() }
    private var selectedPosition = 0
    private val viewModel by viewModels<AppKeyManagerVm>()


    override fun initListener() {
        super.initListener()
        binding.titleBar.setOnBackPressListener(this)
        binding.srlRefresh.setOnRefreshListener(this)
        binding.titleBar.setOnRightClickListener(this)
    }

    override fun initData() {
        super.initData()
        viewModel.logoutObservable.observe(this) { response ->
            parseResource(
                response = response,
                callback = object : OnResourceParseCallback<Boolean>() {
                    override fun onSuccess(data: Boolean?) {
                        finish()
                        SdkHelper.instance.killApp()
                    }
                })
        }
        binding.rvList.layoutManager = LinearLayoutManager(this)
        binding.rvList.adapter = adapter
        val bottom = LayoutInflater.from(this).inflate(R.layout.appkey_add_layout, null)
        binding.rvList.addFooterView(bottom)
        bottom.setOnClickListener { v: View? ->
            AppKeyAddActivity.actionStartForResult(this, 100)
        }
        data
    }

    private fun showConfirmDialog(itemView: View, position: Int) {
        SimpleDialogFragment.Builder(this)
            .setTitle(R.string.em_developer_appkey_warning)
            .setTitleSize(14f)
            .setOnConfirmClickListener(
                R.string.em_developer_appkey_confirm
            ) {
                selectedPosition = position
                adapter.notifyDataSetChanged()
                val appKey = adapter.getItem(position).appKey
                SpDbModel.instance.enableCustomAppKey(!TextUtils.isEmpty(appKey))
                SpDbModel.instance.setCustomAppKey(appKey)
                viewModel.logout(true)
            }
            .setOnCancelClickListener {
                val radio = itemView.findViewById<RadioButton>(R.id.iv_arrow)
                val checked = radio.isChecked
                radio.isChecked = !checked
            }
            .showCancelButton(true)
            .show()
    }

    private fun showSetDefaultDialog() {
        SimpleDialogFragment.Builder(this)
            .setTitle(R.string.em_developer_appkey_warning)
            .setTitleSize(14f)
            .setOnConfirmClickListener(
                R.string.em_developer_appkey_confirm
            ) {
                SpDbModel.instance.enableCustomAppKey(false)
                viewModel.logout(true)
            }
            .showCancelButton(true)
            .show()
    }

    private fun showDeleteDialog(position: Int) {
        AlertDialog.Builder(this)
            .setMessage(R.string.em_developer_appkey_delete)
            .setPositiveButton(getString(R.string.confirm)) { dialog, which ->
                SpDbModel.instance.deleteAppKey(adapter.data[position].appKey)
                data
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private val data: Unit
        get() {
            val appKeys = SpDbModel.instance.appKeys
            var appkey: String? = ""
            selectedPosition = -1
            if (SpDbModel.instance.isCustomAppKeyEnabled()) {
                appkey = SpDbModel.instance.getCustomAppkey()
            }
            if (!appKeys.isNullOrEmpty()) {
                for (i in appKeys.indices) {
                    val entity = appKeys[i]
                    if (TextUtils.equals(entity.appKey, appkey)) {
                        selectedPosition = i
                    }
                }
            }
            adapter.setList(appKeys)
            binding.srlRefresh.finishRefresh()
        }

    override fun onBackPress(view: View) {
        onBackPressed()
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        data
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            data
        }
    }

    override fun onRightClick(view: View) {
        showSetDefaultDialog()
    }

    private inner class RvAdapter :
        BaseBindAdapter<AppKeyEntity, ItemAppkeyManageBinding>(
            layoutResId = R.layout.item_appkey_manage,
            BR.entity
        ) {

        init {
            setOnItemLongClickListener { _, _, position ->
                val item = getItem(position)
                val appKey = item.appKey
                if (!TextUtils.equals(appKey, OptionsHelper.get().defAppKey)
                    && !TextUtils.equals(appKey, EMClient.getInstance().options.appKey)
                ) {
                    showDeleteDialog(position)
                }
                return@setOnItemLongClickListener true
            }
            setOnItemClickListener { adapter, view, position ->
                DataBindingUtil.getBinding<ItemAppkeyManageBinding>(view)?.let { bind ->
                    bind.ivArrow.isChecked = !bind.ivArrow.isChecked
                    showConfirmDialog(view, position)
                }
            }
        }

        override fun convert(
            holder: BaseDataBindingHolder<ItemAppkeyManageBinding>,
            item: AppKeyEntity
        ) {
            super.convert(holder, item)
            holder.dataBinding?.apply {
                checked = selectedPosition == holder.absoluteAdapterPosition
            }
        }

    }

    companion object {
        @JvmStatic
        fun actionStartForResult(activity: Activity, requestCode: Int) {
            val starter = Intent(activity, AppKeyManageActivity::class.java)
            activity.startActivityForResult(starter, requestCode)
        }
    }
}