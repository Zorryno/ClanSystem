package de.zorryno.clansystem.commands;

import de.zorryno.clansystem.util.clans.Clan;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ClanChatCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(args.length == 0) return false;
        if (!(sender instanceof Player player))
            return true;

        Clan clan = Clan.getClanFromPlayer(player);
        if(clan == null)
            return true;

        String message = "";
        for (String part : args) {
            message = message + part;
        }

        for(UUID uuid : clan.getMembers()) {
            Player clanPlayer = Bukkit.getPlayer(uuid);
            if(clanPlayer == null) continue;
            clanPlayer.sendMessage("§bCLAN§8 » §f" + player.getName() + ": " + message);
        }

        Bukkit.getConsoleSender().sendMessage("§bCLAN§8 » §f" + clan.getPrefix() + player.getName() + ": " + message);

        return true;
    }
}
