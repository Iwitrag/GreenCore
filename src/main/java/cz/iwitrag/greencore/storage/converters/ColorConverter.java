package cz.iwitrag.greencore.storage.converters;

import cz.iwitrag.greencore.helpers.Color;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class ColorConverter extends AbstractStringConverter implements AttributeConverter<Color, String> {

    @Override
    public String convertToDatabaseColumn(Color attribute) {
        if (attribute == null)
            return null;
        return attribute.toString();
    }

    @Override
    public Color convertToEntityAttribute(String dbData) {
        if (dbData == null)
            return null;
        return Color.fromToString(dbData);
    }
}
