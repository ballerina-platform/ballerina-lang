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

package org.ballerinalang.services.dispatchers.file;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.Resource;
import org.ballerinalang.model.Service;
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
public class FileResourceDispatcher implements ResourceDispatcher {
    private static final Logger log = LoggerFactory.getLogger(FileResourceDispatcher.class);

    @Override
    @Deprecated
    public Resource findResource(Service service, CarbonMessage cMsg, CarbonCallback callback,
                                 Context balContext) throws BallerinaException {
        return null;
    }

    @Override
    public ResourceInfo findResource(ServiceInfo service, CarbonMessage cMsg, CarbonCallback callback) throws
                                                                                                       BallerinaException {
        if (log.isDebugEnabled()) {
            log.debug("Starting to find resource in the file service " + service.getName() + " to "
                              + "deliver the message");
        }
        if (cMsg.getProperty(Constants.FILE_TRANSPORT_EVENT_NAME).equals(Constants.FILE_UPDATE)) {
            return getResource(service, Constants.ANNOTATION_NAME_ON_UPDATE);
        } else if (cMsg.getProperty(Constants.FILE_TRANSPORT_EVENT_NAME).equals(Constants.FILE_ROTATE)) {
            return getResource(service, Constants.ANNOTATION_NAME_ON_ROTATE);
        } else {
            throw new BallerinaException("no matching resource found");
        }
    }

    @Override
    public String getProtocol() {
        return Constants.PROTOCOL_FILE;
    }

    private ResourceInfo getResource(ServiceInfo service, String annotationName) {
        for (ResourceInfo resource : service.getResourceInfoList()) {
            if (resource.getAnnotationAttachmentInfo(Constants.FILE_PACKAGE_NAME, annotationName) != null) {
                return resource;
            }
        }
        return null;
    }
}
