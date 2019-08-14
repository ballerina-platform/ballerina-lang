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
     * Get finished failover value.
     *
     * @return finishedFailover
     */
    public boolean isFinishedFailover() {
        return finishedFailover;
    }

    /**
     * Assign the finishedFailover of the FailoverContext to the variable finishedFailover.
     *
     * @param finishedFailover if true, do failover in the remaining url.
     */
    public void setFinishedFailover(boolean finishedFailover) {
        this.finishedFailover = finishedFailover;
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
     * Assign the index of the FailoverContext to the variable index.
     *
     * @param currentIndex a current index.
     */
    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    /**
     * Get target urls.
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
     * the variable failoverInterval.
     *
     * @param failoverInterval a failover interval.
     */
    public void setFailoverInterval(int failoverInterval) {
        this.failoverInterval = failoverInterval;
    }

    /**
     * Assign the failover interval of the FailoverContext to the variable failoverInterval.
     * @return failoverInterval
     */
    public int getFailoverInterval() {
        return failoverInterval;
    }

    /**
     * Get connectionMade.
     *
     * @return connectionMade
     */
    public boolean isConnectionMade() {
        return connectionMade;
    }

    /**
     * Assign the connection state of the FailoverContext to the variable connectionMade.
     */
    public void setConnectionMade() {
        this.connectionMade = true;
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
     * Assign the initialIndex of the FailoverContext to the variable initialIndex.
     *
     * @param initialIndex a initial index.
     */
    public void setInitialIndex(int initialIndex) {
        this.initialIndex = initialIndex;
    }

}
