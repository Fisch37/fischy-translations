package de.fisch37.fischyTranslations.api;

import de.fisch37.fischyTranslations.FischyTranslations;
import net.kyori.adventure.text.Component;

@FunctionalInterface
public interface TranslationEngine {
    default void initialize(FischyTranslations plugin) { }

    default void dispose() { }

    Component translate(TranslationTask task);
}
