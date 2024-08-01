package de.fisch37.fischyTranslations.api;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;

public class LanguageRegistry {
    private static final LanguageRegistry INSTANCE = new LanguageRegistry();

    public static LanguageRegistry getInstance() {
        return INSTANCE;
    }

    private final HashMap<String, Language> languageByCode = new HashMap<>();

    private LanguageRegistry() { }

    public void register(Language language) {
        languageByCode.put(language.code(), language);
    }

    public @Nullable Language getLanguage(String code) {
        return languageByCode.get(code);
    }

    public Collection<Language> allLanguages() {
        return languageByCode.values();
    }

}
