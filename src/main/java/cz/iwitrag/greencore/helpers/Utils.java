package cz.iwitrag.greencore.helpers;

import cz.iwitrag.greencore.gameplay.chat.ChatUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Pattern;
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

    /**
     * Replaces all placeholders in input
     * @param words Words to replace, case insensitive
     * @param margins What margins to use, must be 1 char or 2 chars long strings (left and right margin)
     * @param input String to look for words
     * @param replacement What to replace it for
     * @return Input with replacements
     */
    public static String replacePlaceholders(String[] words, String[] margins, String input, String replacement) {
        List<String> patterns = preparePatterns(words, margins);

        for (String pattern : patterns) {
            // (?i) makes it case insensitive
            input = input.replaceAll("(?i)" + pattern, replacement);
        }
        return input;
    }

    /** Replaces all placeholders in input, uses default margins "{}", "%%", "[]"
     * @param words Words to replace, case insensitive
     * @param input String to look for words
     * @param replacement What to replace it for
     * @return Input with replacements
     */
    public static String replacePlaceholders(String[] words, String input, String replacement) {
        return replacePlaceholders(words, new String[]{"{}", "%%", "[]"}, input, replacement);
    }

    public static BaseComponent[] replacePlaceholders(String[] words, String[] margins, String input, BaseComponent[] replacement) {
        List<String> patterns = preparePatterns(words, margins);
        StringBuilder pattern = new StringBuilder("(?i)(");
        for (int i = 0; i < patterns.size(); i++) {
            pattern.append(patterns.get(i));
            if (i < patterns.size()-1)
                pattern.append("|");
        }
        pattern.append(")");
        String[] split = input.split(pattern.toString());

        ComponentBuilder builder = new ComponentBuilder("");
        if (split.length == 1) {
            // No placeholders found (split removed nothing)
            if (input.equals(split[0]))
                builder.append(input);
            // Entire input was just placeholder
            else if (split[0].isEmpty())
                builder.append(replacement);
            // Placeholder was in the end
            else if (input.startsWith(split[0])) {
                builder.append(split[0]);
                builder.append(replacement);
            }
            // Placeholder was in the beginning
            else if (input.endsWith(split[0])) {
                builder.append(replacement);
                builder.append(split[0]);
            }
            // Placeholder was in the beginning and in the end
            else {
                builder.append(replacement);
                builder.append(split[0]);
                builder.append(replacement);
            }
        } else {
            for (int i = 0; i < split.length; i++) {
                builder.appendLegacy(split[i]);
                if (i < split.length - 1)
                    builder.append(replacement);
            }
        }
        return builder.create();
    }

    public static BaseComponent[] replacePlaceholders(String[] words, String input, BaseComponent[] replacement) {
        return replacePlaceholders(words, new String[]{"{}", "%%", "[]"}, input, replacement);
    }

    private static List<String> preparePatterns(String[] words, String[] margins) {
        List<String> patterns = new ArrayList<>();
        for (String word : words) {
            for (String margin : margins) {
                if (margin.isEmpty())
                    continue;
                char left = margin.charAt(0);
                char right = margin.length() == 1 ? left : margin.charAt(1);
                patterns.add(Pattern.quote(left + word + right));
            }
        }
        return patterns;
    }

    public static String chatColorToAlternativeColorCodes(char altColorChar, String textToTranslate) {
        for (ChatColor chatColor : ChatColor.values()) {
            textToTranslate = textToTranslate.replaceAll(
                    Pattern.quote(chatColor.toString()),
                    String.valueOf(altColorChar) + chatColor.getChar());
        }
        return textToTranslate;
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

    public static BaseComponent[] getItemStacksForChat(Collection<ItemStack> items, boolean withAmounts, ChatColor separatorColor, BaseComponent[] prefix, BaseComponent[] suffix) {
        Set<ItemStack> itemStacks = mergeItemStacks(items);
        ComponentBuilder builder = new ComponentBuilder("").appendLegacy("§r");
        if (itemStacks.isEmpty())
            builder.appendLegacy("§cNic");
        else {
            for (Iterator<ItemStack> iterator = itemStacks.iterator(); iterator.hasNext(); ) {
                ItemStack itemStack = iterator.next();
                if (prefix != null)
                    builder.append(prefix);
                builder.append(ChatUtils.getItemableText(withAmounts, itemStack));
                if (suffix != null)
                    builder.append(suffix);
                if (iterator.hasNext())
                    builder.appendLegacy(separatorColor + ", §r");
            }
        }
        return builder.create();
    }

    public static Set<ItemStack> mergeItemStacks(Collection<ItemStack> items) {
        Set<ItemStack> merged = new HashSet<>();
        for (ItemStack item : items) {
            ItemStack existingItem = merged.stream().filter((i) -> i.isSimilar(item)).findAny().orElse(null);
            if (existingItem == null)
                merged.add(item);
            else
                existingItem.setAmount(existingItem.getAmount() + item.getAmount());
        }
        return merged;
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

    public static double randomDoubleBetween(double lowerInclusive, double upperExclusive) {
        return Math.random() * (upperExclusive - lowerInclusive) + lowerInclusive;
    }
    public static int randomIntBetween(int lowerInclusive, int upperExclusive) {
        return (int) Math.floor(randomDoubleBetween(lowerInclusive, upperExclusive));
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

    public static String locationToString(Location location, boolean yawAndPitch, boolean worldName) {
        List<String> parts = new ArrayList<>();
        parts.add("X: " + twoDecimal(location.getX()));
        parts.add("Y: " + twoDecimal(location.getY()));
        parts.add("Z: " + twoDecimal(location.getZ()));
        if (yawAndPitch) {
            parts.add("Yaw: " + twoDecimal(location.getYaw()));
            parts.add("Pitch: " + twoDecimal(location.getPitch()));
        }
        return StringUtils.join(parts, ", ") + (worldName ? (" (" + location.getWorld().getName() + ")") : "");
    }

    public static boolean isSurvivalWorld(World world) {
        return (Arrays.asList("world", "world_nether", "world_the_end").contains(world.getName().toLowerCase()));
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
