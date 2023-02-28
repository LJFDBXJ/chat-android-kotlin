package com.hyphenate.easeim.common.repositories

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import com.hyphenate.easeim.common.interfaceOrImplement.ResultCallBack
import com.hyphenate.easeim.common.net.Resource
import com.hyphenate.easeui.manager.EaseThreadManager
import com.hyphenate.easeui.model.VideoEntity
import com.hyphenate.easeui.utils.EaseCompat
import com.hyphenate.util.PathUtil
import com.hyphenate.util.VersionUtils
import java.io.File
import java.io.IOException
import java.util.*

class MediaManagerRepository : BaseEMRepository() {
    /**
     * 从多媒体库和私有目录下的视频存放文件夹中获取视频文件
     * @param context
     * @return
     */
    fun getVideoListFromMediaAndSelfFolder(context: Context,callBack: ResultCallBack<List<VideoEntity>>) {
        EaseThreadManager.getInstance().runOnIOThread {
            val mList: MutableList<VideoEntity> = ArrayList()
            val mContentResolver = context.contentResolver
            val cursor = mContentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null, null
            )
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // ID:MediaStore.Audio.Media._ID
                    val id = cursor.getInt(
                        cursor
                            .getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                    )

                    // title：MediaStore.Audio.Media.TITLE
                    val title = cursor.getString(
                        cursor
                            .getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
                    )
                    // path：MediaStore.Audio.Media.DATA
                    var url: String? = null
                    if (!VersionUtils.isTargetQ(context)) {
                        url = cursor.getString(
                            cursor
                                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                        )
                    }

                    // duration：MediaStore.Audio.Media.DURATION
                    val duration = cursor
                        .getInt(
                            cursor
                                .getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                        )

                    // 大小：MediaStore.Audio.Media.SIZE
                    val size = cursor.getLong(
                        cursor
                            .getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                    ).toInt()

                    // 最近一次修改时间：MediaStore.Audio.DATE_MODIFIED
                    val lastModified = cursor.getLong(
                        cursor
                            .getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN)
                    )
                    if (size <= 0) {
                        continue
                    }
                    val uri =
                        Uri.parse(MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString() + File.separator + id)
                    val entty = VideoEntity()
                    entty.ID = id
                    entty.title = title
                    entty.filePath = url
                    entty.duration = duration
                    entty.size = size
                    entty.uri = uri
                    entty.lastModified = lastModified
                    mList.add(entty)
                } while (cursor.moveToNext())
            }
            cursor?.close()
            getSelfVideoFiles(context, mList)
            if (mList.isNotEmpty()) {
                sortVideoEntities(mList)
            }
            callBack.onSuccess(mList)
        }

    }

    private fun getSelfVideoFiles(context: Context, mList: MutableList<VideoEntity>) {
        val videoFolder = PathUtil.getInstance().videoPath
        if (videoFolder.exists() && videoFolder.isDirectory) {
            val files = videoFolder.listFiles()
            if (!files.isNullOrEmpty()) {
                var entty: VideoEntity
                for (i in files.indices) {
                    entty = VideoEntity()
                    val file = files[i]
                    if (!EaseCompat.isVideoFile(context, file.name) || file.length() <= 0) {
                        continue
                    }
                    entty.filePath = file.absolutePath
                    entty.size = file.length().toInt()
                    entty.title = file.name
                    entty.lastModified = file.lastModified()
                    val player = MediaPlayer()
                    try {
                        player.setDataSource(file.path)
                        player.prepare()
                        entty.duration = player.duration
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    if (entty.size <= 0 || entty.duration <= 0) {
                        continue
                    }
                    mList.add(entty)
                }
            }
        }
    }

    private fun sortVideoEntities(mList: List<VideoEntity>) {
        Collections.sort(mList, Comparator { o1, o2 ->
            if (o1 == null && o2 == null) {
                return@Comparator 0
            }
            if (o1 == null) {
                return@Comparator 1
            }
            if (o2 == null) {
                return@Comparator -1
            }
            val result = o2.lastModified - o1.lastModified
            if (result == 0L) 0 else if (result > 0) 1 else -1
        })
    }
}