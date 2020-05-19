package cz.iwitrag.greencore.gameplay.treasurechests;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import cz.iwitrag.greencore.gameplay.chat.ChatUtils;
import cz.iwitrag.greencore.helpers.Color;
import cz.iwitrag.greencore.helpers.Percent;
import cz.iwitrag.greencore.helpers.StringHelper;
import cz.iwitrag.greencore.helpers.Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.slot.ClickOptions;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.type.ChestMenu;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@CommandAlias("tchest|treasurechest|tch")
@CommandPermission("tchest.admin")
public class TreasureChestCommands extends BaseCommand {

    @HelpCommand
    public void baseTreasureChestCommand(CommandSender sender, CommandHelp help) {
        sender.sendMessage("§8" + StringHelper.getChatLine());
        sender.sendMessage("§a" + StringHelper.centerMessage("NÁPOVĚDA K TREASURE CHESTKÁM"));
        help.showHelp();
        sender.sendMessage("§8" + StringHelper.getChatLine());
    }

    @Subcommand("create|new|define|vytvorit|nova|definovat")
    @Description("Vytvoří novou treasure chestku")
    @CommandCompletion("@chance")
    public void createCommand(Player sender, @Optional Percent chance) {
        if (chance == null)
            chance = new Percent(10.0);
        TreasureChestManager.getInstance().setPlayerClickOperation(sender.getName(), TreasureChestClickOperation.createOperation(chance));
        sender.sendMessage("§aKlikni na blok, ze kterého chceš udělat treasure chestku");
        sender.sendMessage("§2Pokud bude v sobě mít daný blok věci, použijí se jako základ databáze");
        sender.sendMessage("§7§o(operaci zrušíš kliknutím levým do vzduchu)");
    }

    @Subcommand("select|sel|choose|vybrat|vyber")
    @Description("Vybere treasure chestku pro provádění dalších příkazů")
    public void selectCommand(Player sender) {
        TreasureChestManager.getInstance().setPlayerClickOperation(sender.getName(), TreasureChestClickOperation.selectOperation());
        sender.sendMessage("§aKlikni na treasure chestku pro její výběr");
        sender.sendMessage("§7§o(operaci zrušíš kliknutím levým do vzduchu)");
    }

    @Subcommand("list|seznam")
    @Description("Vypíše seznam treasure chestek")
    @CommandCompletion("@worlds")
    public void listCommand(CommandSender sender, @Optional World world) {
        Set<TreasureChest> tChests = TreasureChestManager.getInstance().getTreasureChests();
        if (tChests.isEmpty()) {
            sender.sendMessage("§cŽádné treasure chestky nejsou vytvořeny.");
            return;
        }
        if (world != null) {
            tChests.removeIf(chest -> !chest.getLocation().getWorld().equals(world));
        }
        if (tChests.isEmpty()) {
            sender.sendMessage("§cŽádné treasure chestky ve světě §4" + world.getName() + " §cnejsou vytvořeny.");
            return;
        }
        sender.sendMessage("§8" + StringHelper.getChatLine());
        if (world == null)
            sender.sendMessage("§a" + StringHelper.centerMessage("SEZNAM VŠECH TREASURE CHESTEK"));
        else
            sender.sendMessage("§a" + StringHelper.centerMessage("SEZNAM TREASURE CHESTEK VE SVĚTĚ " + world.getName()));
        ComponentBuilder builder = new ComponentBuilder("");
        for (TreasureChest chest : tChests) {
            builder.append(getTreasureChestForChat(chest, sender, true)).append(", ").color(ChatColor.DARK_GRAY);
        }
        sender.sendMessage(builder.create());
        sender.sendMessage("§8" + StringHelper.getChatLine());
    }

