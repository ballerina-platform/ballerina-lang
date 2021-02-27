/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.http.mock.nonlistening;

import io.ballerina.runtime.api.values.BMap;
import org.ballerinalang.net.http.HTTPServicesRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP connector mock listener for Ballerina testing.
 */
public class MockHTTPConnectorListener {

    private static MockHTTPConnectorListener testListener = new MockHTTPConnectorListener();
    private Map<Integer, RegistryHolder> listenerServicesRegistries = new HashMap<>();

    private MockHTTPConnectorListener() {
    }

    public static MockHTTPConnectorListener getInstance() {
        return testListener;
    }

    public RegistryHolder getHttpServicesRegistry(int listenerPort) {
        return listenerServicesRegistries.get(listenerPort);
    }

    public void setHttpServicesRegistry(int listenerPort, HTTPServicesRegistry httpServicesRegistry,
                                        BMap endpointConfig) {
        listenerServicesRegistries.put(listenerPort, new RegistryHolder(httpServicesRegistry, endpointConfig));
    }

    /**
     * Holder class to hold registry and the endpoint config.
     */
    public static class RegistryHolder {
        private final HTTPServicesRegistry registry;
        private final BMap endpointConfig;

        public RegistryHolder(HTTPServicesRegistry registry, BMap endpointConfig) {
            this.registry = registry;
            this.endpointConfig = endpointConfig;
        }

        public HTTPServicesRegistry getRegistry() {
            return registry;
        }

        public BMap getEndpointConfig() {
            return endpointConfig;
        }
    }
}
