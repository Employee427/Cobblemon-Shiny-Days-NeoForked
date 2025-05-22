package me.shadymitsu.cobblemonshinydays.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import me.shadymitsu.cobblemonshinydays.config.ConfigLoader
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.common.NeoForge
import java.time.LocalDateTime

object ShinyCommand {
    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        event.dispatcher.register(
            LiteralArgumentBuilder.literal<CommandSourceStack>("shinyday")
                .executes { ctx ->
                    val day = LocalDateTime.now().dayOfWeek.name
                    val config = ConfigLoader.loadConfig()
                    val matches = config.filter {
                        it.days.any { d -> d.equals(day, ignoreCase = true) }
                    }

                    if (matches.isEmpty()) {
                        ctx.source.sendSystemMessage(Component.literal("§eNo shiny boosts are active today."))
                    } else {
                        matches.forEach {
                            val species = it.species.joinToString(", ")
                            val labels = it.labels.joinToString(", ")
                            val types = it.types?.joinToString(", ") ?: ""

                            val msg = Component.literal(
                                "§aBoosted today: §d$species §b$labels §c$types §7(x${it.multiplier})"
                            )
                            ctx.source.sendSystemMessage(msg)
                        }
                    }
                    return@executes 1
                }
        )
    }

    fun register() {
        NeoForge.EVENT_BUS.register(this)
    }
}
