package dev.versatia.chat.frame

import com.google.gson.JsonParser
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.io.TempDir

class FramePackTest {
    @Test
    fun `writes the pack and its chat types`(@TempDir datapacks: Path) {
        assertFalse(FramePack.has(datapacks, FramePack.WITH_CHANNEL))
        val chatTypes = "data/versatiachat/chat_type"
        assertEquals(listOf("pack.mcmeta", "$chatTypes/line.json", "$chatTypes/line_without_channel.json"), FramePack.write(datapacks))
        assertTrue(FramePack.chatTypes.all { FramePack.has(datapacks, it) })

        val pack = json(datapacks.resolve("VersatiaChat/pack.mcmeta")).getAsJsonObject("pack")
        assertEquals(121, pack["min_format"].asInt)
        assertEquals(121, pack["max_format"].asInt)
        assertChatType(datapacks.resolve("VersatiaChat/$chatTypes/line.json"), "%s %s: %s", "target", "sender", "content")
        assertChatType(datapacks.resolve("VersatiaChat/$chatTypes/line_without_channel.json"), "%s: %s", "sender", "content")
    }

    @Test
    fun `keeps what is there and writes only what is missing`(@TempDir datapacks: Path) {
        FramePack.write(datapacks)
        val chatType = datapacks.resolve("VersatiaChat").resolve(FramePack.WITH_CHANNEL.path)
        Files.writeString(chatType, "edited")
        Files.delete(datapacks.resolve("VersatiaChat").resolve(FramePack.WITHOUT_CHANNEL.path))

        assertEquals(listOf(FramePack.WITHOUT_CHANNEL.path), FramePack.write(datapacks))
        assertEquals("edited", Files.readString(chatType))
        assertEquals(emptyList(), FramePack.write(datapacks))
    }

    private fun assertChatType(file: Path, format: String, vararg parameters: String) {
        val chat = json(file).getAsJsonObject("chat")
        assertEquals(format, chat["translation_key"].asString)
        assertEquals(parameters.toList(), chat.getAsJsonArray("parameters").map { it.asString })
        assertEquals("chat.type.text.narrate", json(file).getAsJsonObject("narration")["translation_key"].asString)
    }

    private fun json(file: Path) = JsonParser.parseString(Files.readString(file)).asJsonObject
}
