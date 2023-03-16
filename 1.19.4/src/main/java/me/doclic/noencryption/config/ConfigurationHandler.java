package me.doclic.noencryption.config;

import me.doclic.noencryption.NoEncryption;
import me.doclic.noencryption.utils.CommentedConfiguration;
import me.doclic.noencryption.utils.FileMgmt;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Level;

public class ConfigurationHandler {
    private static NoEncryption main;

    public static void initialize(NoEncryption main) {
        ConfigurationHandler.main = main;

        Config.initialize();
        Notices.initialize();
    }

    public static boolean loadSettings() {
        FileMgmt.checkFolders(new String[]{
                main.getRootFolder(),
                main.getRootFolder() + FileMgmt.fileSeparator() + "settings",
                main.getRootFolder() + FileMgmt.fileSeparator() + "storage"});

        return Notices.loadNotices() && Config.loadConfig();
    }

    public static class Config {
        private static CommentedConfiguration config, newConfig;
        private static HashMap<String, Object> newOptions;
        private static boolean firstConfig;

        private static void initialize() {
            newOptions = new HashMap<>();
        }

        private static boolean loadConfig() {
            String filepath = main.getRootFolder() + FileMgmt.fileSeparator() + "settings" + FileMgmt.fileSeparator() + "config.yml";
            firstConfig = !FileMgmt.CheckYMLExists(new File(filepath));

            File file = new File(filepath);
            if (firstConfig) {
                FileMgmt.createNewFile(file);
            }

            // read the config.yml into memory
            config = new CommentedConfiguration(file);
            if (!config.load()) {
                NoEncryption.logger().log(Level.SEVERE, "Failed to load config.yml");
                return false;
            }

            setDefaults(file);
            config.save();

            return true;
        }

        private static void setDefaults(File file) {
            newConfig = new CommentedConfiguration(file);
            newConfig.load();

            for (ConfigNodes root : ConfigNodes.values()) {
                if (root.getComments().length > 0) {
                    addComment(root.getRoot(), root.getComments());
                }

                setNewProperty(root.getRoot(), (config.get(root.getRoot().toLowerCase()) != null) ? config.get(root.getRoot().toLowerCase()) : root.getDefault());

                if (!firstConfig && !config.getKeys(true).contains(root.getRoot()) && root.getDefaultOrNull() != null) {
                    newOptions.put(root.getRoot(), root.getDefault());
                }

                if (root.getNotice() != null) {
                    Notices.messages.put(root.getRoot(), root.getNotice());
                }

            }

            config = newConfig;
            newConfig = null;
        }

        public static void printChanges() {
            NoEncryption.logger().info("Checking for new config option...");

            if (!newOptions.isEmpty()) {

                newOptions.forEach((root, def) -> NoEncryption.logger().info("  " + root + ": " + def));
            } else {
                NoEncryption.logger().info("No new config options detected");
            }
        }

        private static void addComment(String root, String... comments) {
            newConfig.addComment(root.toLowerCase(), comments);
        }

        private static void setProperty(String root, Object value) {
            config.set(root.toLowerCase(), value.toString());
        }

        private static void setNewProperty(String root, Object value) {
            if (value == null) {
                value = "";
            }

            newConfig.set(root.toLowerCase(), value.toString());
        }

        private static String getString(ConfigNodes node) {
            return config.getString(node.getRoot().toLowerCase(), String.valueOf(node.getDefault()));
        }

        private static boolean getBoolean(ConfigNodes node) {
            String boolString = config.getString(node.getRoot().toLowerCase(), String.valueOf(node.getDefault()));
            return Boolean.parseBoolean(boolString);
        }

        private static double getDouble(ConfigNodes node) {
            String doubleString = config.getString(node.getRoot().toLowerCase(), String.valueOf(node.getDefault()));
            return Double.parseDouble(doubleString);
        }

        private static int getInt(ConfigNodes node) {
            String intString = config.getString(node.getRoot().toLowerCase(), String.valueOf(node.getDefault()));
            return Integer.parseInt(intString);
        }

        public static String getLoginProtectionMessage() {
            return getString(ConfigNodes.LOGIN_PROTECTION_MESSAGE);
        }

        public static boolean getDisableBanner() {
            return getBoolean(ConfigNodes.DISABLE_BANNER);
        }

        public static boolean doAutoUpdateCheck() {
            return getBoolean(ConfigNodes.AUTO_UPDATE_CHECK);
        }

        public static boolean bStatsEnabled() {
            return getBoolean(ConfigNodes.BSTATS_ENABLED);
        }
    }

    public static class Notices {
        private static File noticesFile;
        private static CommentedConfiguration notices, newNotices;
        private static HashMap<String, String> messages, activeMessages;

        private static void initialize() {
            messages = new HashMap<>();
            activeMessages = new HashMap<>();
        }

        private static boolean loadNotices() {
            String filepath = main.getRootFolder() + FileMgmt.fileSeparator() + "storage" + FileMgmt.fileSeparator() + "notices.yml";
            noticesFile = new File(filepath);

            boolean firstNotices = !FileMgmt.CheckYMLExists(noticesFile);

            if (firstNotices) {
                FileMgmt.createNewFile(noticesFile);
            }

            // read the notices.yml into memory
            notices = new CommentedConfiguration(noticesFile);
            if (!notices.load()) {
                NoEncryption.logger().log(Level.SEVERE, "Failed to load notices.yml");
                return false;
            }

            return true;
        }

        public static boolean suppressNotices() {
            if (messages.isEmpty()) {
                return false;
            }

            newNotices = new CommentedConfiguration(noticesFile);
            newNotices.load();

            if (activeMessages.isEmpty())
                return false;

            activeMessages.forEach(Notices::setNewProperty);
            activeMessages = new HashMap<>();

            notices = newNotices;
            newNotices = null;

            notices.save();
            return true;
        }

        public static void loadAndPrintChanges() {
            NoEncryption.logger().info("Checking for important messages...");

            HashMap<String, String> display = new HashMap<>();

            if (!messages.isEmpty()) {
                messages.forEach((root, msg) -> {
                    if (getString(root) == null || !getString(root).equals(msg))
                        display.put(root, msg);
                });

                if (!display.isEmpty()) {
                    display.forEach((root, msg) -> {
                        NoEncryption.logger().warning("  " + root + ": \"" + msg + "\"");
                        activeMessages.put(root, msg);
                    });

                    NoEncryption.logger().info("These messages can be suppressed with /ne suppressnotices");
                } else {
                    NoEncryption.logger().info("No important messages detected");
                }
            } else {
                NoEncryption.logger().info("No important messages detected");
            }
        }

        private static void setNewProperty(String root, Object value) {
            if (value == null) {
                value = "";
            }

            newNotices.set(root.toLowerCase(), value.toString());
        }

        private static String getString(String root) {
            return notices.getString(root.toLowerCase(), null);
        }
    }
}