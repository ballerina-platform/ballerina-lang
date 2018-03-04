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

import org.ballerinalang.connector.impl.BServerConnectorFuture;
import org.ballerinalang.connector.impl.ResourceExecutor;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.runtime.threadpool.BallerinaWorkerThread;
import org.ballerinalang.runtime.threadpool.ThreadPoolFactory;
import org.ballerinalang.util.tracer.BTracer;

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
     * @param resource   to be executed.
     * @param properties to be passed to context.
     * @param values     required for the resource.
     * @return future object to listen to events if any.
     */
    public static ConnectorFuture execute(Resource resource, Map<String, Object> properties,
                                          BTracer bTracer, BValue... values) {
        BServerConnectorFuture connectorFuture = new BServerConnectorFuture();
        ResourceExecutor.execute(resource, connectorFuture, properties, bTracer, values);
        return connectorFuture;
    }

    /**
     * This method will execute Ballerina resource in non-blocking manner.
     * It will use Ballerina worker-pool for the execution and will return the
     * connector thread immediately.
     *
     * @param resource   to be executed.
     * @param properties to be passed to context.
     * @param values     required for the resource.
     * @return future object to listen to events if any.
     */
    public static ConnectorFuture submit(Resource resource, Map<String, Object> properties,
                                         BTracer bTracer, BValue... values) {
        BServerConnectorFuture connectorFuture = new BServerConnectorFuture();
        ThreadPoolFactory.getInstance().getExecutor().
                execute(new BallerinaWorkerThread(resource, connectorFuture, properties, bTracer, values));
        return connectorFuture;
    }

}
