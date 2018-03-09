/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.toml.model;

/**
 * Describes the proxy object.
 *
 * @since 0.964
 */
public class Proxy {
    private String host;
    private String port;
    private String userName;
    private String password;

    /**
     * Get host name.
     *
     * @return host
     */
    public String getHost() {
        return host;
    }

    /**
     * Set host name.
     *
     * @param host host name
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Get port.
     *
     * @return port proxy server
     */
    public String getPort() {
        return port;
    }

    /**
     * Set the port of the proxy server.
     *
     * @param port port of the proxy
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * Get the username of the proxy server.
     *
     * @return username username of the proxy server
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Set the username of the proxy server.
     *
     * @param userName username of the proxy server
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Ge the password of the proxy server.
     *
     * @return password of the proxy server
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the password for the proxy server.
     *
     * @param password password of the proxy
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
