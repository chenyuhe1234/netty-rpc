package com.chenyuhe.cn.rpc.consumer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ConsumerHandler extends ChannelInboundHandlerAdapter {

	private Object response;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		response = msg;
	}

	/**
	 * 异常的处理逻辑
	 *
	 * @param ctx
	 * @param cause
	 * @throws Exception
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {


		cause.printStackTrace();
		System.out.println("client is exception... ");
	}

	public Object getResponse() {
		return response;
	}
}