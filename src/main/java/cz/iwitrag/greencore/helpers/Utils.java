package cz.iwitrag.greencore.helpers;

import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Utils {

    private Utils() {}

    public static boolean chance(double percent) {
        return Math.random()*100.00 < percent;
    }

    public static <T> T pickRandomElement(Collection<T> collection) {
        int random = (int) (Math.random()*collection.size());
        int i = 0;
        for (T object : collection) {
            if (i == random)
                return object;
            i++;
        }
        return null;
    }

    public static Set<Block> getBlockDirectNeighbours(Block block) {
        Set<Block> set = new HashSet<>();
        if (block != null) {
            set.add(block.getRelative(BlockFace.DOWN));
            set.add(block.getRelative(BlockFace.EAST));
            set.add(block.getRelative(BlockFace.NORTH));
            set.add(block.getRelative(BlockFace.SOUTH));
            set.add(block.getRelative(BlockFace.UP));
            set.add(block.getRelative(BlockFace.WEST));
        }
        return set;
    }

    public static boolean isParticleColorizable(Particle particle) {
        if (particle == null) return false;
        return particle.equals(Particle.REDSTONE);
    }

}
