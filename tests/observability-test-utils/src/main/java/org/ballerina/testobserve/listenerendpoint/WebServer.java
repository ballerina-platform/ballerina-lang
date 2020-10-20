/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerina.testobserve.listenerendpoint;

import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.types.AttachedFunctionType;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.observability.ObservabilityConstants;
import io.ballerina.runtime.observability.ObserverContext;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static io.ballerina.runtime.observability.ObservabilityConstants.PROPERTY_TRACE_PROPERTIES;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_HTTP_METHOD;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_HTTP_URL;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_PROTOCOL;
import static org.ballerina.testobserve.listenerendpoint.Constants.CALLER_TYPE_NAME;
import static org.ballerina.testobserve.listenerendpoint.Constants.NETTY_CONTEXT_NATIVE_DATA_KEY;
import static org.ballerina.testobserve.listenerendpoint.Constants.TEST_OBSERVE_PACKAGE;

/**
 * Web Server used in the mock listener.
 */
public class WebServer {
    private static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";

    private final Map<String, Service> serviceMap = new ConcurrentHashMap<>();
    private final int port;
    private final EventLoopGroup loopGroup;
    private final Runtime runtime;
    private boolean isRunning;

    public WebServer(int port, Runtime runtime) {
        this.port = port;
        this.loopGroup = new NioEventLoopGroup();
        this.runtime = runtime;
    }

    /**
     * Attach a new service object to the listener.
     *
     * @param serviceObject The service object to be attached
     * @return The attached service
     */
    public Service addService(BObject serviceObject) {
        Service service = new Service(serviceObject);
        this.serviceMap.put(service.getServiceName(), service);
        return service;
    }

    /**
     * Remove an attached service from the listener.
     *
     * @param serviceObject The service object to be detached
     * @return The removed service
     */
    public Service removeService(BObject serviceObject) {
        if (this.isRunning) {
            throw new IllegalStateException("Service cannot be detached while the endpoint is active");
        }
        String serviceName = Utils.getServiceName(serviceObject);
        return this.serviceMap.remove(serviceName);
    }

