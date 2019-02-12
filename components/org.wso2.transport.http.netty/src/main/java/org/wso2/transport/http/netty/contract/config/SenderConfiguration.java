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

import org.wso2.transport.http.netty.contractimpl.sender.channel.pool.PoolConfiguration;

/**
 * JAXB representation of the Netty transport sender configuration.
 */
public class SenderConfiguration extends SslConfiguration {

    private static final String DEFAULT_KEY = "netty";

    /**
     * @deprecated
     * @return the default sender configuration.
     */
    @Deprecated
    public static SenderConfiguration getDefault() {
        SenderConfiguration defaultConfig;
        defaultConfig = new SenderConfiguration(DEFAULT_KEY);
        return defaultConfig;
    }

    private String id = DEFAULT_KEY;
    private int socketIdleTimeout = 60000;
    private boolean httpTraceLogEnabled;
    private ChunkConfig chunkingConfig = ChunkConfig.AUTO;
    private KeepAliveConfig keepAliveConfig = KeepAliveConfig.AUTO;
    private boolean forceHttp2 = false;
    private String httpVersion = "1.1";
    private ProxyServerConfiguration proxyServerConfiguration;
    private PoolConfiguration poolConfiguration;

    private ForwardedExtensionConfig forwardedExtensionConfig = ForwardedExtensionConfig.DISABLE;

    public SenderConfiguration() {
        this.poolConfiguration = new PoolConfiguration();
    }

    public SenderConfiguration(String id) {
        this.id = id;
        this.poolConfiguration = new PoolConfiguration();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getSocketIdleTimeout(int defaultValue) {
        if (socketIdleTimeout == 0) {
            return defaultValue;
        }
        return socketIdleTimeout;
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

    public ChunkConfig getChunkingConfig() {
        return chunkingConfig;
    }

    public void setChunkingConfig(ChunkConfig chunkingConfig) {
        this.chunkingConfig = chunkingConfig;
    }

    public KeepAliveConfig getKeepAliveConfig() {
        return keepAliveConfig;
    }

    public void setKeepAliveConfig(KeepAliveConfig keepAliveConfig) {
        this.keepAliveConfig = keepAliveConfig;
    }

    public void setProxyServerConfiguration(ProxyServerConfiguration proxyServerConfiguration) {
        this.proxyServerConfiguration = proxyServerConfiguration;
    }

    public ProxyServerConfiguration getProxyServerConfiguration() {
        return proxyServerConfiguration;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public void setHttpVersion(String httpVersion) {
        if (!httpVersion.isEmpty()) {
            this.httpVersion = httpVersion;
        }
    }

    public boolean isForceHttp2() {
        return forceHttp2;
    }

    public void setForceHttp2(boolean forceHttp2) {
        this.forceHttp2 = forceHttp2;
    }

    public PoolConfiguration getPoolConfiguration() {
        return poolConfiguration;
    }

    public void setPoolConfiguration(PoolConfiguration poolConfiguration) {
        this.poolConfiguration = poolConfiguration;
    }

    public ForwardedExtensionConfig getForwardedExtensionConfig() {
        return forwardedExtensionConfig;
    }

    public void setForwardedExtensionConfig(ForwardedExtensionConfig forwardedExtensionEnabled) {
        this.forwardedExtensionConfig = forwardedExtensionEnabled;
    }
}
