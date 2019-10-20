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

import java.util.ArrayList;

/**
 * Represents a failover client connector config.
 */
public class FailoverContext {

    private boolean finishedFailover = false;
    private int currentIndex = 0;
    private int failoverInterval;
    private boolean connectionMade = false;
    private ArrayList<String> targetUrls;
    private int initialIndex = 0;

    /**
     * Get the value of completed failover.
     *
     * @return finishedFailover
     */
    public boolean isFailoverFinished() {
        return finishedFailover;
    }

    /**
     * Assign the finishedFailover of the FailoverContext to the variable finishedFailover.
     *
     * @param finishedFailover  if it is true, do reconnect in the target URL.
     */
    public void setFailoverFinished(boolean finishedFailover) {
        this.finishedFailover = finishedFailover;
    }

    /**
     * Get the index.
     *
     * @return currentIndex
     */
    public int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * Assigns the index of the FailoverContext to the variable index.
     *
     * @param currentIndex a current index.
     */
    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    /**
     * Gets the target URLs.
     *
     * @return targetUrls
     */
    public ArrayList<String> getTargetUrls() {
        return targetUrls;
    }

    /**
     * Assign the target urls of the FailoverContext to the variable targetUrls.
     *
     * @param targetUrls a target urls.
     */
    public void setTargetUrls(ArrayList<String> targetUrls) {
        this.targetUrls = targetUrls;
    }

    /**
     * Assign the failover interval of the FailoverContext to
     * the `failoverInterval` variable.
     *
     * @param failoverInterval a failover interval.
     */
    public void setFailoverInterval(int failoverInterval) {
        this.failoverInterval = failoverInterval;
    }

    /**
     * Assigns the failover interval of the `FailoverContext` to the `failoverInterval` variable.
     * @return failoverInterval
     */
    public int getFailoverInterval() {
        return failoverInterval;
    }

    /**
     * Gets the `connectionMade`.
     *
     * @return connectionMade
     */
    public boolean isConnectionMade() {
        return connectionMade;
    }

    /**
     * Assigns the connection state of the FailoverContext to the `connectionMade` variable.
     */
    public void setConnectionMade() {
        this.connectionMade = true;
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
     * Assigns the `initialIndex` of the FailoverContext to the `initialIndex` variable.
     *
     * @param initialIndex a initial index.
     */
    public void setInitialIndex(int initialIndex) {
        this.initialIndex = initialIndex;
    }
}
