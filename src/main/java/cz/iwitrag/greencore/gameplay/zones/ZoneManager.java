package cz.iwitrag.greencore.gameplay.zones;

import cz.iwitrag.greencore.gameplay.zones.actions.Action;
import cz.iwitrag.greencore.gameplay.zones.actions.TeleportAction;
import cz.iwitrag.greencore.helpers.GeometryHelper;
import cz.iwitrag.greencore.storage.PersistenceManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class ZoneManager {

    private static ZoneManager instance;

    private Set<Zone> zones = new LinkedHashSet<>();

    private ZoneManager() {}
    public static ZoneManager getInstance() {
        if (instance == null)
            instance = new ZoneManager();
        return instance;
    }

    public void addZone(Zone addedZone, boolean insertToDb) throws ZoneException {
        if (zones.stream().anyMatch(existingZone -> existingZone.getName().equalsIgnoreCase(addedZone.getName())))
            throw new ZoneException("Zóna s tímto názvem již existuje");
        verifyZone(addedZone);
        zones.add(addedZone);

        if (insertToDb)
            PersistenceManager.getInstance().runHibernateAsyncTask((session -> session.save(addedZone)), true);
    }

    public void verifyZone(Zone zone) throws ZoneException {
        Location p1 = zone.getPoint1();
        Location p2 = zone.getPoint2();
        if (p1 == null || p2 == null)
            throw new ZoneException("Zóna nemá platný výběr");
        if (p1.getWorld() == null || p2.getWorld() == null)
            throw new ZoneException("Svět zóny není nastaven");
        if (!p1.getWorld().equals(p2.getWorld()))
            throw new ZoneException("Svět zóny se v rozích liší");
    }

    public void deleteZone(Zone removedZone, boolean removeFromDb) {
        // Remove this zone from all TeleportActions
        Set<TeleportAction> toUpdate = new HashSet<>();
        for (Zone zone : zones) {
            for (Action action : zone.getActions()) {
                if (action instanceof TeleportAction) {
                    TeleportAction tp = (TeleportAction)action;
                    if (tp.containsTarget(removedZone)) {
                        tp.removeTarget(removedZone);
                        toUpdate.add(tp);
                    }
                }
            }
        }

        // Finally remove zone
        zones.remove(removedZone);

        if (removeFromDb) {
            // Do the same in DB side
            PersistenceManager.getInstance().runHibernateAsyncTask((session) -> {
                for (TeleportAction tp : toUpdate) {
                    session.update(tp);
                }
                session.delete(removedZone);
            }, true);
        }
    }

    public boolean containsZone(Zone zone) {
        return zones.contains(zone);
    }

    public Zone getZone(String zoneName) {
        return zones.stream().filter(zone -> zone.getName().equalsIgnoreCase(zoneName)).findFirst().orElse(null);
    }

    public Set<Zone> getZones() {
        return new LinkedHashSet<>(zones);
    }

    public boolean isPlayerInsideZone(Player player, Zone zone) {
        Location p1 = zone.getPoint1().clone().subtract(0.5, 0.5, 0.5);
        Location p2 = zone.getPoint2().clone().add(0.5, 0.5, 0.5);
        return (player.getWorld().equals(zone.getPoint1().getWorld())
                && GeometryHelper.getInstance().intersectPointCube(player.getLocation(), p1, p2));
    }

    public Set<Zone> getZonesPlayerIsInside(Player player) {
        Set<Zone> result = new HashSet<>();
        for (Zone zone : zones) {
            if (isPlayerInsideZone(player, zone))
                result.add(zone);
        }
        return result;
    }

}
