package com.chenyuhe.cn.rpc.protocol;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 协议 也就是定义数据传输的规范
 */
public class InvokerProtocol implements Serializable {


	// 这里面需要定义写啥能

	// 你需要调哪个类 哪个方法  传什么样的参数 方法的形参列表

	/**
	 * 类名
	 */
	private String className;


	/**
	 * 方法名
	 */
	private String methodName;


	/**
	 * 方法的形参列表
	 */
	private Class<?> [] params;


	/**
	 * 实参列表
	 */
	private Object[] values;


	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Class<?>[] getParams() {
		return params;
	}

	public void setParams(Class<?>[] params) {
		this.params = params;
	}

	public Object[] getValues() {
		return values;
	}

	public void setValues(Object[] values) {
		this.values = values;
	}


	@Override
	public String toString() {
		return "InvokerProtocol{" +
				"className='" + className + '\'' +
				", methodName='" + methodName + '\'' +
				", params=" + Arrays.toString(params) +
				", values=" + Arrays.toString(values) +
				'}';
	}
}