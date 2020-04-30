package cz.iwitrag.greencore.gameplay.zones.flags;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("dsc")
public class DisconnectPenaltyFlag extends Flag {

    @Column(name = "dsc_penalty")
    private double penalty;

    public DisconnectPenaltyFlag() {
        this.penalty = 0.0;
    }

    public double getPenalty() {
        return penalty;
    }

    public void setPenalty(double penalty) {
        this.penalty = penalty;
    }

    @Override
    public Flag copy() {
        DisconnectPenaltyFlag flag = new DisconnectPenaltyFlag();
        flag.setPenalty(penalty);
        return flag;
    }
}
