package cz.iwitrag.greencore.gameplay.zones;

import cz.iwitrag.greencore.Main;
import cz.iwitrag.greencore.gameplay.zones.flags.BlockedCommandsFlag;
import cz.iwitrag.greencore.gameplay.zones.flags.DisconnectPenaltyFlag;
import cz.iwitrag.greencore.gameplay.zones.flags.EnderPortalFlag;
import cz.iwitrag.greencore.gameplay.zones.flags.MineFlag;
import cz.iwitrag.greencore.gameplay.zones.flags.ParticlesFlag;
import cz.iwitrag.greencore.gameplay.zones.flags.TpFlag;
import cz.iwitrag.greencore.helpers.LuckPermsHelper;
import cz.iwitrag.greencore.helpers.Utils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ZoneFlagsExecutor implements Listener {

    private static ZoneFlagsExecutor instance;

    private ZoneFlagsExecutor() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), this::doParticlesFlag, 1, 1);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), this::doMineFlag, 300, 300);
    }

    public static ZoneFlagsExecutor getInstance() {
        if (instance == null)
            instance = new ZoneFlagsExecutor();
        return instance;
    }

    @EventHandler
    public void doBlockedCommandsFlag(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage();

        if (LuckPermsHelper.playerHasPermission(player.getName(), "zone.admin"))
            return;

        // Remove namespace if present, for example from /essentials:home it will make just home
        int index = command.split(" ")[0].indexOf(':');
        if (index != -1) {
            try {
                command = command.substring(index + 1);
                command = command.trim();
            } catch (IndexOutOfBoundsException ex) {
                command = "";
            }
        }

        for (Zone zone : ZoneManager.getInstance().getZonesPlayerIsInside(player)) {
            BlockedCommandsFlag flag = zone.getFlagOrDefault(BlockedCommandsFlag.class);

            // Check blocked command from more to less specific
            // For example if player command was /warp bla, it will check first for /warp bla and then for /warp
            while (!command.isEmpty()) {
                if (flag.hasCommand(command)) {
                    event.setCancelled(true);
                    player.sendMessage("§cTento příkaz je zde blokován");
                    return;
                }
                int indexx = command.lastIndexOf(" ");
                if (indexx == -1)
                    break;
                else
                    command = command.substring(0, indexx).trim();
            }
        }
    }

    private Map<String, String> notifyAboutPenalty = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    @EventHandler(priority = EventPriority.LOWEST) // Important, Authme or Essentials changes position of player before he actually quits
    public void doDisconnectPenaltyFlag(PlayerQuitEvent event) {
        Player penalizedPlayer = event.getPlayer();

        if (LuckPermsHelper.playerHasPermission(penalizedPlayer.getName(), "zone.admin"))
            return;

        double biggestPenalty = 0.0;
        for (Zone zone : ZoneManager.getInstance().getZonesPlayerIsInside(penalizedPlayer)) {
            double zonePenalty = zone.getFlagOrDefault(DisconnectPenaltyFlag.class).getPenalty();
            if (zonePenalty > biggestPenalty) {
                biggestPenalty = zonePenalty;
            }
        }
        PlayerInventory inventory = penalizedPlayer.getInventory();
        List<ItemStack> removed = new ArrayList<>();
        if (biggestPenalty > 0) {
            // Remove items from inventory and save them
            for (int i = 0; i < 41; i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null && item.getAmount() > 0 && item.getType() != Material.AIR) {
                    int removedAmount = 0;
                    for (int j = 0; j < item.getAmount(); j++) {
                        if (Utils.chance(biggestPenalty))
                            removedAmount++;
                    }
                    if (removedAmount > 0) {
                        ItemStack removedItemStack = new ItemStack(item);
                        removedItemStack.setAmount(removedAmount);
                        ItemStack keepItemStack = new ItemStack(item);
                        keepItemStack.setAmount(item.getAmount() - removedAmount);

                        removed.add(removedItemStack);
                        inventory.setItem(i, keepItemStack);
                    }
                }
            }
            if (!removed.isEmpty()) {
                // Drop removed items
                Location loc = penalizedPlayer.getLocation();
                for (ItemStack droppedItem : removed) {
                    loc.getWorld().dropItemNaturally(loc, droppedItem);
                }
                // Inform nearby players
                for (Player informedPlayer : Bukkit.getOnlinePlayers()) {
                    Location informedPlayerLoc = informedPlayer.getLocation();
                    if (informedPlayer != penalizedPlayer && informedPlayerLoc.getWorld().equals(loc.getWorld()) && informedPlayerLoc.distance(loc) <= 10) {
                        informedPlayer.sendMessage("§6Hráč §e" + penalizedPlayer.getName() + " §6se odpojil a vypadly mu nějaké věci!");
                    }
                }
                // Save message to inform penalized player after join
                List<String> itemNames = new ArrayList<>();
                for (ItemStack item : removed) {
                    if (item.getItemMeta().hasDisplayName())
                        itemNames.add(item.getAmount() + "x " + item.getItemMeta().getDisplayName());
                    else
                        itemNames.add(item.getAmount() + "x " + item.getI18NDisplayName());
                }
                String message = "§6Po tvém odpojení ti z inventáře vypadly tyto věci: §e" + StringUtils.join(itemNames, "§6, §e");
                notifyAboutPenalty.put(event.getPlayer().getName(), message);
            }
        }
    }

    @EventHandler
    public void informAboutPenaltyFlag(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String message = notifyAboutPenalty.getOrDefault(player.getName(), null);
        if (message != null) {
            player.sendMessage(message);
            notifyAboutPenalty.remove(player.getName());
        }
    }

    @EventHandler
    public void doEnderPortalFlag(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.END_GATEWAY || event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            for (Zone zone : ZoneManager.getInstance().getZonesPlayerIsInside(event.getPlayer())) {
                if (!zone.getFlagOrDefault(EnderPortalFlag.class).isEnderPortalEnabled()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    private void doParticlesFlag() {
        z: for (Zone zone : ZoneManager.getInstance().getZones()) {
            ParticlesFlag flag = zone.getFlagOrDefault(ParticlesFlag.class);
            if (flag.getParticle() != null && flag.getDensity() > 0) {
                double zoneSize = zone.getSize();
                int multiplier;
                double divider;
                if (zoneSize < 1000.0) {
                    multiplier = flag.getDensity();
                    divider = 1.0+(20.0-(zoneSize/50.0));
                } else {
                    multiplier = (int)(flag.getDensity() * ((double)zone.getSize() / 1000.0));
                    divider = 1.0;
                }
                for (int i = 0; i < multiplier; i++) {
                    if (Utils.chance(100.00/divider)) {
                        try {
                            zone.getPoint1().getWorld().spawnParticle(flag.getParticle(), zone.getRandomPoint(),
                                    1, 0.0, 0.0, 0.0, 0.0,
                                    Utils.isParticleColorizable(flag.getParticle()) ? new Particle.DustOptions(Color.fromRGB(flag.getColor().getRed(), flag.getColor().getGreen(), flag.getColor().getBlue()), 1) : null);
                        } catch (IllegalArgumentException e) {
                            Main.getInstance().getLogger().severe("Error with ParticlesFlag (" + flag.getParticle().name() + ") of zone " + zone.getName() + " - " + e.getMessage());
                            continue z;
                        }
                    }
                }
            }
        }
    }

    private void doMineFlag() {
        for (Zone zone : ZoneManager.getInstance().getZones()) {
            MineFlag mineFlag = zone.getFlagOrDefault(MineFlag.class);
            if (mineFlag.hasBlocks() && mineFlag.getRegenPercentage() < 100.00) {
                double blocksExist = 0;
                double blocksMax = zone.getSize();
                for (double x = zone.getPoint1().getX(); x <= zone.getPoint2().getX(); x++) {
                    for (double y = zone.getPoint1().getY(); y <= zone.getPoint2().getY(); y++) {
                        for (double z = zone.getPoint1().getZ(); z <= zone.getPoint2().getZ(); z++) {
                            for (MineFlag.MineBlock mineBlock : mineFlag.getBlocks()) {
                                if (mineBlock.getBlockType() == (zone.getPoint1().getWorld().getBlockAt((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z)).getType())) {
                                    blocksExist++;
                                    break;
                                }
                            }
                        }
                    }
                }
                if ((blocksExist/blocksMax)*100 <= mineFlag.getRegenPercentage() && blocksExist != blocksMax) {
                    if (zone.hasFlag(TpFlag.class)) {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (ZoneManager.getInstance().isPlayerInsideZone(player, zone)) {
                                player.teleport(zone.getFlagOrDefault(TpFlag.class).getLocation());
                                player.sendMessage("§aDošlo k doplnění bloků a proto proběhl teleport na bezpečné místo");
                            }
                        }
                    }
                    for (double x = zone.getPoint1().getX(); x <= zone.getPoint2().getX(); x++) {
                        for (double y = zone.getPoint1().getY(); y <= zone.getPoint2().getY(); y++) {
                            for (double z = zone.getPoint1().getZ(); z <= zone.getPoint2().getZ(); z++) {
                                zone.getPoint1().getWorld().getBlockAt((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z)).setType(mineFlag.pickOneBlock());
                            }
                        }
                    }
                }
            }
        }
    }



}
