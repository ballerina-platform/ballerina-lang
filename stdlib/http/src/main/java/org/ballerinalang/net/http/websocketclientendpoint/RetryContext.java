/*
 * Copyright (c) 2019, WSO2 Inc. (http:www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http:www.apache.orglicensesLICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specif ic language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http.websocketclientendpoint;

/**
 * Represents a retry config.
 */
public class RetryContext {

    private int interval;
    private float backOfFactor;
    private int maxInterval;
    private int maxAttempts;
    private int reconnectAttempts = 0;
    private int initialIndex = 0;
    private int currentIndex = 0;
    private boolean connectionMade = false;

    /**
     * Get webSocket connector factory.
     *
     * @return interval
     */
    public int getInterval() {
        return interval;
    }

    /**
     * Assign the interval of the RetryContext to the variable interval.
     *
     * @param interval a initial index.
     */
    public void setInterval(int interval) {
        this.interval = interval;
    }

    /**
     * Get backOfFactor.
     *
     * @return backOfFactor
     */
    public float getBackOfFactor() {
        return backOfFactor;
    }

    /**
     * Assign the backOfFactor of the RetryContext to the variable backOfFactor.
     *
     * @param backOfFactor a initial index.
     */
    public void setBackOfFactor(float backOfFactor) {
        this.backOfFactor = backOfFactor;
    }

    /**
     * Get maxInterval.
     *
     * @return maxInterval
     */
    public int getMaxInterval() {
        return maxInterval;
    }

    /**
     * Assign the maxInterval of the RetryContext to the variable maxInterval.
     *
     * @param maxInterval a index.
     */
    public void setMaxInterval(int maxInterval) {
        this.maxInterval = maxInterval;
    }

    /**
     * Get maxAttempts.
     *
     * @return maxAttempts
     */
    public int getMaxAttempts() {
        return maxAttempts;
    }

    /**
     * Assign the maxAttempts of the RetryContext to the variable maxAttempts.
     *
     * @param maxAttempts a index.
     */
    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    /**
     * Get reconnectAttempts.
     *
     * @return reconnectAttempts
     */
    public int getReconnectAttempts() {
        return reconnectAttempts;
    }

    /**
     * Assign the reconnectAttempts of the RetryContext to the variable reconnectAttempts.
     *
     * @param reconnectAttempts a index.
     */
    public void setReconnectAttempts(int reconnectAttempts) {
        this.reconnectAttempts = reconnectAttempts;
    }

    /**
     * Get initial index.
     *
     * @return initialIndex
     */
    public int getInitialIndex() {
        return initialIndex;
    }

    /**
     * Assign the initialIndex of the RetryContext to the variable initialIndex.
     *
     * @param initialIndex a initial index.
     */
    public void setInitialIndex(int initialIndex) {
        this.initialIndex = initialIndex;
    }

    /**
     * Get index.
     *
     * @return currentIndex
     */
    public int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * Assign the index of the RetryContext to the variable index.
     *
     * @param index a index.
     */
    public void setCurrentIndex(int index) {
        this.currentIndex = index;
    }

    /**
     * Get connectionMade.
     *
     * @return connectionMade
     */
    public boolean getConnectionState() {
        return connectionMade;
    }

    /**
     * Assign the connection state of the RetryContext to the variable connectionMade.
     */
    public void setConnectionState() {
        this.connectionMade = true;
    }
}
