package com.hyphenate.easeim.common.utils

import com.hyphenate.easeim.AppClient.Companion.instance
import com.hyphenate.easeim.R
import com.hyphenate.easeui.domain.EaseEmojicon
import com.hyphenate.easeui.domain.EaseEmojiconGroupEntity

/**
 * 表情数据类
 */
object EmoIconExampleGroupData {
    private val icons = intArrayOf(
        R.drawable.icon_002_cover,
        R.drawable.icon_007_cover,
        R.drawable.icon_010_cover,
        R.drawable.icon_012_cover,
        R.drawable.icon_013_cover,
        R.drawable.icon_018_cover,
        R.drawable.icon_019_cover,
        R.drawable.icon_020_cover,
        R.drawable.icon_021_cover,
        R.drawable.icon_022_cover,
        R.drawable.icon_024_cover,
        R.drawable.icon_027_cover,
        R.drawable.icon_029_cover,
        R.drawable.icon_030_cover,
        R.drawable.icon_035_cover,
        R.drawable.icon_040_cover
    )
    private val bigIcons = intArrayOf(
        R.drawable.icon_002,
        R.drawable.icon_007,
        R.drawable.icon_010,
        R.drawable.icon_012,
        R.drawable.icon_013,
        R.drawable.icon_018,
        R.drawable.icon_019,
        R.drawable.icon_020,
        R.drawable.icon_021,
        R.drawable.icon_022,
        R.drawable.icon_024,
        R.drawable.icon_027,
        R.drawable.icon_029,
        R.drawable.icon_030,
        R.drawable.icon_035,
        R.drawable.icon_040
    )
    val emoData = createData()
    private fun createData(): EaseEmojiconGroupEntity {
        val emoIconGroupEntity = EaseEmojiconGroupEntity()
        val data = arrayOfNulls<EaseEmojicon>(icons.size)
        for (i in icons.indices) {
            data[i] = EaseEmojicon(icons[i], null, EaseEmojicon.Type.BIG_EXPRESSION)
            data[i]!!.bigIcon = bigIcons[i]
            //you can replace this to any you want
            data[i]!!.name =
                instance.applicationContext.getString(R.string.emojicon_test_name) + (i + 1)
            data[i]!!.identityCode = "em" + (1000 + i + 1)
        }
        emoIconGroupEntity.emojiconList = listOf(*data)
        emoIconGroupEntity.icon = R.drawable.icon_002_cover
        emoIconGroupEntity.type = EaseEmojicon.Type.BIG_EXPRESSION
        return emoIconGroupEntity
    }
}