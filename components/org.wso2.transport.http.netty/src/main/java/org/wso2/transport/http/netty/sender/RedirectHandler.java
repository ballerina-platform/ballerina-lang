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

package org.wso2.transport.http.netty.sender;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
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
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.Util;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.sender.channel.TargetChannel;
import org.wso2.transport.http.netty.sender.channel.pool.ConnectionManager;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLEngine;

import static org.wso2.transport.http.netty.sender.RedirectUtil.isRelativePath;

/**
 * Class responsible for handling redirects for client connector.
 */
public class RedirectHandler extends ChannelInboundHandlerAdapter {
    protected static final Logger LOG = LoggerFactory.getLogger(RedirectHandler.class);

    private Map<String, String> redirectState = null;
    private boolean isRedirect = false;
    private boolean isCrossDoamin = true;
    private HTTPCarbonMessage originalRequest;
    private SSLEngine sslEngine;
    private boolean httpTraceLogEnabled;
    private int maxRedirectCount;
    private Integer currentRedirectCount;
    private HTTPCarbonMessage targetRespMsg;
    private ChannelHandlerContext originalChannelContext;
    private boolean isIdleHandlerOfTargetChannelRemoved = false;
    private ConnectionManager connectionManager;

    public RedirectHandler(SSLEngine sslEngine, boolean httpTraceLogEnabled, int maxRedirectCount,
                           ConnectionManager connectionManager) {
        this.sslEngine = sslEngine;
        this.httpTraceLogEnabled = httpTraceLogEnabled;
        this.maxRedirectCount = maxRedirectCount;
        this.connectionManager = connectionManager;
    }