    @Subcommand("near|around|blizko|pobliz|kolem|okolo")
    @Description("Vypíše treasure chestky v blízkosti")
    @CommandCompletion("@range:0-300(15)")
    public void nearCommand(Player sender, @Default("50") Integer radius) {
        Set<TreasureChest> tChests = TreasureChestManager.getInstance().getTreasureChests();
        if (tChests.isEmpty()) {
            sender.sendMessage("§cŽádné treasure chestky nejsou vytvořeny.");
            return;
        }
        if (radius < 1) {
            radius = 1;
        }
        Set<TreasureChest> tChestsNear = new LinkedHashSet<>();
        Location loc = sender.getLocation();
        for (TreasureChest chest : tChests) {
            if (loc.getWorld().equals(chest.getLocation().getWorld()) && loc.distance(chest.getLocation()) < radius)
                tChestsNear.add(chest);
        }
        if (tChestsNear.isEmpty()) {
            sender.sendMessage("§cŽádné treasure chestky v okolí §4" + radius + " §cbloků nebyly nalezeny.");
            return;
        }

        sender.sendMessage("§8" + StringHelper.getChatLine());
        sender.sendMessage("§a" + StringHelper.centerMessage("TREASURE CHESTKY V OKOLÍ §2" + radius + " §aBLOKŮ"));
        ComponentBuilder builder = new ComponentBuilder("");
        for (TreasureChest chest : tChests) {
            builder.append(getTreasureChestForChat(chest, sender, true)).append(", ").color(ChatColor.DARK_GRAY);
        }
        sender.sendMessage(builder.create());
        sender.sendMessage("§8" + StringHelper.getChatLine());
    }

    @Subcommand("info|about|informace")
    @Description("Vypíše informace o treasure chestce")
    public void infoCommand(Player sender, @Flags("selected") TreasureChest tChest) {
        sender.sendMessage("§8" + StringHelper.getChatLine());
        sender.sendMessage("§a" + StringHelper.centerMessage("OBECNÉ INFORMACE O TREASURE CHESTCE"));
        sender.sendMessage(getTreasureChestForChat(tChest, sender, false));
        sender.sendMessage("§8" + StringHelper.getChatLine());
    }

    @Subcommand("tp|teleport")
    @Description("Teleportuje hráče k treasure chestce")
    @CommandCompletion("@players")
    public void tpCommand(CommandSender sender, @Flags("selected") TreasureChest tChest, @Optional OnlinePlayer target) {
        Player targetPlayer;
        if (target == null) {
            if (sender instanceof Player) {
                targetPlayer = (Player) sender;
            } else {
                sender.sendMessage("§cZadej jméno hráče, kterého chceš teleportovat");
                return;
            }
        }
        else
            targetPlayer = target.getPlayer();
        if (targetPlayer.equals(sender))
            sender.sendMessage("§aTeleportuješ se k chestce");
        else
            sender.sendMessage("§aTeleportuješ hráče §2" + target.getPlayer().getName() + " §ak chestce");
        targetPlayer.teleport(tChest.getLocation());
    }

    @Subcommand("copy|duplicate|kopirovat|duplikovat")
    @Description("Zkopíruje nastavení treasure chestky do jiné")
    public void copyCommand(Player sender, @Flags("selected") TreasureChest tChest) {
        TreasureChestManager.getInstance().setPlayerClickOperation(sender.getName(), TreasureChestClickOperation.copyOperation(tChest));
        sender.sendMessage("§aZkopírováno! Klikni na treasure chestku, do které chceš nastavení vložit");
        sender.sendMessage("§7§o(operaci zrušíš kliknutím levým do vzduchu)");
    }

    @Subcommand("delete|remove|smazat|odstranit")
    @Description("Odstraní treasure chestku")
    public void deleteCommand(Player sender, @Flags("selected") TreasureChest tChest) {
        TreasureChestManager.getInstance().setPlayerClickOperation(sender.getName(), TreasureChestClickOperation.deleteOperation(tChest));
        sender.sendMessage("§aChystáš se odstranit treasure chestku. Pro potvrzení na ni klikni");
        sender.sendMessage("§7§o(operaci zrušíš kliknutím levým do vzduchu)");
    }

