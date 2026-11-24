package dev.versatia.chat

import dev.versatia.chat.frame.ChatFrame
import dev.versatia.chat.line.LineParts
import io.papermc.paper.chat.ChatRenderer
import io.papermc.paper.event.player.AsyncChatDecorateEvent
import io.papermc.paper.event.player.AsyncChatEvent
import java.util.logging.Logger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.chat.ChatType
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

class ChatDeliveryTest {
    private val channel = text("** General **")
    private val messages = FakeMessages.fixed("channel.general" to channel, "sender" to text("Alice"), "card" to text("Level: 0"))
        .with("text") { (it.getValue("text") as Component).color(NamedTextColor.GRAY) }
    private val console = object : Audience {}
    private val alice = ListeningPlayer("Alice")
    private val bob = ListeningPlayer("Bob")

    // Without its start function the frame is the vanilla team chat, as on a server without the datapack
    private fun delivery() = ChatDelivery(ChatFrame(Logger.getLogger("VersatiaChat")), LineParts(messages))

    private fun chat(viewers: Set<Audience>, said: String, typed: String = said) = AsyncChatEvent(
        true, alice.player, viewers.toMutableSet(), ChatRenderer.defaultRenderer(), text(said), text(typed), SignedMessage.system(typed, null),
    )

    @Test
    fun `the text takes the look of its message and stays what was typed`() {
        val event = AsyncChatDecorateEvent(alice.player, text("hello"))

        delivery().onDecorate(event)

        assertEquals(text("hello", NamedTextColor.GRAY), event.result())
    }

    @Test
    fun `every player gets the signed message in the frame and the console the same line`() {
        val event = chat(setOf(alice.player, bob.player, console), "hello")

        delivery().onChat(event)

        assertEquals(setOf(console), event.viewers())
        for (listener in listOf(alice, bob)) {
            val (message, bound) = listener.signed.single()
            assertSame(event.signedMessage(), message)
            assertEquals(ChatType.TEAM_MSG_COMMAND_INCOMING, bound.type())
            assertEquals(channel, bound.target())
            assertEquals("Alice", plain(bound.name()))
        }
        assertEquals("** General ** <Alice> hello", plain(event.renderer().render(alice.player, text("Alice"), event.message(), console)))
    }

    @Test
    fun `a channel with an empty name sends lines without a prefix`() {
        val unnamed = ChatDelivery(ChatFrame(Logger.getLogger("VersatiaChat")), LineParts(messages.with("channel.general") { text("") }))
        val event = chat(setOf(alice.player, console), "hello")

        unnamed.onChat(event)

        val (_, bound) = alice.signed.single()
        assertEquals(ChatType.CHAT, bound.type())
        assertEquals(null, bound.target())
        assertEquals("<Alice> hello", plain(event.renderer().render(alice.player, text("Alice"), event.message(), console)))
    }

    @Test
    fun `a player the event leaves out gets nothing`() {
        delivery().onChat(chat(setOf(alice.player), "hello"))

        assertEquals(1, alice.signed.size)
        assertEquals(emptyList(), bob.signed + bob.unsigned)
    }

    @Test
    fun `a text another plugin changed goes out unsigned in the same frame`() {
        delivery().onChat(chat(setOf(alice.player), "h***o", typed = "hello"))

        assertEquals(emptyList(), alice.signed)
        val (message, bound) = alice.unsigned.single()
        assertEquals(text("h***o"), message)
        assertEquals(ChatType.TEAM_MSG_COMMAND_INCOMING, bound.type())
    }

    private fun plain(component: Component?) = component?.let(PlainTextComponentSerializer.plainText()::serialize)
}
