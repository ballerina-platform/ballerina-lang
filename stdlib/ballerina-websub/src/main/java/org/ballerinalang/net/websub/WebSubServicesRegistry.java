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

package org.ballerinalang.net.websub;

import org.ballerinalang.connector.api.Service;
import org.ballerinalang.net.http.HTTPServicesRegistry;
import org.ballerinalang.net.http.HttpService;
import org.ballerinalang.net.http.WebSocketServicesRegistry;

import java.util.Map;

/**
 * The WebSub service registry which uses an {@link HTTPServicesRegistry} to maintain WebSub Subscriber HTTP services.
 *
 * @since 0.965.0
 */
public class WebSubServicesRegistry  {

    private HTTPServicesRegistry httpServicesRegistry = new HTTPServicesRegistry(new WebSocketServicesRegistry());

    HTTPServicesRegistry getHttpServicesRegistry() {
        return httpServicesRegistry;
    }

    public void registerWebSubSubscriberService(Service service) {
        httpServicesRegistry.registerWebSubSubscriberService(service);
    }

    public Map<String, HttpService> getServicesInfoByInterface() {
        return httpServicesRegistry.getServicesInfoByInterface();
    }

}