    @Subcommand("forget|resetcd|resetcooldown|zapomenout")
    @Description("Resetuje cooldown chestky pro daného hráče")
    @CommandCompletion("@tchest_players_cooldown")
    public void forgetCommand(Player sender, @Flags("selected") TreasureChest tChest, @Optional String playerName) {
        if (playerName == null)
            playerName = sender.getName();
        tChest.removePlayerCooldown(playerName);
        if (sender.getName().equalsIgnoreCase(playerName))
            sender.sendMessage("§aNyní můžeš chestku znovu otevřít");
        else
            sender.sendMessage("§aHráč §2" + playerName + " §anyní může chestku znovu otevřít");
    }

    @Subcommand("forgetall|purgecd|purgecooldown|zapomenoutvse")
    @Description("Resetuje cooldown chestky pro všechny hráče")
    public void forgetAllCommand(Player sender, @Flags("selected") TreasureChest tChest) {
        tChest.purgePlayerCooldowns();
        sender.sendMessage("§aNyní můžou chestku všichni znovu otevřít");
    }

    @Subcommand("cooldown|cd|setcooldown|setcd")
    @Description("Nastaví cooldown pro znovuotevření chestky hráčem (v sekundách)")
    @CommandCompletion("@range:0-600(60)")
    public void cooldownCommand(Player sender, @Flags("selected") TreasureChest tChest, @Optional Integer cooldown) {
        if (cooldown == null)
            sender.sendMessage("§aCooldown chestky je aktuálně §2" + tChest.getCooldown() + " s.");
        else {
            tChest.setCooldown(cooldown);
            sender.sendMessage("§aCooldown chestky nastaven na §2" + cooldown + " s.");
        }
    }

    @Subcommand("key|needkey|neededkey|needitem|neededitem|potrebnyitem|potrebnyklic")
    @Description("Nastaví potřebný item pro otevření chestky hráčem")
    public void keyCommand(Player sender, @Flags("selected") TreasureChest tChest, @Optional @Flags("main_hand") ItemStack itemStack) {
        if (itemStack == null) {
            Runnable getNeededItemRunnable = () -> {
                sender.getInventory().addItem(tChest.getNeededItem());
                sender.sendMessage(new ComponentBuilder("")
                        .appendLegacy("§aDostáváš §r")
                        .append(ChatUtils.getItemableText(true, tChest.getNeededItem()))
                        .create());
            };
            BaseComponent[] neededItem;
            if (tChest.getNeededItem() == null)
                neededItem = TextComponent.fromLegacyText("§cŽádný item");
            else
                neededItem = ChatUtils.getItemableTextWithRunnable(true, tChest.getNeededItem(), getNeededItemRunnable);
            sender.sendMessage(new ComponentBuilder("")
                    .appendLegacy("§aPro otevření této chestky je potřeba: §2")
                    .append(neededItem)
                    .create());
        }
        else {
            tChest.setNeededItem(itemStack);
            sender.sendMessage(new ComponentBuilder("")
                    .appendLegacy("§aPotřebný item pro otevření chestky nastaven na §2")
                    .append(ChatUtils.getItemableText(true, itemStack))
                    .create());
        }
    }

    @Subcommand("permission|perm|permissions|permise|opravneni")
    @Description("Nastaví oprávnění potřebné pro otevření chestky hráčem")
    @CommandCompletion("@tchest_groupperms")
    public void permissionCommand(Player sender, @Flags("selected") TreasureChest tChest, @Optional String permission) {
        tChest.setNeededPermission(permission);
        sender.sendMessage("§aOprávnění potřebné k otevření chestky nastaveno na §2" + (permission == null ? "žádné" : permission));
        sender.sendMessage("§7§o(Např. pro VIP+ a vyšší se používá §f§otchest.vipplus§7§o)");
    }

