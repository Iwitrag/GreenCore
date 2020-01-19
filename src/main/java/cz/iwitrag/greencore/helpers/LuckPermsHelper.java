package cz.iwitrag.greencore.helpers;

import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LuckPermsHelper {

    private LuckPermsHelper() {}

    /** Blocking ! */
    @Nullable private static User getUser(String playerName) {
        UUID uuid = DependenciesProvider.getInstance().getLuckPerms().getUserManager().lookupUniqueId(playerName).join();
        if (uuid == null)
            return null;
        //uuid = Bukkit.getOfflinePlayer(playerName).getUniqueId(); // This would return user even if he doesn't exist
        return DependenciesProvider.getInstance().getLuckPerms().getUserManager().loadUser(uuid).join();
    }

    @Nullable private static Group getGroup(String groupName) {
        return DependenciesProvider.getInstance().getLuckPerms().getGroupManager().getGroup(groupName);
    }

    /** Blocking ! */
    @NotNull public static String getPlayerMainPrefix(String playerName) {
        // Player prefix has priority over group prefix
        User user = getUser(playerName);
        if (user == null)
            return "";

        String prefix = user.getCachedData().getMetaData(QueryOptions.nonContextual()).getPrefix();
        return prefix == null ? "" : prefix;
    }

    /** Blocking ! */
    public static boolean playerHasPermission(String playerName, String permission) {
        if (playerName.equalsIgnoreCase("console") || Bukkit.getOfflinePlayer(playerName).isOp())
            return true;

        User user = getUser(playerName);
        if (user == null)
            return false;

        return user.getCachedData().getPermissionData(QueryOptions.nonContextual()).checkPermission(permission).asBoolean();
    }

    private static boolean userHasPermission(User user, String permission) {
        return user.getCachedData().getPermissionData(QueryOptions.nonContextual()).checkPermission(permission).asBoolean();
    }

    public static List<Group> getGroups(boolean sortByHighestPriorityFirst) {
        List<Group> groups = new ArrayList<>(DependenciesProvider.getInstance().getLuckPerms().getGroupManager().getLoadedGroups());
        if (sortByHighestPriorityFirst) {
            groups.sort((g1, g2) -> g2.getWeight().orElse(0) - g1.getWeight().orElse(0));
        }
        return groups;
    }

    /** Blocking ! */
    @Nullable public static Group getPlayerPrimaryGroup(String playerName) {
        User user = getUser(playerName);
        if (user == null)
            return null;

        for (Group group : getGroups(true)) {
            if (userHasPermission(user, "group." + group.getName())) {
                return group;
            }
        }
        return null;
    }

    /** Blocking ! */
    public static boolean isPlayerInGroup(String playerName, String groupName) {
        User user = getUser(playerName);
        if (user == null)
            return false;

        return userHasPermission(user, "group." + groupName);
    }

    /** Blocking ! */
    private static boolean isUserInGroup(User user, String groupName) {
        return userHasPermission(user, "group." + groupName);
    }

    /** Blocking ! */
    public static boolean addPlayerToGroup(String playerName, String groupName, Long durationSeconds) {
        User user = getUser(playerName);
        if (user == null)
            return false;
        Group group = getGroup(groupName);
        if (group == null)
            return false;
        if (durationSeconds != null && durationSeconds <= 0)
            return true;

        // If player is already in that group, get its node
        List<Node> allUserNodes = (List<Node>) user.getNodes();
        InheritanceNode oldGroupNode = null;
        for (Node node : allUserNodes) {
            InheritanceNode groupNode = NodeType.INHERITANCE.tryCast(node).orElse(null);
            if (groupNode != null
                    && groupNode.getContexts().isEmpty()
                    && groupNode.getGroupName().equalsIgnoreCase(groupName)) {
                oldGroupNode = groupNode;
            }
        }

        // If player is already in that group and its permanent, do nothing
        if (oldGroupNode != null && !oldGroupNode.hasExpiry() && !oldGroupNode.hasExpired())
            return true;

        InheritanceNode newGroupNode;

        // Adding temporary group
        if (durationSeconds != null) {
            long expiryDateInSeconds = Instant.now().getEpochSecond() + durationSeconds;

            // If player is already in that group temporarily, extend his duration
            if (oldGroupNode != null && oldGroupNode.hasExpiry() && !oldGroupNode.hasExpired()) {
                Instant nodeExpiryDate = oldGroupNode.getExpiry();
                if (nodeExpiryDate != null) {
                    expiryDateInSeconds = nodeExpiryDate.getEpochSecond() + durationSeconds;
                }
            }

            newGroupNode = DependenciesProvider.getInstance().getLuckPerms().getNodeBuilderRegistry()
                    .forInheritance()
                    .group(group)
                    .expiry(expiryDateInSeconds)
                    .build();
        }
        // Adding permanent group
        else {
            newGroupNode = DependenciesProvider.getInstance().getLuckPerms().getNodeBuilderRegistry()
                    .forInheritance()
                    .group(group)
                    .build();
        }

        // Remove previous group node (if any)
        if (oldGroupNode != null) {
            user.data().remove(oldGroupNode);
        }

        // Add new group node
        boolean result = user.data().add(newGroupNode) == DataMutateResult.SUCCESS;
        DependenciesProvider.getInstance().getLuckPerms().getUserManager().saveUser(user);
        return result;
    }

    /** Blocking ! */
    public static long getPlayerGroupDuration(String playerName, String groupName) {
        User user = getUser(playerName);
        if (user == null)
            return -1;
        Group group = getGroup(groupName);
        if (group == null)
            return -1;

        List<Node> allUserNodes = (List<Node>) user.getNodes();
        for (Node node : allUserNodes) {
            InheritanceNode groupNode = NodeType.INHERITANCE.tryCast(node).orElse(null);
            if (groupNode != null
                    && groupNode.getContexts().isEmpty()
                    && !groupNode.hasExpired()
                    && groupNode.getGroupName().equalsIgnoreCase(groupName)) {
                if (!groupNode.hasExpiry()) {
                    return Long.MAX_VALUE;
                }
                else {
                    Instant nodeExpiryDate = groupNode.getExpiry();
                    if (nodeExpiryDate != null) {
                        return nodeExpiryDate.getEpochSecond() - Instant.now().getEpochSecond();
                    }
                }
            }
        }

        return 0;
    }

}
