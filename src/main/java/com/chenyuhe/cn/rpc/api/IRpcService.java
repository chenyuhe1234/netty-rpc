package com.chenyuhe.cn.rpc.api;

public interface IRpcService {

	/**
	 * 加
	 * @param a
	 * @param b
	 * @return
	 */
	int add(int a,int b);

	/**
	 * 减
	 * @param a
	 * @param b
	 * @return
	 */
	int sub(int a,int b);

	/**
	 * 乘
	 * @param a
	 * @param b
	 * @return
	 */
	int mult(int a,int b);

	/**
	 * 除
	 * @param a
	 * @param b
	 * @return
	 */
	int div(int a,int b);


}
