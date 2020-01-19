package cz.iwitrag.greencore;

import cz.iwitrag.greencore.helpers.LuckPermsHelper;
import cz.iwitrag.greencore.helpers.PermGroupNames;
import cz.iwitrag.greencore.helpers.TaskChainHelper;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Objects;

public class ScoreboardUnifier implements Listener {

    private Scoreboard globalScoreboard;
    private Team globalTeam;

    public ScoreboardUnifier() {
        globalScoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
        globalTeam = globalScoreboard.registerNewTeam("greenlandia_team");
        applyTeamFlags(globalTeam);

        for (Group group : LuckPermsHelper.getGroups(false)) {
            String groupTeamName = getGroupNameWithPriority(group);
            Team groupTeam = globalScoreboard.getTeam(groupTeamName);
            if (groupTeam == null)
                groupTeam = globalScoreboard.registerNewTeam(groupTeamName);
            applyTeamFlags(groupTeam);
            String groupName = group.getName().toLowerCase();
            if (PermGroupNames.owner().contains(groupName) ||
                    PermGroupNames.admin().contains(groupName) ||
                    PermGroupNames.builder().contains(groupName) ||
                    PermGroupNames.mod().contains(groupName))
                groupTeam.setCanSeeFriendlyInvisibles(true);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            applyGlobalTeam(player);
            applyPlayerTeam(player);
        }

        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                setPlayerTeamPrefix(player);
            }
        }, 200, 200); // Refresh every 10 seconds
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        applyGlobalTeam(event.getPlayer());
        applyPlayerTeam(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        removeScoreboard(event.getPlayer());
    }

    private void applyPlayerTeam(Player player) {
        String playerName = player.getName();
        Team playerTeam = globalScoreboard.getTeam(playerName);
        if (playerTeam == null)
            playerTeam = globalScoreboard.registerNewTeam(playerName);

        if (!playerTeam.hasEntry(playerName))
            playerTeam.addEntry(playerName);

        applyTeamFlags(playerTeam);
        setPlayerTeamPrefix(player);

        TaskChainHelper.newChain()
        .asyncFirst(() -> LuckPermsHelper.getPlayerPrimaryGroup(player.getName()))
        .abortIfNull()
        .syncLast((primaryGroup) -> {
            Team groupTeam = globalScoreboard.getTeam(getGroupNameWithPriority(primaryGroup));
            if (groupTeam != null && !groupTeam.hasEntry(playerName))
                groupTeam.addEntry(playerName);
        })
        .execute();

    }

    private void applyGlobalTeam(Player player) {
        String playerName = player.getName();
        player.setScoreboard(globalScoreboard);

        if (!globalTeam.hasEntry(playerName))
            globalTeam.addEntry(playerName);
    }

    private void removeScoreboard(Player player) {
        String playerName = player.getName();

        if (globalTeam.hasEntry(playerName))
            globalTeam.removeEntry(playerName);

        Team team = player.getScoreboard().getTeam(player.getName());
        if (team != null)
            team.unregister();
    }

    private void applyTeamFlags(Team team) {
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        team.setOption(Team.Option.DEATH_MESSAGE_VISIBILITY, Team.OptionStatus.NEVER);
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        team.setAllowFriendlyFire(true);
        team.setCanSeeFriendlyInvisibles(false);
    }

    private void setPlayerTeamPrefix(Player player) {
        Team team = player.getScoreboard().getTeam(player.getName());
        if (team != null) {
            TaskChainHelper.newChain()
            .asyncFirst(() -> LuckPermsHelper.getPlayerMainPrefix(player.getName()))
            .syncLast((teamPrefix) -> {
                // Must check if the team is still registered
                if (player.getScoreboard().getTeam(player.getName()) != null) {
                    team.setPrefix(ChatColor.translateAlternateColorCodes('&', teamPrefix));
                    if (teamPrefix.length() >= 2 && teamPrefix.charAt(teamPrefix.length() - 2) == '&') {
                        ChatColor teamColor = ChatColor.getByChar(teamPrefix.charAt(teamPrefix.length() - 1));
                        if (teamColor != null)
                            team.setColor(teamColor);
                    }
                }
            }).execute();
        }
    }

    private String getGroupNameWithPriority(Group group) {
        int groupPriority = group.getWeight().orElse(0);
        String result = String.format("%03d", 999 - groupPriority) + group.getName();
        result = result.substring(0, Math.min(result.length(), 16)).toLowerCase();
        return result;
    }

    // TODO - fix player nametags
    // TODO - fix new players white name in TAB (player 0yo was on top of playerlist when first joined)

}
