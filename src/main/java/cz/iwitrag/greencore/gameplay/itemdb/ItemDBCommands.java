package cz.iwitrag.greencore.gameplay.itemdb;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import cz.iwitrag.greencore.gameplay.chat.ChatUtils;
import cz.iwitrag.greencore.helpers.DependenciesProvider;
import cz.iwitrag.greencore.helpers.LuckPermsHelper;
import cz.iwitrag.greencore.helpers.StringHelper;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Iterator;
import java.util.Map;

@CommandAlias("itemdb|idb|itemdatabase|itemdatabaze")
@CommandPermission("itemdb")
public class ItemDBCommands extends BaseCommand {

    public static final String TEMP_SLOT = "#TEMP";
    public static final String GLOBAL_OWNER = "#GLOBAL";

    public ItemDB itemDB;

    public ItemDBCommands() {
        itemDB = DependenciesProvider.getInstance().getDefaultItemDB();
    }

    @HelpCommand
    public void baseItemDBCommand(CommandSender sender, CommandHelp help) {
        sender.sendMessage("§8" + StringHelper.getChatLine());
        sender.sendMessage("§a" + StringHelper.centerMessage("NÁPOVĚDA K DATABÁZI ITEMŮ"));
        help.showHelp();
        sender.sendMessage("§8" + StringHelper.getChatLine());
    }

    @Subcommand("list|seznam|vypsat")
    @Description("Vypíše uložené itemy v databázi")
    public void listCommand(CommandSender sender, @Optional String nick) {
        if (nick == null)
            nick = sender.getName();
        Map<String, ItemStack> items = itemDB.getItems(nick, null);
        if (items.isEmpty()) {
            if (nick.equalsIgnoreCase(GLOBAL_OWNER))
                sender.sendMessage("§cŽádné globální itemy nebyly nalezeny");
            else if (nick.equalsIgnoreCase(sender.getName()))
                sender.sendMessage("§cNemáš žádné uložené itemy");
            else
                sender.sendMessage("§cŽádné uložené itemy hráče §4" + nick + " §cnebyly nalezeny");
        }
        else {
            sender.sendMessage("§8" + StringHelper.getChatLine());
            if (nick.equalsIgnoreCase(GLOBAL_OWNER))
                sender.sendMessage("§a" + StringHelper.centerMessage("Globální uložené itemy"));
            else if (nick.equalsIgnoreCase(sender.getName()))
                sender.sendMessage("§a" + StringHelper.centerMessage("Tvé uložené itemy"));
            else
                sender.sendMessage("§a" + StringHelper.centerMessage("Uložené itemy hráče " + nick));
            ComponentBuilder builder = new ComponentBuilder("");
            for (Iterator<Map.Entry<String, ItemStack>> iterator = items.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, ItemStack> entry = iterator.next();
                String itemName = entry.getKey().split("\\.")[1];
                ItemStack itemStack = entry.getValue();
                builder.append(ChatUtils.getItemableText("§f" + itemName, itemStack));
                if (iterator.hasNext())
                    builder.append(", ").color(ChatColor.GRAY);
            }
            sender.sendMessage(builder.create());
            sender.sendMessage("§8" + StringHelper.getChatLine());
        }
    }

    @Subcommand("save|store|add|ulozit|uloz|pridat")
    @Description("Uloží item v ruce do databáze")
    public void saveCommand(Player sender, @Flags("main_hand") ItemStack hand, @Optional String item) {
        boolean isAdmin = LuckPermsHelper.playerHasPermission(sender.getName(), "itemdb.admin");
        if (item != null) {
            if (!isAdmin && (item.contains("#") || item.contains("."))) {
                sender.sendMessage("§cNázev pro uložení nemůže obsahovat znak # ani tečku");
                return;
            }
            if (item.contains("@")) {
                sender.sendMessage("§cNázev pro uložení nemůže obsahovat zavináč");
                return;
            }
        }

        if (item == null)
            item = TEMP_SLOT;
        String owner = item.contains(".") ? item.split("\\.")[0] : sender.getName();
        item = item.contains(".") ? item.split("\\.")[1] : item;
        itemDB.addItem(owner, item, hand);

        if (item.equals(TEMP_SLOT))
            sender.sendMessage("§aItem úspěšně uložen do bezejmenného slotu");
        else
            sender.sendMessage("§aItem úspěšně uložen pod názvem §2" + item);
        if (!owner.equalsIgnoreCase(sender.getName())) {
            if (owner.equalsIgnoreCase(GLOBAL_OWNER))
                sender.sendMessage("§2(globálně)");
            else
                sender.sendMessage("§2(hráči " + owner + ")");
        }
    }

    @Subcommand("load|get|nacist|ziskat")
    @Description("Načte item s daným názvem z databáze")
    public void loadCommand(CommandSender sender, @Optional String item, @Optional Integer amount) {
        giveCommand(sender, sender.getName(), item, amount);
    }

    @Subcommand("give|dat|givnout")
    @Description("Dá item s daným názvem z databáze nějakému hráči")
    public void giveCommand(CommandSender sender, String target, @Optional String item, @Optional Integer amount) {
        boolean senderIsConsole = sender instanceof ConsoleCommandSender;

        Player targetPlayer = Bukkit.getPlayerExact(target);
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            sender.sendMessage("§cHráč §4" + target + " §cnení ve hře");
            return;
        }

