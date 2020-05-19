package cz.iwitrag.greencore.gameplay.chat;

import cz.iwitrag.greencore.gameplay.commands.artificial.ArtificialCommandsManager;
import cz.iwitrag.greencore.helpers.Utils;
import net.md_5.bungee.api.chat.*;
import org.bukkit.inventory.ItemStack;

public class ChatUtils {

    public static BaseComponent[] getPlainText(String text) {
        return composeDynamicText(TextComponent.fromLegacyText(text), null, null, null, null, null, null);
    }
    public static BaseComponent[] getPlainText(BaseComponent[] text) {
        return composeDynamicText(text, null, null, null, null, null, null);
    }
    public static BaseComponent[] getHoverableText(String text, String hoverText) {
        return composeDynamicText(TextComponent.fromLegacyText(text), TextComponent.fromLegacyText(hoverText), null, null, null, null, null);
    }
    public static BaseComponent[] getHoverableText(BaseComponent[] text, String hoverText) {
        return composeDynamicText(text, TextComponent.fromLegacyText(hoverText), null, null, null, null, null);
    }
    public static BaseComponent[] getHoverableText(String text, BaseComponent[] hoverText) {
        return composeDynamicText(TextComponent.fromLegacyText(text), hoverText, null, null, null, null, null);
    }
    public static BaseComponent[] getHoverableText(BaseComponent[] text, BaseComponent[] hoverText) {
        return composeDynamicText(text, hoverText, null, null, null, null, null);
    }
    public static BaseComponent[] getItemableText(String text, ItemStack itemStack) {
        return composeDynamicText(TextComponent.fromLegacyText(text), null, itemStack, null, null, null, null);
    }
    public static BaseComponent[] getItemableText(BaseComponent[] text, ItemStack itemStack) {
        return composeDynamicText(text, null, itemStack, null, null, null, null);
    }
    public static BaseComponent[] getItemableText(boolean withAmount, ItemStack itemStack) {
        return composeDynamicText(null, null, itemStack, withAmount, null, null, null);
    }
    public static BaseComponent[] getPlainTextWithRunnable(String text, Runnable runnable) {
        return composeDynamicText(TextComponent.fromLegacyText(text), null, null, null, null, runnable, null);
    }
    public static BaseComponent[] getPlainTextWithRunnable(BaseComponent[] text, Runnable runnable) {
        return composeDynamicText(text, null, null, null, null, runnable, null);
    }
    public static BaseComponent[] getHoverableTextWithRunnable(String text, String hoverText, Runnable runnable) {
        return composeDynamicText(TextComponent.fromLegacyText(text), TextComponent.fromLegacyText(hoverText), null, null, null, runnable, null);
    }
    public static BaseComponent[] getHoverableTextWithRunnable(BaseComponent[] text, String hoverText, Runnable runnable) {
        return composeDynamicText(text, TextComponent.fromLegacyText(hoverText), null, null, null, runnable, null);
    }
    public static BaseComponent[] getHoverableTextWithRunnable(String text, BaseComponent[] hoverText, Runnable runnable) {
        return composeDynamicText(TextComponent.fromLegacyText(text), hoverText, null, null, null, runnable, null);
    }
    public static BaseComponent[] getHoverableTextWithRunnable(BaseComponent[] text, BaseComponent[] hoverText, Runnable runnable) {
        return composeDynamicText(text, hoverText, null, null, null, runnable, null);
    }
    public static BaseComponent[] getItemableTextWithRunnable(String text, ItemStack itemStack, Runnable runnable) {
        return composeDynamicText(TextComponent.fromLegacyText(text), null, itemStack, null, null, runnable, null);
    }
    public static BaseComponent[] getItemableTextWithRunnable(BaseComponent[] text, ItemStack itemStack, Runnable runnable) {
        return composeDynamicText(text, null, itemStack, null, null, runnable, null);
    }
    public static BaseComponent[] getItemableTextWithRunnable(boolean withAmount, ItemStack itemStack, Runnable runnable) {
        return composeDynamicText(null, null, itemStack, withAmount, null, runnable, null);
    }
    public static BaseComponent[] getPlainTextWithSuggest(String text, String suggestCmd) {
        return composeDynamicText(TextComponent.fromLegacyText(text), null, null, null, ClickEvent.Action.SUGGEST_COMMAND, null, suggestCmd);
    }
    public static BaseComponent[] getPlainTextWithSuggest(BaseComponent[] text, String suggestCmd) {
        return composeDynamicText(text, null, null, null, ClickEvent.Action.SUGGEST_COMMAND, null, suggestCmd);
    }
    public static BaseComponent[] getHoverableTextWithSuggest(String text, String hoverText, String suggestCmd) {
        return composeDynamicText(TextComponent.fromLegacyText(text), TextComponent.fromLegacyText(hoverText), null, null, ClickEvent.Action.SUGGEST_COMMAND, null, suggestCmd);
    }
    public static BaseComponent[] getHoverableTextWithSuggest(BaseComponent[] text, String hoverText, String suggestCmd) {
        return composeDynamicText(text, TextComponent.fromLegacyText(hoverText), null, null, ClickEvent.Action.SUGGEST_COMMAND, null, suggestCmd);
    }
    public static BaseComponent[] getHoverableTextWithSuggest(String text, BaseComponent[] hoverText, String suggestCmd) {
        return composeDynamicText(TextComponent.fromLegacyText(text), hoverText, null, null, ClickEvent.Action.SUGGEST_COMMAND, null, suggestCmd);
    }
    public static BaseComponent[] getHoverableTextWithSuggest(BaseComponent[] text, BaseComponent[] hoverText, String suggestCmd) {
        return composeDynamicText(text, hoverText, null, null, ClickEvent.Action.SUGGEST_COMMAND, null, suggestCmd);
    }
    public static BaseComponent[] getItemableTextWithSuggest(String text, ItemStack itemStack, String suggestCmd) {
        return composeDynamicText(TextComponent.fromLegacyText(text), null, itemStack, null, ClickEvent.Action.SUGGEST_COMMAND, null, suggestCmd);
    }
    public static BaseComponent[] getItemableTextWithSuggest(BaseComponent[] text, ItemStack itemStack, String suggestCmd) {
        return composeDynamicText(text, null, itemStack, null, ClickEvent.Action.SUGGEST_COMMAND, null, suggestCmd);
    }
    public static BaseComponent[] getItemableTextWithSuggest(boolean withAmount, ItemStack itemStack, String suggestCmd) {
        return composeDynamicText(null, null, itemStack, withAmount, ClickEvent.Action.SUGGEST_COMMAND, null, suggestCmd);
    }
    public static BaseComponent[] getPlainTextWithCommand(String text, String cmd) {
        return composeDynamicText(TextComponent.fromLegacyText(text), null, null, null, ClickEvent.Action.RUN_COMMAND, null, cmd);
    }
    public static BaseComponent[] getPlainTextWithCommand(BaseComponent[] text, String cmd) {
        return composeDynamicText(text, null, null, null, ClickEvent.Action.RUN_COMMAND, null, cmd);
    }
    public static BaseComponent[] getHoverableTextWithCommand(String text, String hoverText, String cmd) {
        return composeDynamicText(TextComponent.fromLegacyText(text), TextComponent.fromLegacyText(hoverText), null, null, ClickEvent.Action.RUN_COMMAND, null, cmd);
    }
    public static BaseComponent[] getHoverableTextWithCommand(BaseComponent[] text, String hoverText, String cmd) {
        return composeDynamicText(text, TextComponent.fromLegacyText(hoverText), null, null, ClickEvent.Action.RUN_COMMAND, null, cmd);
    }
    public static BaseComponent[] getHoverableTextWithCommand(String text, BaseComponent[] hoverText, String cmd) {
        return composeDynamicText(TextComponent.fromLegacyText(text), hoverText, null, null, ClickEvent.Action.RUN_COMMAND, null, cmd);
    }
    public static BaseComponent[] getHoverableTextWithCommand(BaseComponent[] text, BaseComponent[] hoverText, String cmd) {
        return composeDynamicText(text, hoverText, null, null, ClickEvent.Action.RUN_COMMAND, null, cmd);
    }
    public static BaseComponent[] getItemableTextWithCommand(String text, ItemStack itemStack, String cmd) {
        return composeDynamicText(TextComponent.fromLegacyText(text), null, itemStack, null, ClickEvent.Action.RUN_COMMAND, null, cmd);
    }
    public static BaseComponent[] getItemableTextWithCommand(BaseComponent[] text, ItemStack itemStack, String cmd) {
        return composeDynamicText(text, null, itemStack, null, ClickEvent.Action.RUN_COMMAND, null, cmd);
    }
    public static BaseComponent[] getItemableTextWithCommand(boolean withAmount, ItemStack itemStack, String cmd) {
        return composeDynamicText(null, null, itemStack, withAmount, ClickEvent.Action.RUN_COMMAND, null, cmd);
    }
    public static BaseComponent[] getPlainTextWithURL(String text, String URL) {
        return composeDynamicText(TextComponent.fromLegacyText(text), null, null, null, ClickEvent.Action.OPEN_URL, null, URL);
    }
    public static BaseComponent[] getPlainTextWithURL(BaseComponent[] text, String URL) {
        return composeDynamicText(text, null, null, null, ClickEvent.Action.OPEN_URL, null, URL);
    }
    public static BaseComponent[] getHoverableTextWithURL(String text, String hoverText, String URL) {
        return composeDynamicText(TextComponent.fromLegacyText(text), TextComponent.fromLegacyText(hoverText), null, null, ClickEvent.Action.OPEN_URL, null, URL);
    }
    public static BaseComponent[] getHoverableTextWithURL(BaseComponent[] text, String hoverText, String URL) {
        return composeDynamicText(text, TextComponent.fromLegacyText(hoverText), null, null, ClickEvent.Action.OPEN_URL, null, URL);
    }
    public static BaseComponent[] getHoverableTextWithURL(String text, BaseComponent[] hoverText, String URL) {
        return composeDynamicText(TextComponent.fromLegacyText(text), hoverText, null, null, ClickEvent.Action.OPEN_URL, null, URL);
    }
    public static BaseComponent[] getHoverableTextWithURL(BaseComponent[] text, BaseComponent[] hoverText, String URL) {
        return composeDynamicText(text, hoverText, null, null, ClickEvent.Action.OPEN_URL, null, URL);
    }
    public static BaseComponent[] getItemableTextWithURL(String text, ItemStack itemStack, String URL) {
        return composeDynamicText(TextComponent.fromLegacyText(text), null, itemStack, null, ClickEvent.Action.OPEN_URL, null, URL);
    }
    public static BaseComponent[] getItemableTextWithURL(BaseComponent[] text, ItemStack itemStack, String URL) {
        return composeDynamicText(text, null, itemStack, null, ClickEvent.Action.OPEN_URL, null, URL);
    }
    public static BaseComponent[] getItemableTextWithURL(boolean withAmount, ItemStack itemStack, String URL) {
        return composeDynamicText(null, null, itemStack, withAmount, ClickEvent.Action.OPEN_URL, null, URL);
    }

