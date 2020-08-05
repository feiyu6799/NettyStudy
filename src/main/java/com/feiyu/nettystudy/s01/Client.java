package com.feiyu.nettystudy.s01;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;
/**
 * 客户端
 * @author feiyu
 *
 */
public class Client {
	public static void main(String[] args) throws Exception {
		//线程池
		EventLoopGroup group = new NioEventLoopGroup(1);//事件处理的线程池
		
		Bootstrap b = new Bootstrap();//辅助启动类
		
		try {			
			ChannelFuture f = 
					b.group(group)
				.channel(NioSocketChannel.class)//连接类型设置NioSocketChannel
				.handler(new ClientChannelInitializer())//handler发生一个事件交给谁处理
				.connect("localhost", 8888)
				;
						
			f.addListener(new ChannelFutureListener() {//监听链接服务器是否成功
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if(!future.isSuccess()) {
						System.out.println("not connected!");
					} else {
						System.out.println("connected!");
					}
				}
			});
			
			f.sync();//阻塞
			
			System.out.println("...");
			
			
			f.channel().closeFuture().sync();//阻塞，使客户端正常结束
		} finally {
			group.shutdownGracefully();
		}
	}
}
/**
 * 当链接到服务器时事件触发
 * @author feiyu
 *
 */
class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ch.pipeline().addLast(new ClientHandler());//执行的逻辑
	}
	
}
/**
 * 当链接到服务器时，执行的逻辑
 * @author feiyu
 *
 */
class ClientHandler extends ChannelInboundHandlerAdapter {

	/**
	 * 读取服务器响应内容
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf buf = null;
		try {
			buf = (ByteBuf)msg;	
			byte[] bytes = new byte[buf.readableBytes()];
			buf.getBytes(buf.readerIndex(), bytes);
			System.out.println(new String(bytes));
			//System.out.println(buf);
			//System.out.println(buf.refCnt());
		} finally {
			if(buf != null) ReferenceCountUtil.release(buf);
			//System.out.println(buf.refCnt());
		}
	}

	/**
	 * 第一次连接到服务器发送的内容
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		//channle 第一次连上可用，写出一个字符串 Direct Memory
		ByteBuf buf = Unpooled.copiedBuffer("hello".getBytes());
		ctx.writeAndFlush(buf);
	}
	
}