    @Subcommand("minitems|minitem|minofitem|minofitems|minimumitem|minimumitems|minimumitemu")
    @Description("Nastaví minimální počet itemů vylosovaný chestkou")
    @CommandCompletion("@range")
    public void minItemsCommand(Player sender, @Flags("selected") TreasureChest tChest, @Optional Integer minItems) {
        if (minItems == null) {
            minItems = tChest.getMinimumItems();
            String itemsText;
            itemsText = minItems + " ";
            if (minItems <= 0 || minItems >= 5)
                itemsText += "itemů";
            else if (minItems == 1)
                itemsText += "item";
            else
                itemsText += "itemy";
            sender.sendMessage("§aMinimální počet itemů vylosovaný chestkou je nastaven na §2" + itemsText);
        }
        else {
            tChest.setMinimumItems(minItems);
            minItems = tChest.getMinimumItems();
            String itemsText;
            itemsText = minItems + " ";
            if (minItems <= 0 || minItems >= 5)
                itemsText += "itemů";
            else if (minItems == 1)
                itemsText += "item";
            else
                itemsText += "itemy";
            sender.sendMessage("§aMinimální počet itemů vylosovaný chestkou byl nastaven na §2" + itemsText);
        }
        sender.sendMessage("§7§8(Pokud v databázi chestky nebude dost itemů, vylosují se všechny co v ní jsou)");
    }

    @Subcommand("maxitems|maxitem|maxofitem|maxofitems|maximumitem|maximumitems|maximumitemu")
    @Description("Nastaví maximální počet itemů vylosovaný chestkou")
    @CommandCompletion("@range")
    public void maxItemsCommand(Player sender, @Flags("selected") TreasureChest tChest, @Optional Integer maxItems) {
        if (maxItems == null) {
            maxItems = tChest.getMaximumItems();
            String itemsText;
            itemsText = maxItems + " ";
            if (maxItems <= 0 || maxItems >= 5)
                itemsText += "itemů";
            else if (maxItems == 1)
                itemsText += "item";
            else
                itemsText += "itemy";
            sender.sendMessage("§aMaximální počet itemů vylosovaný chestkou je nastaven na §2" + itemsText);
        }
        else {
            tChest.setMaximumItems(maxItems);
            maxItems = tChest.getMaximumItems();
            String itemsText;
            itemsText = maxItems + " ";
            if (maxItems <= 0 || maxItems >= 5)
                itemsText += "itemů";
            else if (maxItems == 1)
                itemsText += "item";
            else
                itemsText += "itemy";
            sender.sendMessage("§aMaximální počet itemů vylosovaný chestkou byl nastaven na §2" + itemsText);
        }
    }

    @Subcommand("dropitems")
    @Description("Nastaví, zda mají z chestky vypadnout věci, které hráč uvnitř po otevření nechá")
    @CommandCompletion("ano|ne")
    public void dropItemsCommand(Player sender, @Flags("selected") TreasureChest tChest, @Optional String value) {
        if (value == null) {
            sender.sendMessage("§aDropnutí itemů ponechaných hráčem v chestce je nastaveno na §2" + tChest.isDropItemsLeftInside());
        }
        else if (Arrays.asList("yes", "1", "true", "on", "enable", "allow", "ano", "zap", "zapnout", "povolit", "povol").contains(value.toLowerCase())) {
            tChest.setDropItemsLeftInside(true);
            sender.sendMessage("§aItemy ponechané hráčem v chestce vypadnou na zem");
        }
        else if (Arrays.asList("no", "0", "false", "off", "disable", "block", "ne", "vyp", "vypnout", "zakazat", "zakaz").contains(value.toLowerCase())) {
            tChest.setDropItemsLeftInside(false);
            sender.sendMessage("§aItemy ponechané hráčem v chestce nevypadnou na zem");
        }
        else {
            sender.sendMessage("§cMusíš zadat \"ano\" nebo \"ne\"");
        }
    }

    @Subcommand("breakdrop")
    @Description("Nastaví, zda se mají po rozbití treasure chestky vysypat věci na zem")
    @CommandCompletion("ano|ne")
    public void breakdropCommand(Player sender, @Flags("selected") TreasureChest tChest, @Optional String value) {
        if (value == null) {
            sender.sendMessage("§aVysypání itemů na zem po rozbití této chestky je nastaveno na §2" + tChest.isDropItemsOnDestroy());
        }
        else if (Arrays.asList("yes", "1", "true", "on", "enable", "allow", "ano", "zap", "zapnout", "povolit", "povol").contains(value.toLowerCase())) {
            tChest.setDropItemsOnDestroy(true);
            sender.sendMessage("§aPo rozbití této treasure chestky nyní vypadnou všechny itemy v databázi");
            sender.sendMessage("§7§o(treasure chestku může rozbít jedině admin)");
        }
        else if (Arrays.asList("no", "0", "false", "off", "disable", "block", "ne", "vyp", "vypnout", "zakazat", "zakaz").contains(value.toLowerCase())) {
            tChest.setDropItemsOnDestroy(false);
            sender.sendMessage("§aPo rozbití této treasure chestky nedojde k dropnutí itemů z databáze na zem");
        }
        else {
            sender.sendMessage("§cMusíš zadat \"ano\" nebo \"ne\"");
        }
    }

