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

class AnyTypeConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromString(value: String?): Any? {
        if (value == null || value.isEmpty()) {
            return null
        }
        return gson.fromJson(value, Any::class.java)
    }

    @TypeConverter
    fun toString(value: Any?): String {
        return gson.toJson(value)
    }
}
