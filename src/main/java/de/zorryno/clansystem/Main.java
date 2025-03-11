package de.zorryno.clansystem;

import de.zorryno.clansystem.commands.ClanChatCommand;
import de.zorryno.clansystem.commands.ClanCommand;
import de.zorryno.clansystem.commands.ClanTabCompleter;
import de.zorryno.clansystem.listener.ChatListener;
import de.zorryno.clansystem.listener.LockEngine;
import de.zorryno.clansystem.listener.SetScoreboardListener;
import de.zorryno.clansystem.util.clans.Clan;
import de.zorryno.clansystem.util.clans.Saver;
import de.zorryno.zorrynosystems.config.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static Saver saver;
    private static Messages messages;
    private static boolean debugMode = false;

    @Override
    public void onLoad() {
        debugMode = getConfig().getBoolean("debugMode", false);
        if(debugMode)
            getLogger().info(ChatColor.DARK_RED + "DebugMode Enabled");
    }

    @Override
    public void onEnable() {
        saver = new Saver(this);
        saver.load();
        messages = new Messages("messages.yml", this);
        saveDefaultConfig();
        getCommand("clan").setExecutor(new ClanCommand(this));
        getCommand("clan").setTabCompleter(new ClanTabCompleter(this));
        getCommand("clanchat").setExecutor(new ClanChatCommand());
        Bukkit.getPluginManager().registerEvents(new ChatListener(), this);
        Bukkit.getPluginManager().registerEvents(new SetScoreboardListener(), this);
        Bukkit.getPluginManager().registerEvents(new LockEngine(this), this);
    }

    @Override
    public void onDisable() {
        saver.save();
        Clan.unregisterAllTeams();
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTasks(this);
    }

    public static Saver getSaver() {
        return saver;
    }

    public static Messages getMessages() {
        return messages;
    }

    public static boolean isDebugMode() {
        return debugMode;
    }
}