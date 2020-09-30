package cz.iwitrag.greencore.storage.converters;

import cz.iwitrag.greencore.helpers.Percent;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class PercentConverter implements AttributeConverter<Percent, Double> {

    @Override
    public Double convertToDatabaseColumn(Percent attribute) {
        if (attribute == null)
            return null;
        return attribute.getPercentValue();
    }

    @Override
    public Percent convertToEntityAttribute(Double dbData) {
        if (dbData == null)
            return null;
        return new Percent(dbData);
    }
}
