package cz.iwitrag.greencore.gameplay.zones.flags;

public class EnderPortalFlag implements Flag {

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
}
