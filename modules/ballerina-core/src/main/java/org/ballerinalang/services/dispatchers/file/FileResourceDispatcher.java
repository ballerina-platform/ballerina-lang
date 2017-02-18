/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.services.dispatchers.file;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.Resource;
import org.ballerinalang.model.Service;
import org.ballerinalang.services.dispatchers.ResourceDispatcher;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Resource level dispatchers handler for file protocol
 */
public class FileResourceDispatcher implements ResourceDispatcher {
    private static final Logger log = LoggerFactory.getLogger(FileResourceDispatcher.class);

    @Override
    public Resource findResource(Service service, CarbonMessage cMsg, CarbonCallback callback,
            Context balContext) throws BallerinaException {
        if (log.isDebugEnabled()) {
            log.debug("Starting to find resource in the file service " + service.getSymbolName().toString() + " to "
                    + "deliver the message");
        }
        Resource[] resources = service.getResources();
        if (resources.length != 1) {
            throw new BallerinaException("A Service of type '" + Constants.PROTOCOL_FILE
                    + "' has to have only one resource associated to itself. " + "Found " + resources.length
                    + " resources in Service: " + service.getSymbolName().getName());
        }
        return resources[0];
    }

    @Override
    public String getProtocol() {
        return Constants.PROTOCOL_FILE;
    }
}
