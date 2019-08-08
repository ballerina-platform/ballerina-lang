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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;

import java.util.ArrayList;

/**
 * Represents a failover client connector config.
 */
public class FailoverClientConnectorConfig {
    private HttpWsConnectorFactory connectorFactory;
    private int omittedUrlIndex;
    private int currentIndex;
    private int failoverInterval;
    private boolean isConnectionMade = false;
    private ArrayList<String> targetUrls;
    private int initialIndex;
    private static final Logger logger = LoggerFactory.getLogger(RetryConnectorConfig.class);

    /**
     * Get webSocket connector factory.
     *
     * @return connectorFactory
     */
    public HttpWsConnectorFactory getConnectorFactory() {
        return connectorFactory;
    }

    /**
     * Assign the webSocket connector factory of the FailoverClientConnectorConfig to the
     * variable connectorFactory.
     *
     * @param connectorFactory a connector factory
     */
    public void setConnectorFactory(HttpWsConnectorFactory connectorFactory) {
        this.connectorFactory = connectorFactory;
    }

    /**
     * Get initial index.
     *
     * @return initialIndex
     */
    public int getOmittedUrlIndex() {
        return omittedUrlIndex;
    }

    /**
     * Assign the initialIndex of the FailoverClientConnectorConfig to the variable initialIndex.
     *
     * @param urlIndex a omitted  url index.
     */
    public void setOmittedUrlIndex(int urlIndex) {
        this.omittedUrlIndex = urlIndex;
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
     * Assign the index of the FailoverClientConnectorConfig to the variable index.
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
     * Assign the target urls of the FailoverClientConnectorConfig to the variable targetUrls.
     *
     * @param targetUrls a target urls.
     */
    public void setTargetUrls(ArrayList<String> targetUrls) {
        this.targetUrls = targetUrls;
    }

    /**
     * Assign the failover interval of the FailoverClientConnectorConfig to
     * the variable failoverInterval.
     *
     * @param failoverInterval a failover interval.
     */
    public void setFailoverInterval(int failoverInterval) {
        if (failoverInterval < 0) {
            logger.warn("The maxInterval's value set for the configuration needs to be " +
                    "greater than -1. The " + failoverInterval + "value is set to 1.0");
            failoverInterval = 1000;
        }
        this.failoverInterval = failoverInterval;
    }

    /**
     * Assign the failover interval of the FailoverClientConnectorConfig to the variable failoverInterval.
     * @return failoverInterval
     */
    public int getFailoverInterval() {
        return failoverInterval;
    }

    /**
     * Get isConnectionMade.
     *
     * @return isConnectionMade
     */
    public boolean getConnectionState() {
        return isConnectionMade;
    }

    /**
     * Assign the connection state of the FailoverClientConnectorConfig to the variable isConnectionMade.
     */
    public void setConnectionState() {
        this.isConnectionMade = true;
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
     * Assign the initialIndex of the FailoverClientConnectorConfig to the variable initialIndex.
     *
     * @param initialIndex a initial index.
     */
    public void setInitialIndex(int initialIndex) {
        this.initialIndex = initialIndex;
    }

}
