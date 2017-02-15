package org.wso2.ballerina.tooling.service.workspace.launcher;

import com.google.gson.Gson;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.wso2.ballerina.tooling.service.workspace.launcher.dto.MessageDTO;

import java.io.PrintStream;

public class LaunchServer {

    /**
     *  Debug server initializer class
     */
    static class LaunchServerInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        public void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new HttpServerCodec());
            pipeline.addLast(new HttpObjectAggregator(65536));
            pipeline.addLast(new LaunchServerHandler());
        }
    }

    public void startServer() {
        //lets start the server in a new thread.
        Runnable run = new Runnable() {
            public void run() {
                LaunchServer.this.startListning();
            }
        };
        (new Thread(run)).start();
    }

    private void startListning() {
        int port = Integer.parseInt(LauncherConstants.LAUNCHER_PORT);
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    //todo enable log in debug mode
                    //.handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new LaunchServerInitializer());
            Channel ch = b.bind(port).sync().channel();

            ch.closeFuture().sync();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        } catch (InterruptedException e) {
            //@todo proper error handling
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * Push message to client.
     *
     * @param launchSession the launch session
     * @param status       the status
     */
    public void pushMessageToClient(LaunchSession launchSession, MessageDTO status) {
        Gson gson = new Gson();
        String json = gson.toJson(status);
        launchSession.getChannel().write(new TextWebSocketFrame(json));
        launchSession.getChannel().flush();
    }

}
