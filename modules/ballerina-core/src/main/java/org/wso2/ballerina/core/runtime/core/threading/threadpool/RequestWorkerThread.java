/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.core.runtime.core.threading.threadpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.runtime.Constants;
import org.wso2.ballerina.core.runtime.core.BalCallback;
import org.wso2.ballerina.core.runtime.core.BalContext;
import org.wso2.ballerina.core.runtime.core.dispatching.ServiceDispatcher;
import org.wso2.ballerina.core.runtime.registry.DispatcherRegistry;

/**
 * Worker Thread which is responsible for request processing
 */
public class RequestWorkerThread extends WorkerThread {

    private static final Logger logger = LoggerFactory.getLogger(RequestWorkerThread.class);


    public RequestWorkerThread(BalContext context, BalCallback callback) {
        super(context, callback);
    }

    public void run() {

        String protocol = (String) context.getProperty(Constants.PROTOCOL);

        ServiceDispatcher dispatcher = DispatcherRegistry.getInstance().getServiceDispatcher(protocol);
        if (dispatcher == null) {
            logger.error("No service dispatcher available to handle protocol : " + protocol);
            //TODO: Handle error
            return;
        }

        dispatcher.dispatch(context, callback);

    }
}
