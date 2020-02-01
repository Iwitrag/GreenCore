package cz.iwitrag.greencore.gameplay.zones;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import cz.iwitrag.greencore.gameplay.zones.actions.Action;
import cz.iwitrag.greencore.gameplay.zones.actions.CommandAction;
import cz.iwitrag.greencore.gameplay.zones.actions.DamageAction;
import cz.iwitrag.greencore.gameplay.zones.actions.MessageAction;
import cz.iwitrag.greencore.gameplay.zones.actions.NothingAction;
import cz.iwitrag.greencore.gameplay.zones.actions.PotionEffectAction;
import cz.iwitrag.greencore.gameplay.zones.actions.TeleportAction;
import cz.iwitrag.greencore.gameplay.zones.flags.EnderPortalFlag;
import cz.iwitrag.greencore.gameplay.zones.flags.Flag;
import cz.iwitrag.greencore.gameplay.zones.flags.MineFlag;
import cz.iwitrag.greencore.gameplay.zones.flags.ParticlesFlag;
import cz.iwitrag.greencore.gameplay.zones.flags.TpFlag;
import cz.iwitrag.greencore.helpers.DependenciesProvider;
import cz.iwitrag.greencore.helpers.StringHelper;
import cz.iwitrag.greencore.helpers.Utils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@CommandAlias("zone|zona|zones|zony")
@CommandPermission("zone.admin")
public class ZoneCommands extends BaseCommand {

    @HelpCommand
    public void baseZoneCommand(CommandSender sender, CommandHelp help) {
        sender.sendMessage("§8" + StringHelper.getChatLine());
        sender.sendMessage(StringHelper.centerMessage("§aNÁPOVĚDA K ZÓNÁM"));
        help.showHelp();
        sender.sendMessage("§8" + StringHelper.getChatLine());
    }

    @Subcommand("create|new|define|vytvorit|nova|definovat")
    @Description("Vytvoří novou zónu")
    public void createCommand(Player sender, String name) {
        Location p1 = DependenciesProvider.getInstance().getWorldEditSelection(sender.getName(), true);
        Location p2 = DependenciesProvider.getInstance().getWorldEditSelection(sender.getName(), false);
        if (p1 == null || p2 == null) {
            sender.sendMessage("§cPřed vytvořením zóny musíš vybrat oblast!");
            return;
        }
        Zone zone = new Zone(name, p1, p2);
        try {
            ZoneManager.getInstance().addZone(zone);
        } catch (ZoneException e) {
            sender.sendMessage("§c" + e.getMessage());
        }
        sender.sendMessage("§aZóna §2" + name + " §aúspěšně vytvořena!");
    }

    @Subcommand("near|around|blizko|pobliz|kolem|okolo")
    @Description("Vypíše zóny v blízkosti")
    public void nearCommand(Player sender, @Default("50") Integer radius) {
        Set<Zone> zones = ZoneManager.getInstance().getZones();
        if (zones.isEmpty()) {
            sender.sendMessage("§cŽádné zóny nejsou vytvořeny.");
            return;
        }
        if (radius < 1) {
            sender.sendMessage("§cNeplatný poloměr.");
            return;
        }
        Set<Zone> zonesNear = new LinkedHashSet<>();
        for (Zone iteratedZone : zones) {
            if (sender.getWorld().equals(iteratedZone.getPoint1().getWorld()) && sender.getLocation().distance(iteratedZone.getCenterPoint()) < radius)
                zonesNear.add(iteratedZone);
        }
        if (zonesNear.isEmpty()) {
            sender.sendMessage("§cŽádné zóny v okolí §4" + radius + " §cbloků nebyly nalezeny.");
            return;
        }

        sender.sendMessage("§8" + StringHelper.getChatLine());
        sender.sendMessage(StringHelper.centerMessage("§aZÓNY V OKOLÍ §2" + radius + " §aBLOKŮ"));
        sender.sendMessage("§f" + StringUtils.join(zonesNear, "§7, §f"));
        sender.sendMessage("§8" + StringHelper.getChatLine());
    }

