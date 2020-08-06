package com.feiyu.nettystudy.s13;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
/**
 * 编码器：负责编码
 * 把一个消息转换成字节
 * @author feiyu
 *
 */
public class TankMsgEncoder extends MessageToByteEncoder<TankMsg>{

	@Override
	protected void encode(ChannelHandlerContext ctx, TankMsg msg, ByteBuf buf) throws Exception {
		buf.writeInt(msg.x);
		buf.writeInt(msg.y);
	}
	

}