    @Subcommand("hide|skryt|skryti")
    @Description("Nastaví, na jak dlouho se má chestka po otevření skrýt")
    @CommandCompletion("@range:0-300(60)")
    public void hideCommand(Player sender, @Flags("selected") TreasureChest tChest, @Optional Integer seconds) {
        if (seconds != null) {
            if (seconds < 0)
                seconds = 0;
            tChest.setHideChestAfterOpen(seconds);
        }
        else
            seconds = tChest.getHideChestAfterOpen();
        sender.sendMessage("§aChestka se po otevření §2" + (seconds <= 0 ? "neskryje" : ("skryje na " + seconds + " s.")));
    }

    @Subcommand("selectitems|selectitem|selitems|selitem|vyberitemu")
    @Description("Nastaví, kolik z vylosovaných itemů si bude moct hráč vybrat")
    @CommandCompletion("@range")
    public void selectItemsCommand(Player sender, @Flags("selected") TreasureChest tChest, @Optional Integer amount) {
        if (amount != null) {
            if (amount < 0)
                amount = 0;
            tChest.setManualSelectItems(amount);
        }
        else
            amount = tChest.getManualSelectItems();
        if (amount == 0)
            sender.sendMessage("§aRežim ručního výběru je u této chestky §2vypnut");
        else {
            String itemsText = amount + " ";
            if (amount <= 0 || amount >= 5)
                itemsText += "itemů";
            else if (amount == 1)
                itemsText += "item";
            else
                itemsText += "itemy";
            sender.sendMessage("§aHráč si po otevření chestky bude moci vybrat §2" + itemsText);
        }
    }

    @Subcommand("particles|particle|particly|partikly|castice|efekt|efekty")
    @Description("Nastaví particles treasure chestky a případně i barvu")
    @CommandCompletion("@particles @colors")
    public void particlesCommand(Player sender, @Flags("selected") TreasureChest tChest, @Optional Particle particle, @Optional Color color) {
        if (particle == null) {
            tChest.setParticle(null);
            tChest.setParticleColor(null);
            sender.sendMessage("§aParticles treasure chestky §2vypnuty");
        } else {
            tChest.setParticle(particle);
            if (color == null) {
                sender.sendMessage("§aParticles treasure chestky nastaveny na §2" + particle.name());
            } else {
                tChest.setParticleColor(color);
                sender.sendMessage("§aParticles treasure chestky nastaveny na §2" + particle.name() + " §aa barva nastavena na §2" + color);
            }
        }
    }

    @Subcommand("hologram|holo")
    @Description("Nastaví hologram nad chestkou")
    @CommandCompletion("@nothing")
    public void hologramCommand(Player sender, @Flags("selected") TreasureChest tChest, @Optional String text) {
        tChest.setHologramText(text);
        if (text == null)
            sender.sendMessage("§aHologram treasure chestky §2vypnut");
        else
            sender.sendMessage("§aHologram treasure chestky nastaven na §r" + text);
        sender.sendMessage("§7§o(Nový řádek lze vytvořit napsáním ||)");
    }

    @Subcommand("msg|msgs|message|messages|zprava|zpravy")
    public class msgCommands extends BaseCommand {

