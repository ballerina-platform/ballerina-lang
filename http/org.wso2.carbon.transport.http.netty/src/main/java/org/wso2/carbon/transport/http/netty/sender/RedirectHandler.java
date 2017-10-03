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
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpVersion;
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
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.net.ssl.SSLEngine;

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
    private SSLEngine sslEngine;
    private boolean httpTraceLogEnabled;
    private int maxRedirectCount;

    public RedirectHandler(ChannelHandlerContext originalChannelContext, SSLEngine sslEngine,
            boolean httpTraceLogEnabled, int maxRedirectCount) {
        this.originalChannelContext = originalChannelContext;
        this.sslEngine = sslEngine;
        this.httpTraceLogEnabled = httpTraceLogEnabled;
        this.maxRedirectCount = maxRedirectCount;
    }

    public RedirectHandler(SSLEngine sslEngine, boolean httpTraceLogEnabled, int maxRedirectCount) {
        this.sslEngine = sslEngine;
        this.httpTraceLogEnabled = httpTraceLogEnabled;
        this.maxRedirectCount = maxRedirectCount;
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
            handleRedirectState(ctx, (HttpResponse) msg);
        } else {
            /* Actual redirection happens only when the full response for the previous request
            has been received */
            if (msg instanceof LastHttpContent) {
                if (isRedirect) {
                    try {
                        URL locationUrl = new URL(redirectState.get(Constants.LOCATION));
                        HTTPCarbonMessage httpCarbonRequest = createHttpCarbonRequest();
                        Util.setupTransferEncodingForRequest(httpCarbonRequest);
                        HttpRequest httpRequest = Util.createHttpRequest(httpCarbonRequest);

                        if (isCrossDoamin) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Send redirect request using a new channel");
                            }
                            writeContentToNewChannel(locationUrl, httpCarbonRequest, httpRequest);
                        } else {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Use existing channel to send the redirect request");
                            }
                            ctx.write(httpRequest);
                            ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                        }
                    } catch (MalformedURLException e) {
                        LOG.error("Error occurred when parsing redirect url", e);
                        originalChannelContext.fireExceptionCaught(e);
                    } catch (Exception e) {
                        LOG.error("Error occurred during redirection", e);
                        originalChannelContext.fireExceptionCaught(e);
                    }

                } else {
                    originalChannelContext.fireChannelRead(msg);
                }
            } else {
                //Content is still flowing through the channel and when it's not a redirect, pass it to the next handler
                if (!isRedirect) {
                    originalChannelContext.fireChannelRead(msg);
                }
            }
        }
    }

    /**
     * Handle redirect state for the channel.
     *
     * @param ctx ChannelHandler context
     * @param msg Response message
     */
    private void handleRedirectState(ChannelHandlerContext ctx, HttpResponse msg) {
        try {
            originalRequest = ctx.channel().attr(Constants.ORIGINAL_REQUEST).get();
            String location = msg.headers().get(HttpHeaderNames.LOCATION) != null ?
                    URLDecoder.decode(msg.headers().get(HttpHeaderNames.LOCATION), Constants.UTF8) :
                    null;
            int statusCode = msg.getStatus().code();
            if (location != null) {
                redirectState = getRedirectState(location, statusCode, originalRequest);
            } else {
                redirectState = null;
            }

            boolean isRedirectEligible =
                    redirectState != null && !redirectState.isEmpty() && redirectState.get(Constants.LOCATION) != null
                            && redirectState.get(Constants.HTTP_METHOD) != null;

            if (isRedirectEligible) {
                isCrossDoamin = isCrossDomain(location, originalRequest);

                AtomicInteger redirectCount = originalChannelContext.channel().attr(Constants.REDIRECT_COUNT).get();
                if (redirectCount != null && redirectCount.intValue() != 0) {
                    redirectCount.getAndIncrement();
                } else {
                    redirectCount = new AtomicInteger(1);
                }
                originalChannelContext.channel().attr(Constants.REDIRECT_COUNT).set(redirectCount);

                if (redirectCount.intValue() < maxRedirectCount) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Redirection required");
                    }
                    isRedirect = true;
                } else {
                    //Fire next handler in the original channel
                    isRedirect = false;
                    originalChannelContext.fireChannelRead(msg);
                }
            } else {
                //Fire next handler in the original channel
                isRedirect = false;
                originalChannelContext.fireChannelRead(msg);
            }
        } catch (UnsupportedEncodingException e) {
            LOG.error("Error occurred when deciding whether a redirection is required", e);
            originalChannelContext.fireExceptionCaught(e);
        }
    }

    /**
     * Check whether the response is redirection eligible and if yes, get url from the location header of the response
     * and decide what http method should be used for the redirection.
     *
     * @param location        value of location header
     * @param originalRequest Original request
     * @return a map with location information and http method to be used for redirection
     * @throws UnsupportedEncodingException
     */
    private Map<String, String> getRedirectState(String location, int statusCode, HTTPCarbonMessage originalRequest)
            throws UnsupportedEncodingException {
        Map<String, String> redirectState = new HashMap<String, String>();
        String originalRequestMethod =
                originalRequest != null ? (String) originalRequest.getProperty(Constants.HTTP_METHOD) : null;

        switch (statusCode) {
        case 300:
        case 307:
        case 308:
        case 305:
            if (Constants.HTTP_GET_METHOD.equals(originalRequestMethod) || Constants.HTTP_HEAD_METHOD
                    .equals(originalRequestMethod)) {
                redirectState.put(Constants.HTTP_METHOD, originalRequestMethod);
                redirectState.put(Constants.LOCATION, getLocation(location, originalRequest));
            }
            break;
        case 301:
        case 302:
            if (Constants.HTTP_GET_METHOD.equals(originalRequestMethod) || Constants.HTTP_HEAD_METHOD
                    .equals(originalRequestMethod)) {
                redirectState.put(Constants.HTTP_METHOD, Constants.HTTP_GET_METHOD);
                redirectState.put(Constants.LOCATION, getLocation(location, originalRequest));
            }
            break;
        case 303:
            redirectState.put(Constants.HTTP_METHOD, Constants.HTTP_GET_METHOD);
            redirectState.put(Constants.LOCATION, getLocation(location, originalRequest));
            break;
        default:
            return null;
        }

        return redirectState;
    }

    /**
     * Build redirect url from the location header value.
     *
     * @param location        value of location header
     * @param originalRequest Original request
     * @return a string that holds redirect url
     * @throws UnsupportedEncodingException
     */
    private String getLocation(String location, HTTPCarbonMessage originalRequest) throws UnsupportedEncodingException {

        if (location != null) {
            //if location url starts either with http ot https that means an absolute path has been set in header
            if (!isRelativePath(location)) {
                return location;
            } else {
                //Use relative path to build redirect url
                String requestPath =
                        originalRequest != null ? (String) originalRequest.getProperty(Constants.TO) : null;
                String protocol = originalRequest != null ?
                        (String) originalRequest.getProperty(Constants.PROTOCOL) :
                        Constants.HTTP_SCHEME;
                String host = originalRequest != null ? (String) originalRequest.getProperty(Constants.HOST) : null;
                if (host == null) {
                    return null;
                }
                Integer port = originalRequest != null ?
                        originalRequest.getProperty(Constants.PORT) != null ?
                                (Integer) originalRequest.getProperty(Constants.PORT) :
                                Constants.DEFAULT_HTTP_PORT :
                        Constants.DEFAULT_HTTP_PORT;

                String newPath =
                        requestPath == null ? Constants.FORWRD_SLASH : URLDecoder.decode(requestPath, Constants.UTF8);
                if (location.startsWith(Constants.FORWRD_SLASH)) {
                    newPath = location;
                } else if (newPath.endsWith(Constants.FORWRD_SLASH)) {
                    newPath += location;
                } else {
                    newPath += Constants.FORWRD_SLASH + location;
                }
                StringBuilder newLocation = new StringBuilder(protocol);
                newLocation.append(Constants.URL_AUTHORITY).append(host);
                if (Constants.DEFAULT_HTTP_PORT != port) {
                    newLocation.append(Constants.COLON).append(port);
                }
                if (newPath.charAt(0) != Constants.FORWRD_SLASH.charAt(0)) {
                    newLocation.append(Constants.FORWRD_SLASH.charAt(0));
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
     * @param location        Response message
     * @param originalRequest Original request
     * @return a boolean to denote whether the location is cross domain or not
     * @throws UnsupportedEncodingException
     */
    private boolean isCrossDomain(String location, HTTPCarbonMessage originalRequest)
            throws UnsupportedEncodingException {

        if (!isRelativePath(location)) {
            try {
                URL locationUrl = new URL(location);
                String protocol =
                        originalRequest != null ? (String) originalRequest.getProperty(Constants.PROTOCOL) : null;
                String host = originalRequest != null ? (String) originalRequest.getProperty(Constants.HOST) : null;
                String port = originalRequest != null ?
                        originalRequest.getProperty(Constants.PORT) != null ?
                                Integer.toString((Integer) originalRequest.getProperty(Constants.PORT)) :
                                null :
                        null;

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
     * Create redirect request.
     *
     * @return HTTPCarbonMessage with request properties
     * @throws MalformedURLException
     */
    private HTTPCarbonMessage createHttpCarbonRequest() throws MalformedURLException {
        URL locationUrl = new URL(redirectState.get(Constants.LOCATION));

        HttpMethod httpMethod = new HttpMethod(redirectState.get(Constants.HTTP_METHOD));
        HTTPCarbonMessage httpCarbonRequest = new HTTPCarbonMessage(
                new DefaultHttpRequest(HttpVersion.HTTP_1_1, httpMethod, ""));
        httpCarbonRequest.setProperty(Constants.PORT, locationUrl.getPort());
        httpCarbonRequest.setProperty(Constants.PROTOCOL, locationUrl.getProtocol());
        httpCarbonRequest.setProperty(Constants.HOST, locationUrl.getHost());
        httpCarbonRequest.setProperty(Constants.HTTP_METHOD, redirectState.get(Constants.HTTP_METHOD));
        httpCarbonRequest.setProperty(Constants.REQUEST_URL, locationUrl.getPath());
        httpCarbonRequest.setProperty(Constants.TO, locationUrl.getPath());

        httpCarbonRequest.setHeader(Constants.HOST, locationUrl.getHost() + Constants.COLON + locationUrl.getPort());
        httpCarbonRequest.setEndOfMsgAdded(true);
        return httpCarbonRequest;
    }

    /**
     * Send the redirect request using a new channel.
     *
     * @param redirectUrl       Redirect URL
     * @param httpCarbonRequest HTTPCarbonMessage needs to be set as an attribute in the channel, so that it can be
     *                          used with the next redirect if need be
     * @param httpRequest       HttpRequest that send through the newly created channel
     */
    private void writeContentToNewChannel(URL redirectUrl, HTTPCarbonMessage httpCarbonRequest,
            HttpRequest httpRequest) {

        if (Constants.HTTP_SCHEME.equals(redirectUrl.getProtocol())) {
            sslEngine = null;
        }

        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap clientBootstrap = new Bootstrap();
        clientBootstrap.group(group).channel(NioSocketChannel.class)
                .remoteAddress(new InetSocketAddress(redirectUrl.getHost(), redirectUrl.getPort())).handler(
                new RedirectChannelInitializer(originalChannelContext, sslEngine, httpTraceLogEnabled,
                        maxRedirectCount));
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
                    originalChannelContext.fireExceptionCaught(future.cause());
                }
            }
        });
    }

    /**
     * Check whether the location includes an absolute path or a relative path.
     *
     * @param location value of location header
     * @return a boolean indicating url state
     */
    private boolean isRelativePath(String location) {
        if (location.toLowerCase(Locale.ROOT).startsWith(Constants.HTTP_SCHEME + Constants.URL_AUTHORITY) || location
                .toLowerCase(Locale.ROOT).startsWith(Constants.HTTPS_SCHEME + Constants.URL_AUTHORITY)) {
            return false;
        }
        return true;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (ctx != null && ctx.channel().isActive()) {
            originalChannelContext.fireExceptionCaught(cause);
            ctx.close();
        }
    }
}
