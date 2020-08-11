/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.contract.config;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * Configurations of proxy server.
 */
public class ProxyServerConfiguration {
    private String proxyHost;

    private int proxyPort;

    private String proxyUsername;

    private String proxyPassword;

    private InetSocketAddress inetSocketAddress;

    public ProxyServerConfiguration(String proxyHost, int proxyPort) throws UnknownHostException {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.inetSocketAddress = new InetSocketAddress(InetAddress.getByName(this.proxyHost), this.proxyPort);
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public InetSocketAddress getInetSocketAddress() {
        return inetSocketAddress;
    }
}
