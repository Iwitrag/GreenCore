package cz.iwitrag.greencore.gameplay.zones.flags;

import cz.iwitrag.greencore.helpers.Color;
import org.bukkit.Particle;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Entity
@DiscriminatorValue("par")
public class ParticlesFlag extends Flag {

    @Enumerated(EnumType.STRING)
    @Column(name = "par_particle")
    private Particle particle;

    @Column(name = "par_density")
    private int density;

    @Column(name = "par_color")
    private Color color;

    public ParticlesFlag() {
        this.particle = null;
        this.density = 5;
        this.color = new Color(0, 0, 0);
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

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public Flag copy() {
        ParticlesFlag flag = new ParticlesFlag();
        flag.setParticle(particle);
        flag.setDensity(density);
        flag.setColor(new Color(color));
        return flag;
    }
}
