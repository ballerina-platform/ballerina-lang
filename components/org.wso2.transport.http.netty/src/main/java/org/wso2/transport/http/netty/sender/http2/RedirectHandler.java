/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.sender.http2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.HttpConversionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpClientConnector;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Locale;

/**
 * {@code RedirectHandler} is responsible for HTTP/2 redirection.
 */
public class RedirectHandler implements Http2DataEventListener {

    private static final Logger log = LoggerFactory.getLogger(RedirectHandler.class);

    private Http2ClientChannel http2ClientChannel;
    private int maxRedirectCount;

    public RedirectHandler(Http2ClientChannel http2ClientChannel, int maxRedirectCount) {
        this.http2ClientChannel = http2ClientChannel;
        this.maxRedirectCount = maxRedirectCount;
    }

    @Override
    public boolean onStreamInit(ChannelHandlerContext ctx, int streamId) {
        return true;
    }

    @Override
    public boolean onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, boolean endOfStream) {

        OutboundMsgHolder outboundMsgHolder = http2ClientChannel.getInFlightMessage(streamId);

        int statusCode = fetchStatusCode(headers);
        if (!isRedirectionResponse(statusCode)) {
            return true;
        }
        String location = fetchLocationHeaderVal(headers);
        if (location == null) {
            return true;
        }
        if (outboundMsgHolder.incrementRedirectCount() > maxRedirectCount) {
            return true;
        }
        if (endOfStream) {
            doRedirection(ctx, streamId, statusCode, outboundMsgHolder, location);
        } else {
            outboundMsgHolder.markForRedirection();
            outboundMsgHolder.setRedirectResponseHeaders(headers);
        }
        return false;
    }

    @Override
    public boolean onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, boolean endOfStream) {
        OutboundMsgHolder outboundMsgHolder = http2ClientChannel.getInFlightMessage(streamId);
        if (outboundMsgHolder.isMarkedForRedirection()) {
            if (endOfStream) {
                Http2Headers headers = outboundMsgHolder.getRedirectResponseHeaders();
                doRedirection(
                        ctx, streamId, fetchStatusCode(headers), outboundMsgHolder, fetchLocationHeaderVal(headers));
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean onPushPromiseRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers,
                                     boolean endOfStream) {
        return true;
    }

    @Override
    public boolean onHeadersWrite(ChannelHandlerContext ctx, int streamId, Http2Headers headers, boolean endOfStream) {
        return true;
    }

    @Override
    public boolean onDataWrite(ChannelHandlerContext ctx, int streamId, ByteBuf data, boolean endOfStream) {
        return true;
    }

    @Override
    public boolean onStreamReset(int streamId) {
        return true;
    }

    @Override
    public boolean onStreamClose(int streamId) {
        return true;
    }

    @Override
    public void destroy() {
    }

    private void doRedirection(ChannelHandlerContext ctx, int streamId, int statusCode,
                               OutboundMsgHolder outboundMsgHolder, String location) {
        try {
            HTTPCarbonMessage originalRequest = outboundMsgHolder.getRequest();
            String redirectionMethod = getRedirectionRequestMethod(statusCode, originalRequest);
            String redirectionURL = getLocationURI(location, originalRequest);
            boolean crossDomain = isCrossDomain(location, originalRequest);

            HTTPCarbonMessage request = createRedirectRequest(redirectionURL, redirectionMethod);
            outboundMsgHolder.clearRedirectionState();
            http2ClientChannel.removeInFlightMessage(streamId);

            outboundMsgHolder.updateRequest(request);
            if (crossDomain) {
                DefaultHttpClientConnector connector = ctx.channel().attr(Constants.CLIENT_CONNECTOR).get();
                connector.send(outboundMsgHolder, request);
            } else {
                ctx.write(outboundMsgHolder);
            }

        } catch (UnsupportedEncodingException e) {
            log.error("UnsupportedEncodingException occurred when deciding whether a redirection is required",
                      e);
        } catch (MalformedURLException e) {
            log.error("MalformedURLException occurred when deciding whether a redirection is required", e);
        }
    }

    private int fetchStatusCode(Http2Headers headers) {
        HttpResponseStatus responseStatus;
        try {
            responseStatus = HttpConversionUtil.parseStatus(headers.status());
        } catch (Http2Exception e) {
            responseStatus = HttpResponseStatus.BAD_GATEWAY;
        }
        return responseStatus.code();
    }

    private String fetchLocationHeaderVal(Http2Headers headers) {
        return headers.get(HttpHeaderNames.LOCATION) != null ? headers.get(HttpHeaderNames.LOCATION).toString() : null;
    }

    private boolean isRedirectionResponse(int statusCode) {
        return statusCode >= 300 && statusCode < 400;
    }

    private String getRedirectionRequestMethod(int statusCode, HTTPCarbonMessage originalRequest) {
        String originalRequestMethod =
                originalRequest != null ? (String) originalRequest.getProperty(Constants.HTTP_METHOD) : null;

        switch (statusCode) {
            case 300:
            case 305:
            case 307:
            case 308:
                if (Constants.HTTP_GET_METHOD.equals(originalRequestMethod) || Constants.HTTP_HEAD_METHOD
                        .equals(originalRequestMethod)) {
                    return originalRequestMethod;
                }
                break;
            case 301:
            case 302:
                if (Constants.HTTP_GET_METHOD.equals(originalRequestMethod) || Constants.HTTP_HEAD_METHOD
                        .equals(originalRequestMethod)) {
                    return Constants.HTTP_GET_METHOD;
                }
                break;
            case 303:
                return Constants.HTTP_GET_METHOD;
        }
        return null;
    }

    /**
     * Build redirect url from the location header value.
     *
     * @param location        value of location header
     * @param originalRequest Original request
     * @return a string that holds redirect url
     * @throws UnsupportedEncodingException
     */
    private String getLocationURI(String location, HTTPCarbonMessage originalRequest)
            throws UnsupportedEncodingException {
        if (location != null) {
            //if location url starts either with http ot https that means an absolute path has been set in header
            if (!isRelativePath(location)) {
                if (log.isDebugEnabled()) {
                    log.debug("Location contain an absolute path : " + location);
                }
                return location;
            } else {
                //Use relative path to build redirect url
                String requestPath =
                        originalRequest != null ? (String) originalRequest.getProperty(Constants.TO) : null;
                String protocol = originalRequest != null ?
                                  (String) originalRequest.getProperty(Constants.PROTOCOL) :
                                  Constants.HTTP_SCHEME;
                String host = originalRequest != null ? (String) originalRequest.getProperty(Constants.HTTP_HOST) :
                              null;
                if (host == null) {
                    return null;
                }
                int defaultPort = getDefaultPort(protocol);
                Integer port = originalRequest != null ?
                               originalRequest.getProperty(Constants.HTTP_PORT) != null ?
                               (Integer) originalRequest.getProperty(Constants.HTTP_PORT) :
                               defaultPort :
                               defaultPort;
                return buildRedirectURL(requestPath, location, protocol, host, port);
            }
        }
        return null;
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

    /**
     * Build redirect URL from relative path.
     *
     * @param requestPath request path of the original request
     * @param location    relative path received as the location
     * @param protocol    protocol used in the request
     * @param host        host used in the request
     * @param port        port used in the request
     * @return a string containing absolute path for redirection
     * @throws UnsupportedEncodingException
     */
    private String buildRedirectURL(String requestPath, String location, String protocol, String host, Integer port)
            throws UnsupportedEncodingException {
        String newPath = requestPath == null ? Constants.FORWRD_SLASH : URLDecoder.decode(requestPath, Constants.UTF8);
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
        if (log.isDebugEnabled()) {
            log.debug("Redirect URL build from relative path is : " + newLocation.toString());
        }
        return newLocation.toString();
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
                    if (log.isDebugEnabled()) {
                        log.debug("Is cross domain url : " + false);
                    }
                    return false;
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Is cross domain url : " + true);
                    }
                    return true;
                }
            } catch (MalformedURLException exception) {
                log.error("MalformedURLException occurred while deciding whether the redirect url is cross domain",
                          exception);
                throw exception;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Is cross domain url : " + false);
        }
        return false;
    }

    /**
     * Create redirect request.
     *
     * @return HTTPCarbonMessage with request properties
     * @throws MalformedURLException
     */
    private HTTPCarbonMessage createRedirectRequest(String redirectionUrl, String redirectionMethod)
            throws MalformedURLException {

        URL locationUrl = new URL(redirectionUrl);
        HttpMethod httpMethod = new HttpMethod(redirectionMethod);
        HTTPCarbonMessage httpCarbonRequest = new HTTPCarbonMessage(
                new DefaultHttpRequest(HttpVersion.HTTP_1_1, httpMethod, ""));
        httpCarbonRequest.setProperty(
                Constants.HTTP_PORT,
                locationUrl.getPort() != -1 ? locationUrl.getPort() : getDefaultPort(locationUrl.getProtocol()));
        httpCarbonRequest.setProperty(Constants.PROTOCOL, locationUrl.getProtocol());
        httpCarbonRequest.setProperty(Constants.HTTP_HOST, locationUrl.getHost());
        httpCarbonRequest.setProperty(Constants.HTTP_METHOD, redirectionMethod);
        httpCarbonRequest.setProperty(Constants.REQUEST_URL, locationUrl.getPath());
        httpCarbonRequest.setProperty(Constants.TO, locationUrl.getPath());

        StringBuffer host = new StringBuffer(locationUrl.getHost());
        if (locationUrl.getPort() != -1 && locationUrl.getPort() != Constants.DEFAULT_HTTP_PORT
            && locationUrl.getPort() != Constants.DEFAULT_HTTPS_PORT) {
            host.append(Constants.COLON).append(locationUrl.getPort());
        }
        httpCarbonRequest.setHeader(HttpHeaderNames.HOST.toString(), host.toString());
        httpCarbonRequest.completeMessage();
        return httpCarbonRequest;
    }
}
