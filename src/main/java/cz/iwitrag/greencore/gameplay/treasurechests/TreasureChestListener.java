package cz.iwitrag.greencore.gameplay.treasurechests;

import cz.iwitrag.greencore.helpers.LuckPermsHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

public class TreasureChestListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        EquipmentSlot slot = event.getHand();
        if (slot == null) // May be null when stepping on pressure plate
            return;
        if (!slot.equals(EquipmentSlot.HAND)) // Main-hand, not off-hand
            return;

        Player player = event.getPlayer();
        String playerName = player.getName();
        Action action = event.getAction();
        TreasureChestManager manager = TreasureChestManager.getInstance();
        TreasureChestClickOperation operation = manager.getPlayerClickOperation(playerName);

        // May be setting something related to treasure chests
        if (action.equals(Action.LEFT_CLICK_BLOCK)) {
            if (operation != null) {
                // Never cancel left click because that would cancel blockBreak
                settingTChest(player, operation, event.getClickedBlock());
            }
        }
        // May be opening treasure chest or setting something related to treasure chests
        else if (action.equals(Action.RIGHT_CLICK_BLOCK)) {
            if (operation == null) {
                if (openingTChest(player, event.getClickedBlock()))
                    event.setCancelled(true);
            } else {
                if (settingTChest(player, operation, event.getClickedBlock()))
                    event.setCancelled(true);
            }
        }
        // May be cancelling operation related to treasure chests
        else if (action.equals(Action.LEFT_CLICK_AIR)) {
            if (cancellingOperation(player))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        TreasureChestClickOperation operation = TreasureChestManager.getInstance().getPlayerClickOperation(player.getName());
        Block block = event.getBlock();

        // May be deleting treasure chest
        if (breakingTChest(player, operation, block))
            event.setCancelled(true);
        // May be setting something related to treasure chests (possibly with GM 1)
        else if (operation != null && settingTChest(player, operation, block))
            event.setCancelled(true);
    }

    private boolean settingTChest(Player player, TreasureChestClickOperation operation, Block block) {
        String playerName = player.getName();
        TreasureChestManager manager = TreasureChestManager.getInstance();
        TreasureChestClickOperation.OperationType operationType = operation.getType();
        TreasureChest tChest = getTreasureChest(block);
        Location location = block.getLocation();

        if (operationType == TreasureChestClickOperation.OperationType.CREATE) {
            if (tChest != null) {
                player.sendMessage("§cTento blok už je nastaven jako treasure chestka");
                return true;
            }
            tChest = new TreasureChest(location, operation.getChance());
            manager.addTreasureChest(tChest);
            manager.setSelectedChest(playerName, tChest);
            player.sendMessage("§aVytvořena a vybrána nová treasure chestka");
            if (tChest.hasAnyRewards())
                player.sendMessage("§aU všech itemů byla nastavena šance §2" + operation.getChance());
            manager.unsetPlayerOperation(playerName);
        } else if (operationType == TreasureChestClickOperation.OperationType.COPY) {
            if (tChest != null) {
                player.sendMessage("§cTento blok už je nastaven jako treasure chestka");
                return true;
            }
            tChest = new TreasureChest(location, operation.getTChest());
            manager.addTreasureChest(tChest);
            player.sendMessage("§aTreasure chestka zkopírována a vybrána");
            manager.setSelectedChest(playerName, tChest);
            manager.unsetPlayerOperation(playerName);
        } else if (operationType == TreasureChestClickOperation.OperationType.DELETE) {
            if (tChest == null) {
                player.sendMessage("§cTento blok není treasure chestkou");
                return true;
            }
            if (!tChest.equals(operation.getTChest())) {
                player.sendMessage("§cToto není treasure chestka, která byla vybrána při spouštění příkazu pro odstranění");
                return true;
            }
            manager.unsetPlayerOperation(playerName);
            manager.removeTreasureChest(tChest);
            player.sendMessage("§aTreasure chestka odstraněna");
        } else if (operationType == TreasureChestClickOperation.OperationType.SELECT) {
            if (tChest == null) {
                player.sendMessage("§cTento blok není treasure chestkou");
                return true;
            }
            manager.setSelectedChest(playerName, tChest);
            player.sendMessage("§aTreasure chestka vybrána");
            manager.unsetPlayerOperation(playerName);
        }

        return true;
    }

    private boolean openingTChest(Player player, Block block) {
        TreasureChest tChest = getTreasureChest(block);
        if (tChest == null)
            return false;
        else {
            tChest.open(player);
            return true;
        }
    }

    private boolean cancellingOperation(Player player) {
        TreasureChestManager manager = TreasureChestManager.getInstance();
        String playerName = player.getName();

        if (manager.getPlayerClickOperation(playerName) != null) {
            manager.unsetPlayerOperation(playerName);
            player.sendMessage("§cOperace zrušena");
            return true;
        } else
            return false;
    }

    // TCHEST TODO - barvy v textu a formáty u component builderů se musí nějak zrušit, jinak přetrvávají a nabalují se

    private boolean breakingTChest(Player player, TreasureChestClickOperation operation, Block block) {
        TreasureChestManager manager = TreasureChestManager.getInstance();
        String playerName = player.getName();
        TreasureChest tChest = getTreasureChest(block);

        if (operation == null) {
            if (tChest != null) {
                if (LuckPermsHelper.playerHasPermission(playerName, "tchest.admin")) {
                    manager.setPlayerClickOperation(playerName, TreasureChestClickOperation.breakOperation(tChest));
                    player.sendMessage("§aChystáš se odstranit treasure chestku. Pro potvrzení ji rozbij ještě jednou");
                    if (tChest.isDropItemsOnDestroy())
                        player.sendMessage("§7§o(po rozbití z ní vypadnou všechny věci z databáze)");
                }
                return true;
            }
        } else if (operation.getType() == TreasureChestClickOperation.OperationType.BREAK) {
            if (LuckPermsHelper.playerHasPermission(playerName, "tchest.admin")) {
                if (tChest == null) {
                    player.sendMessage("§cOperace zrušena");
                    manager.unsetPlayerOperation(playerName);
                }
                else if (!tChest.equals(operation.getTChest())) {
                    player.sendMessage("§cToto není treasure chestka, která byla rozbita poprvé");
                }
                else {
                    player.sendMessage("§aTreasure chestka odstraněna");
                    block.setType(Material.AIR);
                    manager.unsetPlayerOperation(playerName);
                    if (tChest.isDropItemsOnDestroy()) {
                        for (PossibleItemReward reward : tChest.getPossibleRewards()) {
                            tChest.getLocation().getWorld().dropItemNaturally(tChest.getLocation().toCenterLocation(), reward.getItem());
                        }
                    }
                    TreasureChestManager.getInstance().removeTreasureChest(tChest);
                }
            } else {
                manager.unsetPlayerOperation(playerName);
                player.sendMessage("§cOperace zrušena, chybí oprávnění");
            }
            return true;
        }
        return false;
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        if (getTreasureChest(event.getBlock()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        if (getTreasureChest(event.getBlock()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if (getTreasureChest(event.getBlock()) != null) {
            event.setCancelled(true);
            return;
        }
        event.blockList().removeIf((block) -> getTreasureChest(block) != null);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf((block) -> getTreasureChest(block) != null);
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (getTreasureChest(event.getBlock()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        TreasureChestManager manager = TreasureChestManager.getInstance();
        manager.unsetPlayerOperation(event.getPlayer().getName());
        manager.unsetSelectedChest(event.getPlayer().getName());
    }

    private TreasureChest getTreasureChest(Block block) {
        return TreasureChestManager.getInstance().getTreasureChest(block.getLocation());
    }

}
