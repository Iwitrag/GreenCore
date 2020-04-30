package cz.iwitrag.greencore.gameplay.zones.flags;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("end")
public class EnderPortalFlag extends Flag {

    @Column(name = "end_enderPortalEnabled")
    private boolean enderPortalEnabled;

    public EnderPortalFlag() {
        this.enderPortalEnabled = true;
    }

    public boolean isEnderPortalEnabled() {
        return enderPortalEnabled;
    }

    public void setEnderPortalEnabled(boolean enderPortalEnabled) {
        this.enderPortalEnabled = enderPortalEnabled;
    }

    @Override
    public Flag copy() {
        EnderPortalFlag flag = new EnderPortalFlag();
        flag.setEnderPortalEnabled(enderPortalEnabled);
        return flag;
    }
}
