package de.fisch37.fischyTranslations.engines;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.fisch37.fischyTranslations.FischyTranslations;
import de.fisch37.fischyTranslations.api.Language;
import de.fisch37.fischyTranslations.api.TranslationEngine;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static de.fisch37.fischyTranslations.api.ComponentUtils.pipeline;

public class LectoEngine implements TranslationEngine {
    private static final URL ENDPOINT;
    private static final Gson GSON = new Gson();
    public static final int CONNECT_TIMEOUT = 5000;
    public static final int READ_TIMEOUT = 5000;

    static {
        try {
            ENDPOINT = new URI("https://api.lecto.ai/v1/translate/text").toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            // This never happens. I can copy-paste
            throw new RuntimeException(e);
        }
    }


    private @Nullable String apiKey;
    private Logger logger;

    @Override
    public void initialize(FischyTranslations plugin) {
        Optional.ofNullable(plugin.getConfig().getConfigurationSection("lecto"))
                .ifPresentOrElse(
                        section -> apiKey = section.getString("api-key"),
                        () -> plugin.getSLF4JLogger().error("Could not load Lecto API. No configuration found")
                );
        logger = plugin.getSLF4JLogger();
    }

    @Override
    public void dispose() {
        TranslationEngine.super.dispose();
    }

    private Component makeError(TranslationTask task) {
        return Component.text("TRANSLATION ERROR, DEFAULTING:").append(task.input());
    }

    @Override
    public Component translate(TranslationTask task) {
        if (apiKey == null) {
            logger.error("Lecto API is not loaded. Check the startup log!");
            return makeError(task);
        }

        final HttpsURLConnection conn;
        try {
            conn = prepareUrlConnection();
        } catch (IOException e) {
            logger.error("Failed to reach Lecto translation API", e);
            return makeError(task);
        }
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);

        try{
            return pipeline(task.input(), texts -> {
                try {
                    writeRequest(conn, task.language(), texts);
                    return getResults(conn);
                } catch (IOException | HTTPException e) {
                    throw new LayerTraversalWrapper(e);
                }
            });
        } catch (LayerTraversalWrapper wrapper) {
            if (wrapper.getCause() instanceof HTTPException e) {
                logger.error("Lecto API responded with http error {}: {}", e.status, e.name);
            } else {
                logger.error("LectoEngine failed to translate due to unknown error", wrapper.getCause());
            }

            return makeError(task);
        } catch (Exception e) {
            logger.error("Unknown error during translation", e);

            return makeError(task);
        }
    }

    private HttpsURLConnection prepareUrlConnection() throws IOException {
        assert apiKey != null;
        final HttpsURLConnection conn = (HttpsURLConnection)ENDPOINT.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("X-API-Key", apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        return conn;
    }

    private void writeRequest(
            HttpsURLConnection conn,
            Language targetLanguage,
            Collection<String> texts
    ) throws IOException {
        java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(conn.getOutputStream());
        // See https://dashboard.lecto.ai/docs#operation/get_languages
        JsonObject request = new JsonObject();
        JsonArray languages = new JsonArray();
        JsonArray jsonTexts = new JsonArray(texts.size());

        for (String text : texts) {
            jsonTexts.add(text);
        }

        languages.add(targetLanguage.code());
        request.add("to", languages);
        request.add("texts", jsonTexts);
        GSON.toJson(request, writer);
    }

    private void throwResponse(HttpsURLConnection conn) throws HTTPException, IOException {
        throw new HTTPException(conn.getResponseCode(), conn.getResponseMessage());
    }

    private Collection<String> getResults(HttpsURLConnection conn) throws IOException, HTTPException {
        conn.connect();
        switch (conn.getResponseCode()) {
            case 400:
                logger.error("Invalid Request format in Lecto Translation API");
                throwResponse(conn);
                break;
            case 401:
                logger.error("Invalid API Key for Lecto Translation API");
                throwResponse(conn);
                break;
            case 404:
                logger.error("Lecto Translation API not found???");
                throwResponse(conn);
                break;
            default:
                logger.error("Lecto Translation API responded with undocumented code: {}", conn.getResponseCode());
                throwResponse(conn);
                break;
            case 200:
                JsonObject object = GSON.fromJson(
                        new java.io.InputStreamReader(conn.getInputStream()),
                        JsonObject.class
                );
                try{
                    JsonArray jsonTranslations = object.getAsJsonArray("translated")
                            .get(0)
                            .getAsJsonObject()
                            .getAsJsonArray("translated");
                    ArrayList<String> translations  = new ArrayList<>(jsonTranslations.size());
                    for (JsonElement element : jsonTranslations) {
                        translations.add(element.getAsString());
                    }
                    return translations;
                } catch (ClassCastException | UnsupportedOperationException | IllegalStateException e) {
                    throw new IllegalStateException("Received invalid output from Lecto API", e);
                }
        }
        throw new IllegalStateException("This should be impossible");
    }

    private static class LayerTraversalWrapper extends RuntimeException {
        private LayerTraversalWrapper(Exception e) {
            super(e);
        }
    }

    private static class HTTPException extends Exception {
        public final int status;
        public final String name;

        private HTTPException(int status, String name) {
            super();
            this.status = status;
            this.name = name;
        }
    }
}
