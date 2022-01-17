package de.zorryno.clansystem.util.clans;

import de.zorryno.clansystem.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Team;

import java.util.*;

/**
 * Represents a Clan
 */
public class Clan {
    private Plugin plugin;
    private UUID owner;
    private List<UUID> admin;
    private List<UUID> members;
    private final Team team;
    private String prefix;

    private List<Location> protectedBlocks;
    private List<String> alliance;

    /**
     * Creates a new Clan
     *
     * @param plugin      The Plugin
     * @param owner       The UUID from the Owner
     * @param name        The Team name for the Scoreboard
     * @param displayName The Name of the Clan
     * @param prefix      The Prefix without [ and ] (added in this Method)
     * @see #Clan(Plugin, UUID, String, String, String, List, List, List)
     */
    public Clan(Plugin plugin, UUID owner, String name, String displayName, String prefix) {
        this(plugin, owner, name, displayName, "§r[" + prefix + "§r] ", null, null, null, null);
    }

    /**
     * Creates a new Clan with Admins and Members
     *
     * @param plugin      The Plugin
     * @param owner       The UUID from the Owner
     * @param name        The Team name for the Scoreboard
     * @param displayName The Name of the Clan
     * @param prefix      The Prefix with [ and ] surround it
     * @param admins      The UUID List with the Admins
     * @param members     The UUID List with all Members
     * @see #Clan(Plugin, UUID, String, String, String)
     */
    public Clan(Plugin plugin, UUID owner, String name, String displayName, String prefix, List<UUID> admins, List<UUID> members, List<Location> protectedBlocks, List<String> alliance) {
        if (Bukkit.getScoreboardManager() == null)
            throw new NullPointerException("This can't happen call the police or something \n Bukkit.getScoreboardManager() is null");

        team = getOrCreateTeam(name);

        team.setPrefix(ChatColor.translateAlternateColorCodes('&', prefix));
        this.prefix = prefix.substring(1, prefix.length() - 1);
        team.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        this.plugin = plugin;
        this.owner = owner;
        this.admin = admins != null ? new ArrayList<>(admins) : new ArrayList<>();
        this.members = members != null ? new ArrayList<>(members) : new ArrayList<>();
        this.protectedBlocks = protectedBlocks != null ? new ArrayList<>(protectedBlocks) : new ArrayList<>();
        for (UUID uuid : this.members) {
            String entryName = Bukkit.getOfflinePlayer(uuid).getName();
            if (entryName != null)
                team.addEntry(entryName);
        }
        this.alliance = alliance != null ? new ArrayList<>(alliance) : new ArrayList<>();

        String ownerName = Bukkit.getOfflinePlayer(owner).getName() != null ? Bukkit.getOfflinePlayer(owner).getName() : owner.toString();

        if (!this.members.contains(owner))
            this.members.add(owner);
        if (!this.admin.contains(owner))
            this.admin.add(owner);
        team.addEntry(ownerName);
        Main.getSaver().addClan(this);

        if (Main.isDebugMode()) {
            plugin.getLogger().info(" Name : " + getName());
            plugin.getLogger().info(" DisplayName : " + getDisplayName());
            plugin.getLogger().info(" Prefix : " + getPrefix());
            plugin.getLogger().info(" Owner : " + getOwner());
            plugin.getLogger().info(" Admins : " + getAdmins());
            plugin.getLogger().info(" Members : " + getMembers());
        }
    }

    /**
     * Unregisters and deletes this Clan
     *
     * @return true if the Clan is unregistered
     */
    public boolean unregister() {
        String name = getName();
        try {
            Main.getSaver().removeClan(this);
            admin = new ArrayList<>();
            members = new ArrayList<>();
            owner = null;
            team.unregister();
        } catch (IllegalStateException e) {
            return false;
        }
        if (Main.isDebugMode())
            plugin.getLogger().info("unregisterd Clan " + name);

        return true;
    }

    /**
     * Removes an Admin from this Clan
     *
     * @param uuid The UUID of the admin
     * @return if the admins have changed
     */
    public boolean removeAdmin(UUID uuid) {
        if (Main.isDebugMode())
            plugin.getLogger().info("Clan " + getName() + " : Admin removed " + uuid);
        return admin.remove(uuid);
    }

    /**
     * Adds an Admin to this Clan if the user is a Member
     *
     * @param uuid the UUID of the Member
     * @return if the admins have changed
     */
    public boolean addAdmin(UUID uuid) {
        if (!members.contains(uuid) || admin.contains(uuid)) return false;

        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (player.getName() == null)
            return false;

        if (Main.isDebugMode())
            plugin.getLogger().info("Clan " + getName() + " : Admin added " + uuid);
        return admin.add(uuid);
    }

