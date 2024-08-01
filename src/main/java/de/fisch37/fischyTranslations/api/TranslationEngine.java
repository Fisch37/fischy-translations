package de.fisch37.fischyTranslations.api;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface TranslationEngine extends BaseTranslationEngine {
    Component translate(TranslationTask task);

    record TranslationTask(
            @NotNull Component input,
            @NotNull Language language
    ) { }
}
