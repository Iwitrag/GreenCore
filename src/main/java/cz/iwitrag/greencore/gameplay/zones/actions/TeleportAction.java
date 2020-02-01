package cz.iwitrag.greencore.gameplay.zones.actions;

import cz.iwitrag.greencore.gameplay.zones.Zone;
import cz.iwitrag.greencore.gameplay.zones.ZoneManager;
import cz.iwitrag.greencore.gameplay.zones.flags.TpFlag;
import cz.iwitrag.greencore.helpers.Utils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class TeleportAction extends Action {

    private Set<Zone> targets = new HashSet<>();

    public TeleportAction(Zone zone) {
        targets.add(zone);
        validateParameters();
    }

    public TeleportAction(Zone... zones) {
        targets.addAll(Arrays.asList(zones));
        validateParameters();
    }

    @Override
    public String getDescription() {
        validateParameters();
        if (targets.size() == 0)
            return "Teleport nikam";
        else if (targets.size() == 1)
            return "Teleportuje k zóně " + targets.stream().findFirst().get().getName();
        else
            return "Teleportuje k zónám " + targets.stream().map(Zone::getName).collect(Collectors.joining(", "));
    }

    @Override
    public void execute(Player player) {
        validateParameters();
        Zone targetZone = Utils.pickRandomElement(targets);
        if (targetZone != null) {
            player.teleport(targetZone.getFlagOrDefault(TpFlag.class).getTpLocation());
            player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.0f, 1.0f);
        }
    }

    @Override
    public Action copy() {
        return new TeleportAction(targets.toArray(new Zone[0]));
    }

    public Set<Zone> getTargets() {
        validateParameters();
        return new HashSet<>(targets);
    }

    public void addTarget(Zone zone) {
        targets.add(zone);
        validateParameters();
    }

    public void removeTarget(Zone zone) {
        targets.remove(zone);
        validateParameters();
    }

    public boolean containsTarget(Zone zone) {
        validateParameters();
        return targets.contains(zone);
    }

    private void validateParameters() {
        targets.removeIf(validatedZone -> !ZoneManager.getInstance().containsZone(validatedZone));
    }
}
