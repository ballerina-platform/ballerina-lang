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

package org.ballerinalang.services.dispatchers.fs;

import org.ballerinalang.services.dispatchers.ResourceDispatcher;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Resource level dispatchers handler for file protocol.
 */
public class FileSystemResourceDispatcher implements ResourceDispatcher {
    private static final Logger log = LoggerFactory.getLogger(FileSystemResourceDispatcher.class);

    @Override
    public ResourceInfo findResource(ServiceInfo service, CarbonMessage cMsg, CarbonCallback callback)
            throws BallerinaException {
        if (log.isDebugEnabled()) {
            log.debug("Starting to find resource in the file service " + service.getName() + " to " +
                      "deliver the message");
        }
        ResourceInfo[] resourceInfoList = service.getResourceInfoEntries();
        if (resourceInfoList.length != 1) {
            throw new BallerinaException("A service bound to the '" + Constants.PROTOCOL_FILE_SYSTEM +
                                                 "' protocol has to have only one resource associated to itself. " +
                                                 "Found " +
                                                 resourceInfoList.length + " resources in service: " +
                                                 service.getName());
        }
        return resourceInfoList[0];
    }

    @Override
    public String getProtocol() {
        return Constants.PROTOCOL_FILE_SYSTEM;
    }
}
