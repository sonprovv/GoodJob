package com.project.job.data.source.local.room.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ListStringConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromString(value: String?): List<String> {
        if (value == null || value.isEmpty()) {
            return emptyList()
        }
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun toString(list: List<String>?): String {
        return gson.toJson(list)
    }
}
