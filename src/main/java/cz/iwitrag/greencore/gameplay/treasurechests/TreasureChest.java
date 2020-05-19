package cz.iwitrag.greencore.gameplay.treasurechests;

import com.sainttx.holograms.api.Hologram;
import com.sainttx.holograms.api.line.TextLine;
import cz.iwitrag.greencore.Main;
import cz.iwitrag.greencore.gameplay.chat.ChatUtils;
import cz.iwitrag.greencore.helpers.Color;
import cz.iwitrag.greencore.helpers.*;
import net.luckperms.api.model.group.Group;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.ClickOptions;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.type.ChestMenu;

import javax.persistence.Transient;
import java.util.*;

public class TreasureChest {

    private long id;
    private Location location;
    private List<PossibleItemReward> itemDatabase = new ArrayList<>();
    private Map<String, Date> playerCooldowns = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    @Transient
    private Date hideCooldown = null;
    @Transient
    private BlockData hiddenBlockData = null;
    @Transient
    private boolean hidden = false;

    private int cooldown = -1;
    private ItemStack neededItem = null;
    private String neededPermission = null;

    private int minimumItems = -1;
    private int maximumItems = -1;
    private String privateMessage = null;
    private String broadcastMessage = null;

    private boolean dropItemsLeftInside = true;
    private boolean dropItemsOnDestroy = false;
    private int hideChestAfterOpen = 0;
    private int manualSelectItems = 0;

    private String hologramText = null;
    @Transient
    private Hologram hologram = null;
    @Transient
    private static int hologramId = 0;

    private Particle particle = null;
    private Color particleColor = null;
    @Transient
    private BukkitRunnable particleRunnable = null;

    public TreasureChest(Location location, Percent itemChance) {
        this.location = location.getBlock().getLocation().clone();
        addRewardsFromBlock(location.getBlock(), itemChance);
        spawnHologram();
        startSpawningParticles();
    }

    public List<PossibleItemReward> getPossibleRewards() {
        List<PossibleItemReward> result = new ArrayList<>();
        for (PossibleItemReward item : itemDatabase) {
            result.add(new PossibleItemReward(item));
        }
        return result;
    }

