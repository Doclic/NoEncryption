package me.doclic.noencryption;

import me.doclic.noencryption.compatibility.Compatibility;
import me.doclic.noencryption.config.ConfigurationHandler;
import me.doclic.noencryption.utils.FileMgmt;
import me.doclic.noencryption.utils.InternalMetrics;
import me.doclic.noencryption.utils.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class NoEncryption extends JavaPlugin {

    private static NoEncryption plugin;
    private static Logger logger;

    @Override
    public void onEnable() {

        plugin = this;
        logger = getLogger();
        InternalMetrics.loadMetrics();

        if (Compatibility.SERVER_COMPATIBLE) {

            FileMgmt.initialize(this);
            ConfigurationHandler.initialize(this);

            if (!ConfigurationHandler.loadSettings()) {
                logger().severe("Configuration could not be loaded, disabling...");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }

            ConfigurationHandler.printChanges();

            Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

            logger().info("Compatibility successful!");

            logger().info("If you used /reload to update NoEncryption, your players need to disconnect and join back");

            InternalMetrics.insertChart(new Metrics.SimplePie("enabledDisabled", () -> "Enabled"));

        } else {

            logger().severe("Failed to setup NoEncryption's compatibility!");
            logger().severe("Your server version (" + Compatibility.SERVER_VERSION + ") is not compatible with this JAR! Check here for the latest version: https://github.com/Doclic/NoEncryption/releases/latest");

            InternalMetrics.insertChart(new Metrics.SimplePie("enabledDisabled", () -> "Disabled"));

            Bukkit.getPluginManager().disablePlugin(this);
        }

    }

    public static NoEncryption plugin() {
        return plugin;
    }

    public static Logger logger() {
        return logger;
    }

    public String getRootFolder() {
        return this.getDataFolder().getPath();
    }

    public static boolean usesKyoriChat() {
        try {
            Class.forName("net.kyori.adventure.Adventure");

            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
