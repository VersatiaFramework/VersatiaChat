package dev.versatia.chat.line

import dev.versatia.chat.FakeMessages
import dev.versatia.chat.ListeningPlayer
import kotlin.test.Test
import kotlin.test.assertEquals
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.Component.textOfChildren
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent

class LinePartsTest {
    @Test
    fun `the sender shows the card on hover and acts as in vanilla`() {
        val card = text("Level: 0")
        val sender = LineParts(FakeMessages.fixed("sender" to text("Steve"), "card" to card)).sender(ListeningPlayer("Steve").player)

        assertEquals(HoverEvent.showText(card), sender.hoverEvent())
        assertEquals(ClickEvent.suggestCommand("/tell Steve "), sender.clickEvent())
        assertEquals("Steve", sender.insertion())
    }

    @Test
    fun `a channel is its message`() =
        assertEquals(text("** General **"), LineParts(FakeMessages.fixed("channel.general" to text("** General **"))).channel("general"))

    @Test
    fun `the text is the value of its message`() {
        val parts = LineParts(FakeMessages.fixed().with("text") { textOfChildren(text("> "), it.getValue("text") as Component) })

        assertEquals(textOfChildren(text("> "), text("<red>hi")), parts.text(text("<red>hi")))
    }
}
