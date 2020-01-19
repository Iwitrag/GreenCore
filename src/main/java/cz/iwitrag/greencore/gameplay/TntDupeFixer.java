package cz.iwitrag.greencore.gameplay;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import cz.iwitrag.greencore.Main;
import cz.iwitrag.greencore.helpers.DependenciesProvider;
import cz.iwitrag.greencore.helpers.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class TntDupeFixer {

    public TntDupeFixer() {
        DependenciesProvider.getInstance().getProtocolLib().addPacketListener(
                new PacketAdapter(Main.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Server.SPAWN_ENTITY) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        Entity entity = event.getPacket().getEntityModifier(event).read(0);
                        if (entity.getType() == EntityType.PRIMED_TNT) {
                            Location loc = entity.getLocation();
                            World world = loc.getWorld();
                            if (world != null) {
                                Block centerBlock = world.getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                                for (Block block : Utils.getBlockDirectNeighbours(centerBlock)) {
                                    if (block.getType().equals(Material.SLIME_BLOCK)) {
                                        event.setCancelled(true);
                                        entity.remove();
                                        world.spawnParticle(Particle.EXPLOSION_HUGE, loc, 3);
                                        for (Player player : loc.getNearbyPlayers(10)) {
                                            player.sendMessage("§cNa tomto serveru je používání TNT u Slime blocků zakázáno.");
                                            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
                                                player.stopSound(Sound.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS);
                                                player.playSound(loc, Sound.ITEM_TOTEM_USE, SoundCategory.BLOCKS, (float)0.8, (float)1.0);
                                            }, 3);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
        );
    }

}
