package cz.iwitrag.greencore.helpers;

import org.bukkit.Location;

public class LocationHelper {

    private static LocationHelper instance;

    private LocationHelper() {
        instance = this;
    }

    public static LocationHelper getInstance() {
        if (instance == null)
            instance = new LocationHelper();
        return instance;
    }

    public void sortLocations(Location toLower, Location toHigher) {
        if (toLower.getX() > toHigher.getX()) {
            double temp = toHigher.getX();
            toHigher.setX(toLower.getX());
            toLower.setX(temp);
        }
        if (toLower.getY() > toHigher.getY()) {
            double temp = toHigher.getY();
            toHigher.setY(toLower.getY());
            toLower.setY(temp);
        }
        if (toLower.getZ() > toHigher.getZ()) {
            double temp = toHigher.getZ();
            toHigher.setZ(toLower.getZ());
            toLower.setZ(temp);
        }
    }
}
