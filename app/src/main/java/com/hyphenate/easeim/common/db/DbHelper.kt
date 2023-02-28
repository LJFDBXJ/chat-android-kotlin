package com.hyphenate.easeim.common.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import com.hyphenate.easeim.AppClient
import com.hyphenate.easeim.common.db.dao.AppKeyDao
import com.hyphenate.easeim.common.db.dao.EmUserDao
import com.hyphenate.easeim.common.db.dao.InviteMessageDao
import com.hyphenate.easeim.common.db.dao.MsgTypeManageDao
import com.hyphenate.easeim.common.utils.MD5
import com.hyphenate.util.EMLog

class DbHelper {
    private var currentUser: String? = null
    private var mDatabase: AppDatabase? = null
    val mIsDatabaseCreated: LiveData<Boolean> get() = _mIsDatabaseCreated
    private val _mIsDatabaseCreated = MutableLiveData<Boolean>()

    /**
     * 初始化数据库
     * @param user
     */
    fun initDb(user: String) {
        if (currentUser != null) {
            if (currentUser == user) {
                EMLog.i(TAG, "you have opened the db")
                return
            }
            closeDb()
        }
        currentUser = user
        val userMd5 = MD5.encrypt2MD5(user)
        // 以下数据库升级设置，为升级数据库将清掉之前的数据，如果要保留数据，慎重采用此种方式
        // 可以采用addMigrations()的方式，进行数据库的升级
        val dbName = String.format("em_%1\$s.db", userMd5)
        EMLog.i(TAG, "db name = $dbName")
        mDatabase = Room.databaseBuilder(AppClient.instance, AppDatabase::class.java, dbName)
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
        _mIsDatabaseCreated.postValue(true)
    }


    /**
     * 关闭数据库
     */
    fun closeDb() {
        mDatabase?.close()
        mDatabase = null
        currentUser = null
    }

    val userDao: EmUserDao?
        get() {
            EMLog.i(TAG, "get userDao failed, should init db first")
            return mDatabase?.userDao()
        }
    val inviteMessageDao: InviteMessageDao?
        get() {
            EMLog.i(TAG, "get inviteMessageDao failed, should init db first")
            return mDatabase?.inviteMessageDao()
        }
    val msgTypeManageDao: MsgTypeManageDao?
        get() {
            EMLog.i(TAG, "get msgTypeManageDao failed, should init db first")
            return mDatabase?.msgTypeManageDao()
        }
    val appKeyDao: AppKeyDao?
        get() {
            EMLog.i(TAG, "get appKeyDao failed, should init db first")
            return mDatabase?.appKeyDao()
        }

    companion object {
        private const val TAG = "DemoDbHelper"
        private var instance: DbHelper? = null

        @JvmStatic
        fun dbHelper(): DbHelper {
            if (instance == null) {
                synchronized(DbHelper::class.java) {
                    if (instance == null) {
                        instance = DbHelper()
                    }
                }
            }
            return instance!!
        }
    }

}