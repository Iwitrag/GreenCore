package cz.iwitrag.greencore.gameplay.zones;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import cz.iwitrag.greencore.gameplay.zones.actions.Action;
import cz.iwitrag.greencore.gameplay.zones.actions.CommandAction;
import cz.iwitrag.greencore.gameplay.zones.actions.DamageAction;
import cz.iwitrag.greencore.gameplay.zones.actions.MessageAction;
import cz.iwitrag.greencore.gameplay.zones.actions.NothingAction;
import cz.iwitrag.greencore.gameplay.zones.actions.PotionEffectAction;
import cz.iwitrag.greencore.gameplay.zones.actions.TeleportAction;
import cz.iwitrag.greencore.gameplay.zones.flags.BlockedCommandsFlag;
import cz.iwitrag.greencore.gameplay.zones.flags.DisconnectPenaltyFlag;
import cz.iwitrag.greencore.gameplay.zones.flags.EnderPortalFlag;
import cz.iwitrag.greencore.gameplay.zones.flags.Flag;
import cz.iwitrag.greencore.gameplay.zones.flags.MineFlag;
import cz.iwitrag.greencore.gameplay.zones.flags.ParticlesFlag;
import cz.iwitrag.greencore.gameplay.zones.flags.TpFlag;
import cz.iwitrag.greencore.helpers.Color;
import cz.iwitrag.greencore.helpers.DependenciesProvider;
import cz.iwitrag.greencore.helpers.GeometryHelper;
import cz.iwitrag.greencore.helpers.StringHelper;
import cz.iwitrag.greencore.helpers.Utils;
import cz.iwitrag.greencore.storage.PersistenceManager;
import org.apache.commons.lang.StringUtils;
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

/*
 When adding new FLAG
 1. Add annotated class to PersistenceManager
 2. Add keywords to getFlagKeywords()
 3. flagListCommand()
 4. flagSetCommannd()
 5. command completion (zone_flags_param)
 */

/*
 When adding new ACTION
 1. Add annotated class to PersistenceManager
 2. Add keywords to getActionKeywords()
 3. constructAction()
 4. command completion (zone_actions_param)
 */

@CommandAlias("zone|zona|zones|zony")
@CommandPermission("zone.admin")
public class ZoneCommands extends BaseCommand {

    @HelpCommand
    public void baseZoneCommand(CommandSender sender, CommandHelp help) {
        sender.sendMessage("§8" + StringHelper.getChatLine());
        sender.sendMessage("§a" + StringHelper.centerMessage("NÁPOVĚDA K ZÓNÁM"));
        help.showHelp();
        sender.sendMessage("§8" + StringHelper.getChatLine());
    }

    @Subcommand("create|new|define|vytvorit|nova|definovat")
    @Description("Vytvoří novou zónu")
    @CommandCompletion("@nothing")
    public void createCommand(Player sender, String name) {
        Location p1 = DependenciesProvider.getInstance().getWorldEditSelection(sender.getName(), true);
        Location p2 = DependenciesProvider.getInstance().getWorldEditSelection(sender.getName(), false);
        if (p1 == null || p2 == null) {
            sender.sendMessage("§cPřed vytvořením zóny musíš vybrat oblast!");
            return;
        }
        Zone zone = new Zone(name, p1, p2);
        try {
            ZoneManager.getInstance().addZone(zone, true);
        } catch (ZoneException e) {
            sender.sendMessage("§c" + e.getMessage());
            return;
        }
        sender.sendMessage("§aZóna §2" + name + " §aúspěšně vytvořena!");
    }

    @Subcommand("list|seznam")
    @Description("Vypíše seznam zón")
    @CommandCompletion("@worlds")
    public void listCommand(CommandSender sender, @Optional World world) {
        Set<Zone> zones = ZoneManager.getInstance().getZones();
        if (zones.isEmpty()) {
            sender.sendMessage("§cŽádné zóny nejsou vytvořeny.");
            return;
        }
        if (world != null) {
            zones.removeIf(zone -> !zone.getPoint1().getWorld().equals(world));
        }
        if (zones.isEmpty()) {
            sender.sendMessage("§cŽádné zóny ve světě §4" + world.getName() + " §cnejsou vytvořeny.");
            return;
        }

        sender.sendMessage("§8" + StringHelper.getChatLine());
        if (world == null)
            sender.sendMessage("§a" + StringHelper.centerMessage("SEZNAM VŠECH ZÓN"));
        else
            sender.sendMessage("§a" + StringHelper.centerMessage("SEZNAM ZÓN VE SVĚTĚ " + world.getName()));
        sender.sendMessage("§f" + StringUtils.join(zones, "§7, §f"));
        sender.sendMessage("§8" + StringHelper.getChatLine());
    }

