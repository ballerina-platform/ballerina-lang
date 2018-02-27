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
package org.ballerinalang.runtime.threadpool;

import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.impl.BServerConnectorFuture;
import org.ballerinalang.connector.impl.ResourceExecutor;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.tracer.TraceContext;

import java.util.Map;

/**
 * Worker Thread which is responsible for request processing.
 */
public class BallerinaWorkerThread implements Runnable {

    private Resource resource;
    private BValue[] bValues;
    private BServerConnectorFuture connectorFuture;
    private Map<String, Object> properties;
    private TraceContext traceContext;

    public BallerinaWorkerThread(Resource resource, BServerConnectorFuture connectorFuture,
                                 Map<String, Object> properties, TraceContext traceContext, BValue... bValues) {
        this.resource = resource;
        this.connectorFuture = connectorFuture;
        this.properties = properties;
        this.traceContext = traceContext;
        this.bValues = bValues;
    }

    public void run() {
        ResourceExecutor.execute(resource, connectorFuture, properties, traceContext, bValues);
    }
}
