package com.hyphenate.easeim.section.ui.me.activity

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMDeviceInfo
import com.hyphenate.easeim.R
import com.hyphenate.easeim.common.utils.SpDbModel
import com.hyphenate.easeim.databinding.ActivityMultiDeviceBinding
import com.hyphenate.easeim.section.base_ktx.BaseInitActivityKtx
import com.hyphenate.exceptions.HyphenateException

class MultiDeviceActivity(override val layoutId: Int = R.layout.activity_multi_device) :
    BaseInitActivityKtx<ActivityMultiDeviceBinding>() {
    var deviceInfos: List<EMDeviceInfo>? = null
    var username: String? = null
    var password: String? = null


    override fun initListener() {
        super.initListener()
        binding.titleBar.setOnBackPressListener { onBackPressed() }
    }

    override fun initData() {
        super.initData()
        registerForContextMenu(binding.list)
        binding.list.adapter = MultiDeviceAdapter(this, 0, ArrayList())
        val model = SpDbModel.instance
        if (TextUtils.isEmpty(model.currentUserPwd)) {
            startActivityForResult(
                Intent(this, NamePasswordActivity::class.java),
                REQUEST_CODE_USERNAME_PASSWORD
            )
        } else {
            username = model.currentLoginUser
            password = model.currentUserPwd
            updateList(username, password)
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu, v: View,
        menuInfo: ContextMenuInfo
    ) {
        val inflater = menuInflater
        //menu.setHeaderTitle("Multi-device context menu");
        inflater.inflate(R.menu.demo_multi_device_menu_item, menu)
        super.onCreateContextMenu(menu, v, menuInfo)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val menuInfo = item.menuInfo as AdapterContextMenuInfo
        if (deviceInfos != null && menuInfo.position < deviceInfos!!.size) {
            val deviceInfo = deviceInfos!![menuInfo.position]
            Thread {
                try {
                    EMClient.getInstance().kickDevice(username, password, deviceInfo.resource)
                    updateList(username, password)
                } catch (e: HyphenateException) {
                    e.printStackTrace()
                }
            }.start()
        }
        return super.onContextItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_CANCELED) {
            finish()
            return
        }
        if (resultCode == RESULT_OK) {
            username = data!!.getStringExtra("username")
            password = data.getStringExtra("password")
            updateList(username, password)
        }
    }

    fun updateList(username: String?, password: String?) {
        Thread {
            try {
                deviceInfos =
                    EMClient.getInstance().getLoggedInDevicesFromServer(username, password)
                runOnUiThread {
                    binding.list.adapter =
                        MultiDeviceAdapter(this@MultiDeviceActivity, 0, deviceInfos)
                }
            } catch (e: HyphenateException) {
                e.printStackTrace()
                runOnUiThread { R.string.get_logged_failed }
            }
        }.start()
    }

    private inner class MultiDeviceAdapter(
        context: Context?,
        res: Int,
        deviceInfos: List<EMDeviceInfo>?
    ) : ArrayAdapter<EMDeviceInfo?>(
        context!!, res, deviceInfos!!
    ) {
        private val inflater: LayoutInflater
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.multi_dev_item, parent, false)
            }
            (convertView!!.findViewById<View>(R.id.multi_device_name) as TextView).text =
                getItem(position)!!.deviceName
            val icon = convertView.findViewById<ImageView>(R.id.iv_device_icon)
            val deviceTypeIcon = getDeviceTypeIcon(getItem(position)!!.resource)
            if (deviceTypeIcon != 0) {
                icon.setImageResource(deviceTypeIcon)
            }
            convertView.tag = getItem(position)!!.deviceName
            return convertView
        }

        init {
            inflater = LayoutInflater.from(context)
        }
    }

    private fun getDeviceTypeIcon(deviceResource: String): Int {
        if (TextUtils.isEmpty(deviceResource) || !deviceResource.contains("_")) {
            return 0
        }
        val deviceType = deviceResource.substring(0, deviceResource.indexOf("_"))
        if (deviceType.equals("ios", ignoreCase = true)) {
            return R.drawable.device_ios
        } else if (deviceType.equals("android", ignoreCase = true)) {
            return R.drawable.device_android
        } else if (deviceType.equals("web", ignoreCase = true)) {
            return R.drawable.device_web
        } else if (deviceType.equals("win", ignoreCase = true)) {
            return R.drawable.device_win
        } else if (deviceType.equals("iMac", ignoreCase = true)) {
            return R.drawable.device_imac
        }
        return 0
    }

    companion object {
        private const val REQUEST_CODE_USERNAME_PASSWORD = 0

        @JvmStatic
        fun actionStart(context: Context) {
            val intent = Intent(context, MultiDeviceActivity::class.java)
            context.startActivity(intent)
        }
    }
}