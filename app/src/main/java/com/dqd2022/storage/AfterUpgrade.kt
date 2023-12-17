package com.dqd2022.storage

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.dqd2022.helpers.App
import com.dqd2022.model.userMessageTable
import kit.LogKit

class AfterUpgrade {
    constructor(db: SQLiteDatabase) {
        // roommember 表建立联合唯一索引
        val sql = "CREATE UNIQUE INDEX idx_unique ON $roomMemberTable(`room_id`, `user_id`)"
        db.execSQL(sql)
    }


}