    private static BaseComponent[] composeDynamicText(BaseComponent[] chatText, BaseComponent[] hoverText, ItemStack hoverItem, Boolean withAmount, ClickEvent.Action clickType, Runnable runnable, String data) {
        // TEXT
        BaseComponent[] baseComponents;
        if (chatText != null)
            baseComponents = chatText;
        else if (hoverItem != null) {
            String amount = (withAmount != null && withAmount) ? (hoverItem.getAmount() + "x ") : "";
            if (hoverItem.hasItemMeta() && hoverItem.getItemMeta().hasDisplayName())
                baseComponents = TextComponent.fromLegacyText(amount + hoverItem.getItemMeta().getDisplayName());
            else
                baseComponents = TextComponent.fromLegacyText(amount + hoverItem.getI18NDisplayName());
        } else
            throw new IllegalArgumentException("Unable to determine base text as chatText and hoverItem were both null");

        // HOVER;
        if (hoverText != null) {
            HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText);
            for (BaseComponent component : baseComponents)
                component.setHoverEvent(hoverEvent);
        } else if (hoverItem != null) {
            HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_ITEM, Utils.itemStackToBaseComponents(hoverItem));
            for (BaseComponent component : baseComponents)
                component.setHoverEvent(hoverEvent);
        }

        // CLICK
        if (runnable != null) {
            ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, ArtificialCommandsManager.getInstance().registerCommand(runnable).getCommandText());
            for (BaseComponent component : baseComponents)
                component.setClickEvent(clickEvent);
        } else if (clickType != null && data != null) {
            if (data.charAt(0) != '/' && (clickType == ClickEvent.Action.RUN_COMMAND || clickType == ClickEvent.Action.SUGGEST_COMMAND))
                data = "/" + data;
            ClickEvent clickEvent = new ClickEvent(clickType, data);
            for (BaseComponent component : baseComponents)
                component.setClickEvent(clickEvent);
        }

        // We want to limit hover and click events to only text returned by this method
        TextComponent resetComponent = new TextComponent("");
        resetComponent.setHoverEvent(null);
        resetComponent.setClickEvent(null);
        return new ComponentBuilder("")
                .append(baseComponents)
                .append(resetComponent)
                .create();
    }

}
