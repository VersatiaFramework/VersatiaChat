package dev.versatia.chat

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.bukkit.configuration.file.YamlConfiguration

/** A key the code reads missing from the shipped messages fails every chat message on the server, so it fails the build. */
class ShippedFilesTest {
    private fun yaml(resource: String) =
        YamlConfiguration.loadConfiguration(requireNotNull(javaClass.getResourceAsStream(resource)) { "$resource is not on the classpath" }.reader())

    @Test
    fun `the messages hold every key the plugin reads`() {
        val messages = yaml("/resources/messages/chat.yml")
        for (key in listOf("channel.general", "sender", "card", "text")) assertTrue(messages.contains(key), "messages/chat.yml lacks $key")
    }

    @Test
    fun `the styles are the plugin's own tags`() =
        assertEquals(setOf("generalChannel", "cardSeparator", "chatText"), yaml("/styles.yml").getKeys(false))
}
