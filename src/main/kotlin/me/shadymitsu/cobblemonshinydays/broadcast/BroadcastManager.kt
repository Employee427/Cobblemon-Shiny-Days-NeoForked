package me.shadymitsu.cobblemonshinydays.broadcast

import com.cobblemon.mod.common.util.server
import me.shadymitsu.cobblemonshinydays.config.ConfigLoader
import net.minecraft.network.chat.Component
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object BroadcastManager {

    private val scheduler = Executors.newSingleThreadScheduledExecutor()

    // Map of label keys to nicely formatted names
    private val labelDisplayNames = mapOf(
        "legendary" to "Legendary",
        "restricted" to "Restricted",
        "mythical" to "Mythical",
        "ultra_beast" to "Ultra Beast",
        "fossil" to "Fossil",
        "powerhouse" to "Powerhouse",
        "baby" to "Baby",
        "regional" to "Regional",
        "kantonian_form" to "Kantonian Form",
        "johtonian_form" to "Johtonian Form",
        "hoennian_form" to "Hoennian Form",
        "sinnohan_form" to "Sinnohan Form",
        "unovan_form" to "Unovan Form",
        "kalosian_form" to "Kalosian Form",
        "alolan_form" to "Alolan Form",
        "galarian_form" to "Galarian Form",
        "hisuian_form" to "Hisuian Form",
        "paldean_form" to "Paldean Form",
        "mega" to "Mega",
        "primal" to "Primal",
        "gmax" to "G-Max",
        "totem" to "Totem",
        "paradox" to "Paradox",
        "gen1" to "Gen 1",
        "gen2" to "Gen 2",
        "gen3" to "Gen 3",
        "gen4" to "Gen 4",
        "gen5" to "Gen 5",
        "gen6" to "Gen 6",
        "gen7" to "Gen 7",
        "gen7b" to "Gen 7b",
        "gen8" to "Gen 8",
        "gen8a" to "Gen 8a",
        "gen9" to "Gen 9",
        "customized_official" to "Customized Official",
        "custom" to "Custom"
    )

    // Display names for elemental types
    private val typeDisplayNames = mapOf(
        "normal" to "Normal",
        "fire" to "Fire",
        "water" to "Water",
        "electric" to "Electric",
        "grass" to "Grass",
        "ice" to "Ice",
        "fighting" to "Fighting",
        "poison" to "Poison",
        "ground" to "Ground",
        "flying" to "Flying",
        "psychic" to "Psychic",
        "bug" to "Bug",
        "rock" to "Rock",
        "ghost" to "Ghost",
        "dragon" to "Dragon",
        "dark" to "Dark",
        "steel" to "Steel",
        "fairy" to "Fairy"
    )

    fun generateBroadcastMessage(): String? {
        val now = LocalDateTime.now()
        val currentDay = now.dayOfWeek.name

        val config = ConfigLoader.loadConfig()

        val activeEntries = config.filter { entry ->
            entry.days.any { it.equals(currentDay, ignoreCase = true) }
        }

        val speciesPart = activeEntries.flatMap { it.species }
            .filterNot { it.equals("ALL", ignoreCase = true) }
            .distinct()
            .joinToString(", ") { "§d$it" }

        val labelsPart = activeEntries.flatMap { it.labels }
            .mapNotNull { labelDisplayNames[it.lowercase()] }
            .distinct()
            .joinToString(", ") { "§b$it" }

        val typesPart = activeEntries.flatMap { it.types ?: listOf() }
            .mapNotNull { typeDisplayNames[it.lowercase()] }
            .distinct()
            .joinToString(", ") { "§a$it-type" }

        val fullList = listOfNotNull(
            speciesPart.takeIf { it.isNotEmpty() },
            labelsPart.takeIf { it.isNotEmpty() },
            typesPart.takeIf { it.isNotEmpty() }
        )

        val combined = when (fullList.size) {
            0 -> null
            1 -> fullList.first()
            else -> {
                val parts = fullList.flatMap { it.split(", ") }
                when (parts.size) {
                    1 -> parts[0]
                    2 -> "${parts[0]} §7or ${parts[1]}"
                    else -> parts.dropLast(1).joinToString("§7, ") + " §7or ${parts.last()}"
                }
            }
        }

        return combined?.let {
            "§eToday is a §6Shiny Day! §eYou may encounter a shiny $it §ePokémon!"
        }
    }

    fun startBroadcasting() {
        val config = ConfigLoader.loadConfig()

        val groupedByInterval = config
            .filter { it.broadcastInterval != null }
            .groupBy { it.broadcastInterval }

        for ((intervalSeconds, entries) in groupedByInterval) {
            scheduler.scheduleAtFixedRate({
                val now = LocalDateTime.now()
                val currentDay = now.dayOfWeek.name

                entries.forEach { entry ->
                    val isActiveDay = entry.days.any { it.equals(currentDay, ignoreCase = true) }

                    if (isActiveDay) {
                        val speciesPart = entry.species
                            .filterNot { it.equals("ALL", ignoreCase = true) }
                            .joinToString(", ") { "§d$it" }

                        val labelsPart = entry.labels
                            .mapNotNull { labelDisplayNames[it.lowercase()] }
                            .joinToString(", ") { "§b$it" }

                        val typesPart = entry.types
                            ?.mapNotNull { typeDisplayNames[it.lowercase()] }
                            ?.joinToString(", ") { "§a$it-type" }

                        val fullList = listOfNotNull(
                            speciesPart.takeIf { it.isNotEmpty() },
                            labelsPart.takeIf { it.isNotEmpty() },
                            typesPart.takeIf { !it.isNullOrBlank() }
                        )

                        val combined = when (fullList.size) {
                            0 -> null
                            1 -> fullList.first()
                            else -> {
                                val parts = fullList.flatMap { it.split(", ") }
                                when (parts.size) {
                                    1 -> parts[0]
                                    2 -> "${parts[0]} §7or ${parts[1]}"
                                    else -> parts.dropLast(1).joinToString("§7, ") + " §7or ${parts.last()}"
                                }
                            }
                        }

                        if (combined != null) {
                            val message = "§eToday is a §6Shiny Day! §eIf you're lucky you may encounter a shiny $combined §ePokémon!"
                            broadcastToServer(message)
                        }
                    }
                }
            }, 0, intervalSeconds!!.toLong(), TimeUnit.SECONDS)
        }
    }

    private fun broadcastToServer(message: String) {
        val component = Component.literal(message)
        server()?.playerList?.players?.forEach { player ->
            player.sendSystemMessage(component)
        }
    }

    fun shutdown() {
        scheduler.shutdownNow()
        println("Cobblemon Shiny Days: Broadcast scheduler shut down successfully.")
    }
}
