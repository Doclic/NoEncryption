package me.doclic.noencryption.compatibility;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;

public interface CompatiblePlayer {

    Channel getChannel(Player player);

}
