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
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http.websocket.client;

/**
 * Represents a retry config.
 */
public class RetryContext {

    private int interval = 0;
    private float backOfFactor = 0.0F;
    private int maxInterval = 0;
    private int maxAttempts = 0;
    private int reconnectAttempts = 0;
    private int initialIndex = 0;
    private int currentIndex = 0;
    private boolean firstConnectionMadeSuccessfully = false;

    /**
     * Get the `interval`.
     *
     * @return interval
     */
    public int getInterval() {
        return interval;
    }

    /**
     * Assigns the interval of the `RetryContext` to the `interval` variable.
     *
     * @param interval a initial index.
     */
    public final void setInterval(int interval) {
        this.interval = interval;
    }

    /**
     * Get the `backOfFactor`.
     *
     * @return backOfFactor
     */
    public float getBackOfFactor() {
        return backOfFactor;
    }

    /**
     * Assign the` backOfFactor` of the `RetryContext` to the `backOfFactor` variable.
     *
     * @param backOfFactor a initial index.
     */
    void setBackOfFactor(float backOfFactor) {
        this.backOfFactor = backOfFactor;
    }

    /**
     * Gets the `maxInterval`.
     *
     * @return maxInterval
     */
    public int getMaxInterval() {
        return maxInterval;
    }

    /**
     * Assigns the `maxInterval` of the `RetryContext` to the `maxInterval` variable.
     *
     * @param maxInterval a index.
     */
    void setMaxInterval(int maxInterval) {
        this.maxInterval = maxInterval;
    }

    /**
     * Gets the `maxAttempts`.
     *
     * @return maxAttempts
     */
    public int getMaxAttempts() {
        return maxAttempts;
    }

    /**
     * Assign the `maxAttempts` of the `RetryContext` to the `maxAttempts` variable.
     *
     * @param maxAttempts a index.
     */
    void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    /**
     * Gets the `reconnectAttempts`.
     *
     * @return reconnectAttempts
     */
    public int getReconnectAttempts() {
        return reconnectAttempts;
    }

    /**
     * Assigns the `reconnectAttempts` of the `RetryContext` to the `reconnectAttempts` variable.
     *
     * @param reconnectAttempts a index.
     */
    public void setReconnectAttempts(int reconnectAttempts) {
        this.reconnectAttempts = reconnectAttempts;
    }

    /**
     * Gets the initial index.
     *
     * @return initialIndex
     */
    public int getInitialIndex() {
        return initialIndex;
    }

    /**
     * Assigns the initial Index of the RetryContext to the `initialIndex` variable.
     *
     * @param initialIndex a initial index.
     */
    public void setInitialIndex(int initialIndex) {
        this.initialIndex = initialIndex;
    }

    /**
     * Gets the index.
     *
     * @return currentIndex
     */
    public int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * Assigns the index of the `RetryContext` to the `index` variable.
     *
     * @param index a index.
     */
    public void setCurrentIndex(int index) {
        this.currentIndex = index;
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

    RetryContext() {
    }
}