        @Subcommand("player|p|hrac|hraci")
        @Description("Nastaví zprávu zobrazovanou hráči po otevření treasure chestky")
        @CommandCompletion("@nothing")
        public void msgPlayerCommand(Player sender, @Flags("selected") TreasureChest tChest, @Optional String text) {
            tChest.setPrivateMessage(text);
            if (text == null)
                sender.sendMessage("§aZpráva zobrazovaná hráči po otevření chestky §2vypnuta");
            else
                sender.sendMessage("§aZpráva zobrazovaná hráči po otevření chestky nastavena na §r" + text);
            sender.sendMessage("§7§o(Ve zprávě jde použít i %player% a %items% a to nahradí jméno hráče a itemy, které získal)");
        }

        @Subcommand("broadcast|bc|global|globalni|globalne|vsem")
        @Description("Nastaví zprávu zobrazovanou celému serveru po otevření treasure chestky")
        @CommandCompletion("@nothing")
        public void msgBroadcastCommand(Player sender, @Flags("selected") TreasureChest tChest, @Optional String text) {
            tChest.setBroadcastMessage(text);
            if (text == null)
                sender.sendMessage("§aZpráva zobrazovaná celému serveru po otevření chestky §2vypnuta");
            else
                sender.sendMessage("§aZpráva zobrazovaná celému serveru po otevření chestky nastavena na §r" + text);
            sender.sendMessage("§7§o(Ve zprávě jde použít i %player% a %items% a to nahradí jméno hráče a itemy, které získal)");
        }
    }

    @Subcommand("item|i|items|itemy")
    public class itemCommands extends BaseCommand {

        @Subcommand("add|pridat|pridej")
        @Description("Přidá itemy do databáze chestky")
        @CommandCompletion("@chance")
        public void itemAddCommand(Player sender, @Flags("selected") TreasureChest tChest, @Optional Percent chance) {
            if (chance == null)
                chance = new Percent(10.0);

            ChestMenu menu = ChestMenu.builder(6).title("§1Přidání itemů (šance §3" + chance + "§1)").build();

            ClickOptions clickOptions = ClickOptions.builder().allActions().allClickTypes().build();
            for (Slot slot : menu) {
                slot.setClickOptions(clickOptions);
            }

            final Percent finalChance = chance; // for lambda below
            menu.setCloseHandler((player, menu1) -> {
                int addedItems = 0;
                for (ItemStack item : player.getOpenInventory().getTopInventory()) {
                    if (item != null && item.getType() != Material.AIR) {
                        tChest.addPossibleReward(new PossibleItemReward(item, finalChance));
                        addedItems++;
                    }
                }
                if (addedItems <= 0)
                    sender.sendMessage("§cNebyly přidány žádné itemy");
                else if (addedItems == 1)
                    sender.sendMessage("§aPřidán 1 item do databáze chestky");
                else if (addedItems < 5)
                    sender.sendMessage("§aPřidány " + addedItems + " itemy do databáze chestky");
                else
                    sender.sendMessage("§aPřidáno " + addedItems + " itemů do databáze chestky");
            });

            menu.open(sender);
        }

        @Subcommand("remove|delete|odebrat|odstranit|smazat")
        @Description("Odebere item z databáze chestky")
        @CommandCompletion("@tchest_item_ids")
        public void itemRemoveCommand(Player sender, @Flags("selected") TreasureChest tChest, Integer id) {
            PossibleItemReward reward = tChest.getPossibleReward(id);
            if (reward == null) {
                sender.sendMessage("§cTato treasure chestka neobsahuje item s ID §4" + id);
                return;
            }
            tChest.removeOnePossibleReward(id);
            Runnable itemRunnable = () -> {
                    sender.getInventory().addItem(reward.getItem());
                    sender.sendMessage(new ComponentBuilder("").appendLegacy("§aDostáváš §r").append(ChatUtils.getItemableText(true, reward.getItem())).create());
            };
            BaseComponent[] item = ChatUtils.getItemableTextWithRunnable(true, reward.getItem(), itemRunnable);
            sender.sendMessage(new ComponentBuilder("").appendLegacy("§aItem §r").append(item).appendLegacy(" §aodebrán z databáze chestky").create());
        }

