/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
package io.ballerina.projects.internal.model;

/**
 * Defines the properties used in central.
 *
 * @since 0.964
 */
public class Central {
    private String accesstoken = "";
    private int connectTimeout = 60;
    private int readTimeout = 60;
    private int writeTimeout = 60;
    private int callTimeout = 60;


    private Central(String accesstoken, int connectTimeout, int readTimeout, int writeTimeout,
                    int callTimeout) {
        this.accesstoken = accesstoken;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.writeTimeout = writeTimeout;
        this.callTimeout = callTimeout;
    }

    public static Central from(String accesstoken, int connectTimeout, int readTimeout, int writeTimeout,
                               int callTimeout) {
        return new Central(accesstoken, connectTimeout, readTimeout, writeTimeout, callTimeout);
    }

    public static Central from() {
        return new Central("", 60, 60, 60, 0);
    }

    /**
     * Get the access token.
     *
     * @return access token
     */
    public String getAccessToken() {
        return accesstoken;
    }

    /**
     * Get the connection timeout.
     *
     * @return connection timeout
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Get the read timeout.
     *
     * @return read timeout
     */
    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * Get the write timeout.
     *
     * @return write timeout
     */
    public int getWriteTimeout() {
        return writeTimeout;
    }

    /**
     * Get the call timeout.
     *
     * @return call timeout
     */
    public int getCallTimeout() {
        return callTimeout;
    }

    /**
     * Sets the value of the access token.
     *
     * @param accessToken access token
     */
    public void setAccessToken(String accessToken) {
        this.accesstoken = accessToken;
    }
}
