package dev.versatia.chat

import dev.versatia.messages.Message
import dev.versatia.messages.Messages
import dev.versatia.text.Argument
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

/** Messages for tests without VersatiaCore's parser: each one is a function of its arguments, by name. */
class FakeMessages(private val renders: Map<String, (Map<String, Any?>) -> Component>) : Messages {
    override val keys get() = renders.keys

    override fun contains(key: String) = key in renders

    override fun get(key: String) = getOrNull(key) ?: error("No message $key")

    override fun getOrNull(key: String) = renders[key]?.let { FakeMessage(key, it) }

    /** These messages and [key], rendered by [render]. */
    fun with(key: String, render: (Map<String, Any?>) -> Component) = FakeMessages(renders + (key to render))

    companion object {
        /** Messages that are the same whatever their arguments. */
        fun fixed(vararg texts: Pair<String, Component>) = FakeMessages(texts.associate { (key, text) -> key to { _: Map<String, Any?> -> text } })
    }
}

class FakeMessage(override val key: String, private val render: (Map<String, Any?>) -> Component) : Message {
    override val raw: String get() = PlainTextComponentSerializer.plainText().serialize(get())

    override val file get() = "messages/chat.yml"

    override fun get() = render(emptyMap())

    override fun get(vararg arguments: Argument) = render(arguments.associate { it.name to it.value })

    override fun lines(vararg arguments: Argument) = listOf(get(*arguments))

    override fun broadcast(vararg arguments: Argument) = Unit
}
