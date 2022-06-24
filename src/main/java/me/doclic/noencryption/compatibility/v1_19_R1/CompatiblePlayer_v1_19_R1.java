package me.doclic.noencryption.compatibility.v1_19_R1;

import io.netty.channel.Channel;
import me.doclic.noencryption.compatibility.CompatiblePlayer;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class CompatiblePlayer_v1_19_R1 implements CompatiblePlayer {

    @Override
    public Channel getChannel(Player player) {

        return ((CraftPlayer) player).getHandle().b.b.m; // couldn't get mojang mappings to work

    }

}
