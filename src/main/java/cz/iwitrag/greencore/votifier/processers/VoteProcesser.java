package cz.iwitrag.greencore.votifier.processers;

import cz.iwitrag.greencore.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class VoteProcesser {

    private String remindText;
    private String canVoteAgainText;

    private Map<String, Date> lastVoteTime = new HashMap<>();
    private Map<String, Date> lastRemindTime = new HashMap<>();
    private Map<String, Integer> gracePeriod = new HashMap<>();
    private int scheduledBukkitTask = -1;

    public VoteProcesser(String remindText, String canVoteAgainText) {
        this.remindText = remindText;
        this.canVoteAgainText = canVoteAgainText;
    }

    abstract void processVote(String playerName);

    abstract boolean canVoteAgain(long minutesFromLastVote);

    abstract boolean shouldBeReminded(long minutesFromLastRemind);

    public final void startReminding() {
        if (scheduledBukkitTask != -1)
            return;

        scheduledBukkitTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> {
            final Date now = new Date();

            for (String playerName : gracePeriod.keySet()) {
                gracePeriod.put(playerName, gracePeriod.get(playerName)-1);
            }

            for (Iterator<String> iterator = lastVoteTime.keySet().iterator(); iterator.hasNext(); ) {
                String playerName = iterator.next();
                if (canVoteAgain(minutesElapsed(now, lastVoteTime.get(playerName)))) {
                    iterator.remove();
                    Player player = Bukkit.getPlayer(playerName);
                    if (player != null && player.isOnline()) {
                        lastRemindTime.put(playerName, now);
                        player.sendMessage(getCanVoteAgainText());
                    }
                }
            }

            lastRemindTime.keySet().removeIf(playerName ->
                    shouldBeReminded(minutesElapsed(now, lastRemindTime.get(playerName)))
            );

            for (Player player : Bukkit.getOnlinePlayers()) {
                String playerName = player.getName().toLowerCase();
                if (!lastVoteTime.containsKey(playerName) && !lastRemindTime.containsKey(playerName)) {
                    if (gracePeriod.containsKey(playerName)) {
                        if (gracePeriod.get(playerName) == 0) {
                            player.sendMessage(getRemindText());
                            lastRemindTime.put(playerName, now);
                            gracePeriod.remove(playerName);
                        }
                    }
                    else {
                        gracePeriod.put(playerName, 4);
                    }
                }
            }
        }, 1200, 1200);
    }

    public final void stopReminding() {
        Bukkit.getScheduler().cancelTask(scheduledBukkitTask);
        scheduledBukkitTask = -1;
    }

    public final void playerVoted(String playerName) {
        lastVoteTime.put(playerName.toLowerCase(), new Date());
        processVote(playerName);
    }

    public final String getRemindText() {
        return remindText;
    }

    public final String getCanVoteAgainText() {
        return canVoteAgainText;
    }

    private final long minutesElapsed(Date date1, Date date2) {
        return Math.abs(date1.getTime()-date2.getTime())/1000/60;
    }

}
