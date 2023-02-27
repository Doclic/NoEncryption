package me.doclic.noencryption.compatibility;

import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R2.CraftServer;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CompatiblePlayer {
    public Channel getChannel(Player player) {
        return ((CraftPlayer) player).getHandle().connection.connection.channel;
    }

    public List<Connection> getServerConnections() {
        try {
            return Objects.requireNonNull(((CraftServer) Bukkit.getServer()).getServer().getConnection()).getConnections();
        } catch (NullPointerException e) {
            return Collections.emptyList();
        }
    }
}