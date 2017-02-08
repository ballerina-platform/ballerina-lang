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

package org.wso2.ballerina.core.nativeimpl.connectors.jms.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.nativeimpl.connectors.jms.Constants;
import org.wso2.ballerina.core.runtime.dispatching.ResourceDispatcher;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.jms.utils.JMSConstants;

/**
 * Dispatcher that handles the resources of a JMS Service.
 */
public class JMSResourceDispatcher implements ResourceDispatcher {
    private static final Logger log = LoggerFactory.getLogger(JMSResourceDispatcher.class);

    @Override
    public Resource findResource(Service service, CarbonMessage cMsg, CarbonCallback callback, Context balContext)
            throws BallerinaException {
        if (log.isDebugEnabled()) {
            log.debug("Starting to find resource in the jms service " + service.getSymbolName().toString() + " to "
                            + "deliver the message");
        }
        for (Resource resource : service.getResources()) {
            if (resource.getAnnotation(Constants.ANNOTATION_NAME_ONMESSAGE) != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Found the relevant resource in the jms service " + service.getSymbolName().toString());
                }
                return resource;
            }
        }
        throw new BallerinaException("Resource to handle the jms message is not found in jms service " + service
                .getSymbolName().toString(), balContext);
    }

    @Override
    public String getProtocol() {
        return JMSConstants.PROTOCOL_JMS;
    }
}
