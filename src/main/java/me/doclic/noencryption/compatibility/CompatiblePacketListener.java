package me.doclic.noencryption.compatibility;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

public interface CompatiblePacketListener {

    void readPacket(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception;

    void writePacket(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise promise) throws Exception;

}
