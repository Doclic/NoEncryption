package me.doclic.noencryption.utils.updates;

import me.doclic.noencryption.NoEncryption;
import org.bukkit.Bukkit;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class UpdateChecker {
    static final URL apiUrl;
    public static final URL updateUrl;

    static {
        try {
            apiUrl = new URL("https://api.github.com/repos/Doclic/NoEncryption/releases/latest");
            updateUrl = new URL("https://github.com/Doclic/NoEncryption/releases/latest");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void check(Runnable callbackIfOld, Runnable callbackIfSameOrNew, Runnable callbackIfError) {
        Bukkit.getScheduler().runTaskAsynchronously(NoEncryption.plugin(), () -> {
            try {
                PluginVersion latest = getLatestVersion();
                PluginVersion current = new PluginVersion().current();

                int compare = current.compare(latest);

                Bukkit.getScheduler().runTask(NoEncryption.plugin(), () -> {
                    switch (compare) {
                        case -1:
                            callbackIfOld.run();
                            break;
                        default:
                            callbackIfSameOrNew.run();
                            break;
                    }
                });
            } catch (IOException | ParseException e) {
                e.printStackTrace();
                callbackIfError.run();
            }
        });
    }

    private static String readJson(URL url) throws IOException {
        InputStream input = url.openStream();
        InputStreamReader isr = new InputStreamReader(input);
        BufferedReader reader = new BufferedReader(isr);
        StringBuilder json = new StringBuilder();

        int c;
        while ((c = reader.read()) != -1) {
            json.append((char) c);
        }

        return json.toString();
    }

    private static PluginVersion getLatestVersion() throws IOException, ParseException {
        JSONObject parser = (JSONObject) new JSONParser().parse(readJson(apiUrl));
        String tagName = String.valueOf(parser.get("tag_name"));

        PluginVersion version = new PluginVersion(Integer.parseInt(tagName.split("\\.")[0]), Integer.parseInt(tagName.split("\\.")[1]));
        return version;
    }
}
