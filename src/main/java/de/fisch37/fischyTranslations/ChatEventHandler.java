package de.fisch37.fischyTranslations;

import de.fisch37.fischyTranslations.api.Language;
import de.fisch37.fischyTranslations.api.TranslationEngine;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class ChatEventHandler implements Listener, ChatRenderer {
    private final TranslationEngine engine;
    private final PlayerLanguageSelector languageSelector;

    public ChatEventHandler(@NotNull TranslationEngine engine, @NotNull PlayerLanguageSelector languageSelector) {
        this.engine = engine;
        this.languageSelector = languageSelector;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        event.renderer(this);
    }

    @Override
    public @NotNull Component render(
            @NotNull Player source,
            @NotNull Component sourceDisplayName,
            @NotNull Component message,
            @NotNull Audience viewer
    ) {
        Language language = languageSelector.getLanguage(viewer.get(Identity.UUID).get());
        if (language == null)
            return message;
        else
            return engine.translate(new TranslationEngine.TranslationTask(message, language));
    }
}
