package com.avf.vending.local.database.converter

import androidx.room.TypeConverter

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Long? = value

    @TypeConverter
    fun toTimestamp(value: Long?): Long? = value
}
