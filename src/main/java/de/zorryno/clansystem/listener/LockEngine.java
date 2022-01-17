package de.zorryno.clansystem.listener;

import de.zorryno.clansystem.Main;
import de.zorryno.clansystem.util.clans.Clan;
import org.bukkit.Location;
import org.bukkit.block.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LockEngine implements Listener {
    private Plugin plugin;

    public LockEngine(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onOpen(PlayerInteractEvent event) {
        if(event.getClickedBlock() == null || !(event.getClickedBlock().getState() instanceof Container))
            return;

        Clan clan = getOwnerClan(event.getClickedBlock().getLocation());

        if(clan == null)
            return;

        Clan playerClan = Clan.getClanFromPlayer(event.getPlayer());

        if(playerClan == null || (!clan.equals(playerClan) && !event.getPlayer().isOp() && !clan.isInAlliance(playerClan))) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Main.getMessages().getCache().get("ProtectedBlock.NoPermission").replace("%clan%", clan.getDisplayName()));
            return;
        }

    }

    @EventHandler
    public void onBlockExplosion(EntityExplodeEvent event) {
        for(Block block : event.blockList()) {
            if(getOwnerClan(block.getLocation()) != null) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(!(event.getBlock().getState() instanceof Container))
            return;

        Clan clan = getOwnerClan(event.getBlock().getLocation());

        if(clan == null)
            return;

        if(!clan.equals(Clan.getClanFromPlayer(event.getPlayer())) && !event.getPlayer().isOp()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Main.getMessages().getCache().get("ProtectedBlock.NoPermission").replace("%clan%", clan.getDisplayName()));
            return;
        }

        clan.removeBlock(event.getBlock().getLocation());
    }

    private List<Block> getBlocksAround(Block block) {
        List<Block> blocks = new ArrayList<>();
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};
        for(BlockFace face : faces)
            blocks.add(block.getRelative(face));
        return blocks;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Clan clan = Clan.getClanFromPlayer(event.getPlayer());
        if(!(event.getBlock().getState() instanceof Container))
            return;

        Iterator<Block> iterator = getBlocksAround(event.getBlock()).iterator();
        iterator.forEachRemaining(block -> {
            Clan blockClan = LockEngine.getOwnerClan(block.getLocation());
            if(blockClan != null && !blockClan.equals(clan)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Main.getMessages().getCache().get("ProtectedBlock.NoPermission").replace("%clan%", blockClan.getDisplayName()));
            }
        });

        if(clan == null)
            return;

        clan.addBlock(event.getBlock().getLocation());
    }

    public static Clan getOwnerClan(Location location) {
        for(Clan clan : Main.getSaver().getClans()) {
            if(clan.getProtectedBlocks().contains(location))
                return clan;
        }
        return null;
    }
}
