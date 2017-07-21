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

import org.ballerinalang.services.dispatchers.ServiceDispatcher;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Service Dispatcher for WebSocket Endpoint.
 *
 * @since 0.8.0
 */
public class WebSocketServiceDispatcher implements ServiceDispatcher {

    private static final Logger log = LoggerFactory.getLogger(WebSocketServicesRegistry.class);

    @Override
    public ServiceInfo findService(CarbonMessage cMsg, CarbonCallback callback) {
        String interfaceId = getListenerInterface(cMsg);
        boolean isServer = (boolean) cMsg.getProperty(Constants.IS_WEBSOCKET_SERVER);

        if (isServer) {
            String serviceUri = (String) cMsg.getProperty(Constants.TO);
            serviceUri = WebSocketServicesRegistry.getInstance().refactorUri(serviceUri);
            if (serviceUri == null) {
                return null;
            }
            ServiceInfo service = WebSocketServicesRegistry.getInstance().getServiceEndpoint(interfaceId, serviceUri);

            if (service == null) {
                return null;
            } else {
                return service;
            }

        } else {
            String clientServiceName = (String) cMsg.getProperty(Constants.TO);
            ServiceInfo clientService = WebSocketServicesRegistry.getInstance().getClientService(clientServiceName);
            if (clientService == null) {
                return null;
            }
            return clientService;
        }
    }

    @Override
    public String getProtocol() {
        return Constants.PROTOCOL_WEBSOCKET;
    }

    @Override
    public String getProtocolPackage() {
        return Constants.WEBSOCKET_PACKAGE_PATH;
    }

    @Override
    public void serviceRegistered(ServiceInfo service) {
        WebSocketServicesRegistry.getInstance().registerServiceEndpoint(service);
    }

    @Override
    public void serviceUnregistered(ServiceInfo service) {
        WebSocketServicesRegistry.getInstance().unregisterService(service);
    }

    private String getListenerInterface(CarbonMessage cMsg) {
        String interfaceId = (String) cMsg.getProperty(org.wso2.carbon.messaging.Constants.LISTENER_INTERFACE_ID);
        if (interfaceId == null) {
            if (log.isDebugEnabled()) {
                log.debug("Interface id not found on the message, hence using the default interface");
            }
            interfaceId = Constants.DEFAULT_INTERFACE;
        }

        return interfaceId;
    }

}
