package com.easycampus.data.local.database

import androidx.room.TypeConverter
import java.time.DayOfWeek
import java.time.LocalTime

class Converters {
    @TypeConverter
    fun fromDayOfWeek(dayOfWeek: DayOfWeek): Int = dayOfWeek.value

    @TypeConverter
    fun toDayOfWeek(value: Int): DayOfWeek = DayOfWeek.of(value)

    @TypeConverter
    fun fromLocalTime(time: LocalTime): String = time.toString()

    @TypeConverter
    fun toLocalTime(value: String): LocalTime = LocalTime.parse(value)
}