    /**
     * Start up the web server and bind to the port specified while initializing.
     *
     * @throws InterruptedException if starting the server fails
     */
    public void start() throws InterruptedException {
        try {
            this.isRunning = true;
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            Channel channel = serverBootstrap
                    .group(this.loopGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast("decoder", new HttpRequestDecoder(4096, 8192, 8192, false));
                            pipeline.addLast("aggregator", new HttpObjectAggregator(100 * 1024 * 1024));
                            pipeline.addLast("encoder", new HttpResponseEncoder());
                            pipeline.addLast("handler", new WebServerInboundHandler(runtime, serviceMap));
                        }
                    })
                    .bind(this.port)
                    .sync()
                    .channel();
            Utils.logInfo("Started listener on port " + this.port);
            channel.closeFuture().sync();
        } finally {
            this.loopGroup.shutdownGracefully().sync();
            this.isRunning = false;
        }
    }

    /**
     * Shutdown the web server gracefully waiting for Netty to complete shutdown gracefully.
     *
     * @throws InterruptedException if shutting down fails
     */
    public void shutdownGracefully() throws InterruptedException {
        this.loopGroup.shutdownGracefully().sync();
        this.isRunning = false;
    }

    /**
     * Shutdown the Webserver immediately.
     *
     * @throws InterruptedException if shutting down fails
     */
    public void shutdownNow() throws InterruptedException {
        this.loopGroup.shutdownGracefully(0, 0, TimeUnit.SECONDS).sync();
        this.isRunning = false;
    }

    /**
     * Inbound message handler of the Web Server.
     */
    public static class WebServerInboundHandler extends SimpleChannelInboundHandler<Object> {
        private Runtime runtime;
        private Map<String, Service> serviceMap;

        public WebServerInboundHandler(Runtime runtime, Map<String, Service> serviceMap) {
            this.runtime = runtime;
            this.serviceMap = serviceMap;
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object o) {
            if (!(o instanceof FullHttpRequest)) {
                return;
            }
            final FullHttpRequest request = (FullHttpRequest) o;
            String[] requestUriSplit = request.uri().split("/");
            String serviceName = requestUriSplit[1];
            String resourceName = requestUriSplit[2];

            BObject callerObject = ValueCreator.createObjectValue(TEST_OBSERVE_PACKAGE, CALLER_TYPE_NAME);
            callerObject.addNativeData(NETTY_CONTEXT_NATIVE_DATA_KEY, ctx);

            // Preparing the arguments for dispatching the resource function
            BObject serviceObject = serviceMap.get(serviceName).getServiceObject();
            int paramCount = 0;
            for (AttachedFunctionType attachedFunction : serviceObject.getType().getAttachedFunctions()) {
                if (Objects.equals(attachedFunction.getName(), resourceName)) {
                    paramCount = attachedFunction.getParameterTypes().length;
                    break;
                }
            }
            Object[] args = new Object[paramCount * 2];
            if (paramCount >= 1) {
                args[0] = callerObject;
                args[1] = true;
            }
            if (paramCount >= 2 && request.method() == HttpMethod.POST) {
                String bodyContent = request.content().toString(StandardCharsets.UTF_8);
                args[2] = StringUtils.fromString(bodyContent);
                args[3] = true;
            }

            ObserverContext observerContext = new ObserverContext();
            observerContext.setObjectName("testobserve_listener");
            Map<String, String> httpHeaders = new HashMap<>();
            request.headers().forEach(entry -> httpHeaders.put(entry.getKey(), entry.getValue()));
            observerContext.addProperty(PROPERTY_TRACE_PROPERTIES, httpHeaders);
            observerContext.addMainTag(TAG_KEY_HTTP_METHOD, request.method().name());
            observerContext.addMainTag(TAG_KEY_PROTOCOL, "http");
            observerContext.addMainTag(TAG_KEY_HTTP_URL, request.uri());

            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put(ObservabilityConstants.KEY_OBSERVER_CONTEXT, observerContext);

            StrandMetadata strandMetadata = new StrandMetadata(TEST_OBSERVE_PACKAGE.getOrg(),
                    TEST_OBSERVE_PACKAGE.getName(), TEST_OBSERVE_PACKAGE.getVersion(), resourceName);
            Utils.logInfo("Dispatching resource function " + serviceName + "." + resourceName);
            runtime.invokeMethodAsync(serviceObject, resourceName, null, strandMetadata,
                    new WebServerCallableUnitCallback(ctx, serviceName, resourceName), properties, args);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            writeResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, cause.getMessage());
            Utils.logError("Exception occurred in web server %s", cause.getMessage());
            ctx.close();
        }

        /**
         * Callable unit used in executing ballerina resource function.
         */
        public static class WebServerCallableUnitCallback implements Callback {
            private final ChannelHandlerContext ctx;
            private final String resourceName;

            public WebServerCallableUnitCallback(ChannelHandlerContext ctx, String serviceName, String resourceName) {
                this.ctx = ctx;
                this.resourceName = serviceName + "." + resourceName;
            }

            @Override
            public void notifySuccess() {
                Utils.logInfo("Successfully executed resource " + this.resourceName);
            }

            @Override
            public void notifyFailure(BError error) {
                writeResponse(this.ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, error.getMessage());
                Utils.logError("Failed to execute resource " + this.resourceName + " "
                        + error.getPrintableStackTrace());
            }
        }
    }

    /**
     * Write a response to a Netty context issued by the webserver.
     * This may be invoked by the caller object or by the webserver itself on failures.
     *
     * @param ctx The netty context
     * @param status The HTTP status code to be used
     * @param content The body content to be written as the response
     */
    public static void writeResponse(ChannelHandlerContext ctx, HttpResponseStatus status, String content) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = Unpooled.wrappedBuffer(bytes);

        // Build the response object.
        final FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, buf, false);
        final ZonedDateTime dateTime = ZonedDateTime.now();
        final DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
        final DefaultHttpHeaders headers = (DefaultHttpHeaders) response.headers();
        headers.set(HttpHeaderNames.SERVER, "web-server");
        headers.set(HttpHeaderNames.DATE, dateTime.format(formatter));
        headers.set(HttpHeaderNames.CONTENT_TYPE, JSON_CONTENT_TYPE);
        headers.set(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(bytes.length));

        // Close the non-keep-alive connection after the write operation is done.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
