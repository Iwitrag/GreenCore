package cz.iwitrag.greencore.votifier.processers;

import cz.iwitrag.greencore.Main;
import cz.iwitrag.greencore.helpers.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class VoteProcesser {

    private String remindText;
    private String canVoteAgainText;

    /** Date of player's last vote */
    private Map<String, Date> lastVoteTime = new HashMap<>();
    /** Date of when was player lastly reminded to vote, when player gets reminded, this value is removed */
    private Map<String, Date> lastRemindTime = new HashMap<>();
    /** Remaining minutes of grace period (without any reminders) */
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

        // This code runs every minute
        scheduledBukkitTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> {
            final Date now = new Date();

            // Decrease existing grace periods by 1 minute
            for (String playerName : gracePeriod.keySet()) {
                gracePeriod.put(playerName, gracePeriod.get(playerName)-1);
            }

            // Notify players who already voted that they can vote again and save their remind date to now
            for (Iterator<String> iterator = lastVoteTime.keySet().iterator(); iterator.hasNext(); ) {
                String playerName = iterator.next();
                if (canVoteAgain(minutesElapsed(now, lastVoteTime.get(playerName)))) {
                    iterator.remove();
                    Player player = Bukkit.getPlayer(playerName);
                    if (player != null && player.isOnline()) {
                        lastRemindTime.put(playerName, now);
                        player.sendMessage(getCanVoteAgainText(player.getName()));
                    }
                }
            }

            // If player should be reminded, remove his last remind date
            lastRemindTime.keySet().removeIf(playerName ->
                    shouldBeReminded(minutesElapsed(now, lastRemindTime.get(playerName)))
            );

            // Remind players of voting (if not reminded recently nor in grace period)
            for (Player player : Bukkit.getOnlinePlayers()) {
                String playerName = player.getName().toLowerCase();
                if (!lastVoteTime.containsKey(playerName) && !lastRemindTime.containsKey(playerName)) {
                    if (gracePeriod.containsKey(playerName)) {
                        if (gracePeriod.get(playerName) == 0) {
                            player.sendMessage(getRemindText(player.getName()));
                            lastRemindTime.put(playerName, now);
                            gracePeriod.remove(playerName);
                        }
                    }
                    else {
                        gracePeriod.put(playerName, 4);
                    }
                }
            }
        }, 20*60, 20*60);
    }

    public final void stopReminding() {
        Bukkit.getScheduler().cancelTask(scheduledBukkitTask);
        scheduledBukkitTask = -1;
    }

    public final void playerVoted(String playerName) {
        lastVoteTime.put(playerName.toLowerCase(), new Date());
        processVote(playerName);
    }

    public final String getRemindText(String player) {
        return Utils.replacePlaceholders(new String[]{"player"}, new String[]{"%"}, remindText, player);
    }

    public final String getCanVoteAgainText(String player) {
        return Utils.replacePlaceholders(new String[]{"player"}, new String[]{"%"}, canVoteAgainText, player);
    }

    private long minutesElapsed(Date date1, Date date2) {
        return Math.abs(date1.getTime()-date2.getTime())/1000/60;
    }

}
