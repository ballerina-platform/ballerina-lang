/*
 *  Copyright (c) 2015 WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.transport.http.netty.contract.config;

import java.util.ArrayList;
import java.util.List;

/**
 * JAXB representation of a transport listener.
 */
public class ListenerConfiguration extends SslConfiguration {

    public static final String DEFAULT_KEY = "default";

    /**
     * @deprecated
     * @return the default listener configuration.
     */
    @Deprecated
    public static ListenerConfiguration getDefault() {
        ListenerConfiguration defaultConfig;
        defaultConfig = new ListenerConfiguration(DEFAULT_KEY, "0.0.0.0", 8080);
        return defaultConfig;
    }
    private String id = DEFAULT_KEY;
    private String host = "0.0.0.0";
    private int port = 9090;
    private ChunkConfig chunkingConfig = ChunkConfig.AUTO;
    private KeepAliveConfig keepAliveConfig = KeepAliveConfig.AUTO;
    private boolean bindOnStartup = false;
    private String version = "1.1";
    private long socketIdleTimeout;
    private String messageProcessorId;
    private boolean httpTraceLogEnabled;
    private boolean httpAccessLogEnabled;
    private String serverHeader = "wso2-http-transport";
    private List<Parameter> parameters = getDefaultParameters();
    private RequestSizeValidationConfig requestSizeValidationConfig = new RequestSizeValidationConfig();
    private boolean pipeliningEnabled;
    private boolean webSocketCompressionEnabled;
    private long pipeliningLimit;

    public ListenerConfiguration() {
    }

    public ListenerConfiguration(String id, String host, int port) {
        this.id = id;
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isBindOnStartup() {
        return bindOnStartup;
    }

    public void setBindOnStartup(boolean bindOnStartup) {
        this.bindOnStartup = bindOnStartup;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    private List<Parameter> getDefaultParameters() {
        return new ArrayList<>();

    }

    public long getSocketIdleTimeout() {
        return socketIdleTimeout;
    }

    public String getMessageProcessorId() {
        return messageProcessorId;
    }

    public void setMessageProcessorId(String messageProcessorId) {
        this.messageProcessorId = messageProcessorId;
    }

    public void setSocketIdleTimeout(int socketIdleTimeout) {
        this.socketIdleTimeout = socketIdleTimeout;
    }

    public boolean isHttpTraceLogEnabled() {
        return httpTraceLogEnabled;
    }

    public void setHttpTraceLogEnabled(boolean httpTraceLogEnabled) {
        this.httpTraceLogEnabled = httpTraceLogEnabled;
    }

    public boolean isHttpAccessLogEnabled() {
        return httpAccessLogEnabled;
    }

    public void setHttpAccessLogEnabled(boolean httpAccessLogEnabled) {
        this.httpAccessLogEnabled = httpAccessLogEnabled;
    }

    public RequestSizeValidationConfig getRequestSizeValidationConfig() {
        return requestSizeValidationConfig;
    }

    public void setRequestSizeValidationConfig(RequestSizeValidationConfig requestSizeValidationConfig) {
        this.requestSizeValidationConfig = requestSizeValidationConfig;
    }

    public ChunkConfig getChunkConfig() {
        return chunkingConfig;
    }

    public void setChunkConfig(ChunkConfig chunkConfig) {
        this.chunkingConfig = chunkConfig;
    }

    public KeepAliveConfig getKeepAliveConfig() {
        return keepAliveConfig;
    }

    public void setKeepAliveConfig(KeepAliveConfig keepAliveConfig) {
        this.keepAliveConfig = keepAliveConfig;
    }

    public String getServerHeader() {
        return serverHeader;
    }

    public void setServerHeader(String serverHeader) {
        this.serverHeader = serverHeader;
    }

    public boolean isPipeliningEnabled() {
        return pipeliningEnabled;
    }

    public void setPipeliningEnabled(boolean pipeliningEnabled) {
        this.pipeliningEnabled = pipeliningEnabled;
    }

    public long getPipeliningLimit() {
        return pipeliningLimit;
    }

    public void setPipeliningLimit(long pipeliningLimit) {
        this.pipeliningLimit = pipeliningLimit;
    }

    public boolean isWebSocketCompressionEnabled() {
        return webSocketCompressionEnabled;
    }

    public void setWebSocketCompressionEnabled(boolean webSocketCompressionEnabled) {
        this.webSocketCompressionEnabled = webSocketCompressionEnabled;
    }
}
