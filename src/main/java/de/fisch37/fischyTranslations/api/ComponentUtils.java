package de.fisch37.fischyTranslations.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class ComponentUtils {
    public static void visit(Component component, Consumer<Component> visitor) {
        visitor.accept(component);
        for (Component child : component.children()) {
            visit(child, visitor);
        }
    }

    public static boolean translatable(Component component) {
        return getTranslatablePart(component).isPresent();
    }

    public static Optional<String> getTranslatablePart(Component component) {
        if (component instanceof TextComponent text)
            return Optional.of(text.content());
        if (component instanceof TranslatableComponent translation)
            return Optional.ofNullable(translation.fallback());
        return Optional.empty();
    }

    public static Component replaceText(Component component, String replacement) {
        if (component instanceof TextComponent text)
            return text.content(replacement);
        if (component instanceof TranslatableComponent translation)
            return translation.fallback(replacement);
        throw new IllegalArgumentException("Non-translatable component in replaceText");
    }

    public static Collection<String> getTranslationCollection(Component component) {
        ArrayList<String> result = new ArrayList<>();

        visit(component, node -> getTranslatablePart(node).ifPresent(result::add));
        return result;
    }

    public static Component replace(Component template, Collection<String> translatedSequence) {
        return replace(template, translatedSequence.iterator());
    }

    private static Component replace(Component template, Iterator<String> textProvider) {
        if (translatable(template)) {
            ArrayList<Component> children = new ArrayList<>(template.children().size());
            for (Component child : template.children()) {
                children.add(replace(child, textProvider));
            }
            return template.children(children);
        } else {
            return replaceText(template, textProvider.next());
        }
    }

    public static Component pipeline(
            Component input,
            Function<Collection<String>, Collection<String>> translator
    ) {
        return replace(
                input,
                translator.apply(
                        getTranslationCollection(input)
                )
        );
    }
}
