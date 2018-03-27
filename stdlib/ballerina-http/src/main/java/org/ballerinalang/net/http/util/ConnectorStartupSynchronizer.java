/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * A utility class for facilitating the parallel start up of connectors. Any exceptions which occur during connector
 * start up can be wrapped in this class and later thrown in the Ballerina side.
 *
 * @since 0.94
 */
public class ConnectorStartupSynchronizer {

    private List<String> inUseConnectors = new ArrayList<>();
    private Map<String, Exception> exceptions = new HashMap<>();
    private CountDownLatch connectorStartupLatch;

    public ConnectorStartupSynchronizer(int noOfConnectors) {
        this.connectorStartupLatch = new CountDownLatch(noOfConnectors);
    }

    public void addServerConnector(String connectorId) {
        inUseConnectors.add(connectorId);
        connectorStartupLatch.countDown();
    }

    public Iterator<String> inUseConnectorsIterator() {
        return inUseConnectors.iterator();
    }

    public void addException(String connectorId, Exception ex) {
        exceptions.put(connectorId, ex);
        connectorStartupLatch.countDown();
    }

    public Iterator<Map.Entry<String, Exception>> failedConnectorsIterator() {
        return exceptions.entrySet().iterator();
    }

    public int getNoOfFailedConnectors() {
        return exceptions.size();
    }

    public void syncConnectors() throws InterruptedException {
        connectorStartupLatch.await();
    }
}