        if (item == null)
            item = TEMP_SLOT;
        item = item.contains(".") ? item.split("\\.")[1] : item;

        String owner;
        if (item.contains("."))
            owner = item.split("\\.")[0];
        else {
            if (senderIsConsole)
                owner = GLOBAL_OWNER;
            else
                owner = sender.getName();
        }

        ItemStack itemStack = itemDB.getItemExact(owner, item);
        if (itemStack == null) {
            owner = GLOBAL_OWNER;
            itemStack = itemDB.getItemExact(owner, item);
            if (itemStack == null) {
                sender.sendMessage("§cItem pod tímto názvem neexistuje");
                return;
            }
        }
        if (amount == null || amount < 1)
            amount = 1;

        boolean notEnoughSpace = false;
        PlayerInventory inv = targetPlayer.getInventory();
        for (int i = 0; i < amount; i++) {
            Map<Integer, ItemStack> remainder = inv.addItem(itemStack);
            if (!remainder.isEmpty()) {
                targetPlayer.getWorld().dropItemNaturally(targetPlayer.getLocation(), remainder.get(0));
                notEnoughSpace = true;
            }
        }

        if (targetPlayer.equals(sender)) {
            sender.sendMessage("§aDostáváš §2" + amount + "x " + owner + "." + item);
            if (notEnoughSpace) {
                sender.sendMessage("§eNebylo dost místa v inventáři, zbytek věcí je na zemi");
            }
        }
        else {
            sender.sendMessage("§aHráč §2" + targetPlayer.getName() + " §adostává §2" + amount + "x " + owner + "." + item);
            targetPlayer.sendMessage("§aDostáváš §2" + amount + "x " + item);
            if (notEnoughSpace) {
                sender.sendMessage("§eHráč neměl dost místa v inventáři, zbytek věcí je na zemi");
                targetPlayer.sendMessage("§eNemáš dost místa v inventáři, zbytek věcí je na zemi");
            }
        }
    }

    @Subcommand("remove|delete|odebrat|odstranit|smazat")
    @Description("Odstraní daný item z databáze")
    public void removeCommand(CommandSender sender, @Optional String item) {
        boolean isAdmin = LuckPermsHelper.playerHasPermission(sender.getName(), "itemdb.admin");
        if (item != null) {
            if (!isAdmin && (item.contains("#") || item.contains("."))) {
                sender.sendMessage("§cNázev pro uložení nemůže obsahovat znak # ani tečku");
                return;
            }
            if (item.contains("@")) {
                sender.sendMessage("§cNázev pro uložení nemůže obsahovat zavináč");
                return;
            }
        }
        if (item == null)
            item = TEMP_SLOT;
        String owner = item.contains(".") ? item.split("\\.")[0] : sender.getName();
        item = item.contains(".") ? item.split("\\.")[1] : item;

        boolean success = itemDB.removeItem(owner, item);
        if (!success) {
            sender.sendMessage("§cItem pod tímto názvem neexistuje nebo nemáš práva na jeho odebrání");
            return;
        }
        if (item.equals(TEMP_SLOT))
            sender.sendMessage("§aItem úspěšně odebrán z bezejmenného slotu");
        else
            sender.sendMessage("§aItem §2" + item + " §aúspěšně odebrán");
        if (!owner.equalsIgnoreCase(sender.getName())) {
            if (owner.equalsIgnoreCase(GLOBAL_OWNER))
                sender.sendMessage("§2(globálně)");
            else
                sender.sendMessage("§2(hráči " + owner + ")");
        }
    }

    @Subcommand("search|find|seek|vyhledat|hledat|najit")
    @Description("Vyhledá item v databázi")
    public void searchCommand(CommandSender sender, String item, @Optional String nick) {
        // prohledá všechny databáze a zkusí najít item podle názvu, zadáním nicku můžete hledání omezit na něčí databázi
        Map<String, ItemStack> items = itemDB.getItems(nick, item);
        if (items.isEmpty()) {
            sender.sendMessage("§cNebyly nalezeny žádné itemy");
        }
        else {
            sender.sendMessage("§8" + StringHelper.getChatLine());
            if (nick == null)
                sender.sendMessage("§a" + StringHelper.centerMessage("Nalezené předměty"));
            else
                sender.sendMessage("§a" + StringHelper.centerMessage("Nalezené předměty (" + nick + ")"));
            ComponentBuilder builder = new ComponentBuilder("");
            for (Iterator<Map.Entry<String, ItemStack>> iterator = items.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, ItemStack> entry = iterator.next();
                String itemName;
                if (nick == null)
                    itemName = entry.getKey();
                else
                    itemName = entry.getKey().split("\\.")[1];
                ItemStack itemStack = entry.getValue();
                builder.append(ChatUtils.getItemableText("§f" + itemName, itemStack));
                if (iterator.hasNext())
                    builder.append(", ").color(ChatColor.GRAY);
            }
            sender.sendMessage(builder.create());
            sender.sendMessage("§8" + StringHelper.getChatLine());
        }
    }

}