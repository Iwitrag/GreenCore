package cz.iwitrag.greencore.storage.converters;

import cz.iwitrag.greencore.helpers.Percent;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class PercentConverter extends AbstractStringConverter implements AttributeConverter<Percent, String> {

    @Override
    public String convertToDatabaseColumn(Percent attribute) {
        return attribute.toString();
    }

    @Override
    public Percent convertToEntityAttribute(String dbData) {
        return new Percent(dbData);
    }
}
