package cz.iwitrag.greencore.gameplay.oregen;

import javafx.util.Pair;
import org.bukkit.Material;

import java.util.List;

public class OreSettings {

    private Material ore;
    private List<Pair<Integer, Double>> distribution;
    private int minOrePieces;
    private int medOrePieces;
    private int maxOrePieces;
    private boolean directlyConnectedOnly;

    public OreSettings() {
        ore = null;
        distribution = null;
        minOrePieces = 0;
        medOrePieces = 0;
        maxOrePieces = 0;
        directlyConnectedOnly = false;
    }

    public Material getOre() {
        return ore;
    }

    public OreSettings setOre(Material ore) {
        this.ore = ore;
        return this;
    }

    public List<Pair<Integer, Double>> getDistribution() {
        return distribution;
    }

    public OreSettings setDistribution(List<Pair<Integer, Double>> distribution) {
        this.distribution = distribution;
        return this;
    }

    public int getMinOrePieces() {
        return minOrePieces;
    }

    public OreSettings setMinOrePieces(int minOrePieces) {
        this.minOrePieces = Math.max(minOrePieces, 0);
        return this;
    }

    public int getMedOrePieces() {
        return medOrePieces;
    }

    public OreSettings setMedOrePieces(int medOrePieces) {
        this.medOrePieces = Math.max(medOrePieces, 0);
        return this;
    }

    public int getMaxOrePieces() {
        return maxOrePieces;
    }

    public OreSettings setMaxOrePieces(int maxOrePieces) {
        this.maxOrePieces = Math.max(maxOrePieces, 0);
        return this;
    }

    public boolean isDirectlyConnectedOnly() {
        return directlyConnectedOnly;
    }

    public OreSettings setDirectlyConnectedOnly(boolean directlyConnectedOnly) {
        this.directlyConnectedOnly = directlyConnectedOnly;
        return this;
    }
}
