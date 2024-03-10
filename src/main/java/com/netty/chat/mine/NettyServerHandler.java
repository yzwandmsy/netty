package com.netty.chat.mine;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义Handler需要继承netty规定好的某个HandlerAdapter(规范)
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    //GlobalEventExecutor.INSTANCE是全局的事件执行器，是一个单例
    private static final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private static final Map<ChannelId, String> channelNameMap = new ConcurrentHashMap<>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 客户端连接后添加到channelGroup中， 并通知各个客户端
        channelGroup.writeAndFlush(Unpooled.copiedBuffer(("[系统消息]有新的用户 " + ctx.channel().remoteAddress() +" 加入聊天室: " + sdf.format(new java.util.Date()) + "\r\n").getBytes(StandardCharsets.UTF_8)));
        channelGroup.add(ctx.channel());
        channelNameMap.put(ctx.channel().id(), ctx.channel().remoteAddress().toString());
    }

    /**
     * 读取客户端发送的数据
     *
     * @param ctx 上下文对象, 含有通道channel，管道pipeline
     * @param msg 就是客户端发送的数据
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("服务器读取线程 " + Thread.currentThread().getName());
        //Channel channel = ctx.channel();
        //ChannelPipeline pipeline = ctx.pipeline(); //本质是一个双向链接, 出站入站
        //将 msg 转成一个 ByteBuf，类似NIO 的 ByteBuffer
        ByteBuf buf = (ByteBuf) msg;

        Channel ownerChannel = ctx.channel();
        ChannelGroup channelGroup = NettyServerHandler.channelGroup;

        String channelSendMsg = buf.toString(CharsetUtil.UTF_8);

        if (channelSendMsg.startsWith("my name:")) {
            channelNameMap.put(ownerChannel.id(), channelSendMsg.split("my name:")[1]);
        }


        channelGroup.forEach(channel -> {

            if (channel != ownerChannel) {
                //将 msg 转成一个 ByteBuf，类似NIO 的 ByteBuffer
                channel.writeAndFlush(Unpooled.copiedBuffer(("[客户端 " + channelNameMap.get(ownerChannel.id()) +" 消息]" + buf.toString(CharsetUtil.UTF_8) + "\r\n").getBytes(StandardCharsets.UTF_8)));
            } else {
                channel.writeAndFlush(Unpooled.copiedBuffer(("[自己 " + channelNameMap.get(ownerChannel.id()) + " ] 发送消息是:" + buf.toString(CharsetUtil.UTF_8)).getBytes(StandardCharsets.UTF_8)));
            }

        });

        System.out.println("客户端" + ownerChannel.localAddress() + "， 发送消息是:" + buf.toString(CharsetUtil.UTF_8));
    }

    /**
     * channel断开连接触发事件
     *
     * @param ctx 上下文对象, 含有通道channel，管道pipeline
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        Channel channel = ctx.channel();
        channelGroup.remove(channel);

        channelGroup.writeAndFlush(Unpooled.copiedBuffer(("客户端" + channelNameMap.get(channel.id()) + "， 已经离开聊天室").getBytes(StandardCharsets.UTF_8)));

        System.out.println("客户端" + channel.remoteAddress() + "， 已经离开聊天室");


    }


    /**
     * 处理异常, 一般是需要关闭通道
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}