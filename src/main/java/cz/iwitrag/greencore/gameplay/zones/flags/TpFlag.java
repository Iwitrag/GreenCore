package cz.iwitrag.greencore.gameplay.zones.flags;

import cz.iwitrag.greencore.gameplay.zones.Zone;
import org.bukkit.Location;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("tp")
public class TpFlag extends Flag {

    @Column(name = "tp_location")
    private Location location;

    public TpFlag() {
        this.location = null;
    }

    public TpFlag(Zone zone) {
        this.location = zone.getCenterPoint();
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public Flag copy() {
        TpFlag flag = new TpFlag();
        flag.setLocation(location);
        return flag;
    }
}
