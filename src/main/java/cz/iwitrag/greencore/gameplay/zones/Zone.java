package cz.iwitrag.greencore.gameplay.zones;

import cz.iwitrag.greencore.gameplay.zones.actions.Action;
import cz.iwitrag.greencore.gameplay.zones.flags.Flag;
import org.bukkit.Location;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Zone {

    private String name;
    private int priority;
    private Location point1; // Lower
    private Location point2; // Upper
    private List<Action> actions = new ArrayList<>();
    private Set<Flag> flags = new HashSet<>();
    private Map<String, Date> executions = new HashMap<>();

    public Zone(String name, Location point1, Location point2) {
        this.priority = 0;
        this.name = name;
        this.point1 = point1;
        this.point2 = point2;
        sortLocations(this.point1, this.point2);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Location getPoint1() {
        return point1;
    }

    public void setPoint1(Location point1) {
        this.point1 = point1;
        sortLocations(this.point1, this.point2);
    }

    public Location getPoint2() {
        return point2;
    }

    public void setPoint2(Location point2) {
        this.point2 = point2;
        sortLocations(this.point1, this.point2);
    }

    public int getSizeInX() {
        return Math.abs(point1.getBlockX() - point2.getBlockX()) + 1;
    }

    public int getSizeInY() {
        return Math.abs(point1.getBlockY() - point2.getBlockY()) + 1;
    }

    public int getSizeInZ() {
        return Math.abs(point1.getBlockZ() - point2.getBlockZ()) + 1;
    }

    public Location getCenterPoint() {
        return new Location(point1.getWorld(), point1.getX()+point2.getX()/2, point1.getY()+point2.getY()/2, point1.getZ()+point2.getZ()/2);
    }

    public <T extends Flag> boolean hasFlag(Class<T> flagType) {
        for (Flag flag : flags) {
            if (flagType.isInstance(flag)) {
                return true;
            }
        }
        return false;
    }

    public <T extends Flag> T getFlagOrNull(Class<T> flagType) {
        for (Flag flag : flags) {
            if (flagType.isInstance(flag)) {
                return flagType.cast(flag);
            }
        }
        return null;
    }

    public <T extends Flag> T getFlagOrDefault(Class<T> flagType) {
        T flag = getFlagOrNull(flagType);
        if (flag == null) {
            try {
                return flagType.getDeclaredConstructor(Zone.class).newInstance(this);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                try {
                    return flagType.getDeclaredConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                    ex.printStackTrace();
                }
            }
            return null;
        } else
            return flag;
    }

    public List<Action> getActions() {
        return new ArrayList<>(actions);
    }

    public int getActionsAmount() {
        return actions.size();
    }

    public Action getAction(int id) {
        if (id >= actions.size())
            return null;
        return actions.get(id);
    }

    public void addAction(Action action) {
        int index = 0;
        int actionTime = action.getTime();
        for(;;) {
            if (index == actions.size()) {
                actions.add(action);
                break;
            }
            if (actionTime < actions.get(index).getTime()) {
                actions.add(action);
                break;
            }
            index++;
        }
    }

    public int addActionToId(Action action, int index) {
        if (index < 0)
            index = 0;
        if (index > actions.size())
            index = actions.size();
        for(;;) {
            if (index == actions.size()) {
                actions.add(index, action);
                return index;
            }
            int addedActionTime = action.getTime();
            int iteratedActionTime = actions.get(index).getTime();
            if (addedActionTime < iteratedActionTime) {
                index--;
            }
            else if (addedActionTime > iteratedActionTime) {
                index++;
            } else {
                actions.add(index, action);
                return index;
            }
        }
    }

    public void removeAction(Action action) {
        actions.remove(action);
    }

    @Override
    public String toString() {
        return name;
    }

    private static void sortLocations(Location loc1, Location loc2) {
        if (loc1.getX() > loc2.getX()) {
            double temp = loc2.getX();
            loc2.setX(loc1.getX());
            loc1.setX(temp);
        }
        if (loc1.getY() > loc2.getY()) {
            double temp = loc2.getY();
            loc2.setY(loc1.getY());
            loc1.setY(temp);
        }
        if (loc1.getZ() > loc2.getZ()) {
            double temp = loc2.getZ();
            loc2.setZ(loc1.getZ());
            loc1.setZ(temp);
        }
    }
}
