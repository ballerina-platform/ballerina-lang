package org.wso2.carbon.transport.http.netty.util.client.http;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;

import java.net.InetSocketAddress;
import java.net.URI;

/**
 * Test HTTP netty client which close the connection once the request is sent.
 */
public class HttpClient {

    private final String host;
    private final int port;
    private final String rawPath;
    private final String path;
    private final String method;

    public HttpClient(URI baseURI, String path, String method) {
        this.host = baseURI.getHost();
        this.port = baseURI.getPort();
        this.rawPath = baseURI.getRawPath();
        this.path = path;
        this.method = method;
    }

    public void createAndSendRequest() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(host, port))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new HttpClientHandler());
                        }
                    });

            // Make the connection attempt.
            Channel ch = b.connect(host, port).sync().channel();

            // Prepare the HTTP request.
            HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
            request.headers().set(HttpHeaderNames.HOST, host);
//            request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
//            request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);

            // Send the HTTP request.
            ch.writeAndFlush(request);

            // Wait for the server to close the connection.
            ch.closeFuture().sync();
//            ch.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Shut down executor threads to exit.
            group.shutdownGracefully();
        }
    }


}
