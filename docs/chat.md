# How a chat line reaches players

Minecraft's chat is signed: the client of the player who writes signs the message, the server passes
it on with a **chat type**, and the client of every player reading it builds the line from that chat
type, a frame with the sender's name and the text in it. What the client knows about such a line
depends on it staying the player's message: it can be reported to Mojang, hidden with the player in
the social interactions screen, read out by the narrator ("Steve says hello"), kept by "Only Show
Secure Chat", and deleted from everyone's chat by the server.

VersatiaChat keeps all of that. For every message it:

1. decorates the text with its message `text` in Paper's `AsyncChatDecorateEvent`, which is how
   Minecraft lets a server style chat: the decorated text travels with the signed message as its
   unsigned content, players see it, and the signature still covers the text as typed;
2. builds the channel's name (`channel.general`) and the sender (`sender`, with the `card` as its
   hover) from `messages/chat.yml`;
3. takes the players out of the viewers of Paper's `AsyncChatEvent`, so that Paper sends them
   nothing, and sets the event's renderer, which gives the console and the other viewers that are
   no player the line as plain text;
4. sends every one of those players the sender's signed message, with its chat type bound to the
   channel's name (the chat type's target) and the sender's name.

The text is the player's own, inserted as a value and never parsed: tags a player types stay plain
text.

## The frame

The frame is the chat type's: its text around the parts, which the client fills in. A channel whose
name shows no text, `general: ""`, frames its lines without a prefix.

| Chat type | Frame | Line |
|---|---|---|
| `versatiachat:line` | `%s %s: %s` | `** General ** Steve: hello` |
| `versatiachat:line_without_channel` | `%s: %s` | `Steve: hello` |
| vanilla team chat, `minecraft:team_msg_command_incoming` | `%s <%s> %s` | `** General ** <Steve> hello` |
| vanilla chat, `minecraft:chat` | `<%s> %s` | `<Steve> hello` |

The server reads chat types only when it starts, before any plugin, and Paper 26.3 gives plugins no
way to add one, so VersatiaChat's come in a datapack. When the plugin starts, it writes the files
of the pack that the main world's `datapacks` folder lacks:

```text
world/datapacks/VersatiaChat/pack.mcmeta
world/datapacks/VersatiaChat/data/versatiachat/chat_type/line.json
world/datapacks/VersatiaChat/data/versatiachat/chat_type/line_without_channel.json
```

A pack found in that folder when the server starts is enabled by itself (`/datapack list` shows it as
`file/VersatiaChat`), so its frames apply from the first restart after the files were written.
Until then, and whenever the pack is disabled or lacks a chat type, the lines it would frame use the
frames of vanilla chat, and the plugin logs which chat types the server has not read and how to get
them.

- Like the files of a data folder, a file of the pack is never overwritten; deleting the folder
  brings the plugin's copy back at the next start.
- The pack is made for the data pack format of Minecraft 26.3 (121).
- A chat type is no part of the world or of the players' data, so removing the pack harms nothing:
  lines return to the vanilla frame.

### What players notice

Nothing to install or accept. When a player joins, the server sends the chat types with the rest of
its registries, as it sends the vanilla ones. The frame is literal text, the same in every language
of the client, while the narrator still reads "Steve says hello" in the player's language.

## Other plugins

VersatiaChat delivers from `AsyncChatEvent` at the `HIGHEST` priority, only when the event is not
cancelled, and leaves it uncancelled, so plugins that relay or log chat still see it.

- A plugin that cancels the event at a lower priority stops the line.
- A plugin that takes a player out of the event's viewers hides the line from that player.
- A plugin that changes the text breaks the signature, which covers the text as typed: VersatiaChat
  then sends the changed text unsigned, in the same frame, and the client shows it as a line the
  server sent.

## Corner cases

- A server in offline mode (`online-mode=false`) never has signed messages: the client shows every
  player's line as not secure, as it does on any offline server, with or without plugins.
- The client marks a line as modified by the server when it no longer holds the text the player
  signed, so `text` keeps `{text}` whole; what it adds around it is fine.
- "Only Show Secure Chat" shows the text as typed, without its decoration, still in the frame and
  with the sender's name.
- The card is built on the chat thread, with the line: properties that only read the player, such as
  `{player.level}` or `{player.ping}`, are safe there.
- The console shows the frame in English (`<Steve>` for the vanilla one), the client in its own
  language.