    /**
     * Removes a Member from this Clan
     *
     * @param uuid the UUID of the Member
     * @return if the members have changed
     */
    public boolean removeMember(UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

        admin.remove(uuid);
        members.remove(uuid);
        if (player.getName() == null)
            return false;

        if (Main.isDebugMode())
            plugin.getLogger().info("Clan " + getName() + " : Member removed " + uuid);

        return team.removeEntry(player.getName());
    }

    /**
     * Adds a Member to this Clan
     *
     * @param uuid the UUID of the Member to add
     * @return if the members have changed
     */
    public boolean addMember(UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (player.getName() == null || team.hasEntry(player.getName())) {
            return false;
        }
        members.add(uuid);
        team.addEntry(player.getName());
        return true;
    }

    /**
     * Removes a protected Block
     *
     * @param location the Location of the Block to remove
     */
    public boolean removeBlock(Location location) {
        return protectedBlocks.remove(location);
    }

    /**
     * Adds a protected Block
     *
     * @param location the Location of the Block to add
     */
    public boolean addBlock(Location location) {
        if(!protectedBlocks.contains(location)) {
            protectedBlocks.add(location);
            return true;
        }
        return false;
    }

    /**
     * Gets the Name of the Clan
     *
     * @return the Name
     */
    public String getName() {
        return team.getName();
    }

    /**
     *
     */
    public String getCleanPrefix() {
        return prefix;
    }

    /**
     * Gets the Prefix of the Clan
     *
     * @return the Prefix
     */
    public String getPrefix() {
        return team.getPrefix();
    }

    /**
     * Sets the Prefix of the Clan with [prefix]
     *
     * @param prefix the Prefix
     */
    public void setPrefix(String prefix) {
        team.setPrefix(ChatColor.translateAlternateColorCodes('&', "&r[" + prefix + "&r] "));
        this.prefix = prefix;
    }

    /**
     * Gets the DisplayName of the Clan
     *
     * @return the DisplayName
     */
    public String getDisplayName() {
        return team.getDisplayName();
    }

    /**
     * Sets the DisplayName
     *
     * @param name the DisplayName
     */
    public void setDisplayName(String name) {
        team.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
    }

    /**
     * Gets the Owner UUID if this Clan
     *
     * @return the Owner UUID
     */
    public UUID getOwner() {
        return owner;
    }

    /**
     * Sets the Owner of the Clan
     *
     * @param uuid the UUID from this Member
     * @return if the Owner was updated
     */
    public boolean setOwner(UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (player.getName() == null || !members.contains(uuid))
            return false;

        team.addEntry(player.getName());
        this.owner = uuid;
        admin.add(uuid);
        return true;
    }

    /**
     * Gets the Admins
     * Changes have no Effect
     *
     * @return the Admin List
     */
    public List<UUID> getAdmins() {
        return new ArrayList<>(admin);
    }

    /**
     * Gets the AdminNames
     * Changes have no Effect
     *
     * @return the Admin List (Names)
     */
    public List<String> getAdminNames() {
        List<String> names = new ArrayList<>();
        getAdmins().forEach(uuid -> names.add(Bukkit.getOfflinePlayer(uuid).getName()));
        return names;
    }

    /**
     * Gets the Members
     * Changes have no Effect
     *
     * @return the Member List
     */
    public List<UUID> getMembers() {
        return new ArrayList<>(members);
    }

    /**
     * Gets the MemberNames
     * Changes have no Effect
     *
     * @return the Member List (Names)
     */
    public List<String> getMemberNames() {
        List<String> names = new ArrayList<>();
        getMembers().forEach(uuid -> names.add(Bukkit.getOfflinePlayer(uuid).getName()));
        return names;
    }

    /**
     * Gets the Members without the Admins and the Owner
     * Changes have no Effect
     *
     * @return the Member List without Admins and the Owner
     */
    public List<UUID> getMembersOnly() {
        List<UUID> membersOnly = new ArrayList<>(members);
        membersOnly.removeAll(admin);
        membersOnly.remove(owner);
        return membersOnly;
    }

    /**
     * Gets the protected Blocks from this Clan
     * Changes have no Effect
     *
     * @return the Location from the protected Blocks
     */
    public List<Location> getProtectedBlocks() {
        return new ArrayList<>(protectedBlocks);
    }

    /**
     * Gets the Scoreboard Team
     *
     * @return the Team
     */
    public Team getTeam() {
        return team;
    }

