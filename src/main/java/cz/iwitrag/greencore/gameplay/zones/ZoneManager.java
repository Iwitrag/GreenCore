package cz.iwitrag.greencore.gameplay.zones;

import org.bukkit.Location;

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

    public void addZone(Zone addedZone) throws ZoneException {
        if (zones.stream().anyMatch(existingZone -> existingZone.getName().equalsIgnoreCase(addedZone.getName())))
            throw new ZoneException("Zóna s tímto názvem již existuje");
        verifyZone(addedZone);
        zones.add(addedZone);
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

    public void deleteZone(Zone removedZone) {
        zones.remove(removedZone);
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
}
