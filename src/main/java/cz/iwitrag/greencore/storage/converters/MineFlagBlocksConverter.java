package cz.iwitrag.greencore.storage.converters;

import cz.iwitrag.greencore.gameplay.zones.ZoneException;
import cz.iwitrag.greencore.gameplay.zones.flags.MineFlag;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.LinkedHashSet;
import java.util.Set;

@Converter(autoApply = true)
public class MineFlagBlocksConverter implements AttributeConverter<Set<MineFlag.MineBlock>, String> {

    @Override
    public String convertToDatabaseColumn(Set<MineFlag.MineBlock> attribute) {
        if (attribute == null)
            return null;
        try {
            return MineFlag.makeStringFromBlocks(attribute);
        } catch (ZoneException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public Set<MineFlag.MineBlock> convertToEntityAttribute(String dbData) {
        if (dbData == null)
            return null;
        try {
            return MineFlag.makeBlocksFromString(dbData);
        } catch (ZoneException e) {
            e.printStackTrace();
        }
        return new LinkedHashSet<>();
    }
}
