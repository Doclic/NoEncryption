package me.doclic.noencryption.compatibility;

import me.doclic.noencryption.NoEncryption;
import me.doclic.noencryption.compatibility.versionhandler.*;
import org.bukkit.Bukkit;

public class Compatibility {
    public static final MinecraftVersion MIN_VERSION = new MinecraftVersion(1, 19);
    public static final MinecraftVersion MAX_VERSION = new MinecraftVersion(1, 19, 4);
    public static final MinecraftVersion SERVER_VERSION;
    public static final VersionHandler VERSION_HANDLER;
    public static final boolean SERVER_COMPATIBLE;

    static {
        String minecraftVersion;

        try {
            // getBukkitVersion() returns it in this format: "1.19.4-R0.1-SNAPSHOT"
            // If the version couldn't be found, it returns "Unknown-Version"
            minecraftVersion = Bukkit.getBukkitVersion();
        } catch (ArrayIndexOutOfBoundsException exception) {
            minecraftVersion = null;
        }

        if(minecraftVersion == null || minecraftVersion.equals("Unknown-Version")) {
            NoEncryption.logger().warning("Couldn't find your server version, assuming " + MAX_VERSION);
            SERVER_VERSION = MAX_VERSION;
        } else {
            final var versionSplit = minecraftVersion.split("\\.");
            int number0, number1, number2;
            MinecraftVersion version;
            try {
                // fixme this code sucks
                number0 = parseVersionNumber(versionSplit[0]);
                number1 = parseVersionNumber(versionSplit[1]);
                number2 = 0;
                if(versionSplit.length > 3) {
                    try { number2 = parseVersionNumber(versionSplit[2]); }
                    catch (NumberFormatException ignored) { }
                }
                version = new MinecraftVersion(number0, number1, number2);
            } catch (NumberFormatException e) {
                NoEncryption.logger().severe("Couldn't parse server version: " + minecraftVersion);
                NoEncryption.logger().severe("Assuming " + MAX_VERSION);
                version = MAX_VERSION;
            }
            SERVER_VERSION = version;
        }

        NoEncryption.logger().info("Your server is running version " + SERVER_VERSION);

        if(SERVER_VERSION.singleNumber < MIN_VERSION.singleNumber) {
            NoEncryption.logger().severe("NoEncryption was made for versions " + MIN_VERSION + "-" + MAX_VERSION);
            NoEncryption.logger().severe("Your server is running version " + SERVER_VERSION + ", which the");
            NoEncryption.logger().severe("plugin doesn't support because players cannot be chat");
            NoEncryption.logger().severe("reported in versions before " + MIN_VERSION);

            SERVER_COMPATIBLE = false;
        } else if(SERVER_VERSION.singleNumber > MAX_VERSION.singleNumber) {
            NoEncryption.logger().warning("NoEncryption was made for versions " + MIN_VERSION + "-" + MAX_VERSION);
            NoEncryption.logger().warning("Your server is running version " + SERVER_VERSION + ", which the");
            NoEncryption.logger().warning("plugin doesn't support yet, please update the plugin at");
            NoEncryption.logger().warning("https://github.com/Doclic/NoEncryption/releases");
            NoEncryption.logger().warning("If the issue persists, please wait for the developers to");
            NoEncryption.logger().warning("release a build for " + SERVER_VERSION);
            NoEncryption.logger().warning("Running as version " + MAX_VERSION + " for now");

            // We set as a compatible, because it most likely is
            SERVER_COMPATIBLE = true;
        } else {
            SERVER_COMPATIBLE = true;
        }

        if(SERVER_COMPATIBLE) VERSION_HANDLER = switch(SERVER_VERSION.singleNumber) {
            case 11900 -> new VersionHandler11900();
            case 11901, 11902 -> new VersionHandler11901();
            case 11903 -> new VersionHandler11903();
            default -> new VersionHandler11904();
        };
        else VERSION_HANDLER = null;
    }

    private static int parseVersionNumber(String numberStr) {
        if(numberStr.contains("-")) numberStr = numberStr.substring(0, numberStr.indexOf('-'));
        return Integer.parseInt(numberStr);
    }
}