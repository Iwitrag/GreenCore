package cz.iwitrag.greencore.votifier;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import org.bukkit.command.CommandSender;

// Czech-craft /vote command
public class VoteCommand extends BaseCommand {

    @CommandAlias("vote|hlasovat|hlas|hlasuj|czechcraft|hlasovani")
    @Description("Hlasuj pro server!")
    public static void infoAboutVoting(CommandSender commandSender) {
        commandSender.sendMessage(VoteProcessersManager.getRegisteredVoteProcesser("Czech-Craft.eu").getRemindText());
    }

}
