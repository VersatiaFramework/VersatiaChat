package dev.versatia.chat.frame

import dev.versatia.Phase
import dev.versatia.di.LocalBean
import dev.versatia.lifecycle.AutoInvoke
import java.util.logging.Logger
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit

/**
 * The frames of chat lines, the text around their channel, sender and message, with or without a
 * channel: a channel whose name shows no text frames no prefix. The client puts a line together from
 * a chat type, and the server reads chat types only when it starts: VersatiaChat's own once its
 * datapack is enabled, those of vanilla chat while the pack is missing, switched off or not read yet.
 * See docs/chat.md.
 */
@LocalBean
internal class ChatFrame(private val logger: Logger) {
    @Volatile
    private var withChannel = Frame.VANILLA_WITH_CHANNEL

    @Volatile
    private var withoutChannel = Frame.VANILLA_WITHOUT_CHANNEL

    @AutoInvoke(Phase.PLUGIN_START_ONLY)
    fun choose() {
        val datapacks = Bukkit.getWorldContainer().toPath().resolve(Bukkit.getWorlds().first().name).resolve("datapacks")
        val pack = Bukkit.getDatapackManager().getPack(FramePack.NAME)
        // Only a file that was there when the server started has registered its chat type
        val read = FramePack.chatTypes.filter { pack?.isEnabled == true && FramePack.has(datapacks, it) }
        val written = FramePack.write(datapacks)
        if (FramePack.WITH_CHANNEL in read) withChannel = FramePack.WITH_CHANNEL.frame
        if (FramePack.WITHOUT_CHANNEL in read) withoutChannel = FramePack.WITHOUT_CHANNEL.frame
        val unread = FramePack.chatTypes - read.toSet()
        when {
            unread.isEmpty() -> Unit
            pack != null && !pack.isEnabled -> logger.info(
                "The datapack ${FramePack.NAME} is disabled, so chat lines use the frames of vanilla chat; " +
                    "for VersatiaChat's own run /datapack enable \"${FramePack.NAME}\" and restart the server",
            )
            else -> logger.info(
                "The server has not read the chat types ${unread.joinToString { it.key.asString() }} of the datapack ${FramePack.NAME}" +
                    (if (written.isEmpty()) "" else ", whose files ${written.joinToString()} are now in $datapacks") +
                    "; until it restarts, the lines they frame use the frames of vanilla chat",
            )
        }
    }

    /** The chat type a line is sent with, bound to its channel's name and its sender. */
    fun bind(channel: Component, sender: Component) = if (named(channel)) withChannel.type.bind(sender, channel) else withoutChannel.type.bind(sender)

    /** The line as players see it, for the console and the other viewers of the chat event that are no player. */
    fun render(channel: Component, sender: Component, message: Component) =
        if (named(channel)) withChannel.fill(channel, sender, message) else withoutChannel.fill(sender, message)

    private fun named(channel: Component) = PlainTextComponentSerializer.plainText().serialize(channel).isNotBlank()
}
