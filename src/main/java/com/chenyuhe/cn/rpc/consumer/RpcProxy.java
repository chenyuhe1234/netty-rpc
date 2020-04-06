package com.chenyuhe.cn.rpc.consumer;

import com.chenyuhe.cn.rpc.protocol.InvokerProtocol;
import com.chenyuhe.cn.rpc.registry.RegistryHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class RpcProxy {

	public static <T> T create(Class<?> clazz) {
		MethodProxy methodProxy = new MethodProxy(clazz);

		T result = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, methodProxy);
		// jdk的动态代理
		return result;
	}

	/**
	 * 将本地调用通过代理的形式变成网络调用
	 */
	private static class MethodProxy implements InvocationHandler {

		private Class<?> clazz;

		public MethodProxy(Class<?> clazz) {
			this.clazz = clazz;
		}


		private Object rpcInvoker(Object proxy, Method method, Object[] args) {

			// 构造协议的内容 --- > 消息
			InvokerProtocol msg = new InvokerProtocol();
			msg.setClassName(this.clazz.getName());
			msg.setMethodName(method.getName());
			msg.setParams(method.getParameterTypes());
			msg.setValues(args);

			final ConsumerHandler consumerHandler = new ConsumerHandler();


			// 发起网络请求 ---- > 通过netty发起网络请求
			EventLoopGroup workGroup = new NioEventLoopGroup();

			try {

				// 客户端 BootStrap
				Bootstrap client = new Bootstrap();
				client.group(workGroup).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<SocketChannel>() {
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
						pipeline.addLast(consumerHandler);
					}
				});

				ChannelFuture future = client.connect("localhost", 8080).sync();
				future.channel().writeAndFlush(msg).sync();
				future.channel().closeFuture().sync();


			} catch (Exception e) {
				e.printStackTrace();
			} finally {

				// 线程池关闭
				workGroup.shutdownGracefully();
			}


			return consumerHandler.getResponse();


		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

			// 如果你是一个实现类就直接调用
			if (Object.class.equals(method.getDeclaringClass())) {
				return method.invoke(this, args);
			} else {
				// 如果是一个接口通过netty发起网络调用
				return rpcInvoker(proxy, method, args);
			}


		}
	}
}