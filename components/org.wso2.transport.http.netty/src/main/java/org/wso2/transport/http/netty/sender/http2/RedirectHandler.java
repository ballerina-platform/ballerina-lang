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
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.HttpConversionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpClientConnector;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.sender.RedirectUtil;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

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
            doRedirection(ctx, streamId, statusCode, outboundMsgHolder, location, fetchUserAgentHeaderVal(headers));
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
                doRedirection(ctx, streamId, fetchStatusCode(headers), outboundMsgHolder,
                              fetchLocationHeaderVal(headers), fetchUserAgentHeaderVal(headers));
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
                               OutboundMsgHolder outboundMsgHolder, String location, String userAgent) {
        try {
            HTTPCarbonMessage originalRequest = outboundMsgHolder.getRequest();
            String redirectionMethod = getRedirectionRequestMethod(statusCode, originalRequest);
            String redirectionURL = RedirectUtil.getLocationURI(location, originalRequest);
            HTTPCarbonMessage request =
                    RedirectUtil.createRedirectCarbonRequest(redirectionURL, redirectionMethod, userAgent);
            outboundMsgHolder.clearRedirectionState();
            http2ClientChannel.removeInFlightMessage(streamId);
            outboundMsgHolder.updateRequest(request);
            DefaultHttpClientConnector connector = ctx.channel().attr(Constants.CLIENT_CONNECTOR).get();
            connector.send(outboundMsgHolder, request);
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

    private String fetchUserAgentHeaderVal(Http2Headers headers) {
        return headers.
                get(HttpHeaderNames.USER_AGENT) != null ? headers.get(HttpHeaderNames.USER_AGENT).toString() : null;
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
}
