package cz.iwitrag.greencore.storage.converters;

import java.util.Map;
import java.util.TreeMap;

public abstract class AbstractStringConverter {

    static final String NULL_STRING = "NULL";

    String composeDatabaseString(String header, Object... input) {
        StringBuilder builder = new StringBuilder();
        builder.append(header);
        builder.append(" - ");
        for (int i = 0; i < input.length; i++) {
            if (input[i] == null)
                builder.append(NULL_STRING);
            else
                builder.append(input[i]);
            if (i % 2 == 0) {
                builder.append(": ");
            } else {
                builder.append(", ");
            }
        }
        builder.setLength(builder.length()-2);
        return builder.toString();
    }

    Map<String, String> extractEntityAttributes(String input) {
        String[] data = input.split(" - ")[1].split(", ");
        Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (String str : data) {
            String[] pair = str.split(": ");
            map.put(pair[0], pair[1]);
        }
        return map;
    }

}
