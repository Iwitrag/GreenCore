package cz.iwitrag.greencore.premium;

import java.util.HashSet;
import java.util.Set;

public class PremiumService {

    private final String internalName;
    private final String friendlyName;
    private final boolean active;
    private final String description;
    private final Set<PremiumVariant> variants;

    PremiumService(String internalName, String friendlyName, boolean active, String description, Set<PremiumVariant> variants) {
        this.internalName = internalName;
        this.friendlyName = friendlyName;
        this.active = active;
        this.description = description;
        this.variants = new HashSet<>(variants);
    }

    public String getInternalName() {
        return internalName;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public boolean isActive() {
        return active;
    }

    public String getDescription() {
        return description;
    }

    public Set<PremiumVariant> getVariants() {
        return new HashSet<>(this.variants);
    }

    public PremiumVariant getVariantByKeyword(String keyword) {
        for (PremiumVariant variant : variants) {
            if (variant.getKeyword().equalsIgnoreCase(keyword))
                return variant;
        }
        return null;
    }
}
