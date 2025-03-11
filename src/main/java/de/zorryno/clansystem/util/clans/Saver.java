package de.zorryno.clansystem.util.clans;

import de.zorryno.clansystem.Main;
import de.zorryno.zorrynosystems.config.Config;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class Saver {
    private List<Clan> clans;
    private Config config;
    private Plugin plugin;

    public Saver(Plugin plugin) {
        this.plugin = plugin;
        config = new Config("Clans.yml", plugin);
        clans = new ArrayList<>();
    }

    public void addClan(Clan clan) {
        if (Main.isDebugMode())
            plugin.getLogger().info("Addet Clan " + clan.getName());
        clans.add(clan);
    }

    public void removeClan(Clan clan) {
        if (Main.isDebugMode())
            plugin.getLogger().info("Removed Clan " + clan.getName());
        clans.remove(clan);
    }

    public List<Clan> getClans() {
        return new ArrayList<>(clans);
    }

    public void save() {
        config.reset();
        if (Main.isDebugMode())
            plugin.getLogger().info("Total Clans: " + clans.size());

        for (Clan clan : clans) {
            List<String> admins = new ArrayList<>();
            for (UUID uuid : clan.getAdmins())
                admins.add(uuid.toString());

            List<String> members = new ArrayList<>();
            for (UUID uuid : clan.getMembers())
                members.add(uuid.toString());



            ConfigurationSection section = config.getConfig().createSection(clan.getTeam().getName());
            section.set("Owner", clan.getOwner().toString());
            section.set("DisplayName", clan.getDisplayName());
            section.set("Prefix", clan.getPrefix());
            section.set("Admins", admins);
            section.set("Members", members);
            ConfigurationSection locations = section.createSection("Blocks");

            Iterator<Location> iterator = clan.getProtectedBlocks().iterator();
            for (int i = 0; iterator.hasNext(); i++) {
                locations.set(i + "", iterator.next());
            }

            section.set("Alliance", clan.getAlliances());
            if (Main.isDebugMode())
                plugin.getLogger().info("Clan " + clan.getName() + " successfully saved");
        }
        config.save();
    }

    public void load() {
        Set<String> keys = config.getConfig().getKeys(false);
        if (Main.isDebugMode())
            plugin.getLogger().info("Total Clans: " + keys.size());

        for (String key : keys) {
            ConfigurationSection section = config.getConfig().getConfigurationSection(key);
            UUID owner = UUID.fromString(section.getString("Owner"));
            String name = key;
            String displayName = section.getString("DisplayName");
            String prefix = section.getString("Prefix");

            List<UUID> admins = new ArrayList<>();
            for (String uuid : section.getStringList("Admins"))
                admins.add(UUID.fromString(uuid));

            List<UUID> members = new ArrayList<>();
            for (String uuid : section.getStringList("Members"))
                members.add(UUID.fromString(uuid));

            List<Location> protectedBlocks = new ArrayList<>();
            ConfigurationSection locations = section.getConfigurationSection("Blocks");
            if(locations != null) {
                for (String locationKey : locations.getKeys(false))
                    protectedBlocks.add(locations.getSerializable(locationKey, Location.class));
            }

            List<String> alliance = section.getStringList("Alliance");

            Clan clan = new Clan(plugin, owner, name, displayName, prefix, admins, members, protectedBlocks, alliance);

            if (Main.isDebugMode())
                plugin.getLogger().info("Clan " + clan.getName() + " successfully loaded");
        }
    }

    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        clans.forEach((clan -> names.add(clan.getName())));
        return names;
    }

    public Clan getClanByName(String name) {
        for (Clan clan : clans) {
            if (clan.getName().equals(name))
                return clan;
        }
        return null;
    }
}
