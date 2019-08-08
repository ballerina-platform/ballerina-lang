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
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnector;

import java.util.ArrayList;

/**
 * Represents a retry config.
 */
public class RetryConnectorConfig {
    private HttpWsConnectorFactory connectorFactory;
    private int interval;
    private float backOfFactor;
    private int maxInterval;
    private int maxAttempts;
    private int reconnectAttempts;
    private int initialIndex;
    private int currentIndex;
    private boolean isConnectionMade = false;
    private ArrayList<String> targetUrls;
    private WebSocketClientConnector clientConnector;
    private int omittedUrlIndex;
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
     * Assign the webSocket connector factory of the RetryConnectorConfig to the variable connectorFactory.
     *
     * @param connectorFactory a connector factory
     */
    public void setConnectorFactory(HttpWsConnectorFactory connectorFactory) {
        this.connectorFactory = connectorFactory;
    }

    /**
     * Get webSocket connector factory.
     *
     * @return interval
     */
    public int getInterval() {
        return interval;
    }

    /**
     * Assign the interval of the RetryConnectorConfig to the variable interval.
     *
     * @param interval a initial index.
     */
    public void setInterval(int interval) {
        if (interval < 0) {
            logger.warn("The interval's value set for the configuration needs to be " +
                    "greater than -1. The interval[" + interval + "] value is set to 1000");
            interval = 1000;
        }
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
     * Assign the backOfFactor of the RetryConnectorConfig to the variable backOfFactor.
     *
     * @param backOfFactor a initial index.
     */
    public void setBackOfFactor(float backOfFactor) {
        if (backOfFactor < 0) {
            logger.warn("The decay's value set for the configuration needs to be " +
                    "greater than -1. The backOfFactor[" + backOfFactor + "] value is set to 1.0");
            backOfFactor = (float) 1.0;
        }
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
     * Assign the maxInterval of the RetryConnectorConfig to the variable maxInterval.
     *
     * @param maxInterval a index.
     */
    public void setMaxInterval(int maxInterval) {
        if (maxInterval < 0) {
            logger.warn("The maxInterval's value set for the configuration needs to be " +
                    "greater than -1. The maxInterval[" + maxInterval + "] value is set to 30000");
            maxInterval =  30000;
        }
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
     * Assign the maxAttempts of the RetryConnectorConfig to the variable maxAttempts.
     *
     * @param maxAttempts a index.
     */
    public void setMaxAttempts(int maxAttempts) {
        if (maxAttempts < 0) {
            logger.warn("The maximum reconnect attempt's value set for the configuration needs to be " +
                    "greater than -1. The maxAttempts[ " + maxAttempts + "] value is set to 0");
            maxAttempts = 0;
        }
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
     * Assign the reconnectAttempts of the RetryConnectorConfig to the variable reconnectAttempts.
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
     * Assign the initialIndex of the RetryConnectorConfig to the variable initialIndex.
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
     * Assign the index of the RetryConnectorConfig to the variable index.
     *
     * @param index a index.
     */
    public void setCurrentIndex(int index) {
        this.currentIndex = index;
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
     * Assign the connection state of the RetryConnectorConfig to the variable isConnectionMade.
     */
    public void setConnectionState() {
        this.isConnectionMade = true;
    }

    /**
     * Get isConnectionMade.
     *
     * @return clientConnector
     */
    public WebSocketClientConnector getClientConnector() {
        return clientConnector;
    }

    /**
     * Assign the connection state of the RetryConnectorConfig to the variable isConnectionMade.
     *
     * @param clientConnector a client connector
     */
    public void setClientConnector(WebSocketClientConnector clientConnector) {
        this.clientConnector = clientConnector;
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
     * Assign the target urls of the RetryConnectorConfig to the variable targetUrls.
     *
     * @param targetUrls a target urls.
     */
    public void setTargetUrls(ArrayList<String> targetUrls) {
        this.targetUrls = targetUrls;
    }

    /**
     * Get omitted url index.
     *
     * @return omittedUrlIndex
     */
    public int getOmittedUrlIndex() {
        return omittedUrlIndex;
    }

    /**
     * Assign the omitted url index of the RetryConnectorConfig to the variable omittedUrlIndex.
     *
     * @param urlIndex a omitted  url index.
     */
    public void setOmittedUrlIndex(int urlIndex) {
        this.omittedUrlIndex = urlIndex;
    }

}
