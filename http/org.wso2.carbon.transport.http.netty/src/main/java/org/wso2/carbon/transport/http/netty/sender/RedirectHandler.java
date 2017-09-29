/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wso2.carbon.transport.http.netty.sender;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.common.Util;
import org.wso2.carbon.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class responsible for handling redirects for client connector.
 */
public class RedirectHandler extends ChannelInboundHandlerAdapter {

    protected static final Logger LOG = LoggerFactory.getLogger(RedirectHandler.class);
    private Map<String, String> redirectState = null;
    private boolean isRedirect = false;
    private boolean isCrossDoamin = true;
    private ChannelHandlerContext originalChannelContext;
    private HTTPCarbonMessage originalRequest;

    public RedirectHandler(ChannelHandlerContext originalChannelContext) {
        this.originalChannelContext = originalChannelContext;
    }

    public RedirectHandler() {
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
            HTTPTransportContextHolder.getInstance().getHandlerExecutor()
                    .executeAtTargetConnectionInitiation(Integer.toString(ctx.hashCode()));
        }

        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        /* Preserve original context, as it is needed to write the final response back to the original channel when
        multiple redirections are involved with different channels.*/
        if (originalChannelContext == null) {
            originalChannelContext = ctx;
        }

        if (msg instanceof HttpResponse) {
            try {
                originalRequest = ctx.channel().attr(Constants.ORIGINAL_REQUEST).get();
                redirectState = getRedirectState((HttpResponse) msg, originalRequest);

                boolean isRedirectEligible = redirectState != null && !redirectState.isEmpty()
                        && redirectState.get(Constants.LOCATION) != null
                        && redirectState.get(Constants.HTTP_METHOD) != null;

                if (isRedirectEligible) {
                    isCrossDoamin = isCrossDomain((HttpResponse) msg, originalRequest);

                    AtomicInteger redirectCount = originalChannelContext.channel().attr(Constants.REDIRECT_COUNT).get();
                    if (redirectCount != null && redirectCount.intValue() != 0) {
                        redirectCount.getAndIncrement();
                    } else {
                        redirectCount = new AtomicInteger(1);
                    }
                    originalChannelContext.channel().attr(Constants.REDIRECT_COUNT).set(redirectCount);

                    if (redirectCount.intValue() < 4) {
                        System.out.println("redirecting header..." + redirectCount);
                        isRedirect = true;
                    } else {
                        isRedirect = false;
                        originalChannelContext.fireChannelRead(msg);
                    }
                } else {
                    isRedirect = false;
                    originalChannelContext.fireChannelRead(msg);
                }
            } catch (UnsupportedEncodingException e) {
                originalChannelContext.fireExceptionCaught(e);
            }
        } else {
            if (msg instanceof LastHttpContent) {
                if (isRedirect) {
                    try {
                        URL locationUrl = new URL(redirectState.get(Constants.LOCATION));
                        HTTPCarbonMessage httpCarbonRequest = createHttpCarbonRequest();
                        Util.setupTransferEncodingForRequest(httpCarbonRequest);
                        HttpRequest httpRequest = Util.createHttpRequest(httpCarbonRequest);

                        if (isCrossDoamin) {
                            writeContentToNewChannel(locationUrl, httpCarbonRequest, httpRequest);
                        } else {
                            ctx.write(httpRequest);
                            ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                        }
                    } catch (MalformedURLException e) {
                        originalChannelContext.fireExceptionCaught(e);
                    } catch (Exception e) {
                        originalChannelContext.fireExceptionCaught(e);
                    }

                } else {
                    originalChannelContext.fireChannelRead(msg);
                }
            } else {
                if (!isRedirect) {
                    originalChannelContext.fireChannelRead(msg);
                }
            }
        }
    }

    /**
     * Check whether the response is redirection eligible and if yes, get url from the location header of the response
     * and decide what http method should be used for the redirection.
     *
     * @param msg             Response message
     * @param originalRequest Original request
     * @return a map with location information and http method to be used for redirection
     * @throws UnsupportedEncodingException
     */
    private Map<String, String> getRedirectState(HttpResponse msg, HTTPCarbonMessage originalRequest)
            throws UnsupportedEncodingException {
        Map<String, String> redirectState = new HashMap<String, String>();
        HttpResponseStatus status = msg.status();
        String originalRequestMethod =
                originalRequest != null ? (String) originalRequest.getProperty(Constants.HTTP_METHOD) : null;

        switch (status.code()) {
        case 300:
        case 307:
        case 308:
        case 305:
            if (Constants.HTTP_GET_METHOD.equals(originalRequestMethod) || Constants.HTTP_HEAD_METHOD
                    .equals(originalRequestMethod)) {
                redirectState.put(Constants.HTTP_METHOD, originalRequestMethod);
                redirectState.put(Constants.LOCATION, getLocation(msg, originalRequest));
                return redirectState;
            }
        case 301:
        case 302:
            if (Constants.HTTP_GET_METHOD.equals(originalRequestMethod) || Constants.HTTP_HEAD_METHOD
                    .equals(originalRequestMethod)) {
                redirectState.put(Constants.HTTP_METHOD, Constants.HTTP_GET_METHOD);
                redirectState.put(Constants.LOCATION, getLocation(msg, originalRequest));
                return redirectState;
            }
        case 303:
            redirectState.put(Constants.HTTP_METHOD, Constants.HTTP_GET_METHOD);
            redirectState.put(Constants.LOCATION, getLocation(msg, originalRequest));
            return redirectState;
        default:
            return null;
        }
    }

    /**
     * @param msg
     * @param originalRequest
     * @return
     * @throws UnsupportedEncodingException
     */
    private String getLocation(HttpResponse msg, HTTPCarbonMessage originalRequest)
            throws UnsupportedEncodingException {

        String location = URLDecoder.decode(msg.headers().get(HttpHeaderNames.LOCATION), Constants.UTF8);
        if (location != null) {
            if (location.toLowerCase().startsWith(Constants.HTTP_SCHEME + Constants.URL_AUTHORITY) || location
                    .toLowerCase().startsWith(Constants.HTTPS_SCHEME + Constants.URL_AUTHORITY)) {
                return location;
            } else {

                String requestPath =
                        originalRequest != null ? (String) originalRequest.getProperty(Constants.TO) : null;
                String protocol =
                        originalRequest != null ? (String) originalRequest.getProperty(Constants.PROTOCOL) : null;
                String host = originalRequest != null ? (String) originalRequest.getProperty(Constants.HOST) : null;
                int port = originalRequest != null ?
                        originalRequest.getProperty(Constants.PORT) != null ?
                                (Integer) originalRequest.getProperty(Constants.PORT) :
                                null :
                        null;

                String newPath = requestPath == null ? "/" : URLDecoder.decode(requestPath, Constants.UTF8);
                if (location.startsWith("/")) {
                    newPath = location;
                } else if (newPath.endsWith("/")) {
                    newPath += location;
                } else {
                    newPath += "/" + location;
                }
                StringBuilder newLocation = new StringBuilder(protocol);
                newLocation.append(Constants.URL_AUTHORITY).append(host);
                if (Constants.DEFAULT_HTTP_PORT != port) {
                    newLocation.append(":").append(port);
                }
                if (newPath.charAt(0) != '/') {
                    newLocation.append('/');
                }
                newLocation.append(newPath);
                return newLocation.toString();
            }
        }
        return null;
    }

    /**
     * Check whether the location indicates a cross domain.
     *
     * @param msg             Response message
     * @param originalRequest Original request
     * @return a boolean to denote whether the location is cross domain or not
     * @throws UnsupportedEncodingException
     */
    private boolean isCrossDomain(HttpResponse msg, HTTPCarbonMessage originalRequest)
            throws UnsupportedEncodingException {

        String location = URLDecoder.decode(msg.headers().get(HttpHeaderNames.LOCATION), Constants.UTF8);
        if (location != null && (location.toLowerCase().startsWith(Constants.HTTP_SCHEME + Constants.URL_AUTHORITY) ||
                location.toLowerCase().startsWith(Constants.HTTPS_SCHEME + Constants.URL_AUTHORITY))) {
            try {
                URL locationUrl = new URL(location);
                String protocol =
                        originalRequest != null ? (String) originalRequest.getProperty(Constants.PROTOCOL) : null;
                String host = originalRequest != null ? (String) originalRequest.getProperty(Constants.HOST) : null;
                String port = originalRequest != null ?
                        originalRequest.getProperty(Constants.PORT) != null ?
                                Integer.toString((Integer) originalRequest.getProperty(Constants.PORT)) : null : null;

                if (locationUrl.getProtocol().equals(protocol) && locationUrl.getHost().equals(host)
                        && locationUrl.getPort() == Integer.parseInt(port)) {
                    return false;
                } else {
                    return true;
                }

            } catch (MalformedURLException e) {
                originalChannelContext.fireExceptionCaught(e);
            }
        }
        return false;
    }

    /**
     *
     * @return
     * @throws MalformedURLException
     */
    private HTTPCarbonMessage createHttpCarbonRequest() throws MalformedURLException {
        URL locationUrl = new URL(redirectState.get(Constants.LOCATION));

        HTTPCarbonMessage httpCarbonRequest = new HTTPCarbonMessage();
        httpCarbonRequest.setProperty(Constants.PORT, locationUrl.getPort());
        httpCarbonRequest.setProperty(Constants.PROTOCOL, locationUrl.getProtocol());
        httpCarbonRequest.setProperty(Constants.HOST, locationUrl.getHost());
        httpCarbonRequest.setProperty(Constants.HTTP_METHOD, redirectState.get(Constants.HTTP_METHOD));
        httpCarbonRequest.setProperty(Constants.REQUEST_URL, locationUrl.getPath());
        httpCarbonRequest.setProperty(Constants.TO, locationUrl.getPath());

        httpCarbonRequest.setHeader(Constants.HTTP_HOST, locationUrl.getHost());
        httpCarbonRequest.setHeader(Constants.HTTP_PORT, Integer.toString(locationUrl.getPort()));
        httpCarbonRequest.setEndOfMsgAdded(true);
        return httpCarbonRequest;
    }

    /**
     *
     * @param locationUrl
     * @param httpCarbonRequest
     * @param httpRequest
     */
    private void writeContentToNewChannel(URL locationUrl, HTTPCarbonMessage httpCarbonRequest,
            HttpRequest httpRequest) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap clientBootstrap = new Bootstrap();
            clientBootstrap.group(group).channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(locationUrl.getHost(), locationUrl.getPort()))
                    .handler(new RedirectChannelInitializer(null, false, originalChannelContext));
            ChannelFuture channelFuture = clientBootstrap.connect();
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess() && future.isDone()) {
                        future.channel().attr(Constants.ORIGINAL_REQUEST).set(httpCarbonRequest);
                        future.channel().write(httpRequest);
                        future.channel().writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                    } else {
                        future.cause().printStackTrace();
                    }
                }
            });

        } finally {
            //   group.shutdownGracefully().sync();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (ctx != null && ctx.channel().isActive()) {
            originalChannelContext.fireExceptionCaught(cause);
            ctx.close();
        }
    }
}
