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
package org.ballerinalang.connector.impl;

import org.ballerinalang.connector.api.Resource;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Worker Thread which is responsible for request processing.
 */
public class BallerinaOldWorkerThread1 implements Runnable {

    private Resource resource;
    private CarbonMessage resourceMessage;
    private CarbonCallback resourceCallback;

    public BallerinaOldWorkerThread1(Resource resource, CarbonMessage carbonMessage,
                                     CarbonCallback carbonCallback) {
        this.resource = resource;
        this.resourceMessage = carbonMessage;
        this.resourceCallback = carbonCallback;
    }

    public void run() {
        ResourceExecutor.execute(resource, resourceMessage, resourceCallback);
    }
}