    public TreasureChest(Location location, TreasureChest chest) {
        this.location = location.getBlock().getLocation().clone();
        this.itemDatabase = chest.getPossibleRewards();
        this.cooldown = chest.getCooldown();
        this.neededItem = chest.getNeededItem();
        this.neededPermission = chest.getNeededPermission();
        this.minimumItems = chest.getMinimumItems();
        this.maximumItems = chest.getMaximumItems();
        this.privateMessage = chest.getPrivateMessage();
        this.broadcastMessage = chest.getBroadcastMessage();
        this.dropItemsLeftInside = chest.isDropItemsLeftInside();
        this.dropItemsOnDestroy = chest.isDropItemsOnDestroy();
        this.hideChestAfterOpen = chest.getHideChestAfterOpen();
        this.manualSelectItems = chest.getManualSelectItems();
        spawnHologram();
        startSpawningParticles();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Location getLocation() {
        return location.clone();
    }

    public PossibleItemReward getPossibleReward(int id) {
        try {
            return new PossibleItemReward(itemDatabase.get(id));
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }

    public void addPossibleReward(PossibleItemReward reward) {
        itemDatabase.add(new PossibleItemReward(reward));
    }

    public void addPossibleRewards(List<PossibleItemReward> rewards) {
        for (PossibleItemReward reward : rewards)
            addPossibleReward(reward);
    }

    public boolean hasAnyRewards() {
        return !itemDatabase.isEmpty();
    }

    public int getPossibleRewardsAmount() {
        return itemDatabase.size();
    }

    public void addRewardsFromBlock(Block block, Percent itemChance) {
        BlockState blockState = block.getState();
        if (blockState instanceof Container) {
            for (ItemStack item : ((Container)blockState).getInventory()) {
                if (item != null && item.getType() != Material.AIR)
                    itemDatabase.add(new PossibleItemReward(item, itemChance));
            }
            ((Container) blockState).getInventory().clear();
        }
    }

    public void setRewardsFromBlock(Block block, Percent itemChance) {
        itemDatabase.clear();
        addRewardsFromBlock(block, itemChance);
    }

    public boolean setChanceForReward(int id, Percent itemChance) {
        PossibleItemReward reward = itemDatabase.get(id);
        if (reward != null) {
            reward.setChance(itemChance);
            return true;
        }
        return false;
    }

    public void setChanceForRewards(Percent itemChance) {
        for (PossibleItemReward item : itemDatabase) {
            item.setChance(itemChance);
        }
    }

    public void setChanceForRewards(List<PossibleItemReward> rewards, Percent itemChance) {
        for (PossibleItemReward item : itemDatabase) {
            for (PossibleItemReward checkAgainst : rewards) {
                if (item.isSimilar(checkAgainst)) {
                    item.setChance(itemChance);
                    break;
                }
            }
        }
    }

    public void setChanceForRewards(PossibleItemReward reward, Percent itemChance) {
        for (PossibleItemReward item : itemDatabase) {
            if (item.isSimilar(reward))
                item.setChance(itemChance);
        }
    }

    public boolean removeOnePossibleReward(PossibleItemReward reward) {
        for (PossibleItemReward item : itemDatabase) {
            if (item.isSimilar(reward)) {
                itemDatabase.remove(item);
                return true;
            }
        }
        return false;
    }

    public boolean removeOnePossibleReward(int id) {
        try {
            itemDatabase.remove(id);
        } catch (IndexOutOfBoundsException ex) {
            return false;
        }
        return true;
    }

    public boolean removeAllPossibleRewards(PossibleItemReward reward) {
        int initialSize = itemDatabase.size();
        itemDatabase.removeIf(item -> item.isSimilar(reward));
        return initialSize > itemDatabase.size();
    }

    public void purgePossibleRewards() {
        itemDatabase.clear();
    }

    public Date getPlayerCooldown(String player) {
        Date cd = playerCooldowns.get(player);
        return cd == null ? null : new Date(cd.getTime());
    }

    public Map<String, Date> getPlayerCooldowns() {
        Map<String, Date> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Map.Entry<String, Date> entry : playerCooldowns.entrySet()) {
            result.put(entry.getKey(), new Date(entry.getValue().getTime()));
        }
        return result;
    }

    public boolean hasPlayerCooldown(String player) {
        Date cd = playerCooldowns.get(player);
        Date now = new Date();
        if (cd == null)
            return false;
        return now.compareTo(cd) < 0;
    }

    public void addPlayerCooldown(String player) {
        Date newCooldown = new Date();
        newCooldown.setTime(newCooldown.getTime() + cooldown * 1000);
        playerCooldowns.put(player, newCooldown);
    }

    public boolean removePlayerCooldown(String player) {
        if (playerCooldowns.containsKey(player)) {
            playerCooldowns.remove(player);
            return true;
        }
        return false;
    }

    public void purgePlayerCooldowns() {
        playerCooldowns.clear();
    }

    public boolean isHidden() {
        if (hidden && hideCooldown.getTime() < new Date().getTime())
            unhide();

        return hidden;
    }

    private void hide() {
        hideCooldown = new Date();
        hideCooldown.setTime(hideCooldown.getTime() + (hideChestAfterOpen*1000));
        hiddenBlockData = location.getBlock().getBlockData().clone();
        hidden = true;
        if (hologram != null && hologram.isSpawned())
            hologram.despawn();
        location.getBlock().setType(Material.AIR);
    }

    private void unhide() {
        location.getBlock().setBlockData(hiddenBlockData, false);
        if (hologram != null && !hologram.isSpawned())
            hologram.spawn();
        hidden = false;
        hiddenBlockData = null;
        hideCooldown = null;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        if (cooldown < 0)
            cooldown = 0;
        this.cooldown = cooldown;

        Date now = new Date();
        for (Date date : playerCooldowns.values()) {
            long remainingSeconds = (date.getTime()-now.getTime())/1000;
            if (remainingSeconds > cooldown)
                date.setTime(now.getTime()+(cooldown*1000));
        }
    }

    public ItemStack getNeededItem() {
        if (neededItem == null)
            return null;
        return new ItemStack(neededItem);
    }

    public void setNeededItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR)
            neededItem = null;
        else
            neededItem = new ItemStack(item);
    }

    public String getNeededPermission() {
        return neededPermission;
    }

    public void setNeededPermission(String permission) {
        if (permission == null || permission.isEmpty())
            permission = null;
        neededPermission = permission;
    }

    public int getMinimumItems() {
        return minimumItems;
    }

    public void setMinimumItems(int amount) {
        if (amount < 0)
            amount = 0;
        if (amount > maximumItems)
            maximumItems = amount;
        minimumItems = amount;
    }

    public int getMaximumItems() {
        return maximumItems;
    }

    public void setMaximumItems(int amount) {
        if (amount < 0)
            amount = 0;
        if (amount < minimumItems)
            minimumItems = amount;
        maximumItems = amount;
    }

    public String getPrivateMessage() {
        return privateMessage;
    }

    public void setPrivateMessage(String message) {
        privateMessage = message;
    }

    public String getBroadcastMessage() {
        return broadcastMessage;
    }

    public void setBroadcastMessage(String message) {
        broadcastMessage = message;
    }

    public boolean isDropItemsLeftInside() {
        return dropItemsLeftInside;
    }

    public void setDropItemsLeftInside(boolean dropItemsLeftInside) {
        this.dropItemsLeftInside = dropItemsLeftInside;
    }

    public boolean isDropItemsOnDestroy() {
        return dropItemsOnDestroy;
    }

    public void setDropItemsOnDestroy(boolean dropItemsOnDestroy) {
        this.dropItemsOnDestroy = dropItemsOnDestroy;
    }

    public int getHideChestAfterOpen() {
        return hideChestAfterOpen;
    }

    public void setHideChestAfterOpen(int hideChestAfterOpen) {
        this.hideChestAfterOpen = hideChestAfterOpen;
    }

    public int getManualSelectItems() {
        return manualSelectItems;
    }

    public void setManualSelectItems(int manualSelectItems) {
        if (manualSelectItems < 0)
            manualSelectItems = 0;
        this.manualSelectItems = manualSelectItems;
    }

    public String getHologramText() {
        return hologramText;
    }

    public void setHologramText(String newHologramText) {
        // Removing hologram
        if (newHologramText == null) {
            if (hologram != null) {
                hologram.despawn();
            }
            hologramText = null;
            hologram = null;
            return;
        }

        // No change to hologram
        if (newHologramText.equals(hologramText)) {
            return;
        }

        // Adding new hologram or updating text
        hologramText = newHologramText;
        if (hologram != null) {
            hologram.despawn();
        }
        spawnHologram();
    }

    private void spawnHologram() {
        if (hologramText == null)
            return;

        hologram = constructHologram(hologramText);
        if (!isHidden())
            hologram.spawn();
    }

    private Hologram constructHologram(String text) {
        String[] split = text.split("\\|\\|");
        Location hologramlocation = location.toCenterLocation();
        hologramlocation.add(0, 0.5, 0);
        Hologram hologram = new Hologram("tchest_hologram_" + hologramId++, hologramlocation, false);
        for (int i = 0; i < split.length; i++) {
            String line = split[i];
            TextLine addedLine = new TextLine(hologram, line);
            hologram.addLine(addedLine);
            if (i != 0) { // first line should not affect hologram position
                if (i == split.length - 1) // is last line
                    hologramlocation.add(0, addedLine.getHeight(), 0);
                else
                    hologramlocation.add(0, addedLine.getHeight() + TextLine.SPACE_BETWEEN_LINES, 0);
            }
        }
        hologram.teleport(hologramlocation);
        return hologram;
    }

    public Particle getParticle() {
        return particle;
    }

    public void setParticle(Particle particle) {
        this.particle = particle;
    }

    public Color getParticleColor() {
        if (particleColor == null)
            return null;
        return new Color(particleColor);
    }

    public void setParticleColor(Color particleColor) {
        if (particleColor == null)
            this.particleColor = null;
        else
            this.particleColor = new Color(particleColor);
    }

    public void startSpawningParticles() {
        if (particleRunnable == null) {
            this.particleRunnable = new BukkitRunnable() {
                @Override
                public void run() {
                    if (particle != null && !isHidden()) {
                        Location randomLocation = location.toCenterLocation().add(
                                Utils.randomDoubleBetween(-0.6, 0.6),
                                Utils.randomDoubleBetween(-0.6, 0.6),
                                Utils.randomDoubleBetween(-0.6, 0.6)
                        );
                        Color color = particleColor == null ? new Color("black") : particleColor;
                        randomLocation.getWorld().spawnParticle(
                                particle, randomLocation, 1, 0.0, 0.0, 0.0, 0.0,
                                Utils.isParticleColorizable(particle)  ? new Particle.DustOptions(org.bukkit.Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue()), 1) : null
                        );
                    }
                }
            };
            particleRunnable.runTaskTimer(Main.getInstance(), 1, 1);
        }
    }

    public void stopSpawningParticles() {
        if (particleRunnable != null) {
            particleRunnable.cancel();
            particleRunnable = null;
        }
    }

    public void open(Player player) {
        if (player == null || !player.isOnline() || isHidden())
            return;

        // Item database must not be empty
        if (itemDatabase.isEmpty()) {
            player.sendMessage("§cTato truhla v sobě nic nemá");
            return;
        }

        String playerName = player.getName();

        // Check player cooldown
        if (hasPlayerCooldown(playerName)) {
            long seconds = (getPlayerCooldown(playerName).getTime()/1000) - (new Date().getTime()/1000);
            player.sendMessage("§cJeště nemůžeš truhlu otevřít");
            player.sendMessage("§eZbývá: §6" + StringHelper.timeToLongString(seconds));
            return;
        }

        // Check permission
        if (neededPermission != null && !LuckPermsHelper.playerHasPermission(playerName, neededPermission)) {
            for (Group group : LuckPermsHelper.getGroups(false)) {
                if (neededPermission.equalsIgnoreCase("tchest." + group.getName())) {
                    player.sendMessage("§cPro otevření této truhly musíš být " + group.getDisplayName() + " §cnebo vyšší");
                    return;
                }
            }
            player.sendMessage("§cPro otevření této truhly musíš mít oprávnění §4" + neededPermission);
            return;
        }

        // Check item in hand
        ItemStack playerItem = player.getInventory().getItemInMainHand();
        if (neededItem != null && neededItem.getType() != Material.AIR) {
            if (!neededItem.asOne().equals(playerItem.asOne()) || neededItem.getAmount() > playerItem.getAmount()) {
                player.sendMessage(new ComponentBuilder("")
                        .appendLegacy("§cPro otevření této truhly musíš mít v ruce §r")
                        .append(ChatUtils.getItemableText(true, neededItem))
                        .create()
                );
                return;
            }
        }

        // Choose rewards
        List<PossibleItemReward> itemPool = getPossibleRewards();
        List<PossibleItemReward> selectedItems;
        // if there are fewer items than minimumItems, select them all
        if (minimumItems > 0 && minimumItems > itemDatabase.size())
            selectedItems = getPossibleRewards();
        else {
            selectedItems = selectRewards(itemPool, false);
            // achieve minimumItems by selecting additional rewards with normalized chances
            if (minimumItems > 0) {
                itemPool.removeAll(selectedItems);
                while (selectedItems.size() < minimumItems) {
                    PossibleItemReward additionalReward = Utils.pickRandomElement(selectRewards(itemPool, true));
                    selectedItems.add(additionalReward);
                    itemPool.remove(additionalReward);
                }
            }
            // achieve maximumItems by randomly removing rewards one by one
            int realMaximumItems;
            if (maximumItems <= 0 || maximumItems > 54)
                realMaximumItems = 54;
            else
                realMaximumItems = maximumItems;
            while (selectedItems.size() > realMaximumItems) {
                selectedItems.remove(Utils.pickRandomElement(selectedItems));
            }
        }

        // Open chest for player
        int realManualSelectItems = manualSelectItems >= selectedItems.size() ? 0 : manualSelectItems;
        String chestMenuName;
        if (selectedItems.size() == 0)
            chestMenuName = "§c§lSmůla :( §4Nejsou tu žádné itemy";
        else if (realManualSelectItems <= 0)
            chestMenuName = "§1Vezmi si §3všechny §1itemy";
        else if (realManualSelectItems == 1)
            chestMenuName = "§1Vyber si §31 §1item";
        else if (realManualSelectItems < 5)
            chestMenuName = "§1Vyber si §3" + manualSelectItems + " §1itemy";
        else
            chestMenuName = "§1Vyber si §3" + manualSelectItems + " §1itemů";
        Set<Integer> emptySlots = new HashSet<>();
        for (int i = 0; i < 54; i++) {
            emptySlots.add(i);
        }
        Menu menu = ChestMenu.builder(6).title(chestMenuName).build();
        Location centerLocation = location.toCenterLocation();
        // Not manual selection
        if (realManualSelectItems <= 0) {
            List<ItemStack> selectedItemStacks = new ArrayList<>();
            for (PossibleItemReward r : selectedItems) {
                selectedItemStacks.add(r.getItem());
            }
            ClickOptions options = ClickOptions.builder()
                    .allClickTypes()
                    .allow(InventoryAction.PICKUP_ALL,
                            InventoryAction.PICKUP_HALF,
                            InventoryAction.PICKUP_ONE,
                            InventoryAction.PICKUP_SOME,
                            InventoryAction.COLLECT_TO_CURSOR,
                            InventoryAction.CLONE_STACK,
                            InventoryAction.MOVE_TO_OTHER_INVENTORY,
                            InventoryAction.HOTBAR_MOVE_AND_READD)
                    .build();
            menu.setCloseHandler((p, m) -> {
                boolean droppedSomething = false;
                if (dropItemsLeftInside) {
                    for (ItemStack item : p.getOpenInventory().getTopInventory()) {
                        if (item != null && item.getType() != Material.AIR) {
                            droppedSomething = true;
                            centerLocation.getWorld().dropItemNaturally(centerLocation, item);
                        }
                    }
                }
                printMessages(player, selectedItemStacks);
                if (droppedSomething)
                    player.sendMessage("§aV truhle zůstaly věci a proto byly dropnuty na zem");
            });
            for (PossibleItemReward selectedItem : selectedItems) {
                int slotNumber = Objects.requireNonNull(Utils.pickRandomElement(emptySlots), "Selected slot number for tChest reward was null");
                emptySlots.remove(slotNumber);
                menu.getSlot(slotNumber).setClickOptions(options);
                menu.getSlot(slotNumber).setItem(selectedItem.getItem());
            }
        // Manual selection
        } else {
            final int[] itemsToPick = {realManualSelectItems};
            ClickOptions options = ClickOptions.builder()
                    .allow(ClickType.LEFT, ClickType.RIGHT)
                    .build();
            List<ItemStack> pickedItems = new ArrayList<>();
            Slot.ClickHandler handler = (p, c) -> {
                if (itemsToPick[0] > 0) {
                    itemsToPick[0]--;
                    ItemStack item = c.getClickedSlot().getItem(p);
                    pickedItems.add(item);
                    for (Iterator<PossibleItemReward> iterator = selectedItems.iterator(); iterator.hasNext(); ) {
                        PossibleItemReward r = iterator.next();
                        if (r.getItem().equals(item)) {
                            iterator.remove();
                            break;
                        }
                    }
                    HashMap<Integer, ItemStack> couldNotHold = p.getInventory().addItem(item);
                    if (!couldNotHold.isEmpty()) {
                        for (Integer i : couldNotHold.keySet()) {
                            centerLocation.getWorld().dropItemNaturally(centerLocation, couldNotHold.get(i));
                        }
                        player.sendMessage("§aNa item nebylo v inventáři místo a proto byl dropnut");
                    }
                    c.getClickedSlot().setClickOptions(ClickOptions.builder().build());
                    c.getClickedSlot().setItem(null);
                    c.getClickedSlot().setClickHandler(null);
                    if (itemsToPick[0] == 0)
                        c.getClickedMenu().close(p);
                }
            };
            menu.setCloseHandler((p, m) -> {
                if (dropItemsLeftInside && itemsToPick[0] > 0) {
                    for (int i = 0; i < itemsToPick[0]; i++) {
                        PossibleItemReward reward = Objects.requireNonNull(Utils.pickRandomElement(selectedItems));
                        selectedItems.remove(reward);
                        pickedItems.add(reward.getItem());
                        centerLocation.getWorld().dropItemNaturally(centerLocation, reward.getItem());
                    }
                    player.sendMessage("§aV truhle zůstaly věci a proto byly náhodně vybrány a dropnuty na zem");
                }
                printMessages(p, pickedItems);
            });
            for (PossibleItemReward selectedItem : selectedItems) {
                int slotNumber = Objects.requireNonNull(Utils.pickRandomElement(emptySlots));
                emptySlots.remove(slotNumber);
                menu.getSlot(slotNumber).setClickOptions(options);
                menu.getSlot(slotNumber).setItem(selectedItem.getItem());
                menu.getSlot(slotNumber).setClickHandler(handler);
            }
        }
        menu.open(player);

        // Hide chest
        if (hideChestAfterOpen > 0) {
            hide();
        }

        // Apply cooldown
        if (cooldown > 0) {
            addPlayerCooldown(playerName);
        }

        // Remove needed item
        if (neededItem != null && neededItem.getType() != Material.AIR) {
            playerItem.setAmount(playerItem.getAmount() - neededItem.getAmount());
        }
    }

    private List<PossibleItemReward> selectRewards(List<PossibleItemReward> itemPool, boolean normalizeChances) {
        double chanceMultiplier = 1.0;
        if (normalizeChances) {
            // find highest chance
            double highestChance = 0.0;
            for (PossibleItemReward reward : itemPool) {
                if (reward.getChance().getBaseValue() > highestChance)
                    highestChance = reward.getChance().getBaseValue();
            }
            // change it to 100% and calculate multiplier which will be applied to all remaining items
            chanceMultiplier = 1.0 / highestChance;
        }
        List<PossibleItemReward> result = new ArrayList<>();
        for (PossibleItemReward reward : itemPool) {
            if (Utils.chance(reward.getChance().getPercentValue() * chanceMultiplier))
                result.add(reward);
        }
        return result;
    }

    private void printMessages(Player player, List<ItemStack> rewards) {
        String[] playerKeywords = new String[]{"player", "hrac", "hráč", "nick", "name", "nickname"};
        String[] itemsKeywords = new String[]{"item", "items", "itemy", "reward", "rewards", "loot", "loots", "odmena", "odmeny", "poklad", "poklady"};
        ComponentBuilder rewardList = new ComponentBuilder("").appendLegacy("§r");
        if (rewards.isEmpty())
            rewardList.appendLegacy("§cNic");
        else {
            for (Iterator<ItemStack> iterator = rewards.iterator(); iterator.hasNext(); ) {
                ItemStack reward = iterator.next();
                rewardList.append(ChatUtils.getItemableText(true, reward));
                if (iterator.hasNext())
                    rewardList.appendLegacy(", §r");

            }
        }
        if (broadcastMessage != null) {
            String tempMessage = Utils.replacePlaceholders(playerKeywords, ChatColor.translateAlternateColorCodes('&', broadcastMessage), player.getName());
            BaseComponent[] message = Utils.replacePlaceholders(itemsKeywords, tempMessage, rewardList.create());
            for (Player p : Bukkit.getOnlinePlayers())
                p.sendMessage(message);
        }
        if (privateMessage != null) {
            String temp = Utils.replacePlaceholders(playerKeywords, ChatColor.translateAlternateColorCodes('&', privateMessage), player.getName());
            BaseComponent[] message = Utils.replacePlaceholders(itemsKeywords, temp, rewardList.create());
            player.sendMessage(message);
        }
    }
}