    /**
     * Checks if the UUID is in the Clan
     *
     * @param uuid the UUID to Check
     * @return if the UUID is a Member of this Clan
     */
    public boolean isInClan(UUID uuid) {
        return members.contains(uuid);
    }

    /**
     * Sends this Clan info to the Player
     *
     * @param player the Player to send the Clan info
     */
    public void sendClanInfo(Player player) {

        String adminNames = "";
        for (UUID adminUUID : admin) {
            if (adminUUID.equals(owner)) continue;
            OfflinePlayer admin = Bukkit.getOfflinePlayer(adminUUID);
            adminNames += admin.getName() != null ? admin.getName() : admin.getUniqueId();
            adminNames += "\n";
        }

        String memberNames = "";
        for (UUID memberUUID : getMembersOnly()) {
            if (memberUUID.equals(owner)) continue;
            OfflinePlayer member = Bukkit.getOfflinePlayer(memberUUID);
            memberNames += member.getName() != null ? member.getName() : member.getUniqueId();
            memberNames += "\n";
        }

        String alliances = "";
        for(String clanName : alliance) {
            alliances += clanName;
            alliances += "\n";
        }

        String finalMemberNames = memberNames;
        String finalAdminNames = adminNames;
        String finalAlliances = alliances;
        OfflinePlayer offlineOwner = Bukkit.getOfflinePlayer(owner);
        String ownerName = offlineOwner.getName() != null ? offlineOwner.getName() : owner.toString();
        Main.getMessages().getMessagesList("ClanInfo").forEach((message) -> player.sendMessage(message.
                    replace("%name%", getName()).
                    replace("%displayName%", getDisplayName()).
                    replace("%prefix%", getPrefix()).
                    replace("%owner%", ownerName).
                    replace("%admins%", finalAdminNames).
                    replace("%members%", finalMemberNames).
                    replace("%alliances", finalAlliances))
        );
    }

    /**
     * Checks if a Player is a Admin in this Clan
     *
     * @param uuid the UUID from the Player
     * @return if the Player is an Admin
     */
    public boolean isAdmin(UUID uuid) {
        return admin.contains(uuid) || owner.equals(uuid);
    }

    /**
     * Adds an Alliance to this Clan
     * @param clan the second Clan
     */
    public void addAlliance(Clan clan) {
        if(!alliance.contains(clan.getName()))
            alliance.add(clan.getName());
    }

    /**
     * Removes an Alliance from this clan
     * @param clan
     */
    public void removeAlliance(Clan clan) {
        alliance.remove(clan.getName());
    }

    /**
     * Get all Alliances this Clan have
     *
     * @return a List with the Clan names
     */
    public List<String> getAlliances() {
        return new ArrayList<>(alliance);
    }

    /**
     * Checks if the Clans are in an Alliance
     *
     * @param clan the second Clan
     * @return if the Clans are in an Alliance
     */
    public boolean isInAlliance(Clan clan) {
        return alliance.contains(clan.getName());
    }

    /**
     * Gets or Creates a Team with this Name
     *
     * @param name the Name of the Team
     * @return the Team
     */
    private static Team getOrCreateTeam(String name) {
        if (Bukkit.getScoreboardManager() == null)
            throw new NullPointerException("This can't happen call the police or something \n Bukkit.getScoreboardManager() is null");

        return Bukkit.getScoreboardManager().getMainScoreboard().getTeam(name) != null ?
                Bukkit.getScoreboardManager().getMainScoreboard().getTeam(name) :
                Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam(name);
    }

    /**
     * Gets the Clan in which the Player is
     *
     * @param player the Player
     * @return the Clan or null if none was found
     */
    public static Clan getClanFromPlayer(Player player) {
        Clan playerClan = null;
        for (Clan clan : Main.getSaver().getClans()) {
            if (clan.isInClan(player.getUniqueId()))
                playerClan = clan;
        }
        return playerClan;
    }

    /**
     * Gets the Clan by his name
     *
     * @param name the Clan Name
     * @return the Clan or null if none was found
     */
    public static Clan getClanByName(String name) {
        Clan targetClan = null;
        for (Clan clan : Main.getSaver().getClans()) {
            if (clan.getName().equals(name))
                targetClan = clan;
        }
        return targetClan;
    }

    /**
     * Unregister all Clans
     *
     * @return if the Teams are unregistered
     */
    public static boolean unregisterAllTeams() {
        if (Bukkit.getScoreboardManager() == null)
            return false;

        for (Team team : Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
            team.unregister();
        }
        return true;
    }
}
