package dev.versatia.chat.frame

import java.util.logging.Logger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import net.kyori.adventure.chat.ChatType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

class ChatFrameTest {
    private val channel = text("** General **", NamedTextColor.DARK_AQUA)
    private val sender = text("Steve")
    private val message = text("hello")

    // Without its start function a frame is vanilla chat's, as on a server without the datapack
    private val frame = ChatFrame(Logger.getLogger("VersatiaChat"))

    @Test
    fun `fills VersatiaChat's frames as the client does`() {
        assertEquals("** General ** Steve: hello", plain(FramePack.WITH_CHANNEL.frame.fill(channel, sender, message)))
        assertEquals("Steve: hello", plain(FramePack.WITHOUT_CHANNEL.frame.fill(sender, message)))
    }

    @Test
    fun `keeps the parts as they are`() = assertTrue(channel in FramePack.WITH_CHANNEL.frame.fill(channel, sender, message).children())

    @Test
    fun `a line of a named channel takes the team chat frame of vanilla`() {
        val bound = frame.bind(channel, sender)

        assertEquals(ChatType.TEAM_MSG_COMMAND_INCOMING, bound.type())
        assertEquals(channel, bound.target())
        assertEquals("** General ** <Steve> hello", plain(frame.render(channel, sender, message)))
    }

    @Test
    fun `a channel whose name shows no text frames no prefix`() {
        for (name in listOf(empty(), text(" ", NamedTextColor.DARK_AQUA))) {
            val bound = frame.bind(name, sender)

            assertEquals(ChatType.CHAT, bound.type())
            assertNull(bound.target())
            assertEquals("<Steve> hello", plain(frame.render(name, sender, message)))
        }
    }

    private fun plain(line: Component) = PlainTextComponentSerializer.plainText().serialize(line)
}