    @Subcommand("list|seznam")
    @Description("Vypíše seznam zón")
    public void listCommand(CommandSender sender, @Optional String worldName) {
        if (worldName == null) {
            Set<Zone> zones = ZoneManager.getInstance().getZones();
            if (zones.isEmpty()) {
                sender.sendMessage("§cŽádné zóny nejsou vytvořeny.");
                return;
            }
            sender.sendMessage("§8" + StringHelper.getChatLine());
            sender.sendMessage(StringHelper.centerMessage("§aSEZNAM VŠECH ZÓN"));
            sender.sendMessage("§f" + StringUtils.join(zones, "§7, §f"));
            sender.sendMessage("§8" + StringHelper.getChatLine());
        } else {
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                sender.sendMessage("§cSvět §4" + worldName + " §cneexistuje.");
                return;
            }
            Set<Zone> zones = new LinkedHashSet<>();
            for (Zone iteratedZone : ZoneManager.getInstance().getZones()) {
                if (iteratedZone.getPoint1().getWorld().equals(world))
                    zones.add(iteratedZone);
            }
            if (zones.isEmpty()) {
                sender.sendMessage("§cŽádné zóny ve světě §4" + world.getName() + " §cnejsou vytvořeny.");
                return;
            }
            sender.sendMessage("§8" + StringHelper.getChatLine());
            sender.sendMessage(StringHelper.centerMessage("§aSEZNAM ZÓN VE SVĚTĚ " + world.getName()));
            sender.sendMessage("§f" + StringUtils.join(zones, "§7, §f"));
            sender.sendMessage("§8" + StringHelper.getChatLine());
        }
    }

    @Subcommand("tp|teleport")
    @Description("Teleportuje hráče k zóně")
    public void tpCommand(Player sender, String zoneName) {
        Zone zone = ZoneManager.getInstance().getZone(zoneName);
        if (zone == null) {
            sender.sendMessage("§cZóna §4" + zoneName + " §cneexistuje.");
            return;
        }
        sender.sendMessage("§aTeleportuješ se k zóně §2" + zone.getName());
        sender.teleport((zone.getFlagOrDefault(TpFlag.class)).getTpLocation());
    }

    @Subcommand("info|about|informace")
    @Description("Vypíše informace o zóně")
    public void infoCommand(CommandSender sender, String zoneName) {
        Zone zone = ZoneManager.getInstance().getZone(zoneName);
        if (zone == null) {
            sender.sendMessage("§cZóna §4" + zoneName + " §cneexistuje.");
            return;
        }
        sender.sendMessage("§8" + StringHelper.getChatLine());
        sender.sendMessage(StringHelper.centerMessage("§aOBECNÉ INFORMACE O ZÓNĚ " + zone.getName()));
        sender.sendMessage("§9Priorita: §f" + zone.getPriority());
        sender.sendMessage("§9Svět: §f" + zone.getPoint1().getWorld().getName());
        sender.sendMessage("§9Velikost zóny: X: §f" + zone.getSizeInX() + "§9, Y: §f" + zone.getSizeInY() + "§9, Z: §f" + zone.getSizeInZ() + "§9, celkem: §f" + zone.getSize());
        sender.sendMessage("§9Bod 1: " + StringHelper.locationToString(zone.getPoint1(), false, "§9", "§f"));
        sender.sendMessage("§9Bod 2: " + StringHelper.locationToString(zone.getPoint2(), false, "§9", "§f"));
        int actions = zone.getActions().size();
        if (actions > 0)
            sender.sendMessage("§7Seznam akcí (" + actions + "): §f/" + getExecCommandLabel() + " actions list " + zoneName);
        sender.sendMessage("§7Seznam vlajek: §f/" + getExecCommandLabel() + " flags list " + zoneName);
        sender.sendMessage("§8" + StringHelper.getChatLine());
    }

    @Subcommand("priority|setpriority|priorita")
    @Description("Změní prioritu zóny")
    public void priorityCommand(CommandSender sender, String zoneName, Integer priority) {
        Zone zone = ZoneManager.getInstance().getZone(zoneName);
        if (zone == null) {
            sender.sendMessage("§cZóna §4" + zoneName + " §cneexistuje.");
            return;
        }
        zone.setPriority(priority);
        sender.sendMessage("§aPriorita zóny §2" + zone.getName() + " §anastavena na §2" + priority);
    }

    @Subcommand("rename|name|setname|nazev|prejmenovat|prejmenuj")
    @Description("Přejmenuje zónu")
    public void renameCommand(CommandSender sender, String zoneName, String newName) {
        Zone zone = ZoneManager.getInstance().getZone(zoneName);
        if (zone == null) {
            sender.sendMessage("§cZóna §4" + zoneName + " §cneexistuje.");
            return;
        }
        if (ZoneManager.getInstance().getZone(newName) != null) {
            sender.sendMessage("§cNázev §4" + newName + " §cse již používá.");
            return;
        }
        sender.sendMessage("§aZóna §2" + zone.getName() + " §apřejmenována na §2" + newName);
        zone.setName(newName);
    }

    @Subcommand("redefine|area|setarea|oblast")
    @Description("Změní oblast zóny podle výběru WorldEditu")
    public void redefineCommand(CommandSender sender, String zoneName) {
        Zone zone = ZoneManager.getInstance().getZone(zoneName);
        if (zone == null) {
            sender.sendMessage("§cZóna §4" + zoneName + " §cneexistuje.");
            return;
        }
        Location p1 = DependenciesProvider.getInstance().getWorldEditSelection(sender.getName(), true);
        Location p2 = DependenciesProvider.getInstance().getWorldEditSelection(sender.getName(), false);
        if (p1 == null || p2 == null) {
            sender.sendMessage("§cPřed změnou oblasti zóny musíš vybrat oblast!");
            return;
        }
        zone.setPoints(p1, p2);
        sender.sendMessage("§aOblast zóny §2" + zone.getName() + " §azměněna!");
    }

    @Subcommand("delete|remove|smazat|odstranit")
    @Description("Odstraní zónu")
    public void deleteCommand(CommandSender sender, String zoneName) {
        Zone zone = ZoneManager.getInstance().getZone(zoneName);
        if (zone == null) {
            sender.sendMessage("§cZóna §4" + zoneName + " §cneexistuje.");
            return;
        }
        if (ZoneExecutor.getInstance().isZoneBeingExecuted(zone)) {
            sender.sendMessage("§cV zóně §4" + zoneName + " §cprávě probíhá série akcí, zkus to za chvíli.");
            return;
        }
        ZoneManager.getInstance().deleteZone(zone);
        sender.sendMessage("§aZóna §2" + zone.getName() + " §abyla odstraněna!");
    }

    @Subcommand("action|actions|akce")
    public class actionCommands extends BaseCommand {

        @HelpCommand
        public void baseActionCommand(CommandSender sender, CommandHelp help) {
            sender.sendMessage("§8" + StringHelper.getChatLine());
            sender.sendMessage(StringHelper.centerMessage("§aNÁPOVĚDA K ZÓNÁM"));
            help.showHelp();
            sender.sendMessage("§8" + StringHelper.getChatLine());
        }

        @Subcommand("list|seznam|vypsat")
        @Description("Vypíše všechny akce zóny")
        public void actionsListCommand(CommandSender sender, String zoneName) {
            Zone zone = ZoneManager.getInstance().getZone(zoneName);
            if (zone == null) {
                sender.sendMessage("§cZóna §4" + zoneName + " §cneexistuje.");
                return;
            }
            sender.sendMessage("§8" + StringHelper.getChatLine());
            sender.sendMessage(StringHelper.centerMessage("§aAKCE ZÓNY " + zone.getName()));
            List<Action> actions = zone.getActions();
            if (actions.isEmpty()) {
                sender.sendMessage("§cŽádné akce nejsou přidány");
            } else {
                for (int i = 0; i < actions.size(); i++) {
                    Action action = actions.get(i);
                    sender.sendMessage("§e[#" + i + "] §9tick: §b" + action.getTime() + " §a" + action.getDescription());
                }
            }
            sender.sendMessage("§8" + StringHelper.getChatLine());
        }

        @Subcommand("add|create|new|define|pridat|vytvorit|nova|definovat")
        @Description("Přidá novou akci do zóny")
        public void actionsAddCommand(CommandSender sender, String zoneName, int ticks, String type, @Optional String params) {
            Zone zone = ZoneManager.getInstance().getZone(zoneName);
            if (zone == null) {
                sender.sendMessage("§cZóna §4" + zoneName + " §cneexistuje.");
                return;
            }
            if (ZoneExecutor.getInstance().isZoneBeingExecuted(zone)) {
                sender.sendMessage("§cV zóně §4" + zoneName + " §cprávě probíhá série akcí, zkus to za chvíli.");
                return;
            }
            if (ticks < 0) {
                sender.sendMessage("§cČas akce nemůže být §4" + zoneName + " §cticků.");
                return;
            }
            Action addedAction;
            try {
                Map<String, List<String>> keywords = getActionKeywords();
                if (keywords.get("commandAction").contains(type.toLowerCase())) {
                    addedAction = constructAction(CommandAction.class, params);
                } else if (keywords.get("damageAction").contains(type.toLowerCase())) {
                    addedAction = constructAction(DamageAction.class, params);
                } else if (keywords.get("messageAction").contains(type.toLowerCase())) {
                    addedAction = constructAction(MessageAction.class, params);
                } else if (keywords.get("nothingAction").contains(type.toLowerCase())) {
                    addedAction = constructAction(NothingAction.class, params);
                } else if (keywords.get("potionEffectAction").contains(type.toLowerCase())) {
                    addedAction = constructAction(PotionEffectAction.class, params);
                } else if (keywords.get("teleportAction").contains(type.toLowerCase())) {
                    addedAction = constructAction(TeleportAction.class, params);
                } else {
                    List<String> firstKeywords = new ArrayList<>();
                    for (String key : keywords.keySet()) {
                        firstKeywords.add(keywords.get(key).get(0));
                    }
                    sender.sendMessage("§cNeplatný typ akce, dostupné jsou tyto typy akcí: §4" + StringUtils.join(firstKeywords, "§c, §4"));
                    return;
                }
            } catch (ZoneException e) {
                sender.sendMessage(e.getMessage());
                return;
            }
            addedAction.setTime(ticks);
            zone.addAction(addedAction);
            sender.sendMessage("§aAkce úspěšně přidána k zóně §2" + zoneName + "§a!");
        }

        @Subcommand("edit|change|param|params|parameter|parameters|upravit|parametr|parametry")
        @Description("Upraví parametry akce v zóně")
        public void actionsEditCommand(CommandSender sender, String zoneName, int id, @Optional String params) {
            Zone zone = ZoneManager.getInstance().getZone(zoneName);
            if (zone == null) {
                sender.sendMessage("§cZóna §4" + zoneName + " §cneexistuje.");
                return;
            }
            if (ZoneExecutor.getInstance().isZoneBeingExecuted(zone)) {
                sender.sendMessage("§cV zóně §4" + zoneName + " §cprávě probíhá série akcí, zkus to za chvíli.");
                return;
            }
            Action action = zone.getAction(id);
            if (action == null) {
                sender.sendMessage("§cV zóně §4" + zoneName + " §cneexistuje akce s ID §4" + id);
                return;
            }
            Action constructedAction;
            try {
                constructedAction = constructAction(action.getClass(), params);
            } catch (ZoneException e) {
                sender.sendMessage(e.getMessage());
                return;
            }
            zone.removeAction(action);
            zone.addActionToId(constructedAction, id);
            sender.sendMessage("§aAkce s ID §2" + id + " §av zóně §2" + zoneName + " §abyla úspěšně upravena!");
        }

        @Subcommand("time|tick|ticks|cas|ticky")
        @Description("Změní čas akce v zóně")
        public void actionsTimeCommand(CommandSender sender, String zoneName, int id, int ticks) {
            Zone zone = ZoneManager.getInstance().getZone(zoneName);
            if (zone == null) {
                sender.sendMessage("§cZóna §4" + zoneName + " §cneexistuje.");
                return;
            }
            if (ZoneExecutor.getInstance().isZoneBeingExecuted(zone)) {
                sender.sendMessage("§cV zóně §4" + zoneName + " §cprávě probíhá série akcí, zkus to za chvíli.");
                return;
            }
            if (ticks < 0) {
                sender.sendMessage("§cČas nelze nastavit na §4" + ticks + " §cticků.");
                return;
            }
            Action action = zone.getAction(id);
            if (action == null) {
                sender.sendMessage("§cV zóně §4" + zoneName + " §cneexistuje akce s ID §4" + id);
                return;
            }
            zone.removeAction(action);
            action.setTime(ticks);
            zone.addAction(action); // Removed, modified and added to force reordering of all actions inside zone
            sender.sendMessage("§aČas akce s ID §2" + id + " §av zóně §2" + zone.getName() + " §azměněn na §2" + ticks + " §aticků!");
        }

        @Subcommand("id|index|identifikator|number|cislo")
        @Description("Změní ID akce v zóně")
        public void actionsIdCommand(CommandSender sender, String zoneName, int oldId, int newId) {
            Zone zone = ZoneManager.getInstance().getZone(zoneName);
            if (zone == null) {
                sender.sendMessage("§cZóna §4" + zoneName + " §cneexistuje.");
                return;
            }
            if (ZoneExecutor.getInstance().isZoneBeingExecuted(zone)) {
                sender.sendMessage("§cV zóně §4" + zoneName + " §cprávě probíhá série akcí, zkus to za chvíli.");
                return;
            }
            Action action = zone.getAction(oldId);
            if (action == null) {
                sender.sendMessage("§cV zóně §4" + zoneName + " §cneexistuje akce s ID §4" + oldId);
                return;
            }
            zone.removeAction(action);
            int finalId = zone.addActionToId(action, newId);
            sender.sendMessage("§aID akce změněn z §2" + oldId + " §ana §2" + finalId + " §av zóně §2" + zone.getName() + "§a!");
        }

        @Subcommand("delete|remove|smazat|odstranit")
        @Description("Odstraní akci v zóně")
        public void actionsDeleteCommand(CommandSender sender, String zoneName, int id) {
            Zone zone = ZoneManager.getInstance().getZone(zoneName);
            if (zone == null) {
                sender.sendMessage("§cZóna §4" + zoneName + " §cneexistuje.");
                return;
            }
            if (ZoneExecutor.getInstance().isZoneBeingExecuted(zone)) {
                sender.sendMessage("§cV zóně §4" + zoneName + " §cprávě probíhá série akcí, zkus to za chvíli.");
                return;
            }
            if (zone.getActionsAmount() == 0) {
                sender.sendMessage("§cZóna §4" + zoneName + " §cnemá nastaveny žádné akce.");
                return;
            }
            Action action = zone.getAction(id);
            if (action == null) {
                sender.sendMessage("§cV zóně §4" + zoneName + " §cneexistuje akce s ID §4" + id);
                return;
            }
            zone.removeAction(action);
            sender.sendMessage("§aAkce s ID §2" + id + " §av zóně §2" + zone.getName() + " §abyla odstraněna!");
        }

        @Subcommand("purge|deleteall|removeall|smazatvse|odstranitvse")
        @Description("Odstraní všechny akce v zóně")
        public void actionsPurgeCommand(CommandSender sender, String zoneName) {
            Zone zone = ZoneManager.getInstance().getZone(zoneName);
            if (zone == null) {
                sender.sendMessage("§cZóna §4" + zoneName + " §cneexistuje.");
                return;
            }
            if (ZoneExecutor.getInstance().isZoneBeingExecuted(zone)) {
                sender.sendMessage("§cV zóně §4" + zoneName + " §cprávě probíhá série akcí, zkus to za chvíli.");
                return;
            }
            if (zone.getActionsAmount() == 0) {
                sender.sendMessage("§cZóna §4" + zoneName + " §cnemá nastaveny žádné akce.");
                return;
            }
            zone.removeAllActions();
            sender.sendMessage("§aVšechny akce v zóně §2" + zone.getName() + " §abyly odstraněny!");
        }

    }

    @Subcommand("flag|flags|flagy|vlajka|vlajky")
    public class flagCommands extends BaseCommand {

        @HelpCommand
        public void baseFlagCommand(CommandSender sender, CommandHelp help) {
            sender.sendMessage("§8" + StringHelper.getChatLine());
            sender.sendMessage(StringHelper.centerMessage("§aNÁPOVĚDA K ZÓNÁM"));
            help.showHelp();
            sender.sendMessage("§8" + StringHelper.getChatLine());
        }

        @Subcommand("list|seznam|vypsat")
        @Description("Vypíše aktuální flagy zóny")
        public void flagsListCommand(CommandSender sender, String zoneName) {
            Zone zone = ZoneManager.getInstance().getZone(zoneName);
            if (zone == null) {
                sender.sendMessage("§cZóna §4" + zoneName + " §cneexistuje.");
                return;
            }
            sender.sendMessage("§8" + StringHelper.getChatLine());
            sender.sendMessage(StringHelper.centerMessage("§aVLAJKY ZÓNY " + zone.getName()));

            sender.sendMessage("§bTeleport");
            String color = zone.hasFlag(TpFlag.class) ? "§e" : "§f";
            TpFlag tpFlag = zone.getFlagOrDefault(TpFlag.class);
            sender.sendMessage("§9Pozice: " + StringHelper.locationToString(tpFlag.getTpLocation(), true, "§9", color));

            sender.sendMessage("§bEnder portály");
            EnderPortalFlag enderPortalFlag = zone.getFlagOrDefault(EnderPortalFlag.class);
            color = zone.hasFlag(EnderPortalFlag.class) ? "§e" : "§f";
            sender.sendMessage("§9Fungují: " + color + (enderPortalFlag.isEnderPortalEnabled() ? "ANO" : "NE"));

            sender.sendMessage("§bParticly");
            ParticlesFlag particlesFlag = zone.getFlagOrDefault(ParticlesFlag.class);
            color = zone.hasFlag(ParticlesFlag.class) ? "§e" : "§f";
            sender.sendMessage("§9Typ: " + color + (particlesFlag.getParticle() == null ? "---" : particlesFlag.getParticle().name()));
            sender.sendMessage("§9Hustota: " + color + particlesFlag.getDensity());
            if (Utils.isParticleColorizable(particlesFlag.getParticle())) {
                sender.sendMessage("§9Barva: Č: " + color + particlesFlag.getRed() + "§9, Z: " + color + particlesFlag.getGreen() + "§9, M: " + color + particlesFlag.getBlue());
            }

            sender.sendMessage("§bDoly");
            MineFlag mineFlag = zone.getFlagOrDefault(MineFlag.class);
            color = zone.hasFlag(MineFlag.class) ? "§e" : "§f";
            sender.sendMessage("§9Bloky: " + color + mineFlag.getBlocksAsString());
            sender.sendMessage("§9Doplnění při: " + color + mineFlag.getRegenPercentage() + " % §9bloků");
            sender.sendMessage("§8" + StringHelper.getChatLine());
        }

        @Subcommand("set|change|define|nastavit|zmenit|definovat")
        @Description("Nastaví flag v zóně")
        public void flagsSetCommand(CommandSender sender, String zoneName, String type, @Optional String params) {
            Zone zone = ZoneManager.getInstance().getZone(zoneName);
            if (zone == null) {
                sender.sendMessage("§cZóna §4" + zoneName + " §cneexistuje.");
                return;
            }

            Map<String, List<String>> keywords = getFlagKeywords();
            if (keywords.get("enderPortalFlag").contains(type.toLowerCase())) {
                if (params == null) {
                    sender.sendMessage("§cMusíš zadat, zda mají být ender portály zapnuty (on) nebo vypnuty (off)");
                    return;
                }
                String firstWord = params.split(" ")[0];
                if (Arrays.asList("yes", "1", "true", "on", "enable", "allow", "ano", "zap", "zapnout", "povolit", "povol").contains(firstWord.toLowerCase())) {
                    EnderPortalFlag flag = new EnderPortalFlag();
                    flag.setEnderPortalEnabled(true);
                    zone.setFlag(flag);
                    sender.sendMessage("§aEnder portály v zóně §2" + zoneName + " §azapnuty.");
                } else if (Arrays.asList("no", "0", "false", "off", "disable", "block", "ne", "vyp", "vypnout", "zakazat", "zakaz").contains(firstWord.toLowerCase())) {
                    EnderPortalFlag flag = new EnderPortalFlag();
                    flag.setEnderPortalEnabled(false);
                    zone.setFlag(flag);
                    sender.sendMessage("§aEnder portály v zóně §2" + zoneName + " §avypnuty.");
                } else {
                    sender.sendMessage("§cMusíš zadat, zda mají být ender portály zapnuty (on) nebo vypnuty (off)");
                }
            } else if (keywords.get("mineFlag").contains(type.toLowerCase())) {
                Map<String, List<String>> paramWords = new HashMap<>();
                paramWords.put("blocks", Arrays.asList("blocks", "block", "bloky", "blok"));
                paramWords.put("regenPercent", Arrays.asList("regenpercent", "regenpercents", "regen", "percent", "percents", "procent", "procenta", "regenerace"));
                List<String> firstParamWords = new ArrayList<>();
                for (String word : paramWords.keySet()) {
                    firstParamWords.add(paramWords.get(word).get(0));
                }

                if (params == null) {
                    sender.sendMessage("§cMusíš zadat parametr, který chceš nastavit, ve formátu <parametr> [hodnota]");
                    sender.sendMessage("§cDostupné jsou tyto parametry: §4" + StringUtils.join(firstParamWords, "§c, §4"));
                    return;
                }

                String[] splitWords = params.split(" ");

                if (paramWords.get("blocks").contains(splitWords[0].toLowerCase())) {
                    if (splitWords.length < 2) {
                        sender.sendMessage("§cMusíš zadat distribuci bloků pro doplňování v dolech");
                        sender.sendMessage("§cPříklad: §490%stone,10%iron_ore");
                        return;
                    }
                    MineFlag flag = zone.getFlagOrDefault(MineFlag.class);
                    try {
                        flag.setBlocksFromString(splitWords[1]);
                    } catch (ZoneException e) {
                        sender.sendMessage(e.getMessage());
                        return;
                    }
                    zone.setFlag(flag);
                    sender.sendMessage("§aBloky dolu zóny §2" + zoneName + " §abyly nastaveny.");
                } else if (paramWords.get("regenPercent").contains(splitWords[0].toLowerCase())) {
                    if (splitWords.length < 2) {
                        sender.sendMessage("§cMusíš zadat minimum procent bloků pro regeneraci dolů");
                        return;
                    }
                    MineFlag flag = zone.getFlagOrDefault(MineFlag.class);
                    double percentageParam;
                    try {
                        percentageParam = Double.parseDouble(splitWords[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cZadaná min. procenta pro regenerace dolů nejsou platná");
                        return;
                    }
                    if (percentageParam < 0) {
                        percentageParam = 0;
                    }
                    if (percentageParam > 100)
                        percentageParam = 100;
                    flag.setRegenPercentage(percentageParam);
                    zone.setFlag(flag);
                    sender.sendMessage("§aMin. procent bloků pro regeneraci dolu zóny §2" + zoneName + " §abyl nastaven na §2" + percentageParam + "§a%.");
                } else {
                    sender.sendMessage("§cNeznámý parametr, dostupné jsou tyto parametry: §4" + StringUtils.join(firstParamWords, "§c, §4"));
                }
            } else if (keywords.get("particlesFlag").contains(type.toLowerCase())) {
                Map<String, List<String>> paramWords = new HashMap<>();
                paramWords.put("particle", Arrays.asList("particles", "particle", "type", "particly", "partikly", "typ", "castice", "efekt", "efekty"));
                paramWords.put("density", Arrays.asList("density", "amount", "count", "hustota", "mnozstvi", "pocet"));
                paramWords.put("red", Arrays.asList("red", "r", "cerveny", "cervena", "cervene"));
                paramWords.put("green", Arrays.asList("green", "g", "zeleny", "zelena", "zelene"));
                paramWords.put("blue", Arrays.asList("blue", "b", "modry", "modra", "modre"));
                List<String> firstParamWords = new ArrayList<>();
                for (String word : paramWords.keySet()) {
                    firstParamWords.add(paramWords.get(word).get(0));
                }

                if (params == null) {
                    sender.sendMessage("§cMusíš zadat parametr, který chceš nastavit, ve formátu <parametr> [hodnota]");
                    sender.sendMessage("§cDostupné jsou tyto parametry: §4" + StringUtils.join(firstParamWords, "§c, §4"));
                    return;
                }

                String[] splitWords = params.split(" ");

                if (paramWords.get("particle").contains(splitWords[0].toLowerCase())) {
                    if (splitWords.length < 2) {
                        sender.sendMessage("§cMusíš zadat název zobrazované částice");
                        sender.sendMessage("§cBarevná může být částice §4REDSTONE");
                        return;
                    }
                    ParticlesFlag flag = zone.getFlagOrDefault(ParticlesFlag.class);
                    Particle particle;
                    try {
                        particle = Particle.valueOf(splitWords[1].toUpperCase());
                    } catch (IllegalArgumentException | NullPointerException e) {
                        sender.sendMessage("§cZadána neznámá částice");
                        return;
                    }
                    flag.setParticle(particle);
                    zone.setFlag(flag);
                    sender.sendMessage("§aZobrazovaná částice zóny §2" + zoneName + " §anastavena na §2" + particle.name() + "§a.");
                } else if (paramWords.get("density").contains(splitWords[0].toLowerCase())) {
                    if (splitWords.length < 2) {
                        sender.sendMessage("§cMusíš zadat hustotu zobrazovaných částic");
                        return;
                    }
                    ParticlesFlag flag = zone.getFlagOrDefault(ParticlesFlag.class);
                    int densityParam;
                    try {
                        densityParam = Integer.parseInt(splitWords[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cZadaná hustota částic není platná");
                        return;
                    }
                    if (densityParam < 0) {
                        densityParam = 0;
                    }
                    flag.setDensity(densityParam);
                    zone.setFlag(flag);
                    sender.sendMessage("§aHustota zobrazovaných částic zóny §2" + zoneName + " §abyla nastavena na §2" + densityParam + "§a.");
                } else if (paramWords.get("red").contains(splitWords[0].toLowerCase())) {
                    if (splitWords.length < 2) {
                        sender.sendMessage("§cMusíš zadat komponentu červené barvy částic (0-255)");
                        return;
                    }
                    ParticlesFlag flag = zone.getFlagOrDefault(ParticlesFlag.class);
                    int redParam;
                    try {
                        redParam = Integer.parseInt(splitWords[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cZadaná komponenta červené barvy není platná");
                        return;
                    }
                    if (redParam < 0) {
                        redParam = 0;
                    }
                    if (redParam > 255)
                        redParam = 255;
                    flag.setRed(redParam);
                    zone.setFlag(flag);
                    sender.sendMessage("§aKomponenta červené barvy částic zóny §2" + zoneName + " §abyla nastavena na §2" + redParam + "§a.");
                } else if (paramWords.get("green").contains(splitWords[0].toLowerCase())) {
                    if (splitWords.length < 2) {
                        sender.sendMessage("§cMusíš zadat komponentu zelené barvy částic (0-255)");
                        return;
                    }
                    ParticlesFlag flag = zone.getFlagOrDefault(ParticlesFlag.class);
                    int greenParam;
                    try {
                        greenParam = Integer.parseInt(splitWords[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cZadaná komponenta zelené barvy není platná");
                        return;
                    }
                    if (greenParam < 0) {
                        greenParam = 0;
                    }
                    if (greenParam > 255)
                        greenParam = 255;
                    flag.setGreen(greenParam);
                    zone.setFlag(flag);
                    sender.sendMessage("§aKomponenta zelené barvy částic zóny §2" + zoneName + " §abyla nastavena na §2" + greenParam + "§a.");
                } else if (paramWords.get("blue").contains(splitWords[0].toLowerCase())) {
                    if (splitWords.length < 2) {
                        sender.sendMessage("§cMusíš zadat komponentu modré barvy částic (0-255)");
                        return;
                    }
                    ParticlesFlag flag = zone.getFlagOrDefault(ParticlesFlag.class);
                    int blueParam;
                    try {
                        blueParam = Integer.parseInt(splitWords[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cZadaná komponenta modré barvy není platná");
                        return;
                    }
                    if (blueParam < 0) {
                        blueParam = 0;
                    }
                    if (blueParam > 255)
                        blueParam = 255;
                    flag.setBlue(blueParam);
                    zone.setFlag(flag);
                    sender.sendMessage("§aKomponenta modré barvy částic zóny §2" + zoneName + " §abyla nastavena na §2" + blueParam + "§a.");
                }  else {
                    sender.sendMessage("§cNeznámý parametr, dostupné jsou tyto parametry: §4" + StringUtils.join(firstParamWords, "§c, §4"));
                }
            } else if (keywords.get("tpFlag").contains(type.toLowerCase())) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cTento flag z konzole nastavit nelze.");
                    return;
                }
                Player player = (Player)sender;
                TpFlag flag = new TpFlag();
                flag.setTpLocation(player.getLocation());
                zone.setFlag(flag);
                sender.sendMessage("§aMísto teleportu zóny §2" + zoneName + " §anastaveno na tvou pozici.");
            } else {
                List<String> firstKeywords = new ArrayList<>();
                for (String key : keywords.keySet()) {
                    firstKeywords.add(keywords.get(key).get(0));
                }
                sender.sendMessage("§cNeplatný flag, dostupné jsou tyto flagy: §4" + StringUtils.join(firstKeywords, "§c, §4"));
            }
        }

        @Subcommand("unset|clear|delete|remove|undefine|zrusit|smazat|odstranit|odebrat")
        @Description("Zruší nastavení flagu v zóně")
        public void flagsUnsetCommand(CommandSender sender, String zoneName, String type, @Optional String params) {
            Zone zone = ZoneManager.getInstance().getZone(zoneName);
            if (zone == null) {
                sender.sendMessage("§cZóna §4" + zoneName + " §cneexistuje.");
                return;
            }

            Class<? extends Flag> flagType;
            Map<String, List<String>> keywords = getFlagKeywords();
            if (keywords.get("enderPortalFlag").contains(type.toLowerCase())) {
                flagType = EnderPortalFlag.class;
            } else if (keywords.get("mineFlag").contains(type.toLowerCase())) {
                flagType = MineFlag.class;
            } else if (keywords.get("particlesFlag").contains(type.toLowerCase())) {
                flagType = ParticlesFlag.class;
            } else if (keywords.get("tpFlag").contains(type.toLowerCase())) {
                flagType = TpFlag.class;
            } else {
                List<String> firstKeywords = new ArrayList<>();
                for (String key : keywords.keySet()) {
                    firstKeywords.add(keywords.get(key).get(0));
                }
                sender.sendMessage("§cNeplatný flag, dostupné jsou tyto flagy: §4" + StringUtils.join(firstKeywords, "§c, §4"));
                return;
            }

            if (!zone.hasFlag(flagType)) {
                sender.sendMessage("§cZóna §4" + zoneName + " §ctento flag nemá nastaven.");
                return;
            }
            zone.unsetFlag(flagType);
            sender.sendMessage("§aFlag §2" + type + " §abyl odebrán ze zóny §2" + zoneName + " §a.");
        }

    }

    @Subcommand("copy|kopirovat|zkopirovat")
    public class copyCommands extends BaseCommand {

        @HelpCommand
        public void baseCopyCommand(CommandSender sender, CommandHelp help) {
            sender.sendMessage("§8" + StringHelper.getChatLine());
            sender.sendMessage(StringHelper.centerMessage("§aNÁPOVĚDA K ZÓNÁM"));
            help.showHelp();
            sender.sendMessage("§8" + StringHelper.getChatLine());
        }

        @Subcommand("action|actions|akce")
        @Description("Zkopíruje akce ze zóny do jiné zóny")
        public void copyActionsCommand(CommandSender sender, String sourceZoneName, String targetZoneName, @Optional Integer sourceActionId, @Optional Integer newActionId) {
            Zone sourceZone = ZoneManager.getInstance().getZone(sourceZoneName);
            if (sourceZone == null) {
                sender.sendMessage("§cZdrojová zóna §4" + sourceZoneName + " §cneexistuje.");
                return;
            }
            Zone targetZone = ZoneManager.getInstance().getZone(targetZoneName);
            if (targetZone == null) {
                sender.sendMessage("§cCílová zóna §4" + targetZoneName + " §cneexistuje.");
                return;
            }
            if (sourceZone.getActionsAmount() == 0) {
                sender.sendMessage("§cZdrojová zóna §4" + sourceZoneName + " §cneobsahuje žádné akce.");
                return;
            }
            if (ZoneExecutor.getInstance().isZoneBeingExecuted(targetZone)) {
                sender.sendMessage("§cV cílové zóně §4" + targetZoneName + " §cprávě probíhá série akcí, zkus to za chvíli.");
                return;
            }
            if (sourceActionId == null) {
                targetZone.removeAllActions();
                for (Action sourceAction : sourceZone.getActions()) {
                    targetZone.addAction(sourceAction.copy());
                }
                sender.sendMessage("§aVšechny akce (§2" + sourceZone.getActionsAmount() + "§a) ze zóny §2" + sourceZoneName + " §abyly zkopírovány do zóny §2" + targetZoneName + "§a.");
            }
            else {
                if (sourceActionId < 0 || sourceActionId >= sourceZone.getActionsAmount()) {
                    sender.sendMessage("§cZdrojová zóna §4" + sourceZoneName + " §cneobsahuje akci s ID §4" + sourceActionId + "§c.");
                    return;
                }
                if (newActionId == null) {
                    targetZone.addAction(sourceZone.getAction(sourceActionId).copy());
                    sender.sendMessage("§aAkce s ID §2" + sourceActionId + " §aze zóny §2" + sourceZoneName + " §abyla zkopírována do zóny §2" + targetZoneName + "§a.");
                }
                else {
                    if (newActionId < 0)
                        newActionId = 0;
                    if (newActionId > targetZone.getActionsAmount())
                        newActionId = targetZone.getActionsAmount();
                    newActionId = targetZone.addActionToId(sourceZone.getAction(sourceActionId).copy(), newActionId);
                    sender.sendMessage("§aAkce s ID §2" + sourceActionId + " §aze zóny §2" + sourceZoneName + " §abyla zkopírována do zóny §2" + targetZoneName + " §as novým ID §2" + newActionId + "§a.");
                }
            }
        }

        @Subcommand("flag|flags|flagy|vlajka|vlajky")
        @Description("Zkopíruje flagy ze zóny do jiné zóny")
        public void copyFlagsCommand(CommandSender sender, String sourceZoneName, String targetZoneName, @Optional String flagType) {
            Zone sourceZone = ZoneManager.getInstance().getZone(sourceZoneName);
            if (sourceZone == null) {
                sender.sendMessage("§cZdrojová zóna §4" + sourceZoneName + " §cneexistuje.");
                return;
            }
            Zone targetZone = ZoneManager.getInstance().getZone(targetZoneName);
            if (targetZone == null) {
                sender.sendMessage("§cCílová zóna §4" + targetZoneName + " §cneexistuje.");
                return;
            }
            if (flagType == null) {
                for (Flag flag : sourceZone.getAllFlags()) {
                    targetZone.setFlag(flag);
                }
                sender.sendMessage("§aVšechny flagy ze zóny §2" + sourceZoneName + " §abyly zkopírovány do zóny §2" + targetZoneName + "§a.");
            } else {
                Class<? extends Flag> flagTypeClass;
                Map<String, List<String>> keywords = getFlagKeywords();
                if (keywords.get("enderPortalFlag").contains(flagType.toLowerCase())) {
                    flagTypeClass = EnderPortalFlag.class;
                } else if (keywords.get("mineFlag").contains(flagType.toLowerCase())) {
                    flagTypeClass = MineFlag.class;
                } else if (keywords.get("particlesFlag").contains(flagType.toLowerCase())) {
                    flagTypeClass = ParticlesFlag.class;
                } else if (keywords.get("tpFlag").contains(flagType.toLowerCase())) {
                    flagTypeClass = TpFlag.class;
                } else {
                    List<String> firstKeywords = new ArrayList<>();
                    for (String key : keywords.keySet()) {
                        firstKeywords.add(keywords.get(key).get(0));
                    }
                    sender.sendMessage("§cNeplatný flag, dostupné jsou tyto flagy: §4" + StringUtils.join(firstKeywords, "§c, §4"));
                    return;
                }

                if (!sourceZone.hasFlag(flagTypeClass)) {
                    sender.sendMessage("§cZdrojová zóna §4" + sourceZoneName + " §cnemá nastaven flag §4" + flagType + "§c.");
                    return;
                }

                targetZone.setFlag(sourceZone.getFlagOrDefault(flagTypeClass));
                sender.sendMessage("§aFlag §2" + flagType + " §abyl zkopírován ze zóny §2" + sourceZoneName + "§a do zóny §2" + targetZoneName + "§a.");
            }

        }

        @Subcommand("all|zone|everything|both|vse|vsechno|zona|zonu|oboje|oba")
        @Description("Zkopíruje flagy a akce ze zóny do jiné zóny")
        public void copyAllCommand(CommandSender sender, String sourceZoneName, String targetZoneName) {
            copyActionsCommand(sender, sourceZoneName, targetZoneName, null, null);
            copyFlagsCommand(sender, sourceZoneName, targetZoneName, null);
        }

    }

    private Map<String, List<String>> getActionKeywords() {
        Map<String, List<String>> keywords = new HashMap<>();
        keywords.put("commandAction", Arrays.asList("command", "cmd", "prikaz"));
        keywords.put("damageAction", Arrays.asList("damage", "dmg", "harm", "hurt", "attack", "zranit", "zraneni", "poskozeni"));
        keywords.put("messageAction", Arrays.asList("message", "msg", "zprava", "text", "chat"));
        keywords.put("nothingAction", Arrays.asList("nothing", "null", "none", "-", "--", "---", "empty", "filler", "nic"));
        keywords.put("potionEffectAction", Arrays.asList("potion", "pot", "effect", "lektvar", "efekt"));
        keywords.put("teleportAction", Arrays.asList("teleport", "tp", "port"));
        return keywords;
    }

    private Map<String, List<String>> getFlagKeywords() {
        Map<String, List<String>> keywords = new HashMap<>();
        keywords.put("enderPortalFlag", Arrays.asList("enderportal", "enderport", "ender"));
        keywords.put("mineFlag", Arrays.asList("mine", "mines", "dul", "doly"));
        keywords.put("particlesFlag", Arrays.asList("particles", "particle", "particly", "partikly", "castice", "efekt", "efekty"));
        keywords.put("tpFlag", Arrays.asList("tp", "port", "teleport", "spawn"));
        return keywords;
    }

    private <T extends Action> T constructAction(Class<T> actionType, String params) throws ZoneException {
        if (actionType == CommandAction.class) {
            if (params == null) {
                throw new ZoneException("§cMusíš zadat příkaz, který se má vykonat.");
            }
            char c = params.charAt(0);
            if (c != '/' && c != '#' && c != '@') {
                throw new ZoneException("§cPříkaz musí začínat znaky / (normální), @ (jako OP) nebo # (jako konzole).");
            }
            if (params.length() < 2) {
                throw new ZoneException("§cZadaný příkaz je příliš krátký.");
            }
            try {
                return actionType.getDeclaredConstructor(String.class).newInstance(params);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
                throw new ZoneException("§cPři zpracování akce došlo k chybě.");
            }
        } else if (actionType == DamageAction.class) {
            if (params == null) {
                throw new ZoneException("§cMusíš zadat hodnotu poškození.");
            }
            int damage;
            try {
                damage = Integer.parseInt(params);
            } catch (NumberFormatException e) {
                throw new ZoneException("§cPoškození musí být platné celé číslo (zadáno §4" + params + "§c).");
            }
            try {
                return actionType.getDeclaredConstructor(int.class).newInstance(damage);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
                throw new ZoneException("§cPři zpracování akce došlo k chybě.");
            }
        } else if (actionType == MessageAction.class) {
            try {
                return actionType.getDeclaredConstructor(String.class).newInstance(params);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
                throw new ZoneException("§cPři zpracování akce došlo k chybě.");
            }
        } else if (actionType == NothingAction.class) {
            try {
                return actionType.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
                throw new ZoneException("§cPři zpracování akce došlo k chybě.");
            }
        } else if (actionType == PotionEffectAction.class) {
            if (params == null) {
                throw new ZoneException("§cMusíš zadat typ lektvaru, za to jeho úroveň a pak trvání v sekundách.");
            }
            String[] paramArray = params.split(" ");
            if (paramArray.length < 3) {
                throw new ZoneException("§cMusíš zadat typ lektvaru, za to jeho úroveň a pak trvání v sekundách.");
            }
            PotionEffectType potionEffectType = PotionEffectType.getByName(paramArray[0]);
            if (potionEffectType == null) {
                throw new ZoneException("§cZadaný typ lektvaru §4" + paramArray[0] + " §cnebyl nalezen.");
            }
            int power;
            try {
                power = Integer.parseInt(paramArray[1]);
            } catch (NumberFormatException e) {
                throw new ZoneException("§cÚroveň lektvaru musí být platné celé číslo (zadáno §4" + paramArray[1] + "§c).");
            }
            int duration;
            try {
                duration = Integer.parseInt(paramArray[2]);
            } catch (NumberFormatException e) {
                throw new ZoneException("§cTrvání lektvaru musí být platné celé číslo (zadáno §4" + paramArray[2] + "§c).");
            }
            try {
                return actionType.getDeclaredConstructor(PotionEffectType.class, int.class, int.class).newInstance(potionEffectType, power-1, duration);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
                throw new ZoneException("§cPři zpracování akce došlo k chybě.");
            }
        } else if (actionType == TeleportAction.class) {
            if (params == null) {
                throw new ZoneException("§cMusíš zadat zóny, na které se má hráč teleportovat.");
            }
            String[] paramArray = params.split(" ");
            Set<Zone> paramsZones = new HashSet<>();
            for (String param : paramArray) {
                Zone foundZone = ZoneManager.getInstance().getZone(param);
                if (foundZone == null) {
                    throw new ZoneException("§cZóna k teleportu §4" + param + " §cneexistuje.");
                }
                paramsZones.add(foundZone);
            }
            try {
                return actionType.getDeclaredConstructor(Zone[].class).newInstance(new Object[]{paramsZones.toArray(new Zone[0])});
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
                throw new ZoneException("§cPři zpracování akce došlo k chybě.");
            }
        }
        throw new ZoneException("§cNeplatný typ akce.");
    }
}
    // ZONE TODO - přidat všude auto-complete
    // ZONE TODO - otestovat auto-complete
    // ZONE TODO - lepší nápověda k příkazům
    // ZONE TODO - implementovat perzistenci
    // ZONE TODO - otestovat perzistenci

