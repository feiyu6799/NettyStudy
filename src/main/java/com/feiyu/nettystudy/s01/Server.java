package com.feiyu.nettystudy.s01;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
/**
 * 服务器
 * @author feiyu
 *
 */
public class Server {
	//clients里面保存了各个链接到服务的Channel，然后就可以往各个服务器上通过Channel发送消息
	public static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);//多客户端之间转发
	
	public static void main(String[] args) throws Exception {
		/* 两个线程池一个负责链接，一个负责连接后的动作*/
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);//负责链接
		EventLoopGroup workerGroup = new NioEventLoopGroup(2);//负责处理读写
		
		try {
			ServerBootstrap b = new ServerBootstrap();//启动前的配置
			ChannelFuture f = b.group(bossGroup, workerGroup)//指定Group职责
				.channel(NioServerSocketChannel.class)//设置链接通道类型NioServerSocketChannel
				.childHandler(new ChannelInitializer<SocketChannel>() {//监听器，监听客户端链接触发
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ChannelPipeline pl = ch.pipeline();
						pl.addLast(new ServerChildHandler());
					}
				})
				.bind(8888)
				.sync();//这里调用sync()是等待成功才会往下执行
			
			System.out.println("server started!");
			
			f.channel().closeFuture().sync(); //没有这句话，程序就结束了。这就话是阻塞的，不去调用ChannelFuture.close()关闭服务
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
		
	}
}
/**
 * 客户端链接成功后触发
 * @author feiyu
 *
 */
class ServerChildHandler extends ChannelInboundHandlerAdapter { //SimpleChannleInboundHandler Codec
	/**
	 * 初始化的处理
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		Server.clients.add(ctx.channel());//ChannelHandlerContext是channel当前运行的网络环境，可以拿到当前的channel
	}

	/**
	 * 读取客户端发送内容
	 * msg：客户端发送的消息
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		
		ByteBuf buf = null;
		try {
			buf = (ByteBuf)msg;
			
			byte[] bytes = new byte[buf.readableBytes()];
			buf.getBytes(buf.readerIndex(), bytes);
			System.out.println(new String(bytes));
			
			Server.clients.writeAndFlush(msg);//返回客户端发送内容，这里调用Server.clients.writeAndFlush(msg)下面finally就不用关闭ReferenceCountUtil.release(buf)
			
			//System.out.println(buf);
			//System.out.println(buf.refCnt());
		} finally {
			//if(buf != null) ReferenceCountUtil.release(buf);//关闭ByteBuf
			//System.out.println(buf.refCnt());buf.refCnt()当前有多少个链接
		}
	}

	/**
	 * 有错误时执行的方法
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();//捕捉错误打印
		ctx.close();//正常关闭
	}
	
	
}





