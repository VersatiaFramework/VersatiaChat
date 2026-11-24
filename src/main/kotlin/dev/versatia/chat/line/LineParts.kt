package dev.versatia.chat.line

import dev.versatia.di.LocalBean
import dev.versatia.messages.Messages
import dev.versatia.text.arg
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.entity.Player

/**
 * The parts of a chat line written in `messages/chat.yml`: a channel's name, the sender, whose name
 * shows the card on hover and, as in vanilla, suggests `/tell` on click and inserts itself on
 * shift-click, and the text the player wrote.
 */
@LocalBean
internal class LineParts(private val messages: Messages) {
    fun channel(id: String) = messages["channel.$id"].get()

    /** [typed] inserted as a value, never parsed, so tags a player types stay plain text. */
    fun text(typed: Component) = messages["text"].get(arg("text", typed))

    fun sender(player: Player) = messages["sender"].get(arg("player", player))
        .hoverEvent(messages["card"].get(arg("player", player)))
        .clickEvent(ClickEvent.suggestCommand("/tell ${player.name} "))
        .insertion(player.name)
}
