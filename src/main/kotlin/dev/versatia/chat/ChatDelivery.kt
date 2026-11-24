package dev.versatia.chat

import dev.versatia.chat.frame.ChatFrame
import dev.versatia.chat.line.LineParts
import io.papermc.paper.chat.ChatRenderer
import io.papermc.paper.event.player.AsyncChatDecorateEvent
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

/**
 * Sends every chat message as Minecraft's own player chat: the sender's signed message goes to each
 * player among the event's viewers with a chat type of [ChatFrame], and the client puts the channel,
 * the sender and the text together. The event stays uncancelled for other plugins; the console and
 * the other viewers that are no player get the same line from the event's renderer.
 */
internal class ChatDelivery(private val frame: ChatFrame, private val parts: LineParts) : Listener {
    /**
     * Gives the text its look the way Minecraft lets a server decorate chat: players see the
     * decorated text, while the signature still covers the text as typed.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onDecorate(event: AsyncChatDecorateEvent) = event.result(parts.text(event.result()))

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onChat(event: AsyncChatEvent) {
        val channel = parts.channel(GENERAL)
        val sender = parts.sender(event.player)
        val players = event.viewers().filterIsInstance<Player>()
        event.viewers().removeAll { it is Player }
        event.renderer(ChatRenderer.viewerUnaware { _, _, message -> frame.render(channel, sender, message) })
        val bound = frame.bind(channel, sender)
        val message = event.message()
        // A plugin that changed the text has broken its signature: the changed text goes out unsigned, in the same frame
        if (message == event.originalMessage()) players.forEach { it.sendMessage(event.signedMessage(), bound) }
        else players.forEach { it.sendMessage(message, bound) }
    }

    private companion object {
        /** The only channel so far, which every player hears. */
        const val GENERAL = "general"
    }
}
