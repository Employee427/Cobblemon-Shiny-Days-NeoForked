package me.shadymitsu.cobblemonshinydays

import me.shadymitsu.cobblemonshinydays.broadcast.BroadcastManager
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent

object EventHandlers {
    @SubscribeEvent
    fun onPlayerJoin(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.entity
        if (player is ServerPlayer) {
            // Use server thread-safe scheduling
            player.server.execute {
                val message = BroadcastManager.generateBroadcastMessage()
                message?.let {
                    println("Sending broadcast message to ${player.name.string}")
                    player.sendSystemMessage(Component.literal(it))
                }
            }
        }
    }
}