        @Subcommand("list|seznam|vypsat")
        @Description("Vypíše itemy v databázi chestky")
        public void itemListCommand(Player sender, @Flags("selected") TreasureChest tChest) {
            if (tChest.getPossibleRewardsAmount() == 0) {
                sender.sendMessage("§cTreasure chestka neobsahuje žádné itemy");
                return;
            }
            ComponentBuilder builder = new ComponentBuilder("").appendLegacy("§7Seznam itemů: ");
            List<PossibleItemReward> possibleRewards = tChest.getPossibleRewards();
            int id = -1;
            for (Iterator<PossibleItemReward> iterator = possibleRewards.iterator(); iterator.hasNext(); ) {
                id++;
                PossibleItemReward reward = iterator.next();
                Runnable itemRunnable = () -> {
                    sender.getInventory().addItem(reward.getItem());
                    sender.sendMessage(new ComponentBuilder("").appendLegacy("§aDostáváš §r").append(ChatUtils.getItemableText(true, reward.getItem())).create());
                };
                builder.append("§e#" + id + ": §7");
                builder.append(ChatUtils.getItemableTextWithRunnable(true, reward.getItem(), itemRunnable));
                builder.appendLegacy(" §7(" + reward.getChance() + ")");
                if (iterator.hasNext())
                    builder.appendLegacy("§8, ");
            }
            sender.sendMessage(builder.create());
        }

        @Subcommand("chance|prob|probability|sance|pravdepodobnost")
        @Description("Nastaví šanci itemu v treasure chestce")
        @CommandCompletion("@tchest_item_ids @chance")
        public void itemChanceCommand(Player sender, @Flags("selected") TreasureChest tChest, Integer id, Percent chance) {
            PossibleItemReward reward = tChest.getPossibleReward(id);
            if (reward == null) {
                sender.sendMessage("§cTato treasure chestka neobsahuje item s ID §4" + id);
                return;
            }

            tChest.setChanceForReward(id, chance);

            Runnable itemRunnable = () -> {
                sender.getInventory().addItem(reward.getItem());
                sender.sendMessage(new ComponentBuilder("").appendLegacy("§aDostáváš §r").append(ChatUtils.getItemableText(true, reward.getItem())).create());
            };
            BaseComponent[] item = ChatUtils.getItemableTextWithRunnable(true, reward.getItem(), itemRunnable);
            sender.sendMessage(new ComponentBuilder("").appendLegacy("§aŠance itemu §r").append(item).appendLegacy(" §anastavena na §2" + chance).create());
        }

        @Subcommand("get|load|nacist|ziskat")
        @Description("Načte item z databáze treasure chestky")
        @CommandCompletion("@tchest_item_ids")
        public void itemGetCommand(Player sender, @Flags("selected") TreasureChest tChest, Integer id) {
            PossibleItemReward reward = tChest.getPossibleReward(id);
            if (reward == null) {
                sender.sendMessage("§cTato treasure chestka neobsahuje item s ID §4" + id);
                return;
            }

            sender.getInventory().addItem(reward.getItem());
            sender.sendMessage(new ComponentBuilder("").appendLegacy("§aDostáváš §r").append(ChatUtils.getItemableText(true, reward.getItem())).create());
        }
    }

