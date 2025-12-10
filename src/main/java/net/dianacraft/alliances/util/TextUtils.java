package net.dianacraft.alliances.util;

import net.dianacraft.alliances.Main;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class TextUtils {
    /* These utils are shamelessly stolen from Mat0u5's Life Series*/

    public static String formatString(String template, Object... args) {
        return format(template, args).getString();
    }

    public static MutableComponent format(String template, Object... args) {
        return formatStyled(false, template, args);
    }

    private static MutableComponent formatStyled(boolean looselyStyled, String template, Object... args){
        MutableComponent result = Component.empty();
        StringBuilder resultLooselyStyled = new StringBuilder();

        int argIndex = 0;
        int lastIndex = 0;
        int placeholderIndex = template.indexOf("{}");

        if (placeholderIndex == -1) {
            Main.LOGGER.error("String ("+template+") formatting does not contain {}.");
        }
        if (args.length <= 0) {
            Main.LOGGER.error("String ("+template+") formatting does have arguments.");
        }
        if (("_"+template+"_").split("\\{\\}").length-1 != args.length) {
            Main.LOGGER.error("String ("+template+") formatting has incorrect number of arguments.");
        }

        while (placeholderIndex != -1 && argIndex < args.length) {
            if (placeholderIndex > lastIndex) {
                String textBefore = template.substring(lastIndex, placeholderIndex);
                result.append(Component.literal(textBefore));
                resultLooselyStyled.append(textBefore);
            }

            Object arg = args[argIndex];
            Component argText = getTextForArgument(arg);
            result.append(argText);
            resultLooselyStyled.append(argText.getString());

            argIndex++;
            lastIndex = placeholderIndex + 2;
            placeholderIndex = template.indexOf("{}", lastIndex);
        }

        if (lastIndex < template.length()) {
            String remainingText = template.substring(lastIndex);
            result.append(Component.literal(remainingText));
            resultLooselyStyled.append(remainingText);
        }

        if (looselyStyled) {
            return Component.literal(resultLooselyStyled.toString());
        }

        return result;
    }

    private static Component getTextForArgument(Object arg) {
        if (arg == null) {
            return Component.empty();
        }
        if (arg instanceof Component text) {
            return text;
        }
        if (arg instanceof ServerPlayer player) {
            Component name = player.getDisplayName();
            if (name == null) return Component.empty();
            return name;
        }
        if (arg instanceof List<?> list) {
            MutableComponent text = Component.empty();
            int index = 0;
            for (Object obj : list) {
                if (index != 0) {
                    text.append(Component.nullToEmpty(", "));
                }
                text.append(getTextForArgument(obj));
                index++;
            }
            return text;
        }
        return Component.nullToEmpty(arg.toString());
    }

    public static Component hereText(ClickEvent event, String label) {
        return clickableText(label, event);
    }

    public static Component hereText(ClickEvent event, String label, ChatFormatting formatting) {
        return clickableText(label, event, formatting);
    }

    public static Component clickableText(String label, ClickEvent event) {
        return clickableText(label, event, ChatFormatting.BLUE);
    }

    public static Component clickableText(String label, ClickEvent event, ChatFormatting formatting) {
        return Component.literal(label)
                .withStyle(style -> style
                        .withColor(formatting)
                        .withClickEvent(event)
                        .withUnderlined(true)
                );
    }
}
