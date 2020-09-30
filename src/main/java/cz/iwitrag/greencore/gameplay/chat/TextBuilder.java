package cz.iwitrag.greencore.gameplay.chat;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TextBuilder {

    // TODO TEXTBUILDER - support ChatColor (§x) in strings
    // TODO TEXTBUILDER - support BaseComponents
    // TODO TEXTBUILDER - support targetAll
    // TODO TEXTBUILDER - support placeholders

    private String pattern;
    private List<Part> parts = new ArrayList<>();
    private Part current;

    public TextBuilder(String pattern) {
        if (pattern == null)
            throw new IllegalArgumentException("Pattern cannot be null");

        this.pattern = pattern;
    }

    public TextBuilder from() {
        finishCurrentPart();
        current = new Part();
        current.start = 0;
        return this;
    }

    public TextBuilder from(String from) {
        if (from == null)
            throw new IllegalArgumentException("From string cannot be null");
        int pos = pattern.indexOf(from);
        if (pos == -1)
            throw new IllegalArgumentException("From string \"" + from + "\" was not found");

        finishCurrentPart();
        current = new Part();
        current.start = pos;
        return this;
    }

    public TextBuilder target(String target) {
        if (target == null)
            throw new IllegalArgumentException("Target string cannot be null");
        int pos = pattern.indexOf(target);
        if (pos == -1)
            throw new IllegalArgumentException("Target string \"" + target + "\" was not found");

        finishCurrentPart();
        current = new Part();
        current.start = pos;
        current.end = pos + target.length();
        return this;
    }

    public TextBuilder replace(Object replacement) {
        targetMustBeSelected();
        if (current.replaced)
            return this;
        if (replacement == null)
            throw new IllegalArgumentException("Replacement cannot be null");

        String currentPart = getCurrentPartString();
        String replacementStr = replacement.toString();
        int currentPartLength = currentPart == null ? 0 : currentPart.length();
        int replacementLength = replacementStr.length();
        int lengthChange = replacementLength - currentPartLength;

        // Replace common letters
        int commonLettersLength = Math.min(currentPartLength, replacementLength);
        for (int i = 0; i < commonLettersLength; i++) {
            int iteratedCharIndex = current.start+i;
            char newChar = replacementStr.charAt(i);
            if (pattern.charAt(iteratedCharIndex) != newChar)
                setLetter(iteratedCharIndex, newChar);
        }
        for (int i = 0; i < Math.abs(lengthChange); i++) {
            // Add additional letters
            if (lengthChange > 0)
                addLetter(current.end, replacementStr.charAt(currentPartLength + i));
            // Remove extra letters
            else if (lengthChange < 0)
                removeLetter(current.start+1);
        }

        current.replaced = true;
        return this;
    }

    public TextBuilder replace(ItemStack item, boolean withAmount) {
        targetMustBeSelected();
        if (current.replaced)
            return this;
        if (item == null)
            throw new IllegalArgumentException("Replacement item cannot be null");

        String amount = withAmount ? (item.getAmount() + "x ") : "";
        String replacement;
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName())
            replacement = amount + item.getItemMeta().getDisplayName();
        else
            replacement = amount + item.getI18NDisplayName();
        return replace(replacement);
    }

    public TextBuilder replaceIf(boolean condition, Object replacement) {
        targetMustBeSelected();
        if (!current.replaced && condition)
            return replace(replacement);
        return this;
    }

    public TextBuilder color(ChatColor color) {
        targetMustBeSelected();
        current.color = color;
        return this;
    }

    public TextBuilder color(org.bukkit.ChatColor color) {
        targetMustBeSelected();
        return color(ChatColor.getByChar(color.getChar()));
    }

    public TextBuilder hover() {
        targetMustBeSelected();
        resetHovers();
        current.hover = getCurrentPartString();
        return this;
    }

    public TextBuilder hover(String text) {
        targetMustBeSelected();
        resetHovers();
        current.hover = text;
        return this;
    }

    public TextBuilder hover(ItemStack item) {
        targetMustBeSelected();
        resetHovers();
        current.hoverItem = item;
        return this;
    }

    public TextBuilder command() {
        targetMustBeSelected();
        resetClickActions();
        current.command = getCurrentPartString();
        return this;
    }

    public TextBuilder command(String command) {
        targetMustBeSelected();
        resetClickActions();
        current.command = command;
        return this;
    }

    public TextBuilder suggest() {
        targetMustBeSelected();
        resetClickActions();
        current.suggest = getCurrentPartString();
        return this;
    }

    public TextBuilder suggest(String suggestion) {
        targetMustBeSelected();
        resetClickActions();
        current.suggest = suggestion;
        return this;
    }

    public TextBuilder URL() {
        targetMustBeSelected();
        resetClickActions();
        current.URL = getCurrentPartString();
        return this;
    }

    public TextBuilder URL(String URL) {
        targetMustBeSelected();
        resetClickActions();
        current.URL = URL;
        return this;
    }

    public TextBuilder runnable(Runnable runnable) {
        targetMustBeSelected();
        resetClickActions();
        current.runnable = runnable;
        return this;
    }

    public TextBuilder itemable(ItemStack item, boolean withAmount) {
        targetMustBeSelected();
        hover(item);
        return replace(item, withAmount);
    }

    public BaseComponent[] create() {
        // TODO TEXTBUILDER - create()
    }

    private void targetMustBeSelected() {
        if (current == null)
            throw new IllegalStateException("No target selected");
    }

    private void resetHovers() {
        if (current != null) {
            current.hover = null;
            current.hoverItem = null;
        }
    }

    private String getCurrentPartString() {
        return getPartString(current);
    }

    private String getPartString(Part part) {
        if (part == null)
            return null;
        int start = part.start == -1 ? 0 : part.start;
        int end = part.end == -1 ? pattern.length() : part.end;
        return pattern.substring(start, end);
    }

    private void resetClickActions() {
        if (current != null) {
            current.command = null;
            current.runnable = null;
            current.suggest = null;
            current.URL = null;
        }
    }

    private void setLetter(int pos, char letter) {
        if (pos < 0 || pos >= pattern.length())
            return;

        // Edit text
        String before = pattern.substring(0, pos);
        String after = pattern.substring(pos+1);
        pattern = before + letter + after;
    }

    private void addLetter(int pos, char letter) {
        if (pos < 0 || pos > pattern.length())
            return;

        // Edit text
        String before = pattern.substring(0, pos);
        String after = pattern.substring(pos);
        pattern = before + letter + after;

        // Edit parts
        List<Part> allParts = new ArrayList<>(parts);
        if (current != null)
            allParts.add(current);
        for (Part part : allParts) {
            if (part.start >= pos)
                part.start++;
            if (part.end >= pos)
                part.end++;
        }

        removeEmptyParts();
    }

    private void removeLetter(int pos) {
        if (pos < 0 || pos >= pattern.length())
            return;

        // Edit text
        String before = pattern.substring(0, pos);
        String after = pattern.substring(pos+1);
        pattern = before + after;

        // Edit parts
        List<Part> allParts = new ArrayList<>(parts);
        if (current != null)
            allParts.add(current);
        for (Part part : allParts) {
            if (part.start > pos)
                part.start--;
            if (part.end > pos)
                part.end--;
        }

        removeEmptyParts();
    }

    private void removeEmptyParts() {
        parts.removeIf(part -> getPartString(part).length() == 0);
    }

    private void finishCurrentPart() {
        if (current != null && getCurrentPartString().length() != 0) {
            parts.add(current);
            current = null;
        }
    }

    private static class Part {
        int start = -1; // inclusive
        int end = -1; // not inclusive
        boolean replaced = false;

        ChatColor color = null;

        String hover = null;
        ItemStack hoverItem = null;

        String command = null;
        String suggest = null;
        String URL = null;
        Runnable runnable = null;
    }
}
