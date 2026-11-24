package dev.versatia.chat

import java.lang.reflect.Proxy
import net.kyori.adventure.chat.ChatType
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player

/** A player with a name only, keeping the chat it receives: all the chat needs, without a server. */
class ListeningPlayer(name: String) {
    val signed = mutableListOf<Pair<SignedMessage, ChatType.Bound>>()
    val unsigned = mutableListOf<Pair<Component, ChatType.Bound>>()

    val player = Proxy.newProxyInstance(Player::class.java.classLoader, arrayOf(Player::class.java)) { proxy, method, arguments ->
        val bound = arguments?.getOrNull(1) as? ChatType.Bound
        when {
            method.name == "getName" -> name
            method.name == "displayName" -> text(name)
            method.name == "sendMessage" && bound != null -> {
                when (val message = arguments.firstOrNull()) {
                    is SignedMessage -> signed += message to bound
                    is Component -> unsigned += message to bound
                    else -> throw UnsupportedOperationException("sendMessage(${message?.javaClass?.simpleName}, ChatType.Bound)")
                }
                null
            }
            method.name == "equals" -> proxy === arguments?.get(0)
            method.name == "hashCode" -> System.identityHashCode(proxy)
            method.name == "toString" -> "player $name"
            else -> throw UnsupportedOperationException("Player.${method.name}")
        }
    } as Player
}
