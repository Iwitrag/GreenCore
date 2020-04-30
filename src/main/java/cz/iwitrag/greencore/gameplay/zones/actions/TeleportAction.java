package cz.iwitrag.greencore.gameplay.zones.actions;

import cz.iwitrag.greencore.gameplay.zones.Zone;
import cz.iwitrag.greencore.gameplay.zones.flags.TpFlag;
import cz.iwitrag.greencore.helpers.Utils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@DiscriminatorValue("tp")
public class TeleportAction extends Action {

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "tpaction_targetzones", joinColumns = @JoinColumn(name="tpAction_id"), inverseJoinColumns = @JoinColumn(name="targetZone_id"))
    private Set<Zone> targets = new HashSet<>();

    public TeleportAction() {}

    public TeleportAction(Zone zone) {
        targets.add(zone);
    }

    public TeleportAction(Zone... zones) {
        targets.addAll(Arrays.asList(zones));
    }

    @Override
    public String getDescription() {
        if (targets.size() == 0)
            return "Teleport nikam";
        else if (targets.size() == 1)
            return "Teleportuje k zóně " + targets.stream().findFirst().get().getName();
        else
            return "Teleportuje k zónám " + targets.stream().map(Zone::getName).collect(Collectors.joining(", "));
    }

    @Override
    public void execute(Player player) {
        Zone targetZone = Utils.pickRandomElement(targets);
        if (targetZone != null) {
            player.teleport(targetZone.getFlagOrDefault(TpFlag.class).getLocation());
            player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.0f, 1.0f);
        }
    }

    @Override
    public Action copy() {
        return new TeleportAction(targets.toArray(new Zone[0]));
    }

    public Set<Zone> getTargets() {
        return new HashSet<>(targets);
    }

    public void addTarget(Zone zone) {
        targets.add(zone);
    }

    public void removeTarget(Zone zone) {
        targets.remove(zone);
    }

    public boolean containsTarget(Zone zone) {
        return targets.contains(zone);
    }
}