    public RedirectHandler(SSLEngine sslEngine, boolean httpTraceLogEnabled, int maxRedirectCount,
                           ChannelHandlerContext originalChannelContext, boolean isIdleHandlerOfTargetChannelRemoved,
                           ConnectionManager connectionManager) {
        this.sslEngine = sslEngine;
        this.httpTraceLogEnabled = httpTraceLogEnabled;
        this.maxRedirectCount = maxRedirectCount;
        this.originalChannelContext = originalChannelContext;
        this.isIdleHandlerOfTargetChannelRemoved = isIdleHandlerOfTargetChannelRemoved;
        this.connectionManager = connectionManager;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (originalChannelContext == null) {
            originalChannelContext = ctx;
        }

        if (msg instanceof HttpResponse) {
            handleRedirectState(ctx, (HttpResponse) msg);
        } else {
            /* Actual redirection happens only when the full response for the previous request
            has been received */
            if (msg instanceof LastHttpContent) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Last content received through channel : " + ctx.channel().id());
                }
                redirectRequest(ctx, msg);
            } else {
                 /*Content is still flowing through the channel and when it's not a redirect, add content to the target
                 response*/
                if (!isRedirect) {
                    if (ctx == originalChannelContext) {
                        originalChannelContext.fireChannelRead(msg);
                    } else {
                        HttpContent httpContent = (HttpContent) msg;
                        targetRespMsg.addHttpContent(httpContent);
                    }
                }
            }
        }
    }

    /**
     * When an exception occurs in channel pipeline, log error and notify the listener.
     *
     * @param ctx   Channel context
     * @param cause Exception occurred
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.error("Exception occurred in RedirectHandler.", cause);
        handleException(ctx, cause);
    }

    /**
     * Notify listener about the exception and close the channel.
     *
     * @param ctx   Channel context
     * @param cause Exception occurred
     */
    private void handleException(ChannelHandlerContext ctx, Throwable cause) {
        if (ctx != null && ctx.channel().isActive()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(" And Channel ID is : " + ctx.channel().id());
            }
            HttpResponseFuture responseFuture = ctx.channel().attr(Constants.RESPONSE_FUTURE_OF_ORIGINAL_CHANNEL).get();
            responseFuture.notifyHttpListener(cause);
            ctx.close();
        }
    }

    /**
     * When a timeout occurs, notify listener and close the channel.
     *
     * @param ctx Channel context
     * @param evt Event
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE || event.state() == IdleState.WRITER_IDLE) {
                if (originalChannelContext == null) {
                    originalChannelContext = ctx;
                }
                if (ctx == originalChannelContext) {
                    originalChannelContext.fireUserEventTriggered(evt);
                    isIdleHandlerOfTargetChannelRemoved = true;
                } else {
                    sendTimeoutError(ctx);
                }
                /*Once a timeout occurs after sending the response, close the channel, otherwise we will still be
                 getting response data  after the timeout, if backend sends data. */
                if (ctx != originalChannelContext) {
                    ctx.close();
                }
            }
        }
    }

    /**
     * Send timeout error
     *
     * @param ctx Channel handler context.
     */
    private void sendTimeoutError(ChannelHandlerContext ctx) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Timeout occurred in RedirectHandler. Channel ID : " + ctx.channel().id());
        }
        HttpResponseFuture responseFuture = ctx.channel().attr(Constants.RESPONSE_FUTURE_OF_ORIGINAL_CHANNEL).get();
        if (responseFuture != null) {
            responseFuture.notifyHttpListener(new Exception(Constants.ENDPOINT_TIMEOUT_MSG));
            responseFuture.removeHttpListener();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Channel " + ctx.channel().id() + " gets inactive so closing it from RedirectHandler.");
        }
        if (originalChannelContext == ctx) { //If this is the original channel it must be destroyed from the pool
            ctx.fireChannelInactive();
        } else {
            ctx.close();
        }
    }

    /**
     * Handle redirect state for the channel.
     *
     * @param ctx ChannelHandler context
     * @param msg Response message
     */
    private void handleRedirectState(ChannelHandlerContext ctx, HttpResponse msg) throws Exception {
        try {
            originalRequest = ctx.channel().attr(Constants.ORIGINAL_REQUEST).get();
            String location = getLocationFromResponseHeader(msg);
            int statusCode = msg.status().code();
            if (location != null) {
                redirectState = getRedirectState(location, statusCode, originalRequest);
            } else {
                redirectState = null;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Handling redirect state for channel : " + ctx.channel().id());
            }
            if (isRedirectEligible()) {
                isCrossDoamin = RedirectUtil.isCrossDomain(location, originalRequest);
                currentRedirectCount = updateAndGetRedirectCount(ctx);
                if (currentRedirectCount <= maxRedirectCount) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Redirection required.");
                    }
                    isRedirect = true;
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Maximum redirect count reached.");
                    }
                    isRedirect = false;
                    sendResponseHeadersToClient(ctx, msg);
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Request is not eligible for redirection.");
                }
                isRedirect = false;
                if (ctx == originalChannelContext) {
                    originalChannelContext.fireChannelRead(msg);
                } else {
                    sendResponseHeadersToClient(ctx, msg);
                }
            }
        } catch (UnsupportedEncodingException exception) {
            LOG.error("UnsupportedEncodingException occurred when deciding whether a redirection is required",
                    exception);
            handleException(ctx, exception.getCause());
        } catch (MalformedURLException exception) {
            LOG.error("MalformedURLException occurred when deciding whether a redirection is required", exception);
            handleException(ctx, exception.getCause());
        }
    }

    /**
     * Handles the actual redirect.
     *
     * @param ctx Channel handler context
     * @param msg Response message
     */
    private void redirectRequest(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (isRedirect) {
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Getting ready for actual redirection for channel " + ctx.channel().id());
                }
                URL locationUrl = new URL(redirectState.get(HttpHeaderNames.LOCATION.toString()));
                HTTPCarbonMessage httpCarbonRequest = RedirectUtil.createRedirectCarbonRequest(
                        redirectState.get(HttpHeaderNames.LOCATION.toString()),
                        redirectState.get(Constants.HTTP_METHOD),
                        redirectState.get(HttpHeaderNames.USER_AGENT.toString()));
                HttpRequest httpRequest = Util.createHttpRequest(httpCarbonRequest);

                if (isCrossDoamin) {
                    writeContentToNewChannel(ctx, locationUrl, httpCarbonRequest, httpRequest);
                } else {
                    writeContentToExistingChannel(ctx, httpCarbonRequest, httpRequest);
                }
            } catch (MalformedURLException exception) {
                LOG.error("Error occurred when parsing redirect url", exception);
                handleException(ctx, exception.getCause());
            } catch (Exception exception) {
                LOG.error("Error occurred during redirection", exception);
                handleException(ctx, exception.getCause());
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("But is not a redirect.");
            }
            if (ctx == originalChannelContext) {
                originalChannelContext.fireChannelRead(msg);
                //ctx.close();
            } else {
                markEndOfMessage(ctx, (HttpContent) msg);
            }
        }
    }

    /**
     * Write content to backend using existing channel.
     *
     * @param ctx               Channel handler context
     * @param httpCarbonRequest Carbon request
     * @param httpRequest       Http request
     */
    private void writeContentToExistingChannel(ChannelHandlerContext ctx, HTTPCarbonMessage httpCarbonRequest,
                                               HttpRequest httpRequest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Use existing channel" + ctx.channel().id() + " to send the redirect request.");
        }
        ctx.channel().attr(Constants.ORIGINAL_REQUEST).set(httpCarbonRequest);
        ctx.write(httpRequest);
        ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
    }

    /**
     * Mark the end of response and reset channel attributes.
     *
     * @param ctx         Channel context
     * @param httpContent Http content
     */
    private void markEndOfMessage(ChannelHandlerContext ctx, HttpContent httpContent) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Mark end of the message and reset channel attributes for channel : " + ctx.channel().id());
        }
        targetRespMsg.addHttpContent(httpContent);
        targetRespMsg.completeMessage();
        targetRespMsg = null;
        currentRedirectCount = 0;
        TargetChannel targetChannel = ctx.channel().attr(Constants.TARGET_CHANNEL_REFERENCE).get();
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Return target channel : " + targetChannel.getChannel().id() + " back to its pool from "
                        + "RedirectHandler. Currently in channel : " + ctx.channel().id());
            }
            Util.resetChannelAttributes(ctx);
            Util.resetChannelAttributes(originalChannelContext);
            if (!isIdleHandlerOfTargetChannelRemoved) {
                if (targetChannel.getChannel().isActive()) {
                    targetChannel.getChannel().pipeline().remove(Constants.IDLE_STATE_HANDLER);
                    isIdleHandlerOfTargetChannelRemoved = true;
                }
            }
            this.connectionManager.returnChannel(targetChannel);
            if (ctx != originalChannelContext) {
                ctx.close();
            }
        } catch (Exception exception) {
            LOG.error(
                    "Error occurred while returning target channel " + targetChannel.getChannel().id() + " from current"
                            + " channel" + ctx.channel().id() + " " + "to its pool in " + "markEndOfMessage",
                    exception);
            handleException(ctx, exception.getCause());
        }
    }

    /**
     * Notify listener about received content.
     *
     * @param ctx Channel context
     * @param msg Http response message
     */
    private void sendResponseHeadersToClient(ChannelHandlerContext ctx, HttpResponse msg) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Pass along received response headers to client. Channel id : " + ctx.channel().id());
        }
        HttpResponseFuture responseFuture = ctx.channel().attr(Constants.RESPONSE_FUTURE_OF_ORIGINAL_CHANNEL).get();
        responseFuture.notifyHttpListener(setUpCarbonResponseMessage(msg));
    }

    /**
     * Check whether the request is redirect eligible.
     *
     * @return boolean indicating redirect eligibility
     */
    private boolean isRedirectEligible() {
        return redirectState != null && !redirectState.isEmpty() && redirectState.get(
                HttpHeaderNames.LOCATION.toString()) != null && redirectState.get(Constants.HTTP_METHOD) != null;
    }

    /**
     * Get the location from the received response header.
     *
     * @param msg HttpResponse message
     * @return a string containing location value
     * @throws UnsupportedEncodingException if url decoding fails
     */
    private String getLocationFromResponseHeader(HttpResponse msg) throws UnsupportedEncodingException {
        return msg.headers().get(HttpHeaderNames.LOCATION) != null ?
                URLDecoder.decode(msg.headers().get(HttpHeaderNames.LOCATION), Constants.UTF8) :
                null;
    }

    /**
     * Increment redirect count by 1.
     *
     * @param ctx Channel handler context
     * @return integer indicating current redirect count
     */
    private Integer updateAndGetRedirectCount(ChannelHandlerContext ctx) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Increment redirect count.");
        }
        Integer redirectCount = ctx.channel().attr(Constants.REDIRECT_COUNT).get();
        if (redirectCount != null && redirectCount.intValue() != 0) {
            redirectCount++;
        } else {
            redirectCount = 1;
        }
        currentRedirectCount = redirectCount;
        ctx.channel().attr(Constants.REDIRECT_COUNT).set(redirectCount);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Current redirect count." + currentRedirectCount + " and channel id is : " + ctx.channel().id());
        }
        return redirectCount;
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
            throws UnsupportedEncodingException, URISyntaxException {
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
                    redirectState.put(HttpHeaderNames.LOCATION.toString(), getResolvedURI(location, originalRequest));
                }
                break;
            case 301:
            case 302:
                if (Constants.HTTP_GET_METHOD.equals(originalRequestMethod) || Constants.HTTP_HEAD_METHOD
                        .equals(originalRequestMethod)) {
                    redirectState.put(Constants.HTTP_METHOD, Constants.HTTP_GET_METHOD);
                    redirectState.put(HttpHeaderNames.LOCATION.toString(), getResolvedURI(location, originalRequest));
                }
                break;
            case 303:
                redirectState.put(Constants.HTTP_METHOD, Constants.HTTP_GET_METHOD);
                redirectState.put(HttpHeaderNames.LOCATION.toString(), getResolvedURI(location, originalRequest));
                break;
            default:
                return null;
        }
        if (originalRequest != null && originalRequest.getHeader(HttpHeaderNames.USER_AGENT.toString()) != null) {
            redirectState.put(HttpHeaderNames.USER_AGENT.toString(),
                    originalRequest.getHeader(HttpHeaderNames.USER_AGENT.toString()));
        }
        return redirectState;
    }

    private String getResolvedURI(String locationString, HTTPCarbonMessage originalRequest)
            throws URISyntaxException, UnsupportedEncodingException {
        URI location = new URI(locationString);
        if (!location.isAbsolute()) {
            // location is not absolute, we need to resolve it.
            String baseURIAsString = (String) originalRequest.getProperty(Constants.REQUEST_URL);
            if (baseURIAsString != null) {
                URI baseUri = new URI(baseURIAsString);
                location = baseUri.resolve(location.normalize());
            }
        }
        return location.toString();
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
            throws UnsupportedEncodingException, MalformedURLException {
        if (!isRelativePath(location)) {
            try {
                URL locationUrl = new URL(location);
                String protocol =
                        originalRequest != null ? (String) originalRequest.getProperty(Constants.PROTOCOL) : null;
                String host = originalRequest != null ? (String) originalRequest.getProperty(Constants.HTTP_HOST) :
                        null;
                String port = originalRequest != null ?
                        originalRequest.getProperty(Constants.HTTP_PORT) != null ?
                                Integer.toString((Integer) originalRequest.getProperty(Constants.HTTP_PORT)) :
                                null :
                        null;
                if (locationUrl.getProtocol().equals(protocol) && locationUrl.getHost().equals(host)
                        && locationUrl.getPort() == Integer.parseInt(port)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Is cross domain url : " + false);
                    }
                    return false;
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Is cross domain url : " + true);
                    }
                    return true;
                }
            } catch (MalformedURLException exception) {
                LOG.error("MalformedURLException occurred while deciding whether the redirect url is cross domain",
                        exception);
                throw exception;
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Is cross domain url : " + false);
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("Create redirect request with http method  : " + redirectState.get(Constants.HTTP_METHOD));
        }
        URL locationUrl = new URL(redirectState.get(HttpHeaderNames.LOCATION.toString()));

        HttpMethod httpMethod = new HttpMethod(redirectState.get(Constants.HTTP_METHOD));
        HTTPCarbonMessage httpCarbonRequest = new HTTPCarbonMessage(
                new DefaultHttpRequest(HttpVersion.HTTP_1_1, httpMethod, ""));
        httpCarbonRequest.setProperty(Constants.HTTP_PORT,
                locationUrl.getPort() != -1 ? locationUrl.getPort() : getDefaultPort(locationUrl.getProtocol()));
        httpCarbonRequest.setProperty(Constants.PROTOCOL, locationUrl.getProtocol());
        httpCarbonRequest.setProperty(Constants.HTTP_HOST, locationUrl.getHost());
        httpCarbonRequest.setProperty(Constants.HTTP_METHOD, redirectState.get(Constants.HTTP_METHOD));
        httpCarbonRequest.setProperty(Constants.REQUEST_URL, locationUrl.toString());
        httpCarbonRequest.setProperty(Constants.TO, locationUrl.toString());

        StringBuffer host = new StringBuffer(locationUrl.getHost());
        if (locationUrl.getPort() != -1 && locationUrl.getPort() != Constants.DEFAULT_HTTP_PORT
                && locationUrl.getPort() != Constants.DEFAULT_HTTPS_PORT) {
            host.append(Constants.COLON).append(locationUrl.getPort());
        }
        if (redirectState.get(HttpHeaderNames.USER_AGENT.toString()) != null) {
            httpCarbonRequest.setHeader(HttpHeaderNames.USER_AGENT.toString(),
                    redirectState.get(HttpHeaderNames.USER_AGENT.toString()));
        }
        httpCarbonRequest.setHeader(HttpHeaderNames.HOST.toString(), host.toString());
        httpCarbonRequest.completeMessage();
        return httpCarbonRequest;
    }

    /**
     * Create response message that needs to be sent to the client.
     *
     * @param msg Http message
     * @return HTTPCarbonMessage
     */
    private HTTPCarbonMessage setUpCarbonResponseMessage(Object msg) {
        targetRespMsg = new HTTPCarbonMessage((HttpMessage) msg);
        targetRespMsg.setProperty(Constants.DIRECTION, Constants.DIRECTION_RESPONSE);
        HttpResponse httpResponse = (HttpResponse) msg;
        targetRespMsg.setProperty(Constants.HTTP_STATUS_CODE, httpResponse.status().code());
        return targetRespMsg;
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("Send redirect request using a new channel");
        }
        if (Constants.HTTP_SCHEME.equals(redirectUrl.getProtocol())) {
            sslEngine = null;
        }
        bootstrapClient(channelHandlerContext, redirectUrl, httpCarbonRequest, httpRequest);
    }

    /**
     * Bootstrap a netty client to send the redirect request.
     *
     * @param channelHandlerContext Channel handler context
     * @param redirectUrl           Redirect URL
     * @return ChannelFuture
     */
    private void bootstrapClient(ChannelHandlerContext channelHandlerContext, URL redirectUrl,
                                 HTTPCarbonMessage httpCarbonRequest, HttpRequest httpRequest) {
        EventLoopGroup group = channelHandlerContext.channel().eventLoop();
        Bootstrap clientBootstrap = new Bootstrap();
        clientBootstrap.group(group).channel(NioSocketChannel.class).remoteAddress(
                new InetSocketAddress(redirectUrl.getHost(), redirectUrl.getPort() != -1 ?
                        redirectUrl.getPort() :
                        getDefaultPort(redirectUrl.getProtocol()))).handler(
                new RedirectChannelInitializer(sslEngine, httpTraceLogEnabled, maxRedirectCount, originalChannelContext,
                        isIdleHandlerOfTargetChannelRemoved, connectionManager));
        clientBootstrap.option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15000);
        ChannelFuture channelFuture = clientBootstrap.connect();
        registerListener(channelHandlerContext, channelFuture, httpCarbonRequest, httpRequest);
    }

    /**
     * Register channel future listener on channel future.
     *
     * @param channelHandlerContext Channel handler context
     * @param channelFuture         Chanel future
     * @param httpCarbonRequest     Carbon request
     * @param httpRequest           http request
     */
    private void registerListener(ChannelHandlerContext channelHandlerContext, ChannelFuture channelFuture,
                                  HTTPCarbonMessage httpCarbonRequest, HttpRequest httpRequest) {
        channelFuture.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess() && future.isDone()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Connected to the new channel " + future.channel().id() + " and getting ready to "
                            + "write request.");
                }
                long channelStartTime = channelHandlerContext.channel().attr(Constants.ORIGINAL_CHANNEL_START_TIME)
                        .get();
                int timeoutOfOriginalRequest = channelHandlerContext.channel()
                        .attr(Constants.ORIGINAL_CHANNEL_TIMEOUT).get();
                setChannelAttributes(channelHandlerContext, future, httpCarbonRequest, channelStartTime,
                        timeoutOfOriginalRequest);
                long remainingTimeForRedirection = getRemainingTimeForRedirection(channelStartTime,
                        timeoutOfOriginalRequest);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Remaining time for redirection is : " + remainingTimeForRedirection);
                }
                future.channel().pipeline().addBefore(Constants.REDIRECT_HANDLER, Constants.IDLE_STATE_HANDLER,
                        new IdleStateHandler(remainingTimeForRedirection, remainingTimeForRedirection, 0,
                                TimeUnit.MILLISECONDS));
                future.channel().write(httpRequest);
                future.channel().writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                /* if the previous channel is not original channel, closes it after sending the request through
                 new channel*/
                if (channelHandlerContext != originalChannelContext) {
                    channelHandlerContext.close();
                }
            } else {
                LOG.error("Error occurred while trying to connect to redirect channel.", future.cause());
                handleException(channelHandlerContext, future.cause());
            }
        });
    }

    /**
     * Set channel attributes to the new channel.
     *
     * @param channelHandlerContext    Chanel handler context
     * @param future                   ChannelFuture of newly created channel
     * @param httpCarbonRequest        Carbon request
     * @param channelStartTime         Original channel start time
     * @param timeoutOfOriginalRequest Timeout of the original channel
     */
    private void setChannelAttributes(ChannelHandlerContext channelHandlerContext, ChannelFuture future,
                                      HTTPCarbonMessage httpCarbonRequest, long channelStartTime,
                                      int timeoutOfOriginalRequest) {
        HttpResponseFuture responseFuture = channelHandlerContext.channel()
                .attr(Constants.RESPONSE_FUTURE_OF_ORIGINAL_CHANNEL).get();
        future.channel().attr(Constants.RESPONSE_FUTURE_OF_ORIGINAL_CHANNEL).set(responseFuture);
        future.channel().attr(Constants.ORIGINAL_REQUEST).set(httpCarbonRequest);
        future.channel().attr(Constants.REDIRECT_COUNT).set(currentRedirectCount);
        future.channel().attr(Constants.ORIGINAL_CHANNEL_START_TIME).set(channelStartTime);
        future.channel().attr(Constants.ORIGINAL_CHANNEL_TIMEOUT).set(timeoutOfOriginalRequest);
        TargetChannel targetChannel = channelHandlerContext.channel().attr(Constants.TARGET_CHANNEL_REFERENCE).get();
        future.channel().attr(Constants.TARGET_CHANNEL_REFERENCE).set(targetChannel);
    }

    /**
     * Calculate remaining time for redirection.
     *
     * @param channelStartTime         Original channel start time
     * @param timeoutOfOriginalRequest Timeout of the original channel
     * @return a long value indicating the remaining time in milliseconds
     */
    private long getRemainingTimeForRedirection(long channelStartTime, int timeoutOfOriginalRequest) {
        long timeElapsedSinceOriginalRequest = System.currentTimeMillis() - channelStartTime;
        return timeoutOfOriginalRequest - timeElapsedSinceOriginalRequest;
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
}
