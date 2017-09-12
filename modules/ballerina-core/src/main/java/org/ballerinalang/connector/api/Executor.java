/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.connector.api;

import org.ballerinalang.connector.impl.BConnectorFuture;
import org.ballerinalang.connector.impl.BallerinaOldWorkerThread;
import org.ballerinalang.connector.impl.BallerinaWorkerThread;
import org.ballerinalang.connector.impl.ResourceExecutor;
import org.ballerinalang.connector.impl.ServerConnectorRegistry;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.runtime.threadpool.ThreadPoolFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.Map;

/**
 * {@code Executor} Is the entry point from server connector side to ballerina side.
 * After doing the dispatching and finding the resource, server connector implementations can use
 * this API to invoke Ballerina engine.
 *
 * @since 0.94
 */
public class Executor {

    /**
     * This method will execute Ballerina resource in blocking manner.
     * So connector thread will have to wait until execution finishes.
     *
     * @param resource  to be executed.
     * @param values    required for the resource.
     * @return future object to listen to events if any.
     */
    public static ConnectorFuture execute(Resource resource, BValue... values) {
        ConnectorFuture connectorFuture = new BConnectorFuture();
        ResourceExecutor.execute(resource, connectorFuture, values);
        return connectorFuture;
    }

    /**
     * This method will execute Ballerina resource in non-blocking manner.
     * It will use Ballerina worker-pool for the execution and will return the
     * connector thread immediately.
     *
     * @param resource  to be executed.
     * @param values    required for the resource.
     * @return future object to listen to events if any.
     */
    public static ConnectorFuture submit(Resource resource, BValue... values) {
        BConnectorFuture connectorFuture = new BConnectorFuture();
        ThreadPoolFactory.getInstance().getExecutor().
                execute(new BallerinaWorkerThread(resource, connectorFuture, values));
        return connectorFuture;
    }

    //Temp method until resource signatures are changed(no thread pool)
    public static BConnectorFuture execute(Resource resource, Map<String, Object> properties, BValue... values) {
        BConnectorFuture connectorFuture = new BConnectorFuture();
        ResourceExecutor.execute(resource, connectorFuture, properties, values);
        return connectorFuture;
    }

    //Temp method until resource signatures are changed(with thread pooling)
    public static ConnectorFuture submit(Resource resource, Map<String, Object> properties, BValue... values) {
        BConnectorFuture connectorFuture = new BConnectorFuture();
        ThreadPoolFactory.getInstance().getExecutor().
                execute(new BallerinaOldWorkerThread(resource, connectorFuture, properties, values));
        return connectorFuture;
    }

    /**
     * This method can be used to access the {@code BallerinaServerConnector} object which is at
     * Ballerina level.
     *
     * @param protocolPkgPath   of the server connector.
     * @return  ballerinaServerConnector object.
     */
    public static BallerinaServerConnector getBallerinaServerConnector(String protocolPkgPath) {
        return ServerConnectorRegistry.getInstance().getBallerinaServerConnector(protocolPkgPath);
    }
}
