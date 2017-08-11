/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.model.util;

import org.ballerinalang.services.dispatchers.ServiceDispatcher;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Helper class to imitate serviceDispatchers in test cases.
 *
 * @since 0.89
 */
public class TestHTTPServiceDispatcher implements ServiceDispatcher {
    @Override
    public String getProtocol() {
        return "http";
    }

    @Override
    public String getProtocolPackage() {
        return "ballerina.net.http";
    }

    @Override
    public ServiceInfo findService(CarbonMessage cMsg, CarbonCallback callback) {
        return null;
    }

    @Override
    public void serviceRegistered(ServiceInfo service) {

    }

    @Override
    public void serviceUnregistered(ServiceInfo service) {

    }
}
