package me.doclic.noencryption.compatibility;

import io.netty.channel.Channel;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class CompatiblePlayer {

    public Channel getChannel(Player player) {

        return ((CraftPlayer) player).getHandle().connection.connection.channel;

    }

}
