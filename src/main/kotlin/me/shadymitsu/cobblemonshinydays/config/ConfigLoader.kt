package me.shadymitsu.cobblemonshinydays.config

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader


object ConfigLoader {

    private val configFile = File("config/cobblemonshinydays/config.json")

    fun loadConfig(): List<ConfigFields> {
        if (!configFile.exists()) {
            ConfigCreator.createDefaultConfig()
            return ConfigCreator.getDefaultConfig()
        }

        val gson = GsonBuilder().setPrettyPrinting().create()
        val reader = FileReader(configFile)
        val type = object : TypeToken<List<ConfigFields>>() {}.type
        return gson.fromJson(reader, type)
    }
}
