package dev.versatia.chat.frame

import net.kyori.adventure.chat.ChatType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.Component.textOfChildren

/** The frame of a chat line: the chat type the client fills and its text, each `%s` standing for the next part. */
internal class Frame(val type: ChatType, val format: String) {
    /** The line as the client puts it together, for the console and the other viewers that are no player. */
    fun fill(vararg parts: Component) = textOfChildren(
        *format.split("%s").flatMapIndexed { i, literal ->
            listOfNotNull(literal.takeIf { it.isNotEmpty() }?.let { text(it) }, parts.getOrNull(i))
        }.toTypedArray(),
    )

    companion object {
        /** Vanilla team chat, `chat.type.team.text` in English: channel, `<sender>`, message. */
        val VANILLA_WITH_CHANNEL = Frame(ChatType.TEAM_MSG_COMMAND_INCOMING, "%s <%s> %s")

        /** Vanilla chat, `chat.type.text` in English: `<sender>`, message. */
        val VANILLA_WITHOUT_CHANNEL = Frame(ChatType.CHAT, "<%s> %s")
    }
}
