package com.hyphenate.easeim.common.db.entity

import androidx.room.Entity
import java.io.Serializable

@Entity(tableName = "app_key", primaryKeys = ["appKey"])
class AppKeyEntity(var appKey: String) : Serializable {
    var timestamp: Double = System.currentTimeMillis().toDouble()

}