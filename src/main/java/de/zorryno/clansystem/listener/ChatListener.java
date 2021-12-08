package de.zorryno.clansystem.listener;

import de.zorryno.clansystem.util.clans.Clan;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    @EventHandler
    public void onChatMessage(AsyncPlayerChatEvent event) {
        Clan clan = Clan.getClanFromPlayer(event.getPlayer());

        if(clan == null)
            return;

        event.setCancelled(true);
        for(Player player : Bukkit.getOnlinePlayers())
            player.sendMessage(clan.getPrefix() + event.getPlayer().getName() + ": " + event.getMessage());
    }
}
