package cz.iwitrag.greencore.helpers;

import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.text.DecimalFormat;
import java.text.Normalizer;
import java.util.Collection;
import java.util.regex.Pattern;

public class StringHelper {

    static java.text.SimpleDateFormat fMySQLDate = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Date format for MySQL
    static java.text.SimpleDateFormat fPlayerDate = new java.text.SimpleDateFormat("dd.MM yyyy HH:mm:ss"); // Player readable date format

    /** Check whether this String could be parsed to valid Integer */
    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_{|}~-]+@((\\[[0-9]{1,3}\\." +
                "[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    public static boolean isValidMinecraftUsername(String username) {
        return Pattern.compile("[a-zA-Z0-9_]{3,16}").matcher(username).matches();
    }

    /** Converts given String so ONLY first letter is Capital */
    public static String firstCapital(String str) {
        if (str == null) {
            return null;
        }
        if (str.isEmpty()) {
            return "";
        }
        str = str.toLowerCase();
        return str.substring(0, 1).toUpperCase() + str.substring(1, str.length());
    }

    public static String getChatLine() {
        return "---------------------------------------------------";
    }

    public static String doubleStringWithoutTrailingZeros(double d) {
        String result = new DecimalFormat("#.##").format(d);
        return result.contains(".") ? result.replaceAll("0*$","").replaceAll("\\.$","") : result;
    }

    public static boolean anyStringContains(Collection<String> strings, String searchTarget, boolean ignoreCase) {
        if (strings == null || strings.isEmpty())
            return false;
        for (String str : strings) {
            if (ignoreCase) {
                if (str.toLowerCase().contains(searchTarget.toLowerCase()))
                    return true;
            } else {
                if (str.contains(searchTarget))
                    return true;
            }
        }
        return false;
    }

    public static String locationToString(Location location) {
        return locationToString(location, true, "§f", "§7");
    }
    public static String locationToString(Location location, boolean printWorld) {
        return locationToString(location, printWorld, "§f", "§7");
    }
    public static String locationToString(Location location, String textColor, String valuesColor) {
        return locationToString(location, true, textColor, valuesColor);
    }
    public static String locationToString(Location location, boolean printWorld, String textColor, String valuesColor) {
        String world = "";
        if (printWorld)
            world = textColor + ", Svět: " + valuesColor + ((location.getWorld() == null) ? "---" : location.getWorld().getName());
        DecimalFormat df = new DecimalFormat("#.##");
        return textColor + "X: " + valuesColor + df.format(location.getX()) +
                textColor + ", Y: " + valuesColor + df.format(location.getY()) +
                textColor + ", Z: " + valuesColor + df.format(location.getZ()) + world;
    }

    public static String removeColors(String string) {
        return ChatColor.stripColor(string);
    }

    // Source: https://www.spigotmc.org/threads/free-code-sending-perfectly-centered-chat-message.95872/
    public static String centerMessage(String message) {
        final int CENTER_PX = 154;
        message = ChatColor.translateAlternateColorCodes('&', message);

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for (char c : message.toCharArray()) {
            if (c == '§') {
                previousCode = true;
            }
            else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            }
            else {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += spaceLength;
        }
        return sb.toString() + message;
    }

    // Source: https://stackoverflow.com/questions/955110/similarity-string-comparison-in-java/16018452#16018452
    public static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) {
            longer = s2; shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0)
            return 1.0; // both strings are zero length

        s1 = longer.toLowerCase();
        s2 = shorter.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return (longerLength - costs[s2.length()]) / (double) longerLength;
    }

    public static String timeToLongString(long seconds) {
        seconds = Math.abs(seconds);
        if (seconds == 0)
            return "0 sekund";

        StringBuilder builder = new StringBuilder();
        long minutes = seconds / 60;
        seconds %= 60;
        long hours = minutes / 60;
        minutes %= 60;
        long days = hours / 24;
        hours %= 24;
        long months = days / 30;
        days %= 30;
        long years = months / 12;
        months %= 12;

        if (years >= 5)
            builder.append(years).append(" let, ");
        else if (years > 1)
            builder.append(years).append(" roky, ");
        else if (years == 1)
            builder.append(years).append(" rok, ");

        if (months >= 5)
            builder.append(months).append(" měsíců, ");
        else if (months > 1)
            builder.append(months).append(" měsíce, ");
        else if (months == 1)
            builder.append(months).append(" měsíc, ");

        if (days >= 5)
            builder.append(days).append(" dnů, ");
        else if (days > 1)
            builder.append(days).append(" dny, ");
        else if (days == 1)
            builder.append(days).append(" den, ");

        if (hours >= 5)
            builder.append(hours).append(" hodin, ");
        else if (hours > 1)
            builder.append(hours).append(" hodiny, ");
        else if (hours == 1)
            builder.append(hours).append(" hodina, ");

        if (minutes >= 5)
            builder.append(minutes).append(" minut, ");
        else if (minutes > 1)
            builder.append(minutes).append(" minuty, ");
        else if (minutes == 1)
            builder.append(minutes).append(" minuta, ");

        if (seconds >= 5)
            builder.append(seconds).append(" sekund, ");
        else if (seconds > 1)
            builder.append(seconds).append(" sekundy, ");
        else if (seconds == 1)
            builder.append(seconds).append(" sekunda, ");

        builder.setLength(builder.length()-2);
        return builder.toString();
    }

    // Source: https://stackoverflow.com/questions/15190656/easy-way-to-remove-accents-from-a-unicode-string
    public static String removeDiacritics(String s) {
        s = Normalizer.normalize(s, Normalizer.Form.NFKD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return s;
    }


    // Source: https://www.spigotmc.org/threads/free-code-sending-perfectly-centered-chat-message.95872/
    public enum DefaultFontInfo {
        A('A', 5),
        a('a', 5),
        B('B', 5),
        b('b', 5),
        C('C', 5),
        c('c', 5),
        D('D', 5),
        d('d', 5),
        E('E', 5),
        e('e', 5),
        F('F', 5),
        f('f', 4),
        G('G', 5),
        g('g', 5),
        H('H', 5),
        h('h', 5),
        I('I', 3),
        i('i', 1),
        J('J', 5),
        j('j', 5),
        K('K', 5),
        k('k', 4),
        L('L', 5),
        l('l', 1),
        M('M', 5),
        m('m', 5),
        N('N', 5),
        n('n', 5),
        O('O', 5),
        o('o', 5),
        P('P', 5),
        p('p', 5),
        Q('Q', 5),
        q('q', 5),
        R('R', 5),
        r('r', 5),
        S('S', 5),
        s('s', 5),
        T('T', 5),
        t('t', 4),
        U('U', 5),
        u('u', 5),
        V('V', 5),
        v('v', 5),
        W('W', 5),
        w('w', 5),
        X('X', 5),
        x('x', 5),
        Y('Y', 5),
        y('y', 5),
        Z('Z', 5),
        z('z', 5),
        NUM_1('1', 5),
        NUM_2('2', 5),
        NUM_3('3', 5),
        NUM_4('4', 5),
        NUM_5('5', 5),
        NUM_6('6', 5),
        NUM_7('7', 5),
        NUM_8('8', 5),
        NUM_9('9', 5),
        NUM_0('0', 5),
        EXCLAMATION_POINT('!', 1),
        AT_SYMBOL('@', 6),
        NUM_SIGN('#', 5),
        DOLLAR_SIGN('$', 5),
        PERCENT('%', 5),
        UP_ARROW('^', 5),
        AMPERSAND('&', 5),
        ASTERISK('*', 5),
        LEFT_PARENTHESIS('(', 4),
        RIGHT_PERENTHESIS(')', 4),
        MINUS('-', 5),
        UNDERSCORE('_', 5),
        PLUS_SIGN('+', 5),
        EQUALS_SIGN('=', 5),
        LEFT_CURL_BRACE('{', 4),
        RIGHT_CURL_BRACE('}', 4),
        LEFT_BRACKET('[', 3),
        RIGHT_BRACKET(']', 3),
        COLON(':', 1),
        SEMI_COLON(';', 1),
        DOUBLE_QUOTE('"', 3),
        SINGLE_QUOTE('\'', 1),
        LEFT_ARROW('<', 4),
        RIGHT_ARROW('>', 4),
        QUESTION_MARK('?', 5),
        SLASH('/', 5),
        BACK_SLASH('\\', 5),
        LINE('|', 1),
        TILDE('~', 5),
        TICK('`', 2),
        PERIOD('.', 1),
        COMMA(',', 1),
        SPACE(' ', 3),
        DEFAULT('a', 4);

        private char character;
        private int length;

        DefaultFontInfo(char character, int length) {
            this.character = character;
            this.length = length;
        }

        public char getCharacter() {
            return this.character;
        }

        public int getLength() {
            return this.length;
        }

        public int getBoldLength() {
            if (this == DefaultFontInfo.SPACE) return this.getLength();
            return this.length + 1;
        }

        public static DefaultFontInfo getDefaultFontInfo(char c) {
            c = removeDiacritics(String.valueOf(c)).charAt(0);
            for (DefaultFontInfo dFI : DefaultFontInfo.values()) {
                if (dFI.getCharacter() == c)
                    return dFI;
            }
            return DefaultFontInfo.DEFAULT;
        }
    }

}
