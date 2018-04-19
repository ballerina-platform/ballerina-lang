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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sender configuration for WebSocket client connector.
 */
public class WsClientConnectorConfig {

    private final String remoteAddress;
    private List<String> subProtocols;
    private int idleTimeoutInSeconds = -1;
    private boolean autoRead = true;
    private final Map<String, String> headers = new HashMap<>();

    public WsClientConnectorConfig(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public WsClientConnectorConfig(String remoteAddress, List<String> subProtocols,
                                   int idleTimeoutInSeconds, boolean autoRead) {
        this.remoteAddress = remoteAddress;
        this.subProtocols = subProtocols;
        this.idleTimeoutInSeconds = idleTimeoutInSeconds;
        this.autoRead = autoRead;
    }

    /**
     * Get sub protocols as a comma separated values string.
     *
     * @return a string of comma separated values of sub protocols.
     */
    public String getSubProtocolsAsCSV() {
        if (subProtocols == null) {
            return null;
        }

        String subProtocolsAsCSV = "";
        for (String subProtocol : subProtocols) {
            subProtocolsAsCSV = subProtocolsAsCSV.concat(subProtocol + ",");
        }
        subProtocolsAsCSV = subProtocolsAsCSV.substring(0, subProtocolsAsCSV.length() - 1);
        return subProtocolsAsCSV;
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
        this.subProtocols = Arrays.asList(subProtocols);
    }

    /**
     * Add sub protocols.
     *
     * @param subProtocols a list of sub protocols.
     */
    public void setSubProtocols(List<String> subProtocols) {
        if (subProtocols == null || subProtocols.size() == 0) {
            this.subProtocols = null;
            return;
        }
        this.subProtocols = subProtocols;
    }

    public boolean isAutoRead() {
        return autoRead;
    }

    public void setAutoRead(boolean autoRead) {
        this.autoRead = autoRead;
    }

    /**
     * Get sub protocols as a String array.
     *
     * @return a String list of sub protocols.
     */
    public List<String> getSubProtocols() {
        return this.subProtocols;
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
        this.headers.putAll(headers);
    }

    /**
     * Add single headers.
     *
     * @param key Key of the header.
     * @param value Value of the header.
     */
    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    /**
     * Get all the headers.
     *
     * @return all the headers as a map.
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Check whether a header is contained or not.
     *
     * @param key key of the header.
     * @return true of the header is present.
     */
    public boolean containsHeader(String key) {
        return headers.containsKey(key);
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
}
