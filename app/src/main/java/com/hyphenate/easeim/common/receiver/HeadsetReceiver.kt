package com.hyphenate.easeim.common.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hyphenate.easeim.R
import com.hyphenate.util.EMLog
import android.widget.Toast

/**
 * Created by lzan13 on 2018/4/2.
 * 耳机插入拔出监听广播接收类
 */
class HeadsetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 耳机插入状态 0 拔出，1 插入
        val state = intent.getIntExtra("state", 0) != 0
        // 耳机类型
        val name = intent.getStringExtra("name")
        // 耳机是否带有麦克风 0 没有，1 有
        val mic = intent.getIntExtra("microphone", 0) != 0
        val headsetChange = String.format(context.getString(R.string.headset_insert), state, mic)
        EMLog.d("HeadsetReceiver", headsetChange)
        Toast.makeText(context, headsetChange, Toast.LENGTH_SHORT).show()
    }
}