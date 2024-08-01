package de.fisch37.fischyTranslations;

import de.fisch37.fischyTranslations.api.TranslationEngine;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class FischyTranslations extends JavaPlugin {
    private TranslationEngine engine;
    private final PlayerLanguageSelector languageSelector = new PlayerLanguageSelector();
    private final TranslationRegistry registry = new TranslationRegistry();

    @Override
    public void onEnable() {
        // Plugin startup logic
        registry.setLogger(getLogger());

        saveDefaultConfig();
        registerListener(new ServerStartListener());


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        engine.dispose();
    }

    private void registerListener(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    private void startEngine() {
        new EngineRegistrationEvent(registry).callEvent();
        String engineName = getConfig().getString("engine");
        engine = registry.getEngine(engineName);
        if (engine == null) {
            getSLF4JLogger().error("Could not find an engine for name {}", engineName);
            return;
        }
        engine.initialize(this);
        registerListener(new ChatEventHandler(engine, languageSelector));
    }

    private class ServerStartListener implements Listener {
        @EventHandler(priority = EventPriority.NORMAL)
        public void onServerLoad(ServerLoadEvent event) {
            FischyTranslations.this.startEngine();
        }
    }
}
