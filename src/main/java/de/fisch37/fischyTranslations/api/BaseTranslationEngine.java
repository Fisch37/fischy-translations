package de.fisch37.fischyTranslations.api;

import de.fisch37.fischyTranslations.FischyTranslations;

public interface BaseTranslationEngine {
    default void initialize(FischyTranslations plugin) { }

    default void dispose() { }
}
