package de.fisch37.fischyTranslations.api;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public record TranslationTask(
        @NotNull Component input,
        @NotNull Language language
) { }
