package io.lumstudio.yohub.common.utils

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

val LocalPreferences = compositionLocalOf<PreferencesStore> { error("Not provided.") }

enum class PreferencesName {
    DARK_MODEL
}

class PreferencesStore(
    fileDir: File,
    val preference: SnapshotStateMap<String, String?>
) {
    /**
     * 加载数据
     */
    suspend fun loadPreference() = withContext(Dispatchers.IO) {
        val bytes = try {
            readBytes(FileInputStream(preferenceFile))
        } catch (e: Exception) {
            e.printStackTrace()
            byteArrayOf()
        }
        var config = String(bytes)
        if (config.isBlank()) {
            config = "{}"
        }
        preference.clear()
        preference.putAll(gson.fromJson(config, object : TypeToken<Map<String, String?>>() {}.type))
    }

    /**
     * 持久化数据
     */
    suspend fun submit() = withContext(Dispatchers.IO) {
        val byteArray = gson.toJson(preference).toByteArray()
        writeBytes(FileOutputStream(preferenceFile), byteArray)
    }

    private val path: File by lazy {
        val file = File(fileDir, "shared")
        if (!file.exists()) {
            file.mkdirs()
        }
        file
    }

    private val preferenceFile: File by lazy {
        val file = File(path, "shared.json")
        if (!file.exists()) {
            try {
                file.createNewFile()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        file
    }

    private val gson = Gson()
}