package cz.iwitrag.greencore.helpers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PermGroupNames {

    private PermGroupNames() {}

    public static List<String> owner() { return Arrays.asList("owner", "majitel"); }
    public static List<String> admin() { return Arrays.asList("admin", "administrator"); }
    public static List<String> mod() { return Arrays.asList("mod", "moderator"); }
    public static List<String> builder() { return Arrays.asList("builder", "stavitel"); }
    public static List<String> helperPlus() { return Arrays.asList("helper+", "helperplus"); }
    public static List<String> basicHelper() { return Collections.singletonList("helper"); }
    public static List<String> anyHelper() { return Stream.concat(helperPlus().stream(), basicHelper().stream()).collect(Collectors.toList()); }
    public static List<String> youtuber() { return Collections.singletonList("youtuber"); }
    public static List<String> hero() { return Arrays.asList("hero", "hrdina"); }
    public static List<String> vipPlus() { return Arrays.asList("vip+", "vipplus"); }
    public static List<String> basicVip() { return Collections.singletonList("vip"); }
    public static List<String> anyVip() { return Stream.concat(vipPlus().stream(), basicVip().stream()).collect(Collectors.toList()); }
    public static List<String> player() { return Arrays.asList("hrac", "player", "default"); }

}
