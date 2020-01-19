package cz.iwitrag.greencore.gameplay.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.taskchain.TaskChain;
import cz.iwitrag.greencore.helpers.LuckPermsHelper;
import cz.iwitrag.greencore.helpers.StringHelper;
import cz.iwitrag.greencore.helpers.TaskChainHelper;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class VipCommand extends BaseCommand {

    @CommandAlias("vip|vipko|vipplus|vip+|vipkoplus|vipko+|timeleft|vipt")
    @Description("Zjistí, na jak dlouho zbývá VIP a VIP+")
    @CommandCompletion("@players")
    public static void getVip(CommandSender commandSender, @Optional String target) {
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
            .asyncFirst(() -> LuckPermsHelper.playerHasPermission(commandSender.getName(), "vip.seeothers"))
            .syncLast((hasPerm) -> {
                if (!hasPerm) {
                    chain.setTaskData("target", commandSender.getName());
                }
                printVipDuration(commandSender, chain.getTaskData("target"));
            })
             .execute();
        }
        else {
            printVipDuration(commandSender, target);
        }

    }

    private static void printVipDuration(CommandSender commandSender, String target) {
        TaskChain<?> chain = TaskChainHelper.newChain();
        chain
        .async(() -> {
            chain.setTaskData("vipDuration", LuckPermsHelper.getPlayerGroupDuration(target, "vip"));
            chain.setTaskData("vipPlusDuration", LuckPermsHelper.getPlayerGroupDuration(target, "vipplus"));
        })
        .sync(() -> {
            commandSender.sendMessage("§8" + StringHelper.getChatLine());
            long vipDuration = chain.getTaskData("vipDuration");
            long vipPlusDuration = chain.getTaskData("vipPlusDuration");
            boolean checkingMyVip = target.equalsIgnoreCase(commandSender.getName());
            commandSender.sendMessage(checkingMyVip ? "§aTvé výhody:" : ("§aVýhody hráče " + target + ":"));
            String resultVip = checkingMyVip ? "§cChyba při zjišťování" : "§cHráč nenalezen";
            if (vipDuration == 0)
                resultVip = checkingMyVip ? "§cNemáš VIP" : "§cNemá VIP";
            if (vipDuration > 0)
                resultVip = "§2" + StringHelper.timeToLongString(vipDuration);
            String resultVipPlus = checkingMyVip ? "§cChyba při zjišťování" : "§cHráč nenalezen";
            if (vipPlusDuration == 0)
                resultVipPlus = checkingMyVip ? "§cNemáš VIP+" : "§cNemá VIP+";
            if (vipPlusDuration > 0)
                resultVipPlus = "§2" + StringHelper.timeToLongString(vipPlusDuration);
            commandSender.sendMessage("§a   VIP: " + resultVip);
            commandSender.sendMessage("§a   VIP+: " + resultVipPlus);
            if (checkingMyVip && vipDuration == 0 && vipPlusDuration == 0) {
                commandSender.sendMessage("§aVIP jde zatím koupit jen přes PayPal, PaySafeCard a převodem. " +
                        "Výhody a ceny jsou na webu. V případě zájmu napiš na §2platby@greenlandia.cz");
            }
            commandSender.sendMessage("§8" + StringHelper.getChatLine());
        })
        .execute();
    }

}
