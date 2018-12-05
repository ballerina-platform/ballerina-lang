/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.contractimpl.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.net.InetSocketAddress;
import java.util.Locale;

/**
 * This class updates Forwarded/X-Forwarded-- headers in outbound request.
 */
public class ForwardedHeaderUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(ForwardedHeaderUpdater.class);
    private static final String FOR = "for=";
    private static final String BY = "by=";
    private static final String HOST = "host=";
    private static final String PROTO = "proto=";
    private static final String SEMI_COLON = ";";
    private static final String COMMA = ",";
    private static final String COLON = ":";

    private final String localAddress;
    private final String forwardedHeader;
    private final String xForwardedForHeader;
    private final String xForwardedByHeader;
    private final String xForwardedHostHeader;
    private final String xForwardedProtoHeader;
    private final HttpCarbonMessage httpOutboundRequest;

    public ForwardedHeaderUpdater(HttpCarbonMessage httpOutboundRequest, String localAddress) {
        this.httpOutboundRequest = httpOutboundRequest;
        this.localAddress = localAddress;
        this.forwardedHeader = httpOutboundRequest.getHeader(Constants.FORWARDED);
        this.xForwardedForHeader = httpOutboundRequest.getHeader(Constants.X_FORWARDED_FOR);
        this.xForwardedByHeader = httpOutboundRequest.getHeader(Constants.X_FORWARDED_BY);
        this.xForwardedHostHeader = httpOutboundRequest.getHeader(Constants.X_FORWARDED_HOST);
        this.xForwardedProtoHeader = httpOutboundRequest.getHeader(Constants.X_FORWARDED_PROTO);
    }

    /**
     * Check header values and return true if Forwarded header needs to be set. Forwarded header is set,
     * when both Forwarded or X-Forwarded-- headers not present or only the Forwarded header is present.
     *
     * @return a boolean to denote the required header
     */
    public boolean isForwardedHeaderRequired() {
        return xForwardedForHeader == null && xForwardedByHeader == null && xForwardedHostHeader == null &&
                xForwardedProtoHeader == null;
    }

    /**
     * Check header values and return true if X-Forwarded-- headers need to be set. X-Forwarded-- headers are set,
     * when only the X-Forwarded-- headers are present.
     *
     * @return a boolean to denote the required header
     */
    public boolean isXForwardedHeaderRequired() {
        return forwardedHeader == null && (xForwardedForHeader != null || xForwardedByHeader != null ||
                xForwardedHostHeader != null || xForwardedProtoHeader != null);
    }

    /**
     * Set forwarded header to outbound request.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7239">rfc7239</a>
     */
    public void setForwardedHeader() {
        StringBuilder headerValue = new StringBuilder();
        if (forwardedHeader == null) {
            Object remoteAddressProperty = httpOutboundRequest.getProperty(Constants.REMOTE_ADDRESS);
            if (remoteAddressProperty != null) {
                String remoteAddress = ((InetSocketAddress) remoteAddressProperty).getAddress().getHostAddress();
                headerValue.append(FOR).append(resolveIP(remoteAddress)).append(SEMI_COLON).append(" ");
            }
            headerValue.append(BY).append(resolveIP(localAddress));
            Object hostHeader = httpOutboundRequest.getProperty(Constants.ORIGIN_HOST);
            if (hostHeader != null) {
                headerValue.append(SEMI_COLON + " " + HOST).append(hostHeader.toString());
            }
            Object protocolHeader = httpOutboundRequest.getProperty(Constants.PROTOCOL);
            if (protocolHeader != null) {
                headerValue.append(SEMI_COLON + " " + PROTO).append(protocolHeader.toString());
            }
            httpOutboundRequest.setHeader(Constants.FORWARDED, headerValue.toString());
            return;
        }
        String[] parts = forwardedHeader.split(SEMI_COLON);
        String previousForValue = null;
        String previousByValue = null;
        String previousHostValue = null;
        String previousProtoValue = null;
        for (String part : parts) {
            if (part.toLowerCase(Locale.getDefault()).contains(FOR)) {
                previousForValue = part.trim();
            } else if (part.toLowerCase(Locale.getDefault()).contains(BY)) {
                previousByValue = part.trim().substring(3);
            } else if (part.toLowerCase(Locale.getDefault()).contains(HOST)) {
                previousHostValue = part.substring(5);
            } else if (part.toLowerCase(Locale.getDefault()).contains(PROTO)) {
                previousProtoValue = part.substring(6);
            }
        }
        if (previousForValue == null) {
            previousForValue = previousByValue != null ? FOR + previousByValue : null;
        } else {
            previousForValue =
                    previousByValue == null ? previousForValue : previousForValue + COMMA + " " + FOR + previousByValue;
        }
        headerValue.append(previousForValue != null ? previousForValue + SEMI_COLON + " " : null);
        headerValue.append(BY).append(resolveIP(localAddress));

        if (previousHostValue != null) {
            headerValue.append(SEMI_COLON + " " + HOST).append(previousHostValue);
        }
        if (previousProtoValue != null) {
            headerValue.append(SEMI_COLON + " " + PROTO).append(previousProtoValue);
        }
        httpOutboundRequest.setHeader(Constants.FORWARDED, headerValue.toString());
    }

    /**
     * Transform X-Forwarded-- to Forwarded headers and set to outbound request.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7239#section-7.4">rfc7239</a>
     */
    public void transformAndSetForwardedHeader() {
        if (xForwardedForHeader != null && xForwardedByHeader != null) {
            //Transition is not done, as it's not possible to know in which order the existing fields were added.
            LOG.info("Header transition is not done as both X-Forwarded-For and X-Forwarded-By headers are present.");
            return;
        }
        StringBuilder headerValue = new StringBuilder();
        if (xForwardedForHeader != null) {
            String[] parts = xForwardedForHeader.split(COMMA);
            for (String part : parts) {
                headerValue.append(FOR).append(resolveIP(part.trim())).append(COMMA).append(" ");
            }
            headerValue.replace(headerValue.length() - 2, headerValue.length(), SEMI_COLON + " ");
            httpOutboundRequest.removeHeader(Constants.X_FORWARDED_FOR);
        }
        if (xForwardedByHeader != null) {
            headerValue.append(FOR).append(resolveIP(xForwardedByHeader.trim())).append(SEMI_COLON).append(" ");
            httpOutboundRequest.removeHeader(Constants.X_FORWARDED_BY);
        }
        headerValue.append(BY).append(resolveIP(localAddress));
        if (xForwardedHostHeader != null) {
            headerValue.append(SEMI_COLON + " " + HOST).append(xForwardedHostHeader);
            httpOutboundRequest.removeHeader(Constants.X_FORWARDED_HOST);
        }
        if (xForwardedProtoHeader != null) {
            headerValue.append(SEMI_COLON + " " + PROTO).append(xForwardedProtoHeader);
            httpOutboundRequest.removeHeader(Constants.X_FORWARDED_PROTO);
        }
        httpOutboundRequest.setHeader(Constants.FORWARDED, headerValue.toString());
    }

    /**
     * Set X-Forwarded-- headers to outbound request.
     */
    public void setDefactoForwardedHeaders() {
        if (xForwardedForHeader != null && xForwardedByHeader != null) {
            httpOutboundRequest.setHeader(Constants.X_FORWARDED_FOR,
                    xForwardedForHeader.trim() + COMMA + " " + xForwardedByHeader.trim());
        } else if (xForwardedByHeader != null) {
            httpOutboundRequest.setHeader(Constants.X_FORWARDED_FOR, xForwardedByHeader.trim());
        }
        httpOutboundRequest.setHeader(Constants.X_FORWARDED_BY, localAddress);
    }

    private static String resolveIP(String ipString) {
        //The client's IP address can have an optional port number
        String[] parts = ipString.split(COLON);
        //IPv4 literal i.e 192.0.2.43 or 192.0.2.43:47011
        if (parts.length <= 2) {
            return ipString;
        }
        //IPv6 literal with port. i.e [2001:db8:cafe::17]:47011
        if (ipString.startsWith("[")) {
            return "\"" + ipString + "\"";
        }
        //IPv6 literal host. i.e 2001:db8:cafe::17
        if (ipString.contains(COLON)) {
            return "\"[" + ipString + "]\"";
        }
        //Obfuscated Identifier. i.e _SEVKISEK
        return ipString;
    }
}
