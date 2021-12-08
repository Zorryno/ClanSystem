package de.zorryno.clansystem.util.clans;

import de.zorryno.clansystem.Main;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.UUID;

/**
 * Represents a ClanInvite
 * @see Clan
 */
public class ClanInvite implements Listener {
    private static HashMap<UUID, ClanInvite> invites = new HashMap<>();
    private final Clan clan;
    private final Player inviter;
    private final Player invitedPlayer;
    private final Plugin plugin;
    private boolean accepted;
    private long aliveTicks;

    /**
     * Creates a new {@link ClanInvite}
     *
     * @param clan the {@link Clan} this invite belongs to
     * @param invitedPlayer the {@link Player} that should be invited
     * @param plugin {@link Plugin} witch creates this invite
     * @param aliveTicks the ticks this Invite should be alive
     */
    public ClanInvite(Clan clan, Player inviter, Player invitedPlayer, Plugin plugin, long aliveTicks) {
        this.clan = clan;
        this.inviter = inviter;
        this.invitedPlayer = invitedPlayer;
        this.plugin = plugin;
        this.aliveTicks = aliveTicks;
    }


    /**
     * Get the {@link Clan} this {@link ClanInvite} belongs to
     *
     * @return the {@link Clan}
     */
    public Clan getClan() {
        return clan;
    }

    /**
     * Gets the {@link Player} witch sends this {@link ClanInvite}
     *
     * @return the {@link Player}
     */
    public Player getInviter() {
        return inviter;
    }

    /**
     * Gets the Invited {@link Player}
     *
     * @return the {@link Player}
     */
    public Player getInvitedPlayer() {
        return invitedPlayer;
    }

    /**
     * Shows if this {@link ClanInvite} is accepted or denied
     *
     * @return if this {@link ClanInvite} is accepted or {@code null} if this {@link ClanInvite} is open
     */
    public boolean isAccepted() {
        return accepted;
    }

    /**
     * Accept this {@link ClanInvite}
     */
    public void accept() {
        invites.remove(invitedPlayer.getUniqueId());
        clan.addMember(invitedPlayer.getUniqueId());
        this.accepted = true;
    }

    /**
     * Deny this {@link ClanInvite}
     */
    public void deny() {
        invites.remove(invitedPlayer.getUniqueId());
        this.accepted = false;
    }

    /**
     * Sends this {@link ClanInvite} to the {@link Player}
     */
    public void sendInvite() {
        TextComponent accept = new TextComponent(Main.getMessages().getCache().get("ClanInvite.Accept"));
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan accept"));

        TextComponent deny = new TextComponent(Main.getMessages().getCache().get("ClanInvite.Deny"));
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan deny"));

        TextComponent message = new TextComponent(Main.getMessages().getCache().get("ClanInvite.Message").replace("%clan%", clan.getName()));
        message.addExtra("\n");
        message.addExtra(accept);
        message.addExtra(" §r| ");
        message.addExtra(deny);

        invitedPlayer.spigot().sendMessage(message);

        Bukkit.getScheduler().runTask(plugin, () -> invites.put(invitedPlayer.getUniqueId(), this));
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> invites.remove(invitedPlayer.getUniqueId()) ,aliveTicks);
    }

    /**
     * Gets open {@link ClanInvite}s from this {@link Player}{@link UUID}
     *
     * @param uuid the {@link UUID} from the {@link Player}
     * @return the {@link ClanInvite} or {@code null} if no {@link ClanInvite} exists
     */
    public static ClanInvite getInvite(UUID uuid) {
        return invites.get(uuid);
    }
}
