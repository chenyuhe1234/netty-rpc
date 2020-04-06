package com.chenyuhe.cn.rpc.registry;

import com.chenyuhe.cn.rpc.protocol.InvokerProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RegistryHandler extends ChannelInboundHandlerAdapter {


	//保存所有相关的服务类
	private List<String> classNames = new ArrayList<String>();

	//用保存所有可用的服务
	public static ConcurrentHashMap<String, Object> registryMap = new ConcurrentHashMap<String, Object>();

	/**
	 * 思路
	 * 1.根据一个包名将所有符合条件的class全部扫描出来 放在一个容器中 如果是分布式 读取配置文件
	 * 2.给每一个对应的class起一个唯一的名字 作为服务名称 保存到一个容器中
	 * 3.当有客户端连接过来之后 就会获取协议内容 InvokerProtocol的对象
	 * 4.要去注册好的容器中寻找符合条件的服务
	 * 5.通过远程调用Provider得到返回结果 并回复给客户端
	 *
	 * @throws Exception
	 */

	public RegistryHandler() {


		//完成递归扫描
		scannerClass("com.chenyuhe.cn.rpc.provider");

		// 注册
		doRegister();
	}

	/**
	 * 完成注册
	 */
	private void doRegister() {
		if (classNames.size() == 0) {
			return;
		}
		for (String className : classNames) {
			try {
				Class<?> clazz = Class.forName(className);
				Class<?> i = clazz.getInterfaces()[0];

				// 本来这里存的应该是网络路径 从配置文件中读取
				// 在调用的时候再去解析 用反射调用
				registryMap.put(i.getName(), clazz.newInstance());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	/*
   * 递归扫描 ----- > 正常来说是读取配置文件 这里直接扫描本地class
   */
	private void scannerClass(String packageName) {
		URL url = this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
		File dir = new File(url.getFile());
		for (File file : dir.listFiles()) {
			//如果是一个文件夹，继续递归
			if (file.isDirectory()) {
				scannerClass(packageName + "." + file.getName());
			} else {
				classNames.add(packageName + "." + file.getName().replace(".class", "").trim());
			}
		}
	}


	// 有客户端连上的时候 回调
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		Object result = new Object();
		InvokerProtocol request = (InvokerProtocol) msg;
		if (registryMap.containsKey(request.getClassName())) {
			Object clazz = registryMap.get(request.getClassName());
			Method method = clazz.getClass().getMethod(request.getMethodName(), request.getParams());
			result = method.invoke(clazz, request.getValues());
		}
		ctx.write(result);
		ctx.flush();
		ctx.close();

	}


	// 连接发送异常的时候 会回调
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
	}
}