package com.hyphenate.easeim.section.ui.group.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import com.hyphenate.chat.EMMucSharedFile
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.constant.DemoConstant
import com.hyphenate.easeim.common.livedatas.obs
import com.hyphenate.easeim.databinding.ActivityChatGroupSharedFilesBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.easeim.section.ui.group.adapter.SharedFilesAdapter
import com.hyphenate.easeim.section.ui.group.vm.SharedFilesVm
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.easeui.utils.EaseCompat
import com.hyphenate.easeui.widget.EaseRecyclerView.RecyclerViewContextMenuInfo
import com.hyphenate.easeui.widget.EaseTitleBar.OnBackPressListener
import com.hyphenate.easeui.widget.EaseTitleBar.OnRightClickListener
import com.hyphenate.util.VersionUtils
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import java.io.File

class GroupSharedFilesActivity(override val layoutId: Int = R.layout.activity_chat_group_shared_files) :
    BaseInitActivityKtx<ActivityChatGroupSharedFilesBinding>(), OnRefreshListener,
    OnRefreshLoadMoreListener, OnRightClickListener, OnBackPressListener {
    private val adapter by lazy { SharedFilesAdapter() }
    private val viewModel by viewModels<SharedFilesVm>()
    private var pageSize = 0
    private var groupId: String? = null


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        groupId = intent.getStringExtra("groupId")
        binding.rvList.adapter = adapter
        binding.rvList.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        registerForContextMenu(binding.rvList)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.demo_group_shared_files_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as RecyclerViewContextMenuInfo
        val position = info.position
        when (item.itemId) {
            R.id.action_shared_delete -> deleteFile(adapter.getItem(position))
        }
        return super.onContextItemSelected(item)
    }

    override fun initListener() {
        super.initListener()
        binding.srlRefresh.setOnRefreshLoadMoreListener(this)
        adapter.setOnItemClickListener { _, _, position ->
            showFile(adapter.getItem(position))
        }
        binding.titleBar.setOnBackPressListener(this)
        binding.titleBar.setOnRightClickListener(this)
    }

    override fun initData() {
        super.initData()
        viewModel.filesObservable.observe(this) {
            adapter.setList(it)
            finishRefresh()
            finishLoadMore()
        }
        viewModel.showFileObservable.observe(this) {
            openFile(it)

        }
        viewModel.refreshFiles.observe(this) {
            if (it) {
                refresh()
            }
            dismissLoading()
        }
        DemoConstant.GROUP_SHARE_FILE_CHANGE.obs<EaseEvent>(this) { event ->
            if (event == null) {
                return@obs
            }
            if (event.event == DemoConstant.GROUP_SHARE_FILE_CHANGE) {
                refresh()
            }
        }

        refresh()
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        refresh()
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        loadMore()
    }

    private fun refresh() {
        pageSize = LIMIT
        viewModel.getSharedFiles(groupId, 0, pageSize)
    }

    private fun loadMore() {
        pageSize += LIMIT
        viewModel.getSharedFiles(groupId, 0, pageSize)
    }

    private fun deleteFile(file: EMMucSharedFile) {
        showLoading()
        viewModel.deleteFile(groupId, file)
    }

    private fun showFile(item: EMMucSharedFile) {
        viewModel.showFile(groupId, item)
    }

    private fun openFile(file: File?) {
        if (file != null && file.exists()) {
            EaseCompat.openFile(file, this)
        }
    }

    private fun finishRefresh() {
        runOnUiThread { binding.srlRefresh.finishRefresh() }
    }

    private fun finishLoadMore() {
        runOnUiThread { binding.srlRefresh.finishLoadMore() }
    }

    override fun onRightClick(view: View) {
        selectFileFromLocal()
    }

    private fun selectFileFromLocal() {
        EaseCompat.openImage(this, REQUEST_CODE_SELECT_FILE)
    }

    override fun onBackPress(view: View) {
        onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SELECT_FILE) {
            if (data != null) {
                val uri = data.data
                if (uri != null) {
                    if (VersionUtils.isTargetQ(this)) {
                        showLoading()
                        viewModel.uploadFileByUri(this, groupId, uri)
                    } else {
                        sendByPath(uri)
                    }
                }
            }
        }
    }

    private fun sendByPath(uri: Uri) {
        val path = EaseCompat.getPath(this, uri)
        if (TextUtils.isEmpty(path)) {
            Toast.makeText(this, R.string.File_does_not_exist, Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.uploadFileByUri(this, groupId, Uri.parse(path))
    }

    companion object {
        private const val REQUEST_CODE_SELECT_FILE = 1
        private const val LIMIT = 20
        fun actionStart(context: Context, groupId: String?) {
            val intent = Intent(context, GroupSharedFilesActivity::class.java)
            intent.putExtra("groupId", groupId)
            context.startActivity(intent)
        }
    }
}