package me.doclic.noencryption;

import me.doclic.noencryption.compatibility.Compatibility;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class NoEncryption extends JavaPlugin {

    @Override
    public void onEnable() {

        if (Compatibility.SERVER_COMPATIBLE) {

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
}
