package cz.iwitrag.greencore.storage.converters;

import cz.iwitrag.greencore.helpers.Utils;
import org.bukkit.inventory.ItemStack;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class ItemStackConverter extends AbstractStringConverter implements AttributeConverter<ItemStack, String> {

    @Override
    public String convertToDatabaseColumn(ItemStack attribute) {
        if (attribute == null)
            return null;
        return Utils.itemStackToString(attribute);
    }

    @Override
    public ItemStack convertToEntityAttribute(String dbData) {
        if (dbData == null)
            return null;
        return Utils.stringToItemStack(dbData);
    }
}
