package com.chenyuhe.cn.rpc.registry;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * 注册中心 向外暴露对外服务的端口
 */
public class RpcRegistry {


	private int port;

	public RpcRegistry(int port) {
		this.port = port;
	}

	public void start() {


		try {
			// 以前是通过ServerSocket来暴露服务 现在通过Netty的api来暴露服务 ---- > ServerBootStrap
			// netty是基于NIO来实现的 ---- > Selector 主线程 work线程
			ServerBootstrap server = new ServerBootstrap();
			// server.group(parentGroup,childGroup)
			// netty中的线程池 EventLoopGroup
			EventLoopGroup bossGroup = new NioEventLoopGroup(); // 主线程池 ---- > Selector
			EventLoopGroup workerGroup = new NioEventLoopGroup();  // 工作线程池 ---- > 具体的对应客户端的处理逻辑
			server.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel socketChannel) throws Exception {

					// 在Netty中 把所有的业务逻辑全部归总到一个队列中
					// 这个队列中包含了各种各样的处理逻辑 对这些处理逻辑在Netty中有一个封装
					// 封装成了一个对象 无锁化串行任务队列 ------ > Pipline
					ChannelPipeline pipeline = socketChannel.pipeline();
					// 这里就是对我们处理逻辑的封装

					/** 编解码完成对象数据的解析 */
					// 对自定义协议的内容进行编解码
					pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
					//自定义协议编码器
					pipeline.addLast(new LengthFieldPrepender(4));
					//对象参数类型编码器
					pipeline.addLast("encoder", new ObjectEncoder());
					//对象参数类型解码器
					pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));

					/**
					 * 编写自己的逻辑
					 * 1.注册 给每一个对象起一个名字 对外提供服务的名字
					 * 2.服务的位置做一个登记
					 */
					pipeline.addLast(new RegistryHandler());


				}
			}).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);

			// 正式启动服务 相当于用一个死循环轮询
			ChannelFuture future = server.bind(this.port).sync();
			System.out.println("GP RPC Register start listen at" + this.port);
			future.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		}


	}


	public static void main(String[] args) {

		new RpcRegistry(8080).start();

	}
}