    private BaseComponent[] getTreasureChestForChat(TreasureChest tChest, CommandSender sender, boolean oneLiner) {
        String locationText = Utils.locationToString(tChest.getLocation(), false, true);
        String itemsText;
        int amountOfItems = tChest.getPossibleRewardsAmount();
        itemsText = amountOfItems + " ";
        if (amountOfItems <= 0 || amountOfItems >= 5)
            itemsText += "itemů";
        else if (amountOfItems == 1)
            itemsText += "item";
        else
            itemsText += "itemy";

        if (oneLiner) {
            Runnable runnable = () -> {
                TreasureChestManager.getInstance().setSelectedChest(sender.getName(), tChest);
                sender.sendMessage("§aTreasure chestka vybrána");
            };
            BaseComponent[] components = new ComponentBuilder("").appendLegacy(locationText).appendLegacy(" §8[§7" + itemsText + "§8]").create();
            return ChatUtils.getHoverableTextWithRunnable(components, getTreasureChestForChat(tChest, sender, false), runnable);
        } else {
            ComponentBuilder builder = new ComponentBuilder("");
            Runnable getNeededItemRunnable = () -> {
                if (sender instanceof Player) {
                    ((Player) sender).getInventory().addItem(tChest.getNeededItem());
                    sender.sendMessage(new ComponentBuilder("").appendLegacy("§aDostáváš §r").append(ChatUtils.getItemableText(true, tChest.getNeededItem())).create());
                }
            };
            BaseComponent[] neededItem;
            if (tChest.getNeededItem() == null)
                neededItem = TextComponent.fromLegacyText("§cŽádný item");
            else
                neededItem = ChatUtils.getItemableTextWithRunnable(true, tChest.getNeededItem(), getNeededItemRunnable);
            String manualItemSelectText;
            int amountOfManualSelectItems = tChest.getManualSelectItems();
            manualItemSelectText = amountOfManualSelectItems + " ";
            if (amountOfManualSelectItems <= 0 || amountOfManualSelectItems >= 5)
                manualItemSelectText += "itemů";
            else if (amountOfManualSelectItems == 1)
                manualItemSelectText += "item";
            else
                manualItemSelectText += "itemy";
            builder
                    .appendLegacy("§9Pozice: §f")
                    .append(ChatUtils.getHoverableTextWithCommand(locationText, "§aKliknutím se teleportuješ k chestce", "tchest tp"))
                    .appendLegacy("\n§9Databáze: §f")
                    .append(ChatUtils.getHoverableTextWithCommand(itemsText, "§aKliknutím zobrazíš seznam itemů", "tchest item list"))
                    .appendLegacy("\n§9Cooldown: §f")
                    .appendLegacy(tChest.getCooldown() <= 0 ? "§cŽádný cooldown" : (tChest.getCooldown() + " s."))
                    .appendLegacy("\n§9Potřebný item: §f")
                    .append(neededItem)
                    .appendLegacy("\n§9Text hologramu: §f")
                    .appendLegacy(tChest.getHologramText() == null ? "§cŽádný" : tChest.getHologramText())
                    .appendLegacy("\n§9Particles: §f")
                    .appendLegacy(tChest.getParticle() == null ? "§cŽádné" : tChest.getParticle().name())
                    .appendLegacy("\n§9Barva particles: §f")
                    .appendLegacy(tChest.getParticleColor() == null ? "§cNenastavena" : tChest.getParticleColor().toString())
                    .appendLegacy("\n§9Potřebná permise: §f")
                    .appendLegacy(tChest.getNeededPermission() == null ? "§cŽádná permise" : tChest.getNeededPermission())
                    .appendLegacy("\n§9Min/Max itemů: §f")
                    .appendLegacy(tChest.getMinimumItems() + " / " + tChest.getMaximumItems())
                    .appendLegacy("\n§9Zpráva pro hráče: §f")
                    .appendLegacy(tChest.getPrivateMessage() == null ? "§cŽádná zpráva" : tChest.getPrivateMessage())
                    .appendLegacy("\n§9Zpráva pro všechny: §f")
                    .appendLegacy(tChest.getBroadcastMessage() == null ? "§cŽádná zpráva" : tChest.getBroadcastMessage())
                    .appendLegacy("\n§9Vypadnou věci, které hráč nechá uvnitř: §f")
                    .appendLegacy(tChest.isDropItemsLeftInside() ? "§lANO" : "NE")
                    .appendLegacy("\n§9Vypadnou věci po rozbití: §f")
                    .appendLegacy(tChest.isDropItemsOnDestroy() ? "§lANO" : "NE")
                    .appendLegacy("\n§9Skrytí chestky po otevření: §f")
                    .appendLegacy(tChest.getHideChestAfterOpen() <= 0 ? "NE" : ("§l" + tChest.getHideChestAfterOpen() + " s."))
                    .appendLegacy("\n§9Hráč si ručně vybere itemy: §f")
                    .appendLegacy(tChest.getManualSelectItems() <= 0 ? "NE" : ("§l" + manualItemSelectText));
            return builder.create();
        }
    }

}
