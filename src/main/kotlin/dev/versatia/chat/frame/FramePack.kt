package dev.versatia.chat.frame

import java.nio.file.Files
import java.nio.file.Path
import net.kyori.adventure.chat.ChatType
import net.kyori.adventure.key.Key

/**
 * The datapack registering the chat types of VersatiaChat's frames, written into the `datapacks`
 * folder of the main world, where the server reads it when it starts. Like the files of a data
 * folder, a file already there is never overwritten: deleting the folder brings this copy back.
 */
internal object FramePack {
    /** The folder under the world's `datapacks`. */
    const val FOLDER = "VersatiaChat"

    /** The pack's name for the server, as `/datapack list` shows it. */
    const val NAME = "file/$FOLDER"

    /** The data pack format of Minecraft 26.3. */
    private const val PACK_FORMAT = 121

    /** A line of a channel: `** General ** Steve: hello`. */
    val WITH_CHANNEL = PackChatType("line", "%s %s: %s", "target", "sender", "content")

    /** A line of a channel whose name is empty: `Steve: hello`. */
    val WITHOUT_CHANNEL = PackChatType("line_without_channel", "%s: %s", "sender", "content")

    val chatTypes = listOf(WITH_CHANNEL, WITHOUT_CHANNEL)

    /** The files of the pack, by their path relative to it. */
    val files = mapOf(
        "pack.mcmeta" to """
            {
              "pack": {
                "description": "The frames of VersatiaChat's chat lines",
                "min_format": $PACK_FORMAT,
                "max_format": $PACK_FORMAT
              }
            }
        """.trimIndent() + "\n",
    ) + chatTypes.associate { it.path to it.content }

    /** Whether the pack in [datapacks] holds the file of [chatType]. */
    fun has(datapacks: Path, chatType: PackChatType) = Files.exists(datapacks.resolve(FOLDER).resolve(chatType.path))

    /** Writes the files of the pack that [datapacks] lacks and returns their paths, relative to the pack. */
    fun write(datapacks: Path) = files.filterKeys { !Files.exists(datapacks.resolve(FOLDER).resolve(it)) }.map { (path, content) ->
        val file = datapacks.resolve(FOLDER).resolve(path)
        Files.createDirectories(file.parent)
        Files.writeString(file, content)
        path
    }
}

/** A chat type of the pack: the frame it registers and its file, which fills [format] with [parameters] in order. */
internal class PackChatType(id: String, format: String, vararg parameters: String) {
    val key = Key.key("versatiachat", id)

    val frame = Frame(ChatType.chatType(key), format)

    val path = "data/versatiachat/chat_type/$id.json"

    val content = """
        {
          "chat": {
            "translation_key": "$format",
            "parameters": [${parameters.joinToString { "\"$it\"" }}]
          },
          "narration": {
            "translation_key": "chat.type.text.narrate",
            "parameters": ["sender", "content"]
          }
        }
    """.trimIndent() + "\n"
}
