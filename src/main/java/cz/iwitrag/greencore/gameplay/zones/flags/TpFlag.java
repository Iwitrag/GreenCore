package cz.iwitrag.greencore.gameplay.zones.flags;

import cz.iwitrag.greencore.gameplay.zones.Zone;
import org.bukkit.Location;

public class TpFlag implements Flag {

    private Location tpLocation;

    public TpFlag() {
        this.tpLocation = null;
    }

    public TpFlag(Zone zone) {
        this.tpLocation = zone.getCenterPoint();
    }

    public Location getTpLocation() {
        return tpLocation;
    }

    public void setTpLocation(Location tpLocation) {
        this.tpLocation = tpLocation;
    }
}
