# VersatiaChat

Player chat for Paper 26.3, built on the [Versatia framework](https://versatia.dev/): every line
names its channel in front of the sender, `** General ** Steve: hello`, and stays Minecraft's own
player chat. The sender's signed message reaches every player as typed and the client puts the line
together from a chat type this plugin registers, so reporting, hiding a player, the narrator and
"Only Show Secure Chat" work as in vanilla. Hovering over the sender's name shows a card the server
writes line by line; a click suggests `/tell` and Shift + click inserts the name, as in vanilla.

> In development: one channel, `general`, which every player hears.

Documentation (experimental): [for plugin developers](https://versatia.dev/experimental/plugins/chat)
and [for server admins](https://versatia.dev/admin/experimental/chat).

## Installing

Put `VersatiaChat.jar` into `plugins/` next to VersatiaCore, which it requires (it needs no other
plugin), and start the server. VersatiaChat writes its datapack into the main world's `datapacks`
folder and logs that it did; the server reads chat types only when it starts, so lines take the
frame `{channel} {sender}: {message}` from the next restart on. Until then they use the frame of
vanilla team chat, `** General ** <Steve> hello`, or of vanilla chat for a channel without a name,
`<Steve> hello`. Players install nothing: the server sends them the chat types when they join. See
[docs/chat.md](docs/chat.md#the-frame).

## Configuring

`plugins/VersatiaChat/messages/chat.yml` holds the parts of a line, naming the server's styles:

```yaml
channel:
  general: "<generalChannel>** General **</generalChannel>"
sender: "{player}"
card:
  - "{player}"
  - "<cardSeparator>----------------</cardSeparator>"
  - "<label>Level:</label> {player.level}"
text: "<chatText>{text}</chatText>"
```

- `channel.general` is the name in front of the channel's lines. An empty name, `general: ""`,
  leaves them without one: `Steve: hello`.
- `sender` is the sender's name, which looks like a player's name in every Versatia message.
- `card` is the card shown on hover over the name, one entry per line, top to bottom: any property
  of `{player}`, such as `{player.level}`, `{player.health}` or `{player.ping}`, and those a server
  presentation of players adds.
- `text` is what the player wrote, `{text}`, as typed: tags in it stay plain text.

The plugin's own styles, in `plugins/VersatiaCore/styles.yml`, say how they look:

| Style | Default | What it is |
|---|---|---|
| `generalChannel` | `<dark_aqua>` | the name of the general channel |
| `cardSeparator` | `<dark_gray>` | the line under the name in the card |
| `chatText` | `<gray>` | what players write, quieter than their names |

`/versatia reload chat` applies a change. The frame around the parts is the chat type's and
changes only with a restart.

## Other plugins

VersatiaChat delivers the lines from Paper's `AsyncChatEvent`, at the `HIGHEST` priority and only
when no plugin cancelled it, and leaves the event uncancelled. A plugin that cancels it at a lower
priority stops the line, one that takes a player out of the event's viewers hides it from them, and
one that changes the text sends the changed text unsigned: [docs/chat.md](docs/chat.md#other-plugins).

## Building

Requires JDK 25 and the Versatia artifacts in the local Maven repository (see the
[workspace scripts](https://github.com/VersatiaFramework)):

```bash
./gradlew build -x deployPlugin
./gradlew publishToMavenLocal
```

The plugin JAR is `build/libs/VersatiaChat-<version>.jar`. Copy `local.properties.example` to
`local.properties` and set `pluginsDir` to have `./gradlew build` copy it to your server.

## License

Apache License 2.0; see [LICENSE](LICENSE).
