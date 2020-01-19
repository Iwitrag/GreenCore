package cz.iwitrag.greencore.premium;

import java.util.ArrayList;
import java.util.List;

public class PremiumVariant {

    private final String keyword;
    private final String friendlyName;
    private final String description;
    private final int price;
    private final List<String> params;

    PremiumVariant(String keyword, String friendlyName, String description, int price, List<String> params) {
        this.keyword = keyword;
        this.friendlyName = friendlyName;
        this.description = description;
        this.price = price;
        this.params = new ArrayList<>(params);
    }

    public String getKeyword() {
        return keyword;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public String getDescription() {
        return description;
    }

    public int getPrice() {
        return price;
    }

    public List<String> getParams() {
        return new ArrayList<>(this.params);
    }
}
