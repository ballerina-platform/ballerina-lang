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

package org.wso2.transport.http.netty.sender;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.message.DefaultListener;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * {@code RedirectUtil} contains utility methods used to HTTP client side redirection.
 */
public class RedirectUtil {

    private static final Logger log = LoggerFactory.getLogger(RedirectUtil.class);

    /**
     * Creates redirect request message.
     *
     * @return HTTPCarbonMessage with request properties
     * @throws MalformedURLException if url is malformed
     */
    public static HTTPCarbonMessage createRedirectCarbonRequest(String redirectionUrl, String redirectionMethod,
                                                                String userAgent, ChannelHandlerContext ctx)
            throws MalformedURLException {
        if (log.isDebugEnabled()) {
            log.debug("Create redirect request with http method  : " + redirectionMethod);
        }
        URL locationUrl = new URL(redirectionUrl);

        HttpMethod httpMethod = new HttpMethod(redirectionMethod);
        HTTPCarbonMessage httpCarbonRequest = new HTTPCarbonMessage(
                new DefaultHttpRequest(HttpVersion.HTTP_1_1, httpMethod, ""), new DefaultListener(ctx));
        httpCarbonRequest.setProperty(Constants.HTTP_PORT,
                                      locationUrl.getPort() != -1 ? locationUrl.getPort() :
                                      getDefaultPort(locationUrl.getProtocol()));
        httpCarbonRequest.setProperty(Constants.PROTOCOL, locationUrl.getProtocol());
        httpCarbonRequest.setProperty(Constants.HTTP_HOST, locationUrl.getHost());
        httpCarbonRequest.setProperty(Constants.HTTP_METHOD, redirectionMethod);
        httpCarbonRequest.setProperty(Constants.REQUEST_URL, locationUrl.toString());
        httpCarbonRequest.setProperty(Constants.TO, locationUrl.toString());

        StringBuilder host = new StringBuilder(locationUrl.getHost());
        if (locationUrl.getPort() != -1 && locationUrl.getPort() != Constants.DEFAULT_HTTP_PORT
            && locationUrl.getPort() != Constants.DEFAULT_HTTPS_PORT) {
            host.append(Constants.COLON).append(locationUrl.getPort());
        }
        if (userAgent != null) {
            httpCarbonRequest.setHeader(HttpHeaderNames.USER_AGENT.toString(), userAgent);
        }
        httpCarbonRequest.setHeader(HttpHeaderNames.HOST.toString(), host.toString());
        httpCarbonRequest.completeMessage();
        return httpCarbonRequest;
    }

    /**
     * Builds redirect url from the location header value.
     *
     * @param locationHeaderVal value of the location header
     * @param originalRequest   original HTTPCarbon request
     * @return  resolved redirect url
     * @throws URISyntaxException   if uri syntax is not correct
     * @throws UnsupportedEncodingException if uri decoding fails
     */
    public static String getResolvedRedirectURI(String locationHeaderVal, HTTPCarbonMessage originalRequest)
            throws URISyntaxException, UnsupportedEncodingException {
        URI location = new URI(locationHeaderVal);
        if (!location.isAbsolute()) {
            // location is not absolute, we need to resolve it.
            String baseURIAsString = (String) originalRequest.getProperty(Constants.TO);
            if (baseURIAsString != null) {
                URI baseUri = new URI(baseURIAsString);
                location = baseUri.resolve(location.normalize());
            }
        }
        return location.toString();
    }

    /**
     * Gets the default port based on the protocol.
     *
     * @param protocol the http protocol
     * @return default port as an int
     */
    private static int getDefaultPort(String protocol) {
        return Constants.HTTPS_SCHEME.equals(protocol) ? Constants.DEFAULT_HTTPS_PORT : Constants.DEFAULT_HTTP_PORT;
    }
}
