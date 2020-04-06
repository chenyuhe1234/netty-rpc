package com.chenyuhe.cn.rpc.provider;

import com.chenyuhe.cn.rpc.api.IRpcService;

public class IRpcServiceImpl implements IRpcService {
	@Override
	public int add(int a, int b) {
		return a + b;
	}

	@Override
	public int sub(int a, int b) {
		return a - b;
	}

	@Override
	public int mult(int a, int b) {
		return a * b;
	}

	@Override
	public int div(int a, int b) {
		return a / b;
	}
}