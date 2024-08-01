package de.fisch37.fischyTranslations;

import de.fisch37.fischyTranslations.api.TranslationEngine;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.logging.Logger;

public class TranslationRegistry {
    private static Logger logger;

    private final HashMap<String, TranslationEngine> engines = new HashMap<>();

    TranslationRegistry() { }

    public void register(String engineName, TranslationEngine engine) {
        if (engines.put(engineName, engine) != null) {
            logger.warning("Translation engine %s was overridden".formatted(engineName));
        }
    }

    @Nullable TranslationEngine getEngine(String name) {
        return engines.get(name);
    }

    void setLogger(Logger logger) {
        TranslationRegistry.logger = logger;
    }
}
