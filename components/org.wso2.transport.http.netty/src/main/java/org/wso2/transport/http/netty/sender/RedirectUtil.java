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

import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Locale;

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
                                                                String userAgent)
            throws MalformedURLException {
        if (log.isDebugEnabled()) {
            log.debug("Create redirect request with http method  : " + redirectionMethod);
        }
        URL locationUrl = new URL(redirectionUrl);

        HttpMethod httpMethod = new HttpMethod(redirectionMethod);
        HTTPCarbonMessage httpCarbonRequest = new HTTPCarbonMessage(
                new DefaultHttpRequest(HttpVersion.HTTP_1_1, httpMethod, ""));
        httpCarbonRequest.setProperty(Constants.HTTP_PORT,
                                      locationUrl.getPort() != -1 ? locationUrl.getPort() :
                                      getDefaultPort(locationUrl.getProtocol()));
        httpCarbonRequest.setProperty(Constants.PROTOCOL, locationUrl.getProtocol());
        httpCarbonRequest.setProperty(Constants.HTTP_HOST, locationUrl.getHost());
        httpCarbonRequest.setProperty(Constants.HTTP_METHOD, redirectionMethod);
        httpCarbonRequest.setProperty(Constants.REQUEST_URL, locationUrl.getPath());
        httpCarbonRequest.setProperty(Constants.TO, locationUrl.getPath());

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
     * @param location        value of the location header
     * @param originalRequest Original request message
     * @return a string that holds redirect url
     * @throws UnsupportedEncodingException if url decoding fails
     */
    public static String getLocationURI(String location, HTTPCarbonMessage originalRequest)
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
                if (originalRequest == null) {
                    return null;
                }
                String requestPath = (String) originalRequest.getProperty(Constants.TO);
                String protocol = (String) originalRequest.getProperty(Constants.PROTOCOL);
                String host = (String) originalRequest.getProperty(Constants.HTTP_HOST);
                if (host == null) {
                    return null;
                }
                int defaultPort = getDefaultPort(protocol);
                Integer port = originalRequest.getProperty(Constants.HTTP_PORT) != null ?
                               (Integer) originalRequest.getProperty(Constants.HTTP_PORT) : defaultPort;
                return buildRedirectURL(requestPath, location, protocol, host, port);
            }
        }
        return null;
    }

    /**
     * Checks whether the location indicates a cross domain.
     *
     * @param location        redirected location
     * @param originalRequest Original request message
     * @return whether the location is cross domain or not
     * @throws UnsupportedEncodingException if url decoding fails
     */
    public static boolean isCrossDomain(String location, HTTPCarbonMessage originalRequest)
            throws UnsupportedEncodingException, MalformedURLException {
        if (!isRelativePath(location)) {
            try {
                URL locationUrl = new URL(location);
                String protocol = (String) originalRequest.getProperty(Constants.PROTOCOL);
                String host = (String) originalRequest.getProperty(Constants.HTTP_HOST);
                String port = originalRequest.getProperty(Constants.HTTP_PORT) != null ?
                              Integer.toString((Integer) originalRequest.getProperty(Constants.HTTP_PORT)) : null;
                if (port == null) {
                    port = String.valueOf(getDefaultPort(protocol));
                }

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
     * Builds the redirect URL from relative path.
     *
     * @param requestPath request path of the original request
     * @param location    relative path received as the location
     * @param protocol    protocol used in the request
     * @param host        host used in the request
     * @param port        port used in the request
     * @return a string containing absolute path for redirection
     * @throws UnsupportedEncodingException if url decoding fails
     */
    private static String buildRedirectURL(String requestPath, String location, String protocol,
                                           String host, Integer port) throws UnsupportedEncodingException {
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
     * Checks whether the location includes an absolute path or a relative path.
     *
     * @param location value of location header
     * @return a boolean indicating url state
     */
    private static boolean isRelativePath(String location) {
        return !location.toLowerCase(Locale.ROOT).startsWith(Constants.HTTP_SCHEME + Constants.URL_AUTHORITY) &&
               !location.toLowerCase(Locale.ROOT).startsWith(Constants.HTTPS_SCHEME + Constants.URL_AUTHORITY);
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
