package cz.iwitrag.greencore.helpers;

import org.bukkit.Location;

public class GeometryHelper {

    private static GeometryHelper instance;

    private GeometryHelper() {
        instance = this;
    }

    public static GeometryHelper getInstance() {
        if (instance == null)
            instance = new GeometryHelper();
        return instance;
    }

    /**
     * Checks for intersection of Sphere and axis-aligned Cube
     */
    public boolean intersectSphereCube(Location c, double r, Location p1, Location p2) {
        LocationHelper.getInstance().sortLocations(p1, p2);
        double rr = r * r;
        if (c.getX() < p1.getX()) rr -= Math.pow(c.getX() - p1.getX(), 2);
        else if (c.getX() > p2.getX()) rr -= Math.pow(c.getX() - p2.getX(), 2);
        if (c.getY() < p1.getY()) rr -= Math.pow(c.getY() - p1.getY(), 2);
        else if (c.getY() > p2.getY()) rr -= Math.pow(c.getY() - p2.getY(), 2);
        if (c.getZ() < p1.getZ()) rr -= Math.pow(c.getZ() - p1.getZ(), 2);
        else if (c.getZ() > p2.getZ()) rr -= Math.pow(c.getZ() - p2.getZ(), 2);
        return rr > 0;
    }

    /**
     * Checks for intersection of point and axis-aligned Cube
     */
    public boolean intersectPointCube(Location pp, Location p1, Location p2) {
        LocationHelper.getInstance().sortLocations(p1, p2);
        return pp.getX() >= p1.getX() && pp.getY() >= p1.getY() && pp.getZ() >= p1.getZ() &&
                pp.getX() <= p2.getX() && pp.getY() <= p2.getY() && pp.getZ() <= p2.getZ();
    }
}
