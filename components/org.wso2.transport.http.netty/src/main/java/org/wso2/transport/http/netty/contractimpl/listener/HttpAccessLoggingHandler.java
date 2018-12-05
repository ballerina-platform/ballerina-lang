/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.transport.http.netty.contractimpl.listener;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.wso2.transport.http.netty.contract.Constants;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Calendar;

/**
 * Logging handler for HTTP access logs.
 */
public class HttpAccessLoggingHandler extends LoggingHandler {
    private static final LogLevel LOG_LEVEL = LogLevel.INFO;
    private static final String EVENT_WRITE = "WRITE";
    private String inetAddress;
    private String method;
    private String uri;
    private String protocol;
    private String userAgent = "-";
    private String referrer = "-";
    private int status;
    private Long contentLength = 0L;
    private Calendar calendar;

    public HttpAccessLoggingHandler(String name) {
        super(name, LOG_LEVEL);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        SocketAddress address = ctx.channel().remoteAddress();
        if (address instanceof InetSocketAddress) {
            inetAddress = ((InetSocketAddress) address).getAddress().toString();
        }
        if (inetAddress.startsWith("/")) {
            inetAddress = inetAddress.substring(1);
        }
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelInactive();
        inetAddress = "-";
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) msg;
            calendar = Calendar.getInstance();
            // maybe this request was proxied or load balanced.
            // try and get the real originating IP
            if (httpRequest.headers().contains(Constants.HTTP_X_FORWARDED_FOR)) {
                // can contain multiple IPs for proxy chains. the first ip is our client.
                String proxyChain = httpRequest.headers().get(Constants.HTTP_X_FORWARDED_FOR);
                int firstComma = proxyChain.indexOf(',');
                if (firstComma != -1) {
                    inetAddress = proxyChain.substring(0, proxyChain.indexOf(','));
                } else {
                    inetAddress = proxyChain;
                }
            }
            if (httpRequest.headers().contains(HttpHeaderNames.USER_AGENT)) {
                userAgent = httpRequest.headers().get(HttpHeaderNames.USER_AGENT);
            }
            if (httpRequest.headers().contains(HttpHeaderNames.REFERER)) {
                referrer = httpRequest.headers().get(HttpHeaderNames.REFERER);
            }
            method = httpRequest.method().name();
            uri = httpRequest.uri();
            protocol = httpRequest.protocolVersion().toString();
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ctx.write(msg, promise);
        if (msg instanceof HttpResponse) {
            HttpResponse httpResponse = (HttpResponse) msg;
            status = httpResponse.status().code();
            if (httpResponse.headers().contains(HttpHeaderNames.CONTENT_LENGTH)) {
                contentLength = Long.valueOf(httpResponse.headers().get(HttpHeaderNames.CONTENT_LENGTH));
                if (logger.isEnabled(internalLevel)) {
                    logger.log(internalLevel, format(ctx, EVENT_WRITE, msg));
                }
                clearState();
            }
        } else if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;
            contentLength += httpContent.content().readableBytes();
            if (msg instanceof LastHttpContent) {
                if (logger.isEnabled(internalLevel)) {
                    logger.log(internalLevel, format(ctx, EVENT_WRITE, msg));
                }
                clearState();
            }
        }
    }

    @Override
    protected String format(ChannelHandlerContext ctx, String eventName, Object arg) {
        if (EVENT_WRITE.equals(eventName)) {
            return String.format(Constants.ACCESS_LOG_FORMAT, inetAddress, calendar, method, uri, protocol, status,
                                 contentLength, referrer, userAgent);
        }
        return "";
    }

    private void clearState() {
        method = null;
        uri = null;
        protocol = null;
        status = -1;
        contentLength = 0L;
        calendar = null;
        userAgent = "-";
        referrer = "-";
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelRegistered();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelUnregistered();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.fireExceptionCaught(cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        ctx.bind(localAddress, promise);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
                        ChannelPromise promise) throws Exception {
        ctx.connect(remoteAddress, localAddress, promise);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.disconnect(promise);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.close(promise);
    }

    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.deregister(promise);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelReadComplete();
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelWritabilityChanged();
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
