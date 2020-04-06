package com.chenyuhe.cn.rpc.provider;

import com.chenyuhe.cn.rpc.api.RpcHelloService;

public class RpcHelloServieImpl implements RpcHelloService {





	@Override
	public String sayHello(String str) {
		return "Hello" + str;
	}
}