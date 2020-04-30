package cz.iwitrag.greencore.gameplay.zones;

import cz.iwitrag.greencore.gameplay.zones.actions.Action;
import cz.iwitrag.greencore.gameplay.zones.flags.Flag;
import cz.iwitrag.greencore.helpers.LocationHelper;
import org.bukkit.Location;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Transient;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int priority;

    @Column(nullable = false)
    private Location point1; // Lower

    @Column(nullable = false)
    private Location point2; // Upper

    @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true)
    @Cascade(CascadeType.ALL)
    @OrderColumn(name = "list_id")
    @JoinColumn(name = "zone_id")
    private List<Action> actions = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true)
    @Cascade(CascadeType.ALL)
    @JoinColumn(name = "zone_id")
    private Set<Flag> flags = new HashSet<>();

    @Transient
    private Map<String, Date> executions = new HashMap<>();

    public Zone() {}

    public Zone(String name, Location point1, Location point2) {
        this.id = null;
        this.priority = 0;
        this.name = name;
        this.point1 = point1;
        this.point2 = point2;
        LocationHelper.getInstance().sortLocations(this.point1, this.point2);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Location getPoint2() {
        return point2;
    }

    public void setPoints(Location point1, Location point2) {
        this.point1 = point1;
        this.point2 = point2;
        LocationHelper.getInstance().sortLocations(this.point1, this.point2);
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

    public int getSize() {
        return getSizeInX()*getSizeInY()*getSizeInZ();
    }

    public Location getCenterPoint() {
        double xMin = point1.getX()-0.5;
        double yMin = point1.getY()-0.5;
        double zMin = point1.getZ()-0.5;
        double xMax = point2.getX()+0.5;
        double yMax = point2.getY()+0.5;
        double zMax = point2.getZ()+0.5;
        double xResult = xMin + ((xMax - xMin)/2);
        double yResult = yMin + ((yMax - yMin)/2);
        double zResult = zMin + ((zMax - zMin)/2);
        return new Location(point1.getWorld(), xResult, yResult, zResult);
    }

    public Location getRandomPoint() {
        double xMin = point1.getX()-0.5;
        double yMin = point1.getY()-0.5;
        double zMin = point1.getZ()-0.5;
        double xMax = point2.getX()+0.5;
        double yMax = point2.getY()+0.5;
        double zMax = point2.getZ()+0.5;
        double xResult = xMin + ((xMax - xMin)*Math.random());
        double yResult = yMin + ((yMax - yMin)*Math.random());
        double zResult = zMin + ((zMax - zMin)*Math.random());
        return new Location(point1.getWorld(), xResult, yResult, zResult);
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

    public Set<Flag> getAllFlags() {
        return new HashSet<>(flags);
    }

    public void setFlag(Flag flag) {
        for (Iterator<Flag> iterator = flags.iterator(); iterator.hasNext(); ) {
            Flag iteratedFlag = iterator.next();
            if (iteratedFlag.getClass().equals(flag.getClass())) {
                iterator.remove();
                break;
            }
        }
        flags.add(flag);
    }

    public <T extends Flag> void unsetFlag(Class<T> flagType) {
        for (Iterator<Flag> iterator = flags.iterator(); iterator.hasNext(); ) {
            Flag iteratedFlag = iterator.next();
            if (iteratedFlag.getClass().equals(flagType)) {
                iterator.remove();
                break;
            }
        }
    }

    public List<Action> getActions() {
        return new ArrayList<>(actions);
    }

    public int getActionsAmount() {
        return actions.size();
    }

    public Action getAction(int id) {
        if (id >= actions.size() || id < 0)
            return null;
        return actions.get(id);
    }

    public void addAction(Action action) {
        int index = 0;
        int actionTime = action.getTime();
        for(;;) {
            if (index == actions.size()) {
                actions.add(index, action);
                break;
            }
            if (actionTime < actions.get(index).getTime()) {
                actions.add(index, action);
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
                if (index == 0 || addedActionTime == actions.get(index-1).getTime()) {
                    actions.add(index, action);
                    return index;
                }
                else
                    index--;
            } else if (addedActionTime > iteratedActionTime) {
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

    public void removeAllActions() {
        actions.clear();
    }

    @Override
    public String toString() {
        return name;
    }


}
