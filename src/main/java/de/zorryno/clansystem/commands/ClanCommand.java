package de.zorryno.clansystem.commands;

import de.zorryno.clansystem.Main;
import de.zorryno.clansystem.util.clans.Clan;
import de.zorryno.clansystem.util.clans.ClanInvite;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


//Todo: result Messages + debugMode
// Help Command
public class ClanCommand implements CommandExecutor {
    public ClanCommand(Plugin plugin) {
        this.plugin = plugin;
        BLACKLIST = plugin.getConfig().getStringList("blackList");
    }

    private final Plugin plugin;
    private List<String> BLACKLIST;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player))
            return true;

        Player player = (Player) sender;
        Clan clan = Clan.getClanFromPlayer(player);
        boolean isClanMember = clan != null;
        ClanInvite invite = ClanInvite.getInvite(player.getUniqueId());

        // Send Clan List
        if (args.length == 0) { // /clan
            player.sendMessage(Main.getMessages().getCache().get("ClanListHeader"));
            Main.getSaver().getClans().forEach(existingClan -> player.sendMessage(existingClan.getName()));
            return true;
        }

        switch (args[0].toLowerCase()) {
            // Send Plugin Help
            case "help": // /Clan help
                sendHelp(player);
                break;

            // Send Clan Info
            case "info": // /Clan info <clanName>
                if (args.length == 1) {
                    if (isClanMember)
                        clan.sendClanInfo(player);
                    else
                        player.sendMessage(Main.getMessages().getCache().get("NotAClanMember"));
                    return true;
                }
                if (args.length != 2) {
                    player.sendMessage("/Clan info <clanName>");
                    return true;
                }

                Clan targetClan = Clan.getClanByName(args[1]);
                if (targetClan == null) {
                    player.sendMessage(Main.getMessages().getCache().get("ClanNotFound"));
                    return true;
                }

                targetClan.sendClanInfo(player);
                break;

            // Create a new Clan
            case "create": // /Clan create <name> <displayName> <prefix>
                if (clan != null) {
                    player.sendMessage(Main.getMessages().getCache().get("AlreadyInAClan"));
                    return true;
                }

                if (args.length != 4) {
                    player.sendMessage("/Clan create <name> <displayName> <prefix>");
                    return true;
                }

                List<Illegal> illegals = getIllegals(args[1], args[2], args[3], false);
                if (illegals.isEmpty()) {
                    new Clan(plugin, player.getUniqueId(), args[1], args[2], args[3]);
                    player.sendMessage(Main.getMessages().getCache().get("ClanCreated").replace("%name%", args[1]));
                } else {
                    illegals.forEach(illegal -> player.sendMessage(Main.getMessages().getCache().get("IllegalClanName").replace("%part%", illegal.name)));
                }
                return true;

            // Leave the Clan
            case "leave": // /Clan leave
                leave(player);
                break;

            // Accept Invite
            case "accept": // /Clan accept
                if (invite == null) {
                    player.sendMessage(Main.getMessages().getCache().get("ClanInvite.NotInvited"));
                    return true;
                }

                if (clan != null) {
                    player.sendMessage(Main.getMessages().getCache().get("AlreadyInAClan"));
                    return true;
                }

                invite.accept();

                invite.getInviter().sendMessage(Main.getMessages().getCache().get("ClanInvite.InviteAcceptInviter").replace("%name%", player.getName()));
                player.sendMessage(Main.getMessages().getCache().get("ClanInvite.InviteAcceptInvited"));
                break;

            // Deny Invite
            case "deny": // /Clan deny
                if (invite == null) {
                    player.sendMessage(Main.getMessages().getCache().get("ClanInvite.NotInvited"));
                    return true;
                }

                if (clan != null) {
                    player.sendMessage(Main.getMessages().getCache().get("AlreadyInAClan"));
                    return true;
                }

                invite.deny();

                invite.getInviter().sendMessage(Main.getMessages().getCache().get("ClanInvite.InviteDenyInviter").replace("%name%", player.getName()));
                player.sendMessage(Main.getMessages().getCache().get("ClanInvite.InviteDenyInvited"));
                break;


            //Admin Commands

            // Invite a Player in your Clan
            case "invite": // /Clan invite <player>
                if (clan == null) {
                    player.sendMessage(Main.getMessages().getCache().get("NotAClanMember"));
                    return true;
                }
                if (!clan.isAdmin(player.getUniqueId())) {
                    player.sendMessage(Main.getMessages().getCache().get("NotAClanAdmin"));
                    return true;
                }

                if (args.length != 2) {
                    player.sendMessage("/Clan invite <player>");
                    return true;
                }

                Player inviteTarget = Bukkit.getPlayer(args[1]);
                if (inviteTarget == null) {
                    player.sendMessage(Main.getMessages().getCache().get("PlayerNotFound"));
                    return true;
                }

                if (clan.getMembers().contains(inviteTarget.getUniqueId())) {
                    player.sendMessage(Main.getMessages().getCache().get("ClanInvite.AlreadyInClan"));
                }
                //send invite
                new ClanInvite(clan, player, inviteTarget, plugin, 20 * 60 * 5).sendInvite();
                player.sendMessage(Main.getMessages().getCache().get("ClanInvite.InvitePlayer").replace("%name%", inviteTarget.getName()));

                break;

            // Kick A Player
            case "kick": // /Clan kick <player>
                if (clan == null) {
                    player.sendMessage(Main.getMessages().getCache().get("NotAClanMember"));
                    return true;
                }
                if (!clan.isAdmin(player.getUniqueId())) {
                    player.sendMessage(Main.getMessages().getCache().get("NotAClanAdmin"));
                    return true;
                }

                if (args.length != 2) {
                    player.sendMessage("/Clan kick <player>");
                    return true;
                }

                UUID kickTarget = getUUID(args[1]);
                if (kickTarget == null) {
                    player.sendMessage(Main.getMessages().getCache().get("PlayerNotFound"));
                    return true;
                }

                if (clan.isAdmin(kickTarget) || kickTarget.equals(clan.getOwner())) {
                    player.sendMessage(Main.getMessages().getCache().get("MemberIsAdmin"));
                    return true;
                }

                if (clan.removeMember(kickTarget))
                    player.sendMessage(Main.getMessages().getCache().get("MemberRemoved"));
                else
                    player.sendMessage(Main.getMessages().getCache().get("MemberNotInClan"));
                break;


            //Owner Commands

            // Give Players Admin Permission
            case "setadmin": // /Clan setAdmin <player>
                if (clan == null) {
                    player.sendMessage(Main.getMessages().getCache().get("NotAClanMember"));
                    return true;
                }
                if (!player.getUniqueId().equals(clan.getOwner())) {
                    player.sendMessage(Main.getMessages().getCache().get("NotAClanOwner"));
                    return true;
                }

                if (args.length != 2) {
                    player.sendMessage("/Clan setAdmin <player>");
                    return true;
                }

                UUID adminTarget = getUUID(args[1]);
                if (adminTarget == null) {
                    player.sendMessage(Main.getMessages().getCache().get("PlayerNotFound"));
                    return true;
                }

                if (!clan.isInClan(adminTarget)) {
                    player.sendMessage(Main.getMessages().getCache().get("MemberNotInClan"));
                    return true;
                }

                if (!clan.getAdmins().contains(adminTarget)) {
                    if (clan.addAdmin(adminTarget)) {
                        player.sendMessage(Main.getMessages().getCache().get("SetMemberToAdmin"));
                    }
                } else {
                    if (clan.removeAdmin(adminTarget)) {
                        player.sendMessage(Main.getMessages().getCache().get("SetAdminToMember"));
                    }
                }
                break;

            // Sets the Owner of this Clan
            case "setowner": // /clan setOwner <player>
                if (clan == null) {
                    player.sendMessage(Main.getMessages().getCache().get("NotAClanMember"));
                    return true;
                }
                if (!player.getUniqueId().equals(clan.getOwner())) {
                    player.sendMessage(Main.getMessages().getCache().get("NotAClanOwner"));
                    return true;
                }

                if (args.length != 2) {
                    player.sendMessage("/Clan setOwner <player>");
                    return true;
                }

                UUID ownerTarget = getUUID(args[1]);
                if (ownerTarget == null) {
                    player.sendMessage(Main.getMessages().getCache().get("PlayerNotFound"));
                    return true;
                }

                if (!clan.isInClan(ownerTarget)) {
                    player.sendMessage(Main.getMessages().getCache().get("MemberNotInClan"));
                    return true;
                }

                if (clan.setOwner(ownerTarget))
                    player.sendMessage(Main.getMessages().getCache().get("SetOwner"));
                break;

            // Sets the displayName of this Clan
            case "setdisplayname": // /clan setDisplayName <name>
                if (clan == null) {
                    player.sendMessage(Main.getMessages().getCache().get("NotAClanMember"));
                    return true;
                }
                if (!player.getUniqueId().equals(clan.getOwner())) {
                    player.sendMessage(Main.getMessages().getCache().get("NotAClanOwner"));
                    return true;
                }

                if (args.length != 2) {
                    player.sendMessage("/Clan setDisplayName <name>");
                    return true;
                }

                List<Illegal> illegalDisplayName = getIllegals(clan.getName(), args[1], clan.getPrefix().substring(1, clan.getPrefix().length() - 1), true);
                illegalDisplayName.forEach(illegal -> player.sendMessage(Main.getMessages().getCache().get("IllegalClanName").replace("%part%", illegal.name)));
                if (!illegalDisplayName.isEmpty())
                    return true;

                clan.setDisplayName(args[1]);
                break;

            // Sets the Prefix of this Clan
            case "setprefix": // /clan setPrefix <prefix>
                if (clan == null) {
                    player.sendMessage(Main.getMessages().getCache().get("NotAClanMember"));
                    return true;
                }
                if (!player.getUniqueId().equals(clan.getOwner())) {
                    player.sendMessage(Main.getMessages().getCache().get("NotAClanOwner"));
                    return true;
                }

                if (args.length != 2) {
                    player.sendMessage("/Clan setPrefix <prefix>");
                    return true;
                }

                List<Illegal> illegalPrefix = getIllegals(clan.getName(), clan.getDisplayName(), args[1], true);
                illegalPrefix.forEach(illegal -> player.sendMessage(Main.getMessages().getCache().get("IllegalClanName").replace("%part%", illegal.name)));
                if (!illegalPrefix.isEmpty())
                    return true;

                clan.setPrefix(args[1]);
                break;


            //OP Commands

            //Reload messages and Config
            case "reload":  // /clan reload
                Main.getMessages().reload();
                plugin.reloadConfig();
                BLACKLIST = plugin.getConfig().getStringList("blackList");
                player.sendMessage("Config reloaded");
        }
        return true;
    }

    private void sendHelp(Player player) {
        Main.getMessages().getMessagesList("HelpPage").forEach((message) -> player.sendMessage(message));
    }

    private boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException exception) {
            return false;
        }
        return true;
    }

    /**
     * Let a Player leave his Clan
     *
     * @param player the Player
     */
    private void leave(Player player) {
        Clan clan = Clan.getClanFromPlayer(player);
        if (clan == null) {
            player.sendMessage(Main.getMessages().getCache().get("NotAClanMember"));
        } else {
            if (clan.removeMember(player.getUniqueId())) {
                player.sendMessage(Main.getMessages().getCache().get("PlayerLeaveClan"));
                if (player.getUniqueId().equals(clan.getOwner())) {
                    if (Main.isDebugMode())
                        plugin.getLogger().info("Clan " + clan.getName() + " : Owner left Clan");
                    clan.unregister();
                }
            }
        }
    }

    /**
     * Get the UUID from A name
     *
     * @param name The Players Name
     * @return The UUID from this Player or null if the Player does not exist
     */
    public static UUID getUUID(String name) {
        if (name == null) return null;
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (name.equals(offlinePlayer.getName())) {
                return offlinePlayer.getUniqueId();
            }
        }
        return null;
    }

    /**
     * Returns Illegals with this Names
     *
     * @param name        the ClanName
     * @param displayName The DisplayName
     * @param prefix      The ClanPrefix
     * @return Illegals
     */
    private List<Illegal> getIllegals(String name, String displayName, String prefix, boolean ignoreExistingClans) {
        List<Illegal> illegal = new ArrayList<>();

        for (String badName : BLACKLIST) {
            if (name.contains(badName))
                illegal.add(Illegal.NAME);

            if (displayName.contains(badName))
                illegal.add(Illegal.DISPLAYNAME);

            if (prefix.contains(badName) || ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', prefix)).length() > 6)
                illegal.add(Illegal.PREFIX);
        }

        if (!ignoreExistingClans && Main.getSaver().getNames().contains(name)) {
            illegal.add(Illegal.NAME);
        }

        return illegal;
    }
}

/**
 * represents Illegal States
 */
enum Illegal {
    NAME("Name"),
    DISPLAYNAME("DisplayName"),
    PREFIX("Prefix");

    String name;

    Illegal(String name) {
        this.name = name;
    }
}
