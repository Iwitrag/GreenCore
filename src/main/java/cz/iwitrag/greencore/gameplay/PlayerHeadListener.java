package cz.iwitrag.greencore.gameplay;

import cz.iwitrag.greencore.Main;
import cz.iwitrag.greencore.helpers.LuckPermsHelper;
import cz.iwitrag.greencore.helpers.PermGroupNames;
import cz.iwitrag.greencore.helpers.TaskChainHelper;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import static cz.iwitrag.greencore.helpers.PlayerHeadManager.getPlayerHead;

public class PlayerHeadListener implements Listener {

    @EventHandler
    public void onPlayerHeadClick(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !event.isBlockInHand() && event.getHand() != null && event.getHand().equals(EquipmentSlot.HAND)) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && (clickedBlock.getType().equals(Material.PLAYER_HEAD) || clickedBlock.getType().equals(Material.PLAYER_WALL_HEAD))) {
                OfflinePlayer owner = ((Skull)clickedBlock.getState()).getOwningPlayer();
                if (owner != null && owner.getName() != null) {
                    TaskChainHelper.newChain()
                    .asyncFirst(() -> LuckPermsHelper.getPlayerPrimaryGroup(owner.getName()))
                    .syncLast((group) -> {
                        String role = "hráče";
                        if (group != null) {
                            String groupName = group.getName().toLowerCase();
                            if (PermGroupNames.owner().contains(groupName))
                                role = "majitele";
                            else if (PermGroupNames.admin().contains(groupName))
                                role = "admina";
                            else if (PermGroupNames.mod().contains(groupName))
                                role = "moderátora";
                            else if (PermGroupNames.builder().contains(groupName))
                                role = "buildera";
                            else if (PermGroupNames.anyHelper().contains(groupName))
                                role = "helpera";
                            else if (PermGroupNames.youtuber().contains(groupName))
                                role = "YouTubera";
                            else if (PermGroupNames.hero().contains(groupName))
                                role = "hrdiny";
                            else if (PermGroupNames.vipPlus().contains(groupName))
                                role = "VIP+ hráče";
                            else if (PermGroupNames.basicVip().contains(groupName))
                                role = "VIP hráče";
                        }
                        event.getPlayer().sendMessage("§7Toto je hlava " + role + " §f" + owner.getName());
                    })
                    .execute();
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerHeadBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Block brokenBlock = event.getBlock();
        if ((brokenBlock.getType().equals(Material.PLAYER_HEAD) || brokenBlock.getType().equals(Material.PLAYER_WALL_HEAD)) &&
                event.isDropItems() && event.getPlayer().getGameMode().equals(GameMode.SURVIVAL)) {
            ItemStack head = getPlayerHead(brokenBlock);
            event.setDropItems(false);
            brokenBlock.getWorld().dropItemNaturally(brokenBlock.getLocation(), head);
        }
    }

    @EventHandler
    public void onPlayerCreativeClick(InventoryCreativeEvent event) {
        if (event.getClick().equals(ClickType.CREATIVE) && event.getWhoClicked() instanceof Player) {
            Player player = (Player)event.getWhoClicked();
            Block clickedBlock = player.getTargetBlockExact(10, FluidCollisionMode.NEVER);
            if (player.isSneaking() && clickedBlock != null && (clickedBlock.getType().equals(Material.PLAYER_HEAD) ||
                    clickedBlock.getType().equals(Material.PLAYER_WALL_HEAD))) {
                ItemStack head = getPlayerHead(clickedBlock);
                Bukkit.getScheduler().runTask(Main.getInstance(), () -> event.setCurrentItem(head));
            }
        }
    }
}
