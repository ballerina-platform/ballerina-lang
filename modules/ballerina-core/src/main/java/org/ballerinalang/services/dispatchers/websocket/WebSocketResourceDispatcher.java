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

package org.ballerinalang.services.dispatchers.websocket;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.Resource;
import org.ballerinalang.model.Service;
import org.ballerinalang.services.dispatchers.ResourceDispatcher;
import org.ballerinalang.services.dispatchers.http.Constants;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.messaging.BinaryCarbonMessage;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.ControlCarbonMessage;
import org.wso2.carbon.messaging.StatusCarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;

/**
 * Resource Dispatcher for WebSocket Endpoint
 *
 * @since 0.8.0
 */
public class WebSocketResourceDispatcher implements ResourceDispatcher {

    @Override
    public Resource findResource(Service service, CarbonMessage cMsg, CarbonCallback callback, Context balContext)
            throws BallerinaException {

        try {
            if (cMsg instanceof TextCarbonMessage) {
                return getResource(service, Constants.ANNOTATION_NAME_ON_TEXT_MESSAGE);
            } else if (cMsg instanceof BinaryCarbonMessage) {
                return getResource(service, Constants.ANNOTATION_NAME_ON_BINARY_MESSAGE);
            } else if (cMsg instanceof ControlCarbonMessage) {
                return getResource(service, Constants.ANNOTATION_NAME_ON_PONG_MESSAGE);
            } else if (cMsg instanceof StatusCarbonMessage) {
                StatusCarbonMessage statusMessage = (StatusCarbonMessage) cMsg;
                if (org.wso2.carbon.messaging.Constants.STATUS_CLOSE.equals(statusMessage.getStatus())) {
                    return getResource(service, Constants.ANNOTATION_NAME_ON_CLOSE);
                } else if (org.wso2.carbon.messaging.Constants.STATUS_OPEN.equals(statusMessage.getStatus())) {
                    String connection = (String) cMsg.getProperty(Constants.CONNECTION);
                    String upgrade = (String) cMsg.getProperty(Constants.UPGRADE);

                    /* If the connection is WebSocket upgrade, this block will be executed */
                    if (connection != null && upgrade != null &&
                            Constants.UPGRADE.equals(connection) && Constants.WEBSOCKET_UPGRADE.equals(upgrade)) {
                        return getResource(service, Constants.ANNOTATION_NAME_ON_OPEN);
                    }
                }
            }
        } catch (Throwable e) {
            throw new BallerinaException("Error occurred in WebSocket resource dispatchers : " + e.getMessage(),
                                         balContext);
        }
        throw new BallerinaException("No matching Resource found for dispatchers.");
    }

    @Override
    public String getProtocol() {
        return Constants.PROTOCOL_WEBSOCKET;
    }

    private Resource getResource(Service service, String annotationName) {
        for (Resource resource: service.getResources()) {
            if (resource.getAnnotation(Constants.PROTOCOL_WEBSOCKET, annotationName) != null) {
                return resource;
            }
        }
        return null;
    }
}
