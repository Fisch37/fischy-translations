package de.fisch37.fischyTranslations;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class EngineRegistrationEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final TranslationRegistry registry;

    EngineRegistrationEvent(TranslationRegistry registry) {
        this.registry = registry;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public TranslationRegistry getRegistry() {
        return registry;
    }
}
