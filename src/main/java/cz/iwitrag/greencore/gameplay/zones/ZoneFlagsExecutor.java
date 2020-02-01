package cz.iwitrag.greencore.gameplay.zones;

import cz.iwitrag.greencore.Main;
import cz.iwitrag.greencore.gameplay.zones.flags.EnderPortalFlag;
import cz.iwitrag.greencore.gameplay.zones.flags.MineFlag;
import cz.iwitrag.greencore.gameplay.zones.flags.ParticlesFlag;
import cz.iwitrag.greencore.gameplay.zones.flags.TpFlag;
import cz.iwitrag.greencore.helpers.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class ZoneFlagsExecutor implements Listener {

    private static ZoneFlagsExecutor instance;

    private ZoneFlagsExecutor() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), this::doParticlesFlag, 1, 1);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), this::doMineFlag, 15, 15);
    }

    public static ZoneFlagsExecutor getInstance() {
        if (instance == null)
            instance = new ZoneFlagsExecutor();
        return instance;
    }

    @EventHandler
    public void doEnderPortalFlag(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.END_GATEWAY || event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            for (Zone zone : ZoneManager.getInstance().getZones()) {
                if (!zone.getFlagOrDefault(EnderPortalFlag.class).isEnderPortalEnabled() && ZoneExecutor.getInstance().isPlayerInsideZone(event.getPlayer(), zone)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private void doParticlesFlag() {
        for (Zone zone : ZoneManager.getInstance().getZones()) {
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
                        zone.getPoint1().getWorld().spawnParticle(flag.getParticle(), zone.getRandomPoint(),
                                1, 0.0, 0.0, 0.0, 0.0,
                                Utils.isParticleColorizable(flag.getParticle()) ? new Particle.DustOptions(Color.fromRGB(flag.getRed(), flag.getGreen(), flag.getBlue()), 1) : null);
                    }
                }
            }
        }
    }

    private void doMineFlag() {
        for (Zone zone : ZoneManager.getInstance().getZones()) {
            MineFlag mineFlag = zone.getFlagOrDefault(MineFlag.class);
            if (mineFlag.hasBlocks() && mineFlag.getRegenPercentage() < 100.00) {
                double x = zone.getPoint1().getX();
                double y = zone.getPoint1().getY();
                double z = zone.getPoint1().getZ();
                double blocksExist = 0;
                double blocksMax = zone.getSize();
                while (x < zone.getPoint2().getX()) {
                    while (y < zone.getPoint2().getY()) {
                        while (z < zone.getPoint2().getZ()) {
                            for (MineFlag.MineBlock mineBlock : mineFlag.getBlocks()) {
                                if (mineBlock.getBlockType().equals(zone.getPoint1().getWorld().getBlockAt((int)x, (int)y, (int)z).getType())) {
                                    blocksExist++;
                                    break;
                                }
                            }
                            z++;
                        }
                        y++;
                    }
                    x++;
                }
                if (blocksExist / blocksMax <= mineFlag.getRegenPercentage()) {
                    if (zone.hasFlag(TpFlag.class)) {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (ZoneExecutor.getInstance().isPlayerInsideZone(player, zone)) {
                                player.teleport(zone.getFlagOrDefault(TpFlag.class).getTpLocation());
                                player.sendMessage("§aDošlo k doplnění bloků a proto proběhl teleport na bezpečné místo");
                            }
                        }
                    }
                    while (x < zone.getPoint2().getX()) {
                        while (y < zone.getPoint2().getY()) {
                            while (z < zone.getPoint2().getZ()) {
                                zone.getPoint1().getWorld().getBlockAt((int)x, (int)y, (int)z).setType(mineFlag.pickOneBlock());
                                z++;
                            }
                            y++;
                        }
                        x++;
                    }
                }
            }
        }
    }



}
