package cz.iwitrag.greencore.gameplay.zones.flags;

import org.bukkit.Particle;

public class ParticlesFlag implements Flag {

    private Particle particle;
    private int density;
    private int red;
    private int green;
    private int blue;

    public ParticlesFlag() {
        this.particle = null;
        this.density = 1;
        this.red = 0;
        this.green = 0;
        this.blue = 0;
    }

    public Particle getParticle() {
        return particle;
    }

    public void setParticle(Particle particle) {
        this.particle = particle;
    }

    public int getDensity() {
        return density;
    }

    public void setDensity(int density) {
        this.density = density;
    }

    public int getRed() {
        return red;
    }

    public void setRed(int red) {
        this.red = red;
    }

    public int getGreen() {
        return green;
    }

    public void setGreen(int green) {
        this.green = green;
    }

    public int getBlue() {
        return blue;
    }

    public void setBlue(int blue) {
        this.blue = blue;
    }
}
