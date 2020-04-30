package cz.iwitrag.greencore.helpers;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Utils {

    private Utils() {}

    /** Converts for example 1.6789 to 1.67 */
    public static double twoDecimal(double d) {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.DOWN);
        return Double.parseDouble(df.format(d));
    }

    /** Converts for example 1.6789 to 1.67 */
    public static float twoDecimal(float d) {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.DOWN);
        return Float.parseFloat(df.format(d));
    }

    /** Converts int to hexadecimal format with trailing zeros */
    public static String intToFullHex(int n) {
        return String.format("%1$02X", n);
    }

    /** Sends empty lines to player */
    public static void sendEmptyLines(Player player, int amount) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < amount; i++) {
            builder.append(" \n");
        }
        player.sendMessage(builder.toString());
    }

    /** Sends empty lines to player */
    public static void clearChat(Player player) {
        sendEmptyLines(player, 100);
    }

    /** Returns location centered on block<br />
     *  Yaw and Pitch will be kept */
    public static Location getCenteredLocation(Location location, boolean centerY) {
        float pitch = location.getPitch();
        float yaw = location.getYaw();
        Location result = location.getBlock().getLocation().add(0.5, centerY ? 0.5 : 0.0, 0.5);
        result.setPitch(pitch);
        result.setYaw(yaw);
        return result;
    }

    /** Checks whether the player has empty inventory (including armor slots) */
    public static boolean hasEmptyInventory(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item.getType() != Material.AIR)
                return false;
        }
        return true;
    }

    public static boolean chance(double percent) {
        return Math.random()*100.00 < percent;
    }

    public static <T> T pickRandomElement(Collection<T> collection) {
        int random = (int) (Math.random()*collection.size());
        int i = 0;
        for (T object : collection) {
            if (i == random)
                return object;
            i++;
        }
        return null;
    }

    public static Set<Block> getBlockDirectNeighbours(Block block) {
        Set<Block> set = new HashSet<>();
        if (block != null) {
            set.add(block.getRelative(BlockFace.DOWN));
            set.add(block.getRelative(BlockFace.EAST));
            set.add(block.getRelative(BlockFace.NORTH));
            set.add(block.getRelative(BlockFace.SOUTH));
            set.add(block.getRelative(BlockFace.UP));
            set.add(block.getRelative(BlockFace.WEST));
        }
        return set;
    }

    public static boolean isParticleColorizable(Particle particle) {
        if (particle == null) return false;
        return particle.equals(Particle.REDSTONE);
    }

    // based on https://stackoverflow.com/a/2904266/2872536
    public static <K, V> Set<K> getKeysByValue(Map<K, V> map, V value) {
        return map.entrySet()
                .stream()
                .filter(entry -> Objects.equals(entry.getValue(), value))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public static ItemStack stringToItemStack(String input) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(input);
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            Bukkit.getLogger().warning("Got this input:");
            Bukkit.getLogger().warning(input);
            return null;
        }
        return config.getItemStack("item", null);
    }

    public static String itemStackToString(ItemStack input) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("item", input);
        return config.saveToString();
    }

    public static String itemStackToJson(ItemStack input) {
        net.minecraft.server.v1_15_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(input);
        net.minecraft.server.v1_15_R1.NBTTagCompound compound = new NBTTagCompound();
        nmsItemStack.save(compound);
        return compound.toString();
    }

    public static BaseComponent[] itemStackToBaseComponents(ItemStack input) {
        return new BaseComponent[]{new TextComponent(itemStackToJson(input))};
    }

    public static double ticksToSeconds(int ticks) {
        return (double)ticks/20.0;
    }

    public static int secondsToTick(double seconds) {
        return (int)Math.round(seconds*20.0);
    }

}
