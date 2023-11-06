package io.lumstudio.yohub.common.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date


class DateDeserializer : JsonDeserializer<Date?> {
    private val sf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")

    private val sf1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    @Throws(JsonParseException::class)
    override fun deserialize(je: JsonElement, type: Type, jdc: JsonDeserializationContext): Date? {
        val myDate = je.asString
        if (myDate.isNotEmpty()) {
            try {
                return sf.parse(myDate)
            } catch (e: ParseException) {
                e.printStackTrace()
                try {
                    return Date(myDate.toLong())
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    try {
                        return sf1.parse(myDate)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return null
    }
}