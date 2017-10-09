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

package org.ballerinalang.net.http;

import org.ballerinalang.net.http.util.ConnectorStartupSynchronizer;
import org.wso2.carbon.transport.http.netty.contract.PortBindingEventListener;
import org.wso2.carbon.transport.http.netty.contract.ServerConnector;

import java.io.PrintStream;

/**
 * An implementation of the LifeCycleEventListener. This can be used to listen to the HTTP connector life cycle events.
 *
 * @since 0.94
 */
public class HttpConnectorPortBindingListener implements PortBindingEventListener {

    private static final PrintStream console = System.out;

    private ConnectorStartupSynchronizer connectorStartupSynchronizer;

    public HttpConnectorPortBindingListener(ConnectorStartupSynchronizer connectorStartupSynchronizer) {
        this.connectorStartupSynchronizer = connectorStartupSynchronizer;
    }

    @Override
    public void onOpen(String host, int port) {
        console.println("ballerina: started server connector " + host + "-" + port);
        connectorStartupSynchronizer.getCountDownLatch().countDown();
    }

    @Override
    public void onClose(ServerConnector serverConnector) {
        console.println("ballerina: stopped server connector " + serverConnector.getConnectorID());
    }
}
