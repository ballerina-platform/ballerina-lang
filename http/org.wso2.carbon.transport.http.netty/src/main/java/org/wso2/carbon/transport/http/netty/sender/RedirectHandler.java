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
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.common.Util;
import org.wso2.carbon.transport.http.netty.contract.HttpResponseFuture;
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
    private HTTPCarbonMessage targetRespMsg;

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
                redirectRequest(ctx, msg);
            } else {
                //Content is still flowing through the channel and when it's not a redirect, pass it to the next handler
                if (!isRedirect) {
                    if (originalChannelContext.channel().isActive()) {
                        originalChannelContext.fireChannelRead(msg);
                    } else {
                        //If the original channel is not active add the response content to target message.
                        HttpContent httpContent = (HttpContent) msg;
                        targetRespMsg.addHttpContent(httpContent);
                    }
                }
            }
        }
    }

    /**
     * Handles the actual redirect.
     *
     * @param ctx Channel handler context
     * @param msg Response message
     */
    private void redirectRequest(ChannelHandlerContext ctx, Object msg) {
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
                    writeContentToNewChannel(ctx, locationUrl, httpCarbonRequest, httpRequest);
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Use existing channel to send the redirect request");
                    }
                    ctx.channel().attr(Constants.ORIGINAL_REQUEST).set(httpCarbonRequest);
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
            if (originalChannelContext.channel().isActive()) {
                originalChannelContext.fireChannelRead(msg);
            } else {
                //If the original channel is not active mark the end of the target message.
                HttpContent httpContent = (HttpContent) msg;
                targetRespMsg.addHttpContent(httpContent);
                targetRespMsg.setEndOfMsgAdded(true);
                targetRespMsg = null;
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

                if (redirectCount.intValue() <= maxRedirectCount) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Redirection required");
                    }
                    isRedirect = true;
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Maximum redirect count reached.");
                    }
                    //Fire next handler in the original channel
                    isRedirect = false;
                    originalChannelContext.fireChannelRead(msg);
                }
            } else {
               /* Fire next handler in the original channel if it's still alive. Else send the response directly to
                client using the original future.*/
                isRedirect = false;
                if (originalChannelContext.channel().isActive()) {
                    originalChannelContext.fireChannelRead(msg);
                } else {
                    HttpResponseFuture responseFuture = ctx.channel()
                            .attr(Constants.RESPONSE_FUTURE_OF_ORIGINAL_CHANNEL).get();
                    responseFuture.notifyHttpListener(setUpCarbonMessage(msg));
                }
            }
        } catch (UnsupportedEncodingException e) {
            LOG.error("Error occurred when deciding whether a redirection is required", e);
            originalChannelContext.fireExceptionCaught(e);
        }
    }

    private HTTPCarbonMessage setUpCarbonMessage(Object msg) {
        targetRespMsg = new HTTPCarbonMessage((HttpMessage) msg);
        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
            HTTPTransportContextHolder.getInstance().getHandlerExecutor()
                    .executeAtTargetResponseReceiving(targetRespMsg);
        }
        targetRespMsg.setProperty(org.wso2.carbon.messaging.Constants.DIRECTION,
                org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE);
        HttpResponse httpResponse = (HttpResponse) msg;
        targetRespMsg.setProperty(Constants.HTTP_STATUS_CODE, httpResponse.getStatus().code());
        return targetRespMsg;
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

                int defaultPort = getDefaultPort(protocol);
                Integer port = originalRequest != null ?
                        originalRequest.getProperty(Constants.PORT) != null ?
                                (Integer) originalRequest.getProperty(Constants.PORT) :
                                defaultPort :
                        defaultPort;

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
        httpCarbonRequest.setProperty(Constants.PORT,
                locationUrl.getPort() != -1 ? locationUrl.getPort() : getDefaultPort(locationUrl.getProtocol()));
        httpCarbonRequest.setProperty(Constants.PROTOCOL, locationUrl.getProtocol());
        httpCarbonRequest.setProperty(Constants.HOST, locationUrl.getHost());
        httpCarbonRequest.setProperty(Constants.HTTP_METHOD, redirectState.get(Constants.HTTP_METHOD));
        httpCarbonRequest.setProperty(Constants.REQUEST_URL, locationUrl.getPath());
        httpCarbonRequest.setProperty(Constants.TO, locationUrl.getPath());

        StringBuffer host = new StringBuffer(locationUrl.getHost());
        if (locationUrl.getPort() != -1 && locationUrl.getPort() != Constants.DEFAULT_HTTP_PORT
                && locationUrl.getPort() != Constants.DEFAULT_HTTPS_PORT) {
            host.append(Constants.COLON).append(locationUrl.getPort());
        }
        httpCarbonRequest.setHeader(Constants.HOST, host.toString());
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
    private void writeContentToNewChannel(ChannelHandlerContext channelHandlerContext, URL redirectUrl,
            HTTPCarbonMessage httpCarbonRequest, HttpRequest httpRequest) {

        if (Constants.HTTP_SCHEME.equals(redirectUrl.getProtocol())) {
            sslEngine = null;
        }

        long channelStartTime = channelHandlerContext.channel()
                .attr(Constants.ORIGINAL_CHANNEL_START_TIME).get();

        int timeoutOfOriginalRequest = channelHandlerContext.channel()
                .attr(Constants.ORIGINAL_CHANNEL_TIMEOUT).get();

        long timeElapsedSinceOriginalRequest = System.currentTimeMillis() - channelStartTime;
        long remainingTimeForRedirection = timeoutOfOriginalRequest - timeElapsedSinceOriginalRequest;

        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap clientBootstrap = new Bootstrap();
        clientBootstrap.group(group).channel(NioSocketChannel.class).remoteAddress(
                new InetSocketAddress(redirectUrl.getHost(), redirectUrl.getPort() != -1 ?
                        redirectUrl.getPort() :
                        getDefaultPort(redirectUrl.getProtocol()))).handler(
                new RedirectChannelInitializer(originalChannelContext, channelHandlerContext, sslEngine,
                        httpTraceLogEnabled,
                        maxRedirectCount, remainingTimeForRedirection));
        clientBootstrap.option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15000);
        ChannelFuture channelFuture = clientBootstrap.connect();
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess() && future.isDone()) {
                    HttpResponseFuture responseFuture = channelHandlerContext.channel()
                            .attr(Constants.RESPONSE_FUTURE_OF_ORIGINAL_CHANNEL).get();
                    future.channel().attr(Constants.RESPONSE_FUTURE_OF_ORIGINAL_CHANNEL).set(responseFuture);
                    future.channel().attr(Constants.ORIGINAL_REQUEST).set(httpCarbonRequest);
                    future.channel().attr(Constants.ORIGINAL_CHANNEL_START_TIME).set(channelStartTime);
                    future.channel().attr(Constants.ORIGINAL_CHANNEL_TIMEOUT).set(timeoutOfOriginalRequest);
                    future.channel().write(httpRequest);
                    future.channel().writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                } else {
                    LOG.error("Error occurred while trying to connect to redirect channel.", future.cause());
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

    /**
     * Get default port based on the protocol.
     *
     * @param protocol http protocol
     * @return default port as an int
     */
    private int getDefaultPort(String protocol) {
        int defaultPort = Constants.HTTPS_SCHEME.equals(protocol) ?
                Constants.DEFAULT_HTTPS_PORT :
                Constants.DEFAULT_HTTP_PORT;

        return defaultPort;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (ctx != null && ctx.channel().isActive()) {
            if (originalChannelContext.channel().isActive()) {
                originalChannelContext.fireExceptionCaught(cause);
            } else {
                HttpResponseFuture responseFuture = ctx.channel().attr(Constants.RESPONSE_FUTURE_OF_ORIGINAL_CHANNEL)
                        .get();
                responseFuture.notifyHttpListener(cause);
            }
            ctx.close();
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE || event.state() == IdleState.WRITER_IDLE) {
                ctx.channel().pipeline().remove(Constants.IDLE_STATE_HANDLER);
                String payload = "<errorMessage>" + "ReadTimeoutException occurred while redirecting request "
                        + "</errorMessage>";
                HttpResponseFuture responseFuture = ctx.channel().attr(Constants.RESPONSE_FUTURE_OF_ORIGINAL_CHANNEL)
                        .get();
                responseFuture.notifyHttpListener(Util.createErrorMessage(payload));
            }
        }
        ctx.close();
    }
}

