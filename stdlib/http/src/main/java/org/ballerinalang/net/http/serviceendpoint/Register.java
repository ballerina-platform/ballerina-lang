/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.ballerinalang.net.http.serviceendpoint;

import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.http.HTTPServicesRegistry;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.net.http.websocket.WebSocketException;
import org.ballerinalang.net.http.websocket.server.WebSocketServerService;
import org.ballerinalang.net.http.websocket.server.WebSocketServicesRegistry;

/**
 * Register a service to the listener.
 *
 * @since 0.966
 */
public class Register extends AbstractHttpNativeFunction {
    public static Object register(ObjectValue serviceEndpoint, ObjectValue service,
                                  Object annotationData) {

        HTTPServicesRegistry httpServicesRegistry = getHttpServicesRegistry(serviceEndpoint);
        WebSocketServicesRegistry webSocketServicesRegistry = getWebSocketServicesRegistry(serviceEndpoint);
        Strand strand = Scheduler.getStrand();
        httpServicesRegistry.setScheduler(strand.scheduler);

        BType param;
        AttachedFunction[] resourceList = service.getType().getAttachedFunctions();
        try {
            if (resourceList.length > 0 && (param = resourceList[0].getParameterType()[0]) != null) {
                String callerType = param.getQualifiedName();
                if (HttpConstants.HTTP_CALLER_NAME.equals(
                        callerType)) { // TODO fix should work with equals - rajith
                    httpServicesRegistry.registerService(service);
                } else if (WebSocketConstants.WEBSOCKET_CALLER_NAME.equals(callerType)) {
                    webSocketServicesRegistry.registerService(new WebSocketServerService(service, strand.scheduler));
                } else if (WebSocketConstants.FULL_WEBSOCKET_CLIENT_NAME.equals(callerType)) {
                    return new WebSocketException("Client service cannot be attached to the Listener");
                } else {
                    return HttpUtil.createHttpError("Invalid http Service");
                }
            } else {
                httpServicesRegistry.registerService(service);
            }
        } catch (WebSocketException ex) {
            return ex;
        }
        return null;
    }
}
