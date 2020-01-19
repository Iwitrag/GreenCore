package cz.iwitrag.greencore.votifier.processers;

import cz.iwitrag.greencore.Main;
import cz.iwitrag.greencore.helpers.EconomyHelper;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

public class CzechCraftVoteProcesser extends VoteProcesser {

    private Set<String> votesToBroadcast = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    private int broadcastCountdown = 0;

    public CzechCraftVoteProcesser() {
        super("§7 >> §aHlasuj a získej §2$100.00 §aa §2$25.00 §apro všechny!\n" +
                        "§7 >> §ahttps://czech-craft.eu/vote?id=20073",
                "§7 >> §aUž můžeš zase hlasovat!\n" +
                        "§7 >> §ahttps://czech-craft.eu/vote?id=20073");

        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> {

            if (broadcastCountdown > 0) {
                broadcastCountdown--;

                if (broadcastCountdown == 0) {
                    if (!votesToBroadcast.isEmpty()) {
                        for (Player iteratedPlayer : Bukkit.getOnlinePlayers()) {
                            if (votesToBroadcast.size() == 1) {
                                iteratedPlayer.sendMessage("§7 >> §a1 hráč (§2" + votesToBroadcast.iterator().next() + "§a) hlasoval pro server! (§2/vote§a)");
                                iteratedPlayer.sendMessage("§7 >> §aZískal §2$100.00 §aa všichni na serveru dostávají §2$" + votesToBroadcast.size() * 25 + ".00");
                            } else if (votesToBroadcast.size() > 4) {
                                iteratedPlayer.sendMessage("§7 >> §a" + votesToBroadcast.size() + " hráčů (§2" + StringUtils.join(votesToBroadcast, "§a, §2") + "§a) hlasovalo pro server! (§2/vote§a)");
                                iteratedPlayer.sendMessage("§7 >> §aZískali §2$100.00 §aa všichni na serveru dostávají §2$" + votesToBroadcast.size() * 25 + ".00");
                            } else {
                                iteratedPlayer.sendMessage("§7 >> §a" + votesToBroadcast.size() + " hráči (§2" + StringUtils.join(votesToBroadcast, "§a, §2") + "§a) hlasovali pro server! (§2/vote§a)");
                                iteratedPlayer.sendMessage("§7 >> §aZískali §2$100.00 §aa všichni na serveru dostávají §2$" + votesToBroadcast.size() * 25 + ".00");
                            }

                            EconomyHelper.giveMoney(iteratedPlayer.getName(), votesToBroadcast.size() * 25.00);
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
        Player playerWhoVoted = Bukkit.getPlayer(playerName);
        if (playerWhoVoted != null) {
            playerWhoVoted.sendMessage("§7 >> §aDěkujeme za hlas! Moc si toho vážíme :)");
            playerWhoVoted.sendMessage("§7 >> §aZískáváš §2$100.00 §aa všichni na serveru dostanou §2$25.00");
        }
        if (!EconomyHelper.giveMoney(playerName, 100.00))
            Main.getInstance().getLogger().info("Player " + playerName + " not found.");
    }

    @Override
    public boolean canVoteAgain(long minutesFromLastVote) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        return calendar.get(Calendar.MINUTE) == 0 && calendar.get(Calendar.HOUR_OF_DAY) % 2 == 0;
    }

    @Override
    public boolean shouldBeReminded(long minutesFromLastRemind) {
        return minutesFromLastRemind > 60;
    }
}