package me.doclic.noencryption.compatibility;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

public interface CompatiblePacketListener {

    /**
     * @return the packet that should be read
     */
    Object readPacket(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception;

    /**
     * @return the packet that should be written
     */
    Object writePacket(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise promise) throws Exception;

}
