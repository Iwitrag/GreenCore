package cz.iwitrag.greencore.storage.converters;

import org.bukkit.Bukkit;
import org.bukkit.World;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Map;

@Converter(autoApply = true)
public class WorldConverter extends AbstractStringConverter implements AttributeConverter<World, String> {

    @Override
    public String convertToDatabaseColumn(World attribute) {
        return composeDatabaseString("World",
                "Name", attribute.getName()
        );
    }

    @Override
    public World convertToEntityAttribute(String dbData) {
        Map<String, String> map = extractEntityAttributes(dbData);

        return Bukkit.getWorld(
                map.get("Name")
        );
    }
}
