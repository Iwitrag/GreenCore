package cz.iwitrag.greencore.votifier.processers;

import cz.iwitrag.greencore.Main;
import cz.iwitrag.greencore.gameplay.chat.ChatUtils;
import cz.iwitrag.greencore.gameplay.chat.TextBuilder;
import cz.iwitrag.greencore.gameplay.itemdb.ItemDBCommands;
import cz.iwitrag.greencore.helpers.DependenciesProvider;
import cz.iwitrag.greencore.helpers.EconomyHelper;
import cz.iwitrag.greencore.helpers.StringHelper;
import cz.iwitrag.greencore.helpers.Utils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

public class CzechCraftVoteProcesser extends VoteProcesser {

    private Set<String> votesToBroadcast = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    private int broadcastCountdown = 0;

    private static final int MONEY_EVERYONE = 50;

    public CzechCraftVoteProcesser() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> {
            if (broadcastCountdown > 0) {
                broadcastCountdown--;

                if (broadcastCountdown == 0) {
                    if (!votesToBroadcast.isEmpty()) {
                        int votes = votesToBroadcast.size();
                        for (Player iteratedPlayer : Bukkit.getOnlinePlayers()) {
                            iteratedPlayer.sendMessage(new TextBuilder(" >> X hráčů (seznam) hlasovalo pro server! (/vote)")
                                    .from().color(ChatColor.GREEN)
                                    .target(">>").color(ChatColor.GRAY)
                                    .target("X").replace(votesToBroadcast.size())
                                    .target("hráčů").replaceIf(votes == 1, "hráč").replaceIf(votes > 1 && votes < 5, "hráči")
                                    .target("seznam").replace(StringUtils.join(votesToBroadcast, "§a, §2"))
                                    .target("hlasovalo").replaceIf(votes == 1, "hlasoval").replaceIf(votes > 1 && votes < 5, "hlasovali")
                                    .target("/vote").hover("§aKlikni pro info o hlasování").command().color(ChatColor.DARK_GREEN)
                                    .create()
                            );
                            iteratedPlayer.sendMessage(new TextBuilder(" >> Získali voteklíč a všichni na serveru dostávají peníze")
                                    .from().color(ChatColor.GREEN)
                                    .target(">>").color(ChatColor.GRAY)
                                    .target("Získali").replaceIf(votes == 1, "Získal")
                                    .target("voteklíč").itemable(getVoteKey(), true)
                                    .target("peníze").color(ChatColor.DARK_GREEN).replace(Utils.twoDecimal(votesToBroadcast.size() * MONEY_EVERYONE))
                                    .create()
                            );

                            EconomyHelper.giveMoney(iteratedPlayer.getName(), votesToBroadcast.size() * MONEY_EVERYONE);
                        }
                        votesToBroadcast.clear();
                    }
                }
            }

        }, 0, 20);
    }

    @Override
    public void processVote(String playerName) {
        votesToBroadcast.add(playerName);
        broadcastCountdown = 15;
        Player playerWhoVoted = Bukkit.getPlayerExact(playerName);
        if (playerWhoVoted != null) {
            playerWhoVoted.sendMessage("§7 >> §aDěkujeme za hlas! Moc si toho vážíme :)");
            playerWhoVoted.sendMessage(new ComponentBuilder("")
                    .appendLegacy("§7 >> §aZískáváš ").reset()
                    .appendLegacy("§2").append(ChatUtils.getItemableText(false, getVoteKey())).reset()
                    .appendLegacy(" §aa všichni na serveru dostanou §2$" + Utils.twoDecimal(MONEY_EVERYONE)).reset()
                    .create()
            );
            HashMap<Integer, ItemStack> leftOver = playerWhoVoted.getInventory().addItem(getVoteKey());
            if (!leftOver.isEmpty()) {
                playerWhoVoted.sendMessage("§eNebylo místo v inventáři, klíč byl dropnut na zem");
                playerWhoVoted.getWorld().dropItemNaturally(playerWhoVoted.getLocation(), getVoteKey());
            }
        }
    }

    @Override
    public boolean canVoteAgain(long minutesFromLastVote) {
        return minutesFromLastVote >= 120;
    }

    @Override
    public boolean shouldBeReminded(long minutesFromLastRemind) {
        return minutesFromLastRemind >= 30;
    }

    @Override
    public BaseComponent[] getRemindText(String playerName) {
        return new ComponentBuilder("")
                .appendLegacy("§7 >> §aHlasuj a získej §r")
                .append(ChatUtils.getItemableText(false, getVoteKey())).reset()
                .appendLegacy(" §aa §2$"+ Utils.twoDecimal(MONEY_EVERYONE)+" §apro všechny!\n\n§r")
                .append(ChatUtils.getHoverableTextWithURL(StringHelper.centerMessage("§7§l >>> §a§lKlikni zde §7§l<<<"), "§aKliknutím se otevře stránka pro hlasování", getVoteURL(playerName))).reset()
                .create();
    }

    @Override
    public BaseComponent[] getCanVoteAgainText(String playerName) {
        return new ComponentBuilder("")
                .appendLegacy("§7 >> §aUž můžeš zase hlasovat!\n\n").reset()
                .append(ChatUtils.getHoverableTextWithURL(StringHelper.centerMessage("§7§l >>> §a§lKlikni zde §7§l<<<"), "§aKliknutím se otevře stránka pro hlasování", getVoteURL(playerName))).reset()
                .create();
    }

    private ItemStack getVoteKey() {
        return DependenciesProvider.getInstance().getDefaultItemDB().getItemExact(ItemDBCommands.GLOBAL_OWNER, "key_vote");
    }

    private String getVoteURL(String playerName) {
        return "https://czech-craft.eu/server/greenlandia/vote/?user=" + playerName;
    }
}
