package me.doclic.noencryption;

import me.doclic.noencryption.compatibility.Compatibility;
import me.doclic.noencryption.config.ConfigurationHandler;
import me.doclic.noencryption.utils.FileMgmt;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class NoEncryption extends JavaPlugin {

    @Override
    public void onEnable() {

        if (Compatibility.SERVER_COMPATIBLE) {

            FileMgmt.initialize(this);
            ConfigurationHandler.initialize(this);

            if (!ConfigurationHandler.loadSettings()) {
                getLogger().severe("Configuration could not be loaded, disabling...");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }

            ConfigurationHandler.printChanges();

            Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

            getLogger().info("Compatibility successful!");

            getLogger().info("If you used /reload to update NoEncryption, your players need to");
            getLogger().info("disconnect and join back");

        } else {

            getLogger().severe("Failed to setup NoEncryption's compatibility!");
            getLogger().severe("Your server version (" + Compatibility.SERVER_VERSION + ") is not compatible with this plugin!");

            Bukkit.getPluginManager().disablePlugin(this);
        }

    }

    public String getRootFolder() {
        return this.getDataFolder().getPath();
    }
}
