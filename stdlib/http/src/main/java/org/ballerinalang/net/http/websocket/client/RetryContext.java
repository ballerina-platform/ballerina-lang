/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.http.websocket.client;

/**
 * Represents a retry config.
 *
 * @since 1.2.0
 */
public class RetryContext {

    private int interval = 0;
    private Double backOfFactor = 0.0;
    private int maxInterval = 0;
    private int maxAttempts = 0;
    private int reconnectAttempts = 0;
    private boolean firstConnectionMadeSuccessfully = false;

    /**
     * Gets the `interval`.
     *
     * @return interval
     */
    public int getInterval() {
        return interval;
    }

    /**
     * Assigns the interval of the `RetryContext` to the `interval` variable.
     *
     * @param interval - the initial index.
     */
    public final void setInterval(int interval) {
        this.interval = interval;
    }

    /**
     * Gets the `backOfFactor`.
     *
     * @return backOfFactor
     */
    public Double getBackOfFactor() {
        return backOfFactor;
    }

    /**
     * Assigns the` backOfFactor` of the `RetryContext` to the `backOfFactor` variable.
     *
     * @param backOfFactor - the rate of increase of the reconnect delay.
     */
    void setBackOfFactor(Double backOfFactor) {
        this.backOfFactor = backOfFactor;
    }

    /**
     * Gets the `maxInterval`.
     *
     * @return maximum interval
     */
    public int getMaxInterval() {
        return maxInterval;
    }

    /**
     * Assigns the `maxInterval` of the `RetryContext` to the `maxInterval` variable.
     *
     * @param maxInterval - the maximum time of the retry interval.
     */
    void setMaxInterval(int maxInterval) {
        this.maxInterval = maxInterval;
    }

    /**
     * Gets the `maxAttempts`.
     *
     * @return no of maximum attempts
     */
    public int getMaxAttempts() {
        return maxAttempts;
    }

    /**
     * Assigns the `maxAttempts` of the `RetryContext` to the `maxAttempts` variable.
     *
     * @param maxAttempts - the maximum number of retry attempts.
     */
    void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    /**
     * Gets the `reconnectAttempts`.
     *
     * @return no of reconnect attempts
     */
    public int getReconnectAttempts() {
        return reconnectAttempts;
    }

    /**
     * Assigns the `reconnectAttempts` of the `RetryContext` to the `reconnectAttempts` variable.
     *
     * @param reconnectAttempts - the no of reconnect attempts.
     */
    public void setReconnectAttempts(int reconnectAttempts) {
        this.reconnectAttempts = reconnectAttempts;
    }

    /**
     * Gets the `firstConnectionMadeSuccessfully`.
     *
     * @return firstConnectionMadeSuccessfully
     */
    boolean isFirstConnectionMadeSuccessfully() {
        return firstConnectionMadeSuccessfully;
    }

    /**
     * Assigns the connection state of the `RetryContext` to the `firstConnectionMadeSuccessfully` variable.
     */
    void setFirstConnectionMadeSuccessfully() {
        this.firstConnectionMadeSuccessfully = true;
    }
}
