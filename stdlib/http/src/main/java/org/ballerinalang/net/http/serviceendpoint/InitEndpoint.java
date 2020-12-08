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

import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import org.ballerinalang.net.http.HttpConnectionManager;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpErrorType;
import org.ballerinalang.net.http.HttpUtil;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;

import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_PORT;
import static org.ballerinalang.net.http.HttpUtil.getListenerConfig;

/**
 * Initialize the HTTP listener.
 *
 * @since 0.966
 */
public class InitEndpoint extends AbstractHttpNativeFunction {
    public static Object initEndpoint(BObject serviceEndpoint) {
        try {
            // Creating server connector
            BMap serviceEndpointConfig = serviceEndpoint.getMapValue(HttpConstants.SERVICE_ENDPOINT_CONFIG);
            long port = serviceEndpoint.getIntValue(ENDPOINT_CONFIG_PORT);
            ListenerConfiguration listenerConfiguration = getListenerConfig(port, serviceEndpointConfig);
            ServerConnector httpServerConnector =
                    HttpConnectionManager.getInstance().createHttpServerConnector(listenerConfiguration);
            serviceEndpoint.addNativeData(HttpConstants.HTTP_SERVER_CONNECTOR, httpServerConnector);

            //Adding service registries to native data
            resetRegistry(serviceEndpoint);
            return null;
        } catch (BError BError) {
            return BError;
        } catch (Exception e) {
            return HttpUtil.createHttpError(e.getMessage(), HttpErrorType.GENERIC_LISTENER_ERROR);
        }
    }
}
