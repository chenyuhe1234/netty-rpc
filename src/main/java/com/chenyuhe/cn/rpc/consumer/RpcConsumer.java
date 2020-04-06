package com.chenyuhe.cn.rpc.consumer;

import com.chenyuhe.cn.rpc.api.IRpcService;

public class RpcConsumer {


	public static void main(String[] args) {

		// 本地调用
//		IRpcService service = new IRpcServiceImpl();
//		int result = service.add(8,2);
//		System.out.println(result);

		// 网络调用 涉及到一个【动态代理对象】

		IRpcService service = RpcProxy.create(IRpcService.class);
		int result = service.add(8, 2);
		System.out.println(result);


	}
}