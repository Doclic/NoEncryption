package me.doclic.noencryption.config;

public enum ConfigNodes {
    HEADER(null, "fun", null,
             " ",
            "# |==========================================================|",
            "# |                                                          |",
            "# |           NoEncryption Dynamic Configuration             |",
            "# |              By Doclic and V1nc3ntWasTaken               |",
            "# |                                                          |",
            "# |    This is a dynamic configuration file that will be     |",
            "# |    automatically updated when you update the plugin,     |",
            "# |  although any new features will be disabled by default,  |",
            "# |    and you will receive a one-time notification about    |",
            "# |   any added config options in the console upon startup.  |",
            "# |                                                          |",
            "# |     --------------------------------------               |",
            "# |                                                          |",
            "# |  Config entries will look like the following:            |",
            "# |                                                          |",
            "# |      friends:                                            |",
            "# |                                                          |",
            "# |        # Allows a user to become friends with the devs   |",
            "# |        #                                                 |",
            "# |        # Note: Doesn't actually do anything, just here   |",
            "# |        #       for show, and example.                    |",
            "# |        #                                                 |",
            "# |        # Added in v3.0                                   |",
            "# |        # Default: false                                  |",
            "# |        friendly-dev: false                               |",
            "# |                                                          |",
            "# |==========================================================|",
            " "),
    FRIENDLY_DEV(null, "fun.friendly_dev", false,
            " ",
            "# Allows a user to become friends with the devs",
            "# ",
            "# Note: Doesn't actually do anything, just here",
            "#       for show, and example",
            "# Added in v3.0",
            "# Default: false"),

    LOGIN_PROTECTION_MESSAGE(null, "login_protection_message", "",
            " ",
            "# Displays a message to users when they log in that their messages are protected",
            "# ",
            "# Note: Leave blank to disable",
            "# ",
            "# Added in v3.0",
            "# Default: BLANK"),

    FORWARD_UUID("Due to Minecraft's new message validation techniques, this option is no longer supported", "forward_uuid", false,
            " ",
            "# Forwards UUIDs of message senders. This may improve functionality of some client-side mods/plugins",
            "# ",
            "# Note: Enabling this option may cause chat messages to not be fully stripped, thus showing the red bar, and red exclamation mark",
            "# ",
            "# Added in v3.1",
            "# Modified in v4.0",
            "# ",
            "# Due to Minecraft's new message validation techniques, this option is no longer supported",
            "# Default: false"),

    DISABLE_BANNER(null, "disable_banner", true,
            " ",
            "# Disabled the modified chats banner on the top right of the client",
            "# ",
            "# Added in v3.2",
            "# Default: true");


    private final String Notice;
    private final String Root;
    private final Object Default;
    private final String[] comments;

    ConfigNodes(String notice, String root, Object def, String... comments) {

        this.Notice = notice;
        this.Root = root;
        this.Default = def;
        this.comments = comments;
    }

    /**
     * Retrieves the root for a config option
     *
     * @return The root for a config option
     */
    public String getNotice() {

        return (Notice != null ? Notice.trim() : null);
    }

    /**
     * Retrieves the root for a config option
     *
     * @return The root for a config option
     */
    public String getRoot() {

        return Root;
    }

    /**
     * Retrieves the default value for a config path
     *
     * @return The default value for a config path
     */
    public Object getDefault() {

        return (Default == null ? "" : Default);
    }

    /**
     * Retrieves the default value for a config path, or null if there is none
     *
     * @return The default value for a config path, or null if there is none
     */
    public Object getDefaultOrNull() {

        return Default;
    }

    /**
     * Retrieves the comment for a config path
     *
     * @return The comments for a config path
     */
    public String[] getComments() {

        if (comments != null) {
            return comments;
        }

        String[] comments = new String[1];
        comments[0] = "";
        return comments;
    }
}