    @Subcommand("near|around|blizko|pobliz|kolem|okolo")
    @Description("Vypíše zóny v blízkosti")
    @CommandCompletion("@range:0-300(15)")
    public void nearCommand(Player sender, @Default("50") Integer radius) {
        Set<Zone> zones = ZoneManager.getInstance().getZones();
        if (zones.isEmpty()) {
            sender.sendMessage("§cŽádné zóny nejsou vytvořeny.");
            return;
        }
        if (radius < 1) {
            radius = 1;
        }
        Set<Zone> zonesNear = new LinkedHashSet<>();
        Location loc = sender.getLocation();
        for (Zone iteratedZone : zones) {
            if (loc.getWorld().equals(iteratedZone.getPoint1().getWorld())
                    && GeometryHelper.getInstance().intersectSphereCube(loc, radius, iteratedZone.getPoint1(), iteratedZone.getPoint2()))
                zonesNear.add(iteratedZone);
        }
        if (zonesNear.isEmpty()) {
            sender.sendMessage("§cŽádné zóny v okolí §4" + radius + " §cbloků nebyly nalezeny.");
            return;
        }

        sender.sendMessage("§8" + StringHelper.getChatLine());
        sender.sendMessage("§a" + StringHelper.centerMessage("ZÓNY V OKOLÍ §2" + radius + " §aBLOKŮ"));
        sender.sendMessage("§f" + StringUtils.join(zonesNear, "§7, §f"));
        sender.sendMessage("§8" + StringHelper.getChatLine());
    }

    @Subcommand("tp|teleport")
    @Description("Teleportuje hráče k zóně")
    @CommandCompletion("@zones @players")
    public void tpCommand(CommandSender sender, Zone zone, @Optional OnlinePlayer target) {
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
            sender.sendMessage("§aTeleportuješ se k zóně §2" + zone.getName());
        else
            sender.sendMessage("§aTeleportuješ hráče §2" + target.getPlayer().getName() + " §ak zóně §2" + zone.getName());
        targetPlayer.teleport((zone.getFlagOrDefault(TpFlag.class)).getLocation());
    }

    @Subcommand("info|about|informace")
    @Description("Vypíše informace o zóně")
    @CommandCompletion("@zones")
    public void infoCommand(CommandSender sender, @Optional Zone zone) {
        if (zone == null) {
            try {
                zone = getZonePlayerIsInside(sender);
            } catch (ZoneException e) {
                sender.sendMessage(e.getMessage());
                return;
            }
        }
        sender.sendMessage("§8" + StringHelper.getChatLine());
        sender.sendMessage("§a" + StringHelper.centerMessage("OBECNÉ INFORMACE O ZÓNĚ " + zone.getName()));
        sender.sendMessage("§9Priorita: §f" + zone.getPriority());
        sender.sendMessage("§9Svět: §f" + zone.getPoint1().getWorld().getName());
        sender.sendMessage("§9Velikost zóny: X: §f" + zone.getSizeInX() + "§9, Y: §f" + zone.getSizeInY() + "§9, Z: §f" + zone.getSizeInZ() + "§9, celkem: §f" + zone.getSize());
        sender.sendMessage("§9Bod 1: " + StringHelper.locationToString(zone.getPoint1(), false, "§9", "§f"));
        sender.sendMessage("§9Bod 2: " + StringHelper.locationToString(zone.getPoint2(), false, "§9", "§f"));
        int actions = zone.getActions().size();
        if (actions > 0)
            sender.sendMessage("§7Seznam akcí (" + actions + "): §f/" + getExecCommandLabel() + " actions list " + zone.getName());
        sender.sendMessage("§7Seznam vlajek: §f/" + getExecCommandLabel() + " flags list " + zone.getName());
        sender.sendMessage("§8" + StringHelper.getChatLine());
    }

    @Subcommand("select|sel|choose|vybrat|vyber")
    @Description("Vybere oblast zóny")
    @CommandCompletion("@zones")
    public void selectCommand(Player sender, @Optional Zone zone) {
        if (zone == null) {
            try {
                zone = getZonePlayerIsInside(sender);
            } catch (ZoneException e) {
                sender.sendMessage(e.getMessage());
                return;
            }
        }
        DependenciesProvider.getInstance().setWorldEditSelection(sender.getName(), zone.getPoint1(), zone.getPoint2());
        sender.sendMessage("§aVybrána oblast zóny §2" + zone);
    }

    @Subcommand("priority|setpriority|priorita")
    @Description("Změní prioritu zóny")
    @CommandCompletion("@zones @range:0-10")
    public void priorityCommand(CommandSender sender, Zone zone, Integer priority) {
        zone.setPriority(priority);
        sender.sendMessage("§aPriorita zóny §2" + zone.getName() + " §anastavena na §2" + priority);
    }

