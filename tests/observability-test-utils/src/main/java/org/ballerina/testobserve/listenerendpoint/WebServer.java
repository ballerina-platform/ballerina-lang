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
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ResourceMethodType;
import io.ballerina.runtime.api.types.ServiceType;
import io.ballerina.runtime.api.utils.StringUtils;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

    private final Map<String, Resource> resourceMap = new ConcurrentHashMap<>();
    private final int port;
    private final EventLoopGroup loopGroup;
    private final Runtime runtime;

    public WebServer(int port, Runtime runtime) {
        this.port = port;
        this.loopGroup = new NioEventLoopGroup();
        this.runtime = runtime;
    }

    /**
     * Attach a new service object to the listener.
     *
     * @param serviceObject The service object to be attached
     * @param basePath The base path of the service
     */
    public void addService(BObject serviceObject, String basePath) {
        ResourceMethodType[] resourceFunctions = ((ServiceType) serviceObject.getType()).getResourceMethods();
        for (ResourceMethodType resourceMethodType : resourceFunctions) {
            Resource resource = new Resource(serviceObject, resourceMethodType, basePath);
            String resourceMapKey = generateResourceMapKey(resource.getAccessor(), resource.getResourcePath());
            if (this.resourceMap.containsKey(resourceMapKey)) {
                throw new IllegalArgumentException("Unable to register service with duplicate resource path");
            }
            this.resourceMap.put(resourceMapKey, resource);
            Utils.logInfo("Registered resource path %s", resourceMapKey);
        }
    }

    /**
     * Remove an attached service from the listener.
     *
     * @param serviceObject The service object to be detached
     */
    public void removeService(BObject serviceObject) {
        List<String> resourcesToBeRemoved = new ArrayList<>();
        for (Resource resource : this.resourceMap.values()) {
            if (Objects.equals(serviceObject, resource.getServiceObject())) {
                String resourceMapKey = generateResourceMapKey(resource.getAccessor(), resource.getResourcePath());
                resourcesToBeRemoved.add(resourceMapKey);
            }
        }
        for (String resourcePath : resourcesToBeRemoved) {
            this.resourceMap.remove(resourcePath);
            Utils.logInfo("Removed resource path %s", resourcePath);
        }
    }

    /**
     * Start up the web server and bind to the port specified while initializing.
     *
     * @throws InterruptedException if starting the server fails
     */
    public void start() throws InterruptedException {
        try {
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
                            pipeline.addLast("handler", new WebServerInboundHandler(runtime, resourceMap));
                        }
                    })
                    .bind(this.port)
                    .sync()
                    .channel();
            Utils.logInfo("Started listener on port " + this.port);
            channel.closeFuture().sync();
        } finally {
            this.loopGroup.shutdownGracefully().sync();
        }
    }

    /**
     * Shutdown the web server gracefully waiting for Netty to complete shutdown gracefully.
     *
     * @throws InterruptedException if shutting down fails
     */
    public void shutdownGracefully() throws InterruptedException {
        Utils.logInfo("Shutting down Web Server with port " + port + " gracefully");
        this.loopGroup.shutdownGracefully().sync();
        Utils.logInfo("Shutting down Web Server with port " + port + " gracefully complete");
    }

    /**
     * Shutdown the Webserver immediately.
     *
     * @throws InterruptedException if shutting down fails
     */
    public void shutdownNow() throws InterruptedException {
        Utils.logInfo("Shutting down Web Server with port " + port + " immediately");
        this.loopGroup.shutdownGracefully(0, 0, TimeUnit.SECONDS).sync();
        Utils.logInfo("Shutting down Web Server with port " + port + " immediately complete");
    }

    /**
     * Inbound message handler of the Web Server.
     */
    public static class WebServerInboundHandler extends SimpleChannelInboundHandler<Object> {
        private final Runtime runtime;
        private final Map<String, Resource> resourceMap;

        public WebServerInboundHandler(Runtime runtime, Map<String, Resource> resourceMap) {
            this.runtime = runtime;
            this.resourceMap = resourceMap;
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
            String httpMethod = request.method().name();
            String resourcePath = Utils.normalizeResourcePath(request.uri());
            String resourceMapKey = generateResourceMapKey(httpMethod, resourcePath);

            BObject callerObject = ValueCreator.createObjectValue(TEST_OBSERVE_PACKAGE, CALLER_TYPE_NAME);
            callerObject.addNativeData(NETTY_CONTEXT_NATIVE_DATA_KEY, ctx);

            // Preparing the arguments for dispatching the resource function
            Resource resource = this.resourceMap.get(resourceMapKey);
            if (resource == null) {
                writeResponse(ctx, HttpResponseStatus.NOT_FOUND, "resource " + resourcePath + " not found");
                return;
            }
            BObject serviceObject = resource.getServiceObject();
            String resourceFunctionName = resource.getResourceFunctionName();
            int paramCount = resource.getParamTypes().length;
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
            observerContext.addTag(TAG_KEY_HTTP_METHOD, request.method().name());
            observerContext.addTag(TAG_KEY_PROTOCOL, "http");
            observerContext.addTag(TAG_KEY_HTTP_URL, request.uri());

            Map<String, Object> properties = new HashMap<>();
            properties.put(ObservabilityConstants.KEY_OBSERVER_CONTEXT, observerContext);

            StrandMetadata strandMetadata = new StrandMetadata(TEST_OBSERVE_PACKAGE.getOrg(),
                    TEST_OBSERVE_PACKAGE.getName(), TEST_OBSERVE_PACKAGE.getMajorVersion(),
                    resourceFunctionName);
            Utils.logInfo("Dispatching resource " + resourcePath);
            runtime.invokeMethodAsyncConcurrently(serviceObject, resourceFunctionName, null, strandMetadata,
                                                  new WebServerCallableUnitCallback(ctx, resourcePath), properties,
                                                  resource.getReturnType(), args);
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

            public WebServerCallableUnitCallback(ChannelHandlerContext ctx, String resourcePath) {
                this.ctx = ctx;
                this.resourceName = resourcePath;
            }

            @Override
            public void notifySuccess(Object result) {
                if (result instanceof BError) {
                    notifyFailure(((BError) result));
                } else {
                    Utils.logInfo("Successfully executed resource " + this.resourceName);
                }
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

    /**
     * Generate key for the resource map.
     *
     * @param accessor Ballerina resource function accessor or HTTP Method
     * @param resourcePath Ballerina resource function full resource path or HTTP path
     * @return The key for resource map
     */
    public static String generateResourceMapKey(String accessor, String resourcePath) {
        return accessor.toLowerCase(Locale.ENGLISH) + " " + resourcePath.toLowerCase(Locale.ENGLISH);
    }
}
