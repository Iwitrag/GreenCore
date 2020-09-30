package cz.iwitrag.greencore.storage.converters;

import cz.iwitrag.greencore.helpers.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Map;

@Converter(autoApply = true)
public class LocationConverter extends AbstractStringConverter implements AttributeConverter<Location, String> {

    @Override
    public String convertToDatabaseColumn(Location attribute) {
        if (attribute == null)
            return null;
        return composeDatabaseString("Location",
                "X", Utils.twoDecimal(attribute.getX()),
                "Y", Utils.twoDecimal(attribute.getY()),
                "Z", Utils.twoDecimal(attribute.getZ()),
                "Yaw", Utils.twoDecimal(attribute.getYaw()),
                "Pitch", Utils.twoDecimal(attribute.getPitch()),
                "World", attribute.getWorld() == null ? null : attribute.getWorld().getName()
        );
    }

    @Override
    public Location convertToEntityAttribute(String dbData) {
        if (dbData == null)
            return null;
        Map<String, String> map = extractEntityAttributes(dbData);

        return new Location(
                Bukkit.getWorld(map.get("World")),
                Double.parseDouble(map.get("X")),
                Double.parseDouble(map.get("Y")),
                Double.parseDouble(map.get("Z")),
                Float.parseFloat(map.get("Yaw")),
                Float.parseFloat(map.get("Pitch"))
        );
    }
}
