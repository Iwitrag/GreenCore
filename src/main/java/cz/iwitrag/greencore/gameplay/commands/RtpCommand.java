package cz.iwitrag.greencore.gameplay.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.taskchain.TaskChain;
import cz.iwitrag.greencore.Main;
import cz.iwitrag.greencore.helpers.DependenciesProvider;
import cz.iwitrag.greencore.helpers.LuckPermsHelper;
import cz.iwitrag.greencore.helpers.StringHelper;
import cz.iwitrag.greencore.helpers.TaskChainHelper;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RtpCommand extends BaseCommand {

    private static Map<String, Date> lastTeleports = new HashMap<>();

    @CommandAlias("priroda|survival|rtp|randomtp|randomport|randomteleport|ntp|nahtp|nahport|nahteleport|nahodnytp|nahodnyport|nahodnyteleport")
    @Description("Teleportuje tě na náhodné místo do přírody")
    @CommandPermission("rtp")
    @CommandCompletion("@players")
    public static void RandomTeleport(CommandSender commandSender, @Optional String target) {
        if (target == null) {
            if (commandSender instanceof ConsoleCommandSender) {
                commandSender.sendMessage("§cMusíš zadat přezdívku hráče");
                return;
            }
            else
                target = commandSender.getName();
        }

        if (!target.equalsIgnoreCase(commandSender.getName())) {
            TaskChain<?> chain = TaskChainHelper.newChain();
            chain.setTaskData("target", target);
            chain
            .asyncFirst(() -> LuckPermsHelper.playerHasPermission(commandSender.getName(), "rtp.admin"))
            .syncLast((hasPerm) -> {
                if (!hasPerm) {
                    chain.setTaskData("target", commandSender.getName());
                }
                doRandomTeleport(commandSender, chain.getTaskData("target"));
            })
            .execute();
        }
        else {
            doRandomTeleport(commandSender, target);
        }
    }

    private static void doRandomTeleport(CommandSender commandSender, String target) {

        Player targetPlayer = Bukkit.getPlayer(target);
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            commandSender.sendMessage("§cHráč §4" + target + " §cnení ve hře");
            return;
        } else {
            if (!targetPlayer.getWorld().getName().equalsIgnoreCase("world")) {
                if (!commandSender.getName().equalsIgnoreCase(target))
                    commandSender.sendMessage("§cHráč §4" + target + " §cnení v survival světě");
                else
                    targetPlayer.sendMessage("§cMusíš být v survival světě pro náhodný port :)");

                return;
            }
        }

        target = targetPlayer.getName(); // Because it could have resolved partial name

        TaskChain<?> chain = TaskChainHelper.newChain();
        chain.setTaskData("target", target);
        chain
        .asyncFirst(() -> {
            if (LuckPermsHelper.playerHasPermission(commandSender.getName(), "rtp.admin"))
                return -1;
            else if (LuckPermsHelper.playerHasPermission(commandSender.getName(), "rtp.vip"))
                return 60;
            else
                return 180;
        })
        .syncLast((cooldown) -> {
            String targetName = chain.getTaskData("target");
            long secondsElapsed = Long.MAX_VALUE;
            if (lastTeleports.containsKey(targetName.toLowerCase())) {
                secondsElapsed = secondsElapsed(lastTeleports.get(targetName.toLowerCase()), new Date());
            }
            if (secondsElapsed < cooldown) {
                commandSender.sendMessage("§cDo dalšího teleportu zbývá §4" + StringHelper.timeToLongString(cooldown-secondsElapsed));
                return;
            }

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spreadplayers 0 0 0 8000 false " + targetName);

            lastTeleports.put(targetName.toLowerCase(), new Date());
            targetPlayer.sendMessage("§aTeleportuji tě náhodně do přírody, hodně štěstí :)");
            if (!commandSender.getName().equalsIgnoreCase(targetName)) {
                commandSender.sendMessage("§aHráč §2" + targetName + " §ateleportován");
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
                if (targetPlayer.isOnline()) {
                    DependenciesProvider.getInstance().getEssentials().getUser(targetPlayer.getName()).setHome("rtp", targetPlayer.getLocation());
                    targetPlayer.sendMessage("§7Pokud umřeš, můžeš se dostat na místo posledního náhodného teleportu příkazem §f/home rtp");
                }
            }, 10);
        })
        .execute();
    }

    private static long secondsElapsed(Date date1, Date date2) {
        return Math.abs(date1.getTime()-date2.getTime())/1000;
    }

}