    @Subcommand("rename|name|setname|nazev|prejmenovat|prejmenuj")
    @Description("Přejmenuje zónu")
    @CommandCompletion("@zones @nothing")
    public void renameCommand(CommandSender sender, Zone zone, String newName) {
        Zone existingZoneWithNewName = ZoneManager.getInstance().getZone(newName);
        if (existingZoneWithNewName != null && !existingZoneWithNewName.equals(zone)) { // second condition is to allow characters case changing
            sender.sendMessage("§cNázev §4" + newName + " §cse již používá.");
            return;
        }
        sender.sendMessage("§aZóna §2" + zone.getName() + " §apřejmenována na §2" + newName);
        zone.setName(newName);
    }

    @Subcommand("redefine|area|setarea|oblast")
    @Description("Změní oblast zóny podle výběru WorldEditu")
    @CommandCompletion("@zones")
    public void redefineCommand(CommandSender sender, Zone zone) {
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
    @CommandCompletion("@zones")
    public void deleteCommand(CommandSender sender, Zone zone) {
        if (ZoneExecutor.getInstance().isZoneBeingExecuted(zone)) {
            sender.sendMessage("§cV zóně §4" + zone.getName() + " §cprávě probíhá série akcí, zkus to za chvíli.");
            return;
        }
        ZoneManager.getInstance().deleteZone(zone, true);
        sender.sendMessage("§aZóna §2" + zone.getName() + " §abyla odstraněna!");
    }

    @Subcommand("saveall|ulozitvse")
    @Description("Zapíše veškeré změny do databáze")
    public void saveAllCommand(CommandSender sender) {
        PersistenceManager pm = PersistenceManager.getInstance();
        pm.runHibernateAsyncTask(pm::updateZoneData, true);
        sender.sendMessage("§aUkládání spuštěno, případné chyby se objeví v konzoli a logu");
    }

    @Subcommand("action|actions|akce")
    public class actionCommands extends BaseCommand {

        @Subcommand("list|seznam|vypsat")
        @Description("Vypíše všechny akce zóny")
        @CommandCompletion("@zones")
        public void actionsListCommand(CommandSender sender, Zone zone) {
            sender.sendMessage("§8" + StringHelper.getChatLine());
            sender.sendMessage("§a" + StringHelper.centerMessage("AKCE ZÓNY " + zone.getName()));
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

        @Subcommand("add|set|create|new|define|pridat|nastavit|vytvorit|nova|definovat")
        @Description("Přidá novou akci do zóny")
        @CommandCompletion("@zone @range @zone_actions_types @zone_actions_param:1 @zone_actions_param:2 @zone_actions_param:3 @nothing")
        public void actionsAddCommand(CommandSender sender, Zone zone, int ticks, String actionType, @Optional String param1, @Optional String param2, @Optional String param3, @Optional String otherParams) {
            if (ZoneExecutor.getInstance().isZoneBeingExecuted(zone)) {
                sender.sendMessage("§cV zóně §4" + zone.getName() + " §cprávě probíhá série akcí, zkus to za chvíli.");
                return;
            }
            if (ticks < 0) {
                sender.sendMessage("§cČas akce nemůže být §4" + ticks + " §cticků.");
                return;
            }

            String params = (param1 != null ? param1 : "") + " " + (param2 != null ? param2 : "") + " " + (param3 != null ? param3 : "") + (otherParams != null ? otherParams : "");
            params = params.replaceAll(" +", " ").trim();
            if (params.isEmpty())
                params = null;

            Action addedAction;
            try {
                Class<? extends Action> foundActionClass = findActionClass(actionType);
                if (foundActionClass != null)
                    addedAction = constructAction(foundActionClass, params);
                else {
                    List<String> firstKeywords = new ArrayList<>();
                    Map<Class<? extends Action>, List<String>> keywordsList = getActionKeywords();
                    for (Class<? extends Action> key : keywordsList.keySet()) {
                        firstKeywords.add(keywordsList.get(key).get(0));
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
            sender.sendMessage("§aAkce úspěšně přidána k zóně §2" + zone.getName() + "§a!");
        }

        @Subcommand("edit|change|param|params|parameter|parameters|upravit|parametr|parametry")
        @Description("Upraví parametry akce v zóně")
        @CommandCompletion("@zone @zone_actions_ids:1 @zone_actions_param:1 @zone_actions_param:2 @zone_actions_param:3 @nothing")
        public void actionsEditCommand(CommandSender sender, Zone zone, int id, @Optional String param1, @Optional String param2, @Optional String param3, @Optional String otherParams) {
            if (ZoneExecutor.getInstance().isZoneBeingExecuted(zone)) {
                sender.sendMessage("§cV zóně §4" + zone.getName() + " §cprávě probíhá série akcí, zkus to za chvíli.");
                return;
            }
            Action action = zone.getAction(id);
            if (action == null) {
                sender.sendMessage("§cV zóně §4" + zone.getName() + " §cneexistuje akce s ID §4" + id);
                return;
            }

            String params = (param1 != null ? param1 : "") + " " + (param2 != null ? param2 : "") + " " + (param3 != null ? param3 : "") + (otherParams != null ? otherParams : "");
            params = params.replaceAll(" +", " ").trim();
            if (params.isEmpty())
                params = null;

            Action constructedAction;
            try {
                constructedAction = constructAction(action.getClass(), params);
            } catch (ZoneException e) {
                sender.sendMessage(e.getMessage());
                return;
            }
            zone.removeAction(action);
            zone.addActionToId(constructedAction, id);
            sender.sendMessage("§aAkce s ID §2" + id + " §av zóně §2" + zone.getName() + " §abyla úspěšně upravena!");
        }

        @Subcommand("time|tick|ticks|cas|ticky")
        @Description("Změní čas akce v zóně")
        @CommandCompletion(("@zones @zone_actions_ids:1 @range"))
        public void actionsTimeCommand(CommandSender sender, Zone zone, int id, int ticks) {
            if (ZoneExecutor.getInstance().isZoneBeingExecuted(zone)) {
                sender.sendMessage("§cV zóně §4" + zone.getName() + " §cprávě probíhá série akcí, zkus to za chvíli.");
                return;
            }
            if (ticks < 0) {
                sender.sendMessage("§cČas nelze nastavit na §4" + ticks + " §cticků.");
                return;
            }
            Action action = zone.getAction(id);
            if (action == null) {
                sender.sendMessage("§cV zóně §4" + zone.getName() + " §cneexistuje akce s ID §4" + id);
                return;
            }
            zone.removeAction(action);
            action.setTime(ticks);
            zone.addAction(action); // Removed, modified and added to force reordering of all actions inside zone
            sender.sendMessage("§aČas akce s ID §2" + id + " §av zóně §2" + zone.getName() + " §azměněn na §2" + ticks + " §aticků!");
        }

        @Subcommand("id|index|identifikator|number|cislo")
        @Description("Změní ID akce v zóně")
        @CommandCompletion(("@zones @zone_actions_ids:1 @range"))
        public void actionsIdCommand(CommandSender sender, Zone zone, int oldId, int newId) {
            if (ZoneExecutor.getInstance().isZoneBeingExecuted(zone)) {
                sender.sendMessage("§cV zóně §4" + zone.getName() + " §cprávě probíhá série akcí, zkus to za chvíli.");
                return;
            }
            Action action = zone.getAction(oldId);
            if (action == null) {
                sender.sendMessage("§cV zóně §4" + zone.getName() + " §cneexistuje akce s ID §4" + oldId);
                return;
            }
            zone.removeAction(action);
            int finalId = zone.addActionToId(action, newId);
            sender.sendMessage("§aID akce změněn z §2" + oldId + " §ana §2" + finalId + " §av zóně §2" + zone.getName() + "§a!");
        }

        @Subcommand("delete|remove|unset|smazat|odstranit|odebrat|zrusit")
        @Description("Odstraní akci v zóně")
        @CommandCompletion(("@zones @zone_actions_ids:1"))
        public void actionsDeleteCommand(CommandSender sender, Zone zone, int id) {
            if (ZoneExecutor.getInstance().isZoneBeingExecuted(zone)) {
                sender.sendMessage("§cV zóně §4" + zone.getName() + " §cprávě probíhá série akcí, zkus to za chvíli.");
                return;
            }
            if (zone.getActionsAmount() == 0) {
                sender.sendMessage("§cZóna §4" + zone.getName() + " §cnemá nastaveny žádné akce.");
                return;
            }
            Action action = zone.getAction(id);
            if (action == null) {
                sender.sendMessage("§cV zóně §4" + zone.getName() + " §cneexistuje akce s ID §4" + id);
                return;
            }
            zone.removeAction(action);
            sender.sendMessage("§aAkce s ID §2" + id + " §av zóně §2" + zone.getName() + " §abyla odstraněna!");
        }

        @Subcommand("purge|deleteall|removeall|smazatvse|odstranitvse")
        @Description("Odstraní všechny akce v zóně")
        @CommandCompletion("@zones")
        public void actionsPurgeCommand(CommandSender sender, Zone zone) {
            if (ZoneExecutor.getInstance().isZoneBeingExecuted(zone)) {
                sender.sendMessage("§cV zóně §4" + zone.getName() + " §cprávě probíhá série akcí, zkus to za chvíli.");
                return;
            }
            if (zone.getActionsAmount() == 0) {
                sender.sendMessage("§cZóna §4" + zone.getName() + " §cnemá nastaveny žádné akce.");
                return;
            }
            zone.removeAllActions();
            sender.sendMessage("§aVšechny akce v zóně §2" + zone.getName() + " §abyly odstraněny!");
        }
    }

    @Subcommand("flag|flags|flagy|vlajka|vlajky")
    public class flagCommands extends BaseCommand {

        @Subcommand("list|seznam|vypsat")
        @Description("Vypíše aktuální flagy zóny")
        @CommandCompletion("@zones")
        public void flagsListCommand(CommandSender sender, Zone zone) {
            sender.sendMessage("§8" + StringHelper.getChatLine());
            sender.sendMessage("§a" + StringHelper.centerMessage("VLAJKY ZÓNY " + zone.getName()));

            sender.sendMessage("§bTeleport");
            String color = zone.hasFlag(TpFlag.class) ? "§e" : "§f";
            TpFlag tpFlag = zone.getFlagOrDefault(TpFlag.class);
            sender.sendMessage("§9Pozice: " + StringHelper.locationToString(tpFlag.getLocation(), true, "§9", color));

            sender.sendMessage("§bEnder portály");
            EnderPortalFlag enderPortalFlag = zone.getFlagOrDefault(EnderPortalFlag.class);
            color = zone.hasFlag(EnderPortalFlag.class) ? "§e" : "§f";
            sender.sendMessage("§9Fungují: " + color + (enderPortalFlag.isEnderPortalEnabled() ? "ANO" : "NE"));

            sender.sendMessage("§bPenalizace za odpojení");
            DisconnectPenaltyFlag disconnectPenaltyFlag = zone.getFlagOrDefault(DisconnectPenaltyFlag.class);
            color = zone.hasFlag(DisconnectPenaltyFlag.class) ? "§e" : "§f";
            sender.sendMessage("§9Šance že item vypadne: " + color + Utils.twoDecimal(disconnectPenaltyFlag.getPenalty()) + " %");

            sender.sendMessage("§bParticly");
            ParticlesFlag particlesFlag = zone.getFlagOrDefault(ParticlesFlag.class);
            color = zone.hasFlag(ParticlesFlag.class) ? "§e" : "§f";
            sender.sendMessage("§9Typ: " + color + (particlesFlag.getParticle() == null ? "---" : particlesFlag.getParticle().name()));
            sender.sendMessage("§9Hustota: " + color + particlesFlag.getDensity());
            if (Utils.isParticleColorizable(particlesFlag.getParticle())) {
                sender.sendMessage("§9Barva: " + color + particlesFlag.getColor());
            }

            sender.sendMessage("§bBlokované příkazy");
            BlockedCommandsFlag blockedCommandsFlag = zone.getFlagOrDefault(BlockedCommandsFlag.class);
            color = zone.hasFlag(BlockedCommandsFlag.class) ? "§e" : "§f";
            Set<String> blockedCmds = blockedCommandsFlag.getCommands();
            if (blockedCmds.isEmpty())
                sender.sendMessage("§9Seznam: " + color + "žádné blokované příkazy");
            else
                sender.sendMessage("§9Seznam: " + color + StringUtils.join(blockedCmds, ", "));

            sender.sendMessage("§bDoly");
            MineFlag mineFlag = zone.getFlagOrDefault(MineFlag.class);
            color = zone.hasFlag(MineFlag.class) ? "§e" : "§f";
            String blocksString;
            try {
                blocksString = mineFlag.getBlocksAsString();
                if (blocksString.isEmpty())
                    blocksString = "---";
            } catch (ZoneException e) {
                blocksString = "Chyba: " + e.getMessage();
            }
            sender.sendMessage("§9Bloky: " + color + blocksString);
            sender.sendMessage("§9Doplnění při: " + color + mineFlag.getRegenPercentage() + " % §9bloků");
            sender.sendMessage("§8" + StringHelper.getChatLine());
        }

        @Subcommand("set|add|change|define|nastavit|pridat|zmenit|definovat")
        @Description("Nastaví flag v zóně")
        @CommandCompletion("@zone @zone_flag_types @zone_flags_param:1 @zone_flags_param:2 @zone_flags_param:3 @nothing")
        public void flagsSetCommand(CommandSender sender, Zone zone, String flagType, @Optional String param1, @Optional String param2, @Optional String param3, @Optional String otherParams) {
            String zoneName = zone.getName();

            String params = (param1 != null ? param1 : "") + " " + (param2 != null ? param2 : "") + " " + (param3 != null ? param3 : "") + (otherParams != null ? otherParams : "");
            params = params.replaceAll(" +", " ").trim();
            if (params.isEmpty())
                params = null;

            Class<? extends Flag> foundFlagClass = findFlagClass(flagType);
            if (foundFlagClass == BlockedCommandsFlag.class) {
                Map<String, List<String>> paramWords = new HashMap<>();
                paramWords.put("add", Arrays.asList("add", "insert", "pridat"));
                paramWords.put("remove", Arrays.asList("remove", "delete", "odebrat", "odstranit"));
                paramWords.put("set", Arrays.asList("set", "nastavit"));
                paramWords.put("clear", Arrays.asList("clear", "purge", "empty", "vyprazdnit"));
                List<String> firstParamWords = new ArrayList<>();
                for (String word : paramWords.keySet()) {
                    firstParamWords.add(paramWords.get(word).get(0));
                }

                if (params == null) {
                    sender.sendMessage("§cMusíš zadat, co chceš s blokovanými příkazy dělat, ve formátu <akce> [příkazy]");
                    sender.sendMessage("§cDostupné jsou tyto akce: §4" + StringUtils.join(firstParamWords, "§c, §4"));
                    return;
                }

                String[] splitWords = params.split(" ");
                BlockedCommandsFlag flag = zone.getFlagOrDefault(BlockedCommandsFlag.class);

                String[] commands = params.contains(" ") ? params.substring(params.indexOf(" ")).trim().split(",") : null;
                if (paramWords.get("add").contains(splitWords[0].toLowerCase())) {
                    if (commands == null) {
                        sender.sendMessage("§cMusíš zadat blokované příkazy k přidání oddělené čárkami");
                        return;
                    }
                    for (String command : commands) {
                        flag.addCommand(command);
                    }
                    zone.setFlag(flag);
                    sender.sendMessage("§aBlokované příkazy přidány do zóny §2" + zoneName);
                    sender.sendMessage("§fAktuálně blokované příkazy jsou: §7" + StringUtils.join(flag.getCommands(), "§f, §7"));
                } else if (paramWords.get("remove").contains(splitWords[0].toLowerCase())) {
                    if (commands == null) {
                        sender.sendMessage("§cMusíš zadat blokované příkazy k odebrání oddělené čárkami");
                        return;
                    }
                    for (String command : commands) {
                        flag.removeCommand(command);
                    }
                    zone.setFlag(flag);
                    sender.sendMessage("§aBlokované příkazy odebrány ze zóny §2" + zoneName);
                    sender.sendMessage("§fAktuálně blokované příkazy jsou: §7" + StringUtils.join(flag.getCommands(), "§f, §7"));
                } else if (paramWords.get("set").contains(splitWords[0].toLowerCase())) {
                    if (commands == null) {
                        sender.sendMessage("§cMusíš zadat blokované příkazy k nastavení oddělené čárkami");
                        return;
                    }
                    flag.purgeCommands();
                    for (String command : commands) {
                        flag.addCommand(command);
                    }
                    zone.setFlag(flag);
                    sender.sendMessage("§aBlokované příkazy nastaveny v zóně §2" + zoneName);
                    sender.sendMessage("§fAktuálně blokované příkazy jsou: §7" + StringUtils.join(flag.getCommands(), "§f, §7"));
                } else if (paramWords.get("clear").contains(splitWords[0].toLowerCase())) {
                    zone.unsetFlag(BlockedCommandsFlag.class);
                    sender.sendMessage("§aFlag blokovaných příkazů odebrán ze zóny §2" + zoneName);
                } else {
                    sender.sendMessage("§cNeznámá akce, dostupné jsou tyto akce: §4" + StringUtils.join(firstParamWords, "§c, §4"));
                }
            } else if (foundFlagClass == DisconnectPenaltyFlag.class) {
                if (params == null) {
                    sender.sendMessage("§cMusíš zadat procentuální šanci na drop itemů po odpojení hráče");
                    return;
                }
                // Remove possible percent symbol
                String firstWord = params.split(" ")[0];
                if (firstWord.charAt(firstWord.length()-1) == '%')
                    firstWord = firstWord.substring(0, firstWord.length()-1);

                // Convert chance
                double chance;
                try {
                    chance = Double.parseDouble(firstWord);
                } catch (NumberFormatException ex) {
                    sender.sendMessage("§cNeplatná procentuální šance na drop itemů po odpojení hráče");
                    return;
                }
                if (chance < 0)
                    chance = 0;
                if (chance > 100)
                    chance = 100;

                DisconnectPenaltyFlag flag = new DisconnectPenaltyFlag();
                flag.setPenalty(chance);
                zone.setFlag(flag);
                sender.sendMessage("§aŠance na drop itemů při odpojení v zóně §2" + zoneName + " §anastavena na " + Utils.twoDecimal(chance) + " %.");
            } else if (foundFlagClass == EnderPortalFlag.class) {
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
            } else if (foundFlagClass == MineFlag.class) {
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
                        flag.setBlocksFromString(splitWords[1].toUpperCase());
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
                    float percentageParam;
                    try {
                        percentageParam = Float.parseFloat(splitWords[1]);
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
                    if (percentageParam >= 100.0)
                        sender.sendMessage("§a100% by znamenalo, že se budou doly obnovovat pořád, proto budou vypnuty.");
                } else {
                    sender.sendMessage("§cNeznámý parametr, dostupné jsou tyto parametry: §4" + StringUtils.join(firstParamWords, "§c, §4"));
                }
            } else if (foundFlagClass == ParticlesFlag.class) {
                Map<String, List<String>> paramWords = new HashMap<>();
                paramWords.put("particle", Arrays.asList("particles", "particle", "type", "particly", "partikly", "typ", "castice", "efekt", "efekty"));
                paramWords.put("density", Arrays.asList("density", "amount", "count", "hustota", "mnozstvi", "pocet"));
                paramWords.put("color", Arrays.asList("color", "col", "colors", "barva", "barvy"));
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
                } else if (paramWords.get("color").contains(splitWords[0].toLowerCase())) {
                    if (splitWords.length < 2) {
                        sender.sendMessage("§cMusíš zadat název barvy, například mustard_brown, 205,122,0 nebo #CD7A00");
                        return;
                    }
                    ParticlesFlag flag = zone.getFlagOrDefault(ParticlesFlag.class);
                    Color color;
                    try {
                        if (splitWords[1].indexOf(',') != -1)
                            color = new Color(splitWords[1], ",");
                        else
                            color = new Color(splitWords[1]);
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage("§c" + e.getMessage());
                        return;
                    }
                    flag.setColor(color);
                    zone.setFlag(flag);
                    sender.sendMessage("§aBarva částic zóny §2" + zoneName + " §abyla nastavena na §2" + color + "§a.");
                } else {
                    sender.sendMessage("§cNeznámý parametr, dostupné jsou tyto parametry: §4" + StringUtils.join(firstParamWords, "§c, §4"));
                }
            } else if (foundFlagClass == TpFlag.class) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cTento flag z konzole nastavit nelze.");
                    return;
                }
                Player player = (Player)sender;
                TpFlag flag = new TpFlag();
                flag.setLocation(player.getLocation());
                zone.setFlag(flag);
                sender.sendMessage("§aMísto teleportu zóny §2" + zoneName + " §anastaveno na tvou pozici.");
            } else {
                List<String> firstKeywords = new ArrayList<>();
                Map<Class<? extends Flag>, List<String>> keywords = getFlagKeywords();
                for (Class<? extends Flag> key : keywords.keySet()) {
                    firstKeywords.add(keywords.get(key).get(0));
                }
                sender.sendMessage("§cNeplatný flag, dostupné jsou tyto flagy: §4" + StringUtils.join(firstKeywords, "§c, §4"));
            }
        }

        @Subcommand("unset|clear|delete|remove|undefine|zrusit|smazat|odstranit|odebrat")
        @Description("Zruší nastavení flagu v zóně")
        @CommandCompletion("@zones @zone_flags_types:1")
        public void flagsUnsetCommand(CommandSender sender, Zone zone, String flagType) {
            Class<? extends Flag> foundFlagClass = findFlagClass(flagType);

            if (foundFlagClass == null) {
                List<String> firstKeywords = new ArrayList<>();
                Map<Class<? extends Flag>, List<String>> keywordsMap = getFlagKeywords();
                for (Class<? extends Flag> key : keywordsMap.keySet()) {
                    firstKeywords.add(keywordsMap.get(key).get(0));
                }
                sender.sendMessage("§cNeplatný flag, dostupné jsou tyto flagy: §4" + StringUtils.join(firstKeywords, "§c, §4"));
                return;
            }

            if (!zone.hasFlag(foundFlagClass)) {
                sender.sendMessage("§cZóna §4" + zone.getName() + " §ctento flag nemá nastaven.");
                return;
            }
            zone.unsetFlag(foundFlagClass);
            sender.sendMessage("§aFlag §2" + foundFlagClass + " §abyl odebrán ze zóny §2" + zone.getName() + " §a.");
        }

    }

    @Subcommand("copy|kopirovat|zkopirovat")
    public class copyCommands extends BaseCommand {

        @Subcommand("action|actions|akce")
        @Description("Zkopíruje akce ze zóny do jiné zóny")
        @CommandCompletion(("@zones @zones @zone_actions_ids:1 @range"))
        public void copyActionsCommand(CommandSender sender, Zone sourceZone, Zone targetZone, @Optional Integer sourceActionId, @Optional Integer newActionId) {
            String sourceZoneName = sourceZone.getName();
            String targetZoneName = targetZone.getName();
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
        @CommandCompletion("@zones @zones zone_flags_types:1")
        public void copyFlagsCommand(CommandSender sender, Zone sourceZone, Zone targetZone, @Optional String flagType) {
            if (flagType == null) {
                for (Flag flag : sourceZone.getAllFlags()) {
                    targetZone.setFlag(flag.copy());
                }
                sender.sendMessage("§aVšechny flagy ze zóny §2" + sourceZone.getName() + " §abyly zkopírovány do zóny §2" + targetZone.getName() + "§a.");
            } else {
                Class<? extends Flag> foundFlagClass = findFlagClass(flagType);

                if (foundFlagClass == null) {
                    List<String> firstKeywords = new ArrayList<>();
                    Map<Class<? extends Flag>, List<String>> keywordsMap = getFlagKeywords();
                    for (Class<?> key : keywordsMap.keySet()) {
                        firstKeywords.add(keywordsMap.get(key).get(0));
                    }
                    sender.sendMessage("§cNeplatný flag, dostupné jsou tyto flagy: §4" + StringUtils.join(firstKeywords, "§c, §4"));
                    return;
                }

                if (!sourceZone.hasFlag(foundFlagClass)) {
                    sender.sendMessage("§cZdrojová zóna §4" + sourceZone.getName() + " §cnemá nastaven flag §4" + flagType + "§c.");
                    return;
                }

                targetZone.setFlag(sourceZone.getFlagOrDefault(foundFlagClass).copy());
                sender.sendMessage("§aFlag §2" + flagType + " §abyl zkopírován ze zóny §2" + sourceZone.getName() + "§a do zóny §2" + targetZone.getName() + "§a.");
            }

        }

        @Subcommand("all|zone|everything|both|vse|vsechno|zona|zonu|oboje|oba")
        @Description("Zkopíruje flagy a akce ze zóny do jiné zóny")
        @CommandCompletion("@zones @zones")
        public void copyAllCommand(CommandSender sender, Zone sourceZone, Zone targetZone) {
            copyActionsCommand(sender, sourceZone, targetZone, null, null);
            copyFlagsCommand(sender, sourceZone, targetZone, null);
        }
    }

    // Helper method, used in several places in this class
    private Zone getZonePlayerIsInside(CommandSender sender) throws ZoneException {
        Zone zone;
        if (sender instanceof Player) {
            Set<Zone> zones = ZoneManager.getInstance().getZonesPlayerIsInside((Player)sender);
            if (zones.isEmpty())
                throw new ZoneException("§cMusíš zadát název zóny nebo v nějaké stát.");
            else if (zones.size() == 1)
                zone = zones.stream().findAny().get();
            else
                throw new ZoneException("§cMusíš zadát název zóny\n§6Stojíš v těchto zónách: §e" + StringUtils.join(zones, "§6, §e"));
        }
        else
            throw new ZoneException("§cMusíš zadát název zóny");
        return zone;
    }

    public static Map<Class<? extends Action>, List<String>> getActionKeywords() {
        Map<Class<? extends Action>, List<String>> keywords = new HashMap<>();
        keywords.put(CommandAction.class, Arrays.asList("command", "cmd", "prikaz"));
        keywords.put(DamageAction.class, Arrays.asList("damage", "dmg", "harm", "hurt", "attack", "zranit", "zraneni", "poskozeni"));
        keywords.put(MessageAction.class, Arrays.asList("message", "msg", "zprava", "text", "chat"));
        keywords.put(NothingAction.class, Arrays.asList("nothing", "null", "none", "-", "--", "---", "empty", "filler", "nic"));
        keywords.put(PotionEffectAction.class, Arrays.asList("potion", "pot", "effect", "lektvar", "efekt"));
        keywords.put(TeleportAction.class, Arrays.asList("teleport", "tp", "port"));
        return keywords;
    }

    public static Class<? extends Action> findActionClass(String input) {
        input = input.toLowerCase();
        for (Map.Entry<Class<? extends Action>, List<String>> entry : getActionKeywords().entrySet()) {
            Class<? extends Action> clazz = entry.getKey();
            List<String> keywords = entry.getValue();
            if (keywords.contains(input)) {
                return clazz;
            }
        }
        return null;
    }

     public static Map<Class<? extends Flag>, List<String>> getFlagKeywords() {
        Map<Class<? extends Flag>, List<String>> keywords = new HashMap<>();
        keywords.put(BlockedCommandsFlag.class, Arrays.asList("cmd", "cmds", "command", "commands", "blockcmd", "blockedcmd", "blockcommand", "blockedcommand", "blockcommands", "blockedcommands"));
        keywords.put(DisconnectPenaltyFlag.class, Arrays.asList("dsc", "disconnect", "penalty", "disconnectpenalty", "dscpenalty", "disdrop", "penalizace", "odpojeni"));
        keywords.put(EnderPortalFlag.class, Arrays.asList("enderportal", "enderport", "ender"));
        keywords.put(MineFlag.class, Arrays.asList("mine", "mines", "dul", "doly"));
        keywords.put(ParticlesFlag.class, Arrays.asList("particles", "particle", "particly", "partikly", "castice", "efekt", "efekty"));
        keywords.put(TpFlag.class, Arrays.asList("tp", "port", "teleport", "spawn"));
        return keywords;
    }

    public static Class<? extends Flag> findFlagClass(String input) {
        input = input.toLowerCase();
        for (Map.Entry<Class<? extends Flag>, List<String>> entry : getFlagKeywords().entrySet()) {
            Class<? extends Flag> clazz = entry.getKey();
            List<String> keywords = entry.getValue();
            if (keywords.contains(input)) {
                return clazz;
            }
        }
        return null;
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
