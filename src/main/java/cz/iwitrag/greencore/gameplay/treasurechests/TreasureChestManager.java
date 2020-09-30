package cz.iwitrag.greencore.gameplay.treasurechests;

import cz.iwitrag.greencore.storage.PersistenceManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TreasureChestManager {

    private static TreasureChestManager instance;

    private Set<TreasureChest> treasureChests = new HashSet<>();

    private Map<String, TreasureChestClickOperation> playerOperations = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private Map<String, TreasureChest> selectedTChests = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private TreasureChestManager() {}

    public static TreasureChestManager getInstance() {
        if (instance == null)
            instance = new TreasureChestManager();
        return instance;
    }

    public Set<TreasureChest> getTreasureChests() {
        return new HashSet<>(treasureChests);
    }

    public TreasureChest getTreasureChest(Location location) {
        location = location.getBlock().getLocation();
        for (TreasureChest treasureChest : treasureChests) {
            if (treasureChest.getLocation().equals(location))
                return treasureChest;
        }
        return null;
    }

    public void addTreasureChest(TreasureChest treasureChest, boolean insertToDb) {
        treasureChests.add(treasureChest);

        if (insertToDb)
            PersistenceManager.getInstance().runHibernateAsyncTask((session -> session.save(treasureChest)), true);
    }

    public void removeTreasureChest(TreasureChest treasureChest) {
        treasureChests.removeIf((ch) -> ch.equals(treasureChest));
        treasureChest.stopSpawningParticles();
        treasureChest.setHologramText(null);

        for (Iterator<Map.Entry<String, TreasureChestClickOperation>> iterator = playerOperations.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, TreasureChestClickOperation> entry = iterator.next();
            TreasureChest tChest = entry.getValue().getTChest();
            if (tChest != null && tChest.equals(treasureChest)) {
                iterator.remove();
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null && player.isOnline())
                    player.sendMessage("§cOperace zrušena, chestka byla odstraněna");
            }
        }

        selectedTChests.entrySet().removeIf(entry -> entry.getValue().equals(treasureChest));

        PersistenceManager.getInstance().runHibernateAsyncTask((session -> session.delete(treasureChest)), true);
    }

    public void purgeTreasureChests() {
        treasureChests.clear();
    }

    @Nullable
    public TreasureChestClickOperation getPlayerClickOperation(String player) {
        return playerOperations.get(player);
    }

    public void setPlayerClickOperation(String player, TreasureChestClickOperation operation) {
        playerOperations.put(player, operation);
    }

    public void unsetPlayerOperation(String player) {
        playerOperations.remove(player);
    }

    @Nullable
    public TreasureChest getSelectedChest(String player) {
        return selectedTChests.get(player);
    }

    public void setSelectedChest(String player, TreasureChest tChest) {
        selectedTChests.put(player, tChest);
    }

    public void unsetSelectedChest(String player) {
        selectedTChests.remove(player);
    }
}
