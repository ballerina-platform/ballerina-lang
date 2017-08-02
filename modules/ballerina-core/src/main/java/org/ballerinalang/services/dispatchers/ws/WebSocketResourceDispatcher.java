/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.ballerinalang.services.dispatchers.ws;

import org.ballerinalang.services.dispatchers.ResourceDispatcher;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.messaging.BinaryCarbonMessage;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.ControlCarbonMessage;
import org.wso2.carbon.messaging.StatusCarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;

import java.util.HashMap;
import java.util.Map;
import javax.websocket.Session;

/**
 * Resource Dispatcher for WebSocket Endpoint.
 */
public class WebSocketResourceDispatcher implements ResourceDispatcher {

    @Override
    public ResourceInfo findResource(ServiceInfo service, CarbonMessage cMsg, CarbonCallback callback) throws
            BallerinaException {
        try {

            // Setting default resource arguments
            Map<String, String> resourceArgumentValues = new HashMap<>();
            cMsg.setProperty(org.ballerinalang.runtime.Constants.RESOURCE_ARGS, resourceArgumentValues);

            if (cMsg instanceof TextCarbonMessage) {
                return getResource(service, Constants.ANNOTATION_NAME_ON_TEXT_MESSAGE);
            } else if (cMsg instanceof BinaryCarbonMessage) {
                throw new BallerinaException("Binary messages are not supported!");
            } else if (cMsg instanceof ControlCarbonMessage) {
                throw new BallerinaException("Pong messages are not supported!");
            } else if (cMsg instanceof StatusCarbonMessage) {
                StatusCarbonMessage statusMessage = (StatusCarbonMessage) cMsg;
                if (org.wso2.carbon.messaging.Constants.STATUS_CLOSE.equals(statusMessage.getStatus())) {
                    Session serverSession = (Session) cMsg.getProperty(Constants.WEBSOCKET_SERVER_SESSION);
                    WebSocketConnectionManager.getInstance().removeSessionFromAll(serverSession);
                    return getResource(service, Constants.ANNOTATION_NAME_ON_CLOSE);
                } else if (org.wso2.carbon.messaging.Constants.STATUS_OPEN.equals(statusMessage.getStatus())) {
                    Session session = (Session) statusMessage.getProperty(Constants.WEBSOCKET_SERVER_SESSION);
                    WebSocketConnectionManager.getInstance().addServerSession(service, session, cMsg);
                    callback.done(cMsg);
                    return getResource(service, Constants.ANNOTATION_NAME_ON_OPEN);
                }
            } else {
                throw new BallerinaException("Cannot identify the message type to dispatch!");
            }
        } catch (Throwable e) {
            throw new BallerinaException("Error occurred in WebSocket resource dispatchers : " + e.getMessage());
        }
        return null;
    }

    @Override
    public String getProtocol() {
        return Constants.PROTOCOL_WEBSOCKET;
    }

    private ResourceInfo getResource(ServiceInfo service, String annotationName) {
        for (ResourceInfo resource : service.getResourceInfoEntries()) {
            if (resource.getAnnotationAttachmentInfo(Constants.WEBSOCKET_PACKAGE_PATH, annotationName) != null) {
                return resource;
            }
        }

        throw new BallerinaException("Cannot find a resource for annotation: " + annotationName);
    }
}
