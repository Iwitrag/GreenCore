package cz.iwitrag.greencore.gameplay.zones;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
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
import cz.iwitrag.greencore.gameplay.zones.flags.MineFlag;
import cz.iwitrag.greencore.gameplay.zones.flags.ParticlesFlag;
import cz.iwitrag.greencore.gameplay.zones.flags.TpFlag;
import cz.iwitrag.greencore.helpers.DependenciesProvider;
import cz.iwitrag.greencore.helpers.StringHelper;
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
    public void createCommand(Player sender, String name) {
        Location p1 = DependenciesProvider.getInstance().getWorldEditSelection(sender.getName(), true);
        Location p2 = DependenciesProvider.getInstance().getWorldEditSelection(sender.getName(), false);
        if (p1 == null || p2 == null) {
            sender.sendMessage("§cPřed vytvoření zóny musíš vybrat oblast!");
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
        }
        else {
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
        sender.sendMessage("§9Velikost zóny: X: §f" + zone.getSizeInX() + "§9, Y: §f" + zone.getSizeInY() + "§9, Z: §f" + zone.getSizeInZ());
        sender.sendMessage("§9Bod 1: " + StringHelper.locationToString(zone.getPoint1(), false, "§9", "§f"));
        sender.sendMessage("§9Bod 2: " + StringHelper.locationToString(zone.getPoint2(), false, "§9", "§f"));
        int actions = zone.getActions().size();
        if (actions > 0)
            sender.sendMessage("§7Seznam akcí (" + actions + "): §f/" + getExecCommandLabel() + " actions list " + zoneName);
        sender.sendMessage("§7Seznam vlajek: §f/" + getExecCommandLabel() + " flags list " + zoneName);
        sender.sendMessage("§8" + StringHelper.getChatLine());
    }

    @Subcommand("priority|setpriority|priorita")
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
        zone.setPoint1(p1);
        zone.setPoint2(p2);
        sender.sendMessage("§aOblast zóny §2" + zone.getName() + " §azměněna!");
    }

    @Subcommand("delete|remove|smazat|odstranit")
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
        // ZONE TODO - při odstranění zóny musí přestat fungovat všechny flagy
        sender.sendMessage("§aZóna §2" + zone.getName() + " §abyla odstraněna!");
    }

    @Subcommand("action|actions|akce list|seznam|vypsat")
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

    @Subcommand("action|actions|akce add|create|new|define|pridat|vytvorit|nova|definovat")
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

    @Subcommand("action|actions|akce edit|change|param|params|parameter|parameters|upravit|parametr|parametry")
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

    @Subcommand("action|actions|akce time|tick|ticks|cas|ticky")
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

    @Subcommand("action|actions|akce id|index|identifikator|number|cislo")
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

    @Subcommand("action|actions|akce delete|remove|smazat|odstranit")
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
        Action action = zone.getAction(id);
        if (action == null) {
            sender.sendMessage("§cV zóně §4" + zoneName + " §cneexistuje akce s ID §4" + id);
            return;
        }
        zone.removeAction(action);
        sender.sendMessage("§aAkce s ID §2" + id + " §av zóně §2" + zone.getName() + " §abyla odstraněna!");
    }

    @Subcommand("flag|flags|flagy|vlajka|vlajky list|seznam|vypsat")
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
        String particleName = particlesFlag.getParticle() == null ? "---" : particlesFlag.getParticle().name();
        sender.sendMessage("§9Typ: " + color + particleName);
        sender.sendMessage("§9Hustota: " + color + particlesFlag.getDensity());
        if (particleName.equals(Particle.REDSTONE.name())) {
            sender.sendMessage("§9Barva: Č: " + color + particlesFlag.getRed() + "§9, Z: " + color + particlesFlag.getGreen() + "§9, M: " + color + particlesFlag.getBlue());
        }

        sender.sendMessage("§bDoly");
        MineFlag mineFlag = zone.getFlagOrDefault(MineFlag.class);
        color = zone.hasFlag(MineFlag.class) ? "§e" : "§f";
        sender.sendMessage("§9Bloky: " + color + mineFlag.getBlocksAsString());
        sender.sendMessage("§9Interval: " + color + mineFlag.getRegenTime() + " s.");
        sender.sendMessage("§8" + StringHelper.getChatLine());
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
            } catch (NumberFormatException ex) {
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
            } catch (NumberFormatException ex) {
                throw new ZoneException("§cÚroveň lektvaru musí být platné celé číslo (zadáno §4" + paramArray[1] + "§c).");
            }
            int duration;
            try {
                duration = Integer.parseInt(paramArray[2]);
            } catch (NumberFormatException ex) {
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


    /*
    // ZONE TODO - dodělat příkazy pro kopírování a flagy
    /zone copy actions/flags/all ZDROJ CÍL
    /zone flags set NÁZEV FLAG [TYP HODNOTA]
    /zone flags unset NÁZEV FLAG [TYP]
    // ZONE TODO - realizovat všechny flagy a listenery
    // ZONE TODO - přidat všude auto-complete
    // ZONE TODO - otestovat implementaci zón
    // ZONE TODO - implementovat perzistenci
     */

