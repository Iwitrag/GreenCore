package cz.iwitrag.greencore.helpers;

import cz.iwitrag.greencore.Main;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;

public class WorldManager {

    private World world;

    public WorldManager(String worldName, int worldSizeFromCenter) {
        world = Bukkit.getWorld(worldName);
        if (world == null) {
            Main.getInstance().getLogger().info("World " + worldName + " is not loaded! Loading and applying settings...");
            world = Bukkit.createWorld(new WorldCreator(worldName));
            if (world == null) {
                Main.getInstance().getLogger().severe("Failed to load (or create) world " + worldName + ".");
                return;
            }
        }
        else {
            Main.getInstance().getLogger().info("World " + worldName + " is already loaded. Applying settings...");
        }

        applyDefaultGameRules();

        world.getWorldBorder().setCenter(0.0, 0.0);
        world.getWorldBorder().setSize(worldSizeFromCenter*2);
    }

    private void applyDefaultGameRules() {
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
        world.setGameRule(GameRule.DISABLE_ELYTRA_MOVEMENT_CHECK, true);
        world.setGameRule(GameRule.DISABLE_RAIDS, true);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        world.setGameRule(GameRule.DO_ENTITY_DROPS, true);
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setGameRule(GameRule.DO_LIMITED_CRAFTING, false);
        world.setGameRule(GameRule.DO_MOB_LOOT, true);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
        world.setGameRule(GameRule.DO_TILE_DROPS, true);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, true);
        world.setGameRule(GameRule.KEEP_INVENTORY, false);
        world.setGameRule(GameRule.LOG_ADMIN_COMMANDS, true);
        world.setGameRule(GameRule.MAX_COMMAND_CHAIN_LENGTH, 65536);
        world.setGameRule(GameRule.MAX_ENTITY_CRAMMING, 24);
        world.setGameRule(GameRule.MOB_GRIEFING, true);
        world.setGameRule(GameRule.NATURAL_REGENERATION, true);
        world.setGameRule(GameRule.RANDOM_TICK_SPEED, 3);
        world.setGameRule(GameRule.REDUCED_DEBUG_INFO, false);
        world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, true);
        world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
        world.setGameRule(GameRule.SPAWN_RADIUS, 0);
        world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, true);
    }

    public <T> T getGameRule(GameRule<T> gameRule) {
        return world.getGameRuleValue(gameRule);
    }

    public <T> void setGameRule(GameRule<T> gameRule, T newValue) {
        world.setGameRule(gameRule, newValue);
    }

    public void setTime(long time) {
        world.setTime(time);
    }

    public static boolean isSurvivalWorld(World world) {
        return world.getName().equalsIgnoreCase("world") ||
                world.getName().equalsIgnoreCase("world_nether") ||
                world.getName().equalsIgnoreCase("world_the_end");
    }

}
