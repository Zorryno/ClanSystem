package de.zorryno.clansystem.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class SetScoreboardListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if(Bukkit.getScoreboardManager() != null)
            event.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
}
