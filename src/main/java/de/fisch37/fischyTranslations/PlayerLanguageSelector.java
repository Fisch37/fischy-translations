package de.fisch37.fischyTranslations;

import de.fisch37.fischyTranslations.api.Language;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.UUID;

public class PlayerLanguageSelector {
    private final HashMap<UUID, Language> selectedLanguage = new HashMap<>();

    PlayerLanguageSelector() { }

    public @Nullable Language getLanguage(Player player) {
        return getLanguage(player.getUniqueId());
    }
    public @Nullable Language getLanguage(UUID player) {
        return selectedLanguage.get(player);
    }

    public void setLanguage(UUID player, Language language) {
        selectedLanguage.put(player, language);
    }
}
