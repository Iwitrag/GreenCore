package cz.iwitrag.greencore.storage.converters;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Map;
import java.util.Objects;

@Converter(autoApply = true)
public class PotionEffectConverter extends AbstractStringConverter implements AttributeConverter<PotionEffect, String> {

    @Override
    public String convertToDatabaseColumn(PotionEffect attribute) {
        return composeDatabaseString("PotionEffect",
                "Type", attribute.getType().getName(),
                "Duration", attribute.getDuration(),
                "Amplifier", attribute.getAmplifier(),
                "Ambient", attribute.isAmbient(),
                "Particles", attribute.hasParticles(),
                "Icon", attribute.hasIcon()
        );
    }

    @Override
    public PotionEffect convertToEntityAttribute(String dbData) {
        Map<String, String> map = extractEntityAttributes(dbData);

        return new PotionEffect(
                Objects.requireNonNull(PotionEffectType.getByName(map.get("Type"))),
                Integer.parseInt(map.get("Duration")),
                Integer.parseInt(map.get("Amplifier")),
                Boolean.parseBoolean(map.get("Ambient")),
                Boolean.parseBoolean(map.get("Particles")),
                Boolean.parseBoolean(map.get("Icon"))
        );
    }
}
