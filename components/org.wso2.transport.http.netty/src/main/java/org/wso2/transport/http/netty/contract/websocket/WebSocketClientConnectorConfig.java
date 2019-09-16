/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.transport.http.netty.contract.websocket;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.config.SslConfiguration;

import java.net.URI;
import java.util.Map;

/**
 * Configuration for WebSocket client connector.
 */
public class WebSocketClientConnectorConfig extends SslConfiguration {

    private final String remoteAddress;
    private String subProtocols;
    private int idleTimeoutInSeconds;
    private boolean autoRead;
    private final HttpHeaders headers;
    private int maxFrameSize = 65536;
    private boolean webSocketCompressionEnabled;

    public WebSocketClientConnectorConfig(String remoteAddress) {
        this.remoteAddress = remoteAddress;
        this.headers = new DefaultHttpHeaders();
        this.setScheme(Constants.WSS_SCHEME.equals(URI.create(remoteAddress).getScheme())
                ? Constants.HTTPS_SCHEME : Constants.HTTP_SCHEME);
    }

    /**
     * Get sub protocols as a comma separated values string.
     *
     * @return a string of comma separated values of sub protocols.
     */
    public String getSubProtocolsStr() {
        if (subProtocols == null) {
            return null;
        }

        return subProtocols;
    }

    /**
     * Add sub protocols.
     *
     * @param subProtocols a array of sub protocols.
     */
    public void setSubProtocols(String[] subProtocols) {
        if (subProtocols == null || subProtocols.length == 0) {
            this.subProtocols = null;
            return;
        }
        this.subProtocols = String.join(",", subProtocols);
    }

    public boolean isAutoRead() {
        return autoRead;
    }

    public void setAutoRead(boolean autoRead) {
        this.autoRead = autoRead;
    }

    /**
     * Get the remote address.
     *
     * @return the remote address.
     */
    public String getRemoteAddress() {
        return remoteAddress;
    }

    /**
     * Add multiple headers.
     *
     * @param headers Headers map.
     */
    public void addHeaders(Map<String, String> headers) {
        headers.forEach(this.headers::add);
    }

    /**
     * Add single headers.
     *
     * @param key Key of the header.
     * @param value Value of the header.
     */
    public void addHeader(String key, String value) {
        this.headers.add(key, value);
    }

    /**
     * Get all the headers.
     *
     * @return all the headers as a map.
     */
    public HttpHeaders getHeaders() {
        return headers;
    }

    /**
     * Check whether a header is contained or not.
     *
     * @param key key of the header.
     * @return true of the header is present.
     */
    public boolean containsHeader(String key) {
        return headers.contains(key);
    }

    /**
     * Get idle timeout for WebSocket connection.
     *
     * @return the idle timeout in millis seconds.
     */
    public int getIdleTimeoutInMillis() {
        return idleTimeoutInSeconds;
    }

    /**
     * Set the idle timeout for WebSocket connection.
     *
     * @param idleTimeoutInSeconds the idle timeout in milli seconds.
     */
    public void setIdleTimeoutInMillis(int idleTimeoutInSeconds) {
        this.idleTimeoutInSeconds = idleTimeoutInSeconds;
    }

    public int getMaxFrameSize() {
        return maxFrameSize;
    }

    public void setMaxFrameSize(int maxFrameSize) {
        this.maxFrameSize = maxFrameSize;
    }

    public boolean isWebSocketCompressionEnabled() {
        return webSocketCompressionEnabled;
    }

    public void setWebSocketCompressionEnabled(boolean webSocketCompressionEnabled) {
        this.webSocketCompressionEnabled = webSocketCompressionEnabled;
    }
}
