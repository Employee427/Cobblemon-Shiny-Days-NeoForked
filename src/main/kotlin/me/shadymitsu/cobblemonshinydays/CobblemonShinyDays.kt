package me.shadymitsu.cobblemonshinydays

import com.cobblemon.mod.common.api.events.pokemon.ShinyChanceCalculationEvent
import com.cobblemon.mod.common.api.events.CobblemonEvents
import me.shadymitsu.cobblemonshinydays.broadcast.BroadcastManager
import me.shadymitsu.cobblemonshinydays.commands.ShinyCommand
import me.shadymitsu.cobblemonshinydays.config.ConfigLoader
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.server.ServerStartedEvent
import java.time.LocalDateTime


@Mod("cobblemonshinydays")
class CobblemonShinyDays {

    init {
        println("Cobblemon Shiny Days loaded!")

        // Hook shiny chance logic
        CobblemonEvents.SHINY_CHANCE_CALCULATION.subscribe { event ->
            handleShinyChanceCalculation(event)
        }

        NeoForge.EVENT_BUS.register(EventHandlers)

        ShinyCommand.register()

        // Register server start event manually
        NeoForge.EVENT_BUS.addListener(this::onServerStarted)
    }

    private fun onServerStarted(event: ServerStartedEvent) {
        println("Cobblemon Shiny Days: Server started, beginning broadcasts!")
        BroadcastManager.startBroadcasting()
    }

    private fun handleShinyChanceCalculation(event: ShinyChanceCalculationEvent) {
        val day = LocalDateTime.now().dayOfWeek.name
        val speciesName = event.pokemon.species.name
        val pokemonTypes = event.pokemon.types.map { it.name.uppercase() }

        val config = ConfigLoader.loadConfig()
        val multiplier = config.firstOrNull {
            val dayMatches = it.days.any { configDay -> configDay.equals(day, ignoreCase = true) }
            if (!dayMatches) return@firstOrNull false

            val speciesMatches = it.species.contains("ALL") ||
                    it.species.any { s -> s.equals(speciesName, ignoreCase = true) }

            val labelMatches = it.labels.any { label -> event.pokemon.hasLabels(label) }

            val typeMatches = it.types?.any { type -> pokemonTypes.contains(type.uppercase()) } ?: false

            speciesMatches || labelMatches || typeMatches
        }?.multiplier

        if (multiplier != null) {
            event.addModificationFunction { base, _, _ -> base / multiplier }
        }
    }
}
