package de.zorryno.clansystem.commands;

import de.zorryno.clansystem.Main;
import de.zorryno.clansystem.util.clans.Clan;
import de.zorryno.clansystem.util.clans.ClanInvite;
import de.zorryno.clansystem.util.clans.Saver;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClanTabCompleter implements TabCompleter {
    public ClanTabCompleter(Plugin plugin) {
        this.plugin = plugin;
    }

    private Plugin plugin;

    /*
    Normal Commands
    ** /clan
    ** /clan help
    ** /clan info <clanName>
    ** /clan create <name> <displayName> <prefix>
    ** /clan leave
    ** /clan accept
    ** /clan deny
    *
    Admin commands
    ** /clan kick <player>
    ** /clan invite <player>
    *
    Owner Commands
    ** /clan setAdmin <player>
    ** /clan setOwner <player>
    ** /clan setDisplayName <name>
    ** /clan setPrefix <prefix>
    *
    OP Commands
    ** /clan reload
    */

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> commands = new ArrayList<>();
        List<String> completions = new ArrayList<>();

        if(!(sender instanceof Player)) {
            return Collections.emptyList();
        }

        Player player = (Player) sender;
        Clan clan = Clan.getClanFromPlayer(player);
        boolean isClanMember = clan != null;
        ClanInvite invite = ClanInvite.getInvite(player.getUniqueId());
        if(args.length == 1) { //NoCondition
            commands.add("info");
            commands.add("help");

            if (clan == null) //NoClan
                commands.add("create");
            else { //InClan
                commands.add("leave");
                commands.add("lock");
                commands.add("unlock");
            }

            if (invite != null) { //openInvite
                commands.add("accept");
                commands.add("deny");
            }

            if(clan != null && clan.getAdmins().contains(player.getUniqueId())) { //ClanAdmin
                commands.add("kick");
                commands.add("invite");
            }

            if(clan != null && player.getUniqueId().equals(clan.getOwner())) { //Clan Owner
                commands.add("setAdmin");
                commands.add("setOwner");
                commands.add("setDisplayName");
                commands.add("setPrefix");
                commands.add("alliance");
            }

            if(player.isOp()) { //OP
                commands.add("reload");
            }
        }

        if(args.length == 2) {
            switch(args[0].toLowerCase()) {
                case "info":
                    commands.addAll(Main.getSaver().getNames());
                    break;

                case "alliance":
                    commands.addAll(Main.getSaver().getNames());
                    commands.remove(clan.getName());
                    break;

                case "kick":
                    List<String> kickTargets = new ArrayList<>(clan.getMemberNames());
                    kickTargets.removeAll(clan.getAdminNames());
                    commands.addAll(kickTargets);
                    break;

                case "invite":
                    List<String> inviteNames = new ArrayList<>();
                    Bukkit.getOnlinePlayers().forEach(onlinePlayer -> inviteNames.add(onlinePlayer.getName()));
                    inviteNames.removeAll(clan.getMemberNames());
                    commands.addAll(inviteNames);
                    break;

                case "setadmin":
                    commands.addAll(clan.getMemberNames());
                    break;

                case "setowner":
                    List<String> adminTargetNames = new ArrayList<>(clan.getMemberNames());
                    adminTargetNames.remove(Bukkit.getOfflinePlayer(clan.getOwner()).getName());
                    commands.addAll(adminTargetNames);
                    break;
            }
        }

        StringUtil.copyPartialMatches(args[args.length - 1], commands, completions);
        Collections.sort(completions);
        return completions;
    }
}
