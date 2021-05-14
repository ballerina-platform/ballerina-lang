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
package io.ballerina.projects.internal.model;

/**
 * Describes the proxy object.
 *
 * @since 0.964
 */
public class Proxy {
    private final String host;
    private final int port;
    private final String username;
    private final String password;

    private Proxy(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public static Proxy from(String host, int port, String username, String password) {
        return new Proxy(host, port, username, password);
    }

    public static Proxy from() {
        return new Proxy("", 0, "", "");
    }

    /**
     * Get host name.
     *
     * @return host
     */
    public String host() {
        return host;
    }

    /**
     * Get port.
     *
     * @return port proxy server
     */
    public int port() {
        return port;
    }

    /**
     * Get the username of the proxy server.
     *
     * @return username username of the proxy server
     */
    public String username() {
        return username;
    }

    /**
     * Ge the password of the proxy server.
     *
     * @return password of the proxy server
     */
    public String password() {
        return password;
    }
}
