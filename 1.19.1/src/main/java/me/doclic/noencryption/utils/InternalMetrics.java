package me.doclic.noencryption.utils;

import me.doclic.noencryption.NoEncryption;
import me.doclic.noencryption.config.ConfigurationHandler;

public class InternalMetrics {
    private static Metrics metrics;

    public static void loadMetrics() {
        if (!enabled())
            return;

        int pluginId = 17791;
        InternalMetrics.metrics = new Metrics(NoEncryption.plugin(), pluginId);
    }

    public static void insertChart(Metrics.CustomChart chart) {
        if (!enabled())
            return;

        InternalMetrics.metrics.addCustomChart(chart);
    }

    private static boolean enabled() {
        try {
            return ConfigurationHandler.bStatsEnabled();
        } catch (NullPointerException e) {
            return true;
        }
    }
}
