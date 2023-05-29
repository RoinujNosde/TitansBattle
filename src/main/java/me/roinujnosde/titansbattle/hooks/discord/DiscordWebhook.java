package me.roinujnosde.titansbattle.hooks.discord;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

/**
 * Class used to execute Discord Webhooks
 */
public class DiscordWebhook {

    private final String url;
    private String content;

    /**
     * Constructs a new DiscordWebhook instance
     *
     * @param url The webhook URL obtained in Discord
     */
    public DiscordWebhook(String url) {
        this.url = url;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void execute() throws IOException {
        JsonObject json;

        try {
            json = JsonParser.parseString(content).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            json = new JsonObject();
            json.addProperty("content", content);
        }

        URL url = new URL(this.url);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.addRequestProperty("Content-Type", "application/json");
        connection.addRequestProperty("User-Agent", "TitansBattle");
        connection.setDoOutput(true);

        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(json.toString().getBytes());
            outputStream.flush();
        }

        connection.getInputStream().close();
        connection.disconnect();
    }

}
