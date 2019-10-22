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

package org.ballerinalang.net.http.mock.nonlistening;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.HTTPServicesRegistry;
import org.ballerinalang.net.http.HttpConstants;

import static org.ballerinalang.net.http.HttpConstants.MOCK_LISTENER_ENDPOINT;
import static org.ballerinalang.net.http.HttpConstants.SERVICE_ENDPOINT_CONFIG;

/**
 * Start the HTTP mock listener instance.
 *
 * @since 0.966
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "start",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = MOCK_LISTENER_ENDPOINT,
                structPackage = "ballerina.http"),
        isPublic = true
)
public class NonListeningStart extends org.ballerinalang.net.http.serviceendpoint.Start {
    public static Object start(Strand strand, ObjectValue listener) {
        HTTPServicesRegistry httpServicesRegistry = getHttpServicesRegistry(listener);
        MockHTTPConnectorListener httpListener = MockHTTPConnectorListener.getInstance();
        httpListener.setHttpServicesRegistry(((Long) listener.get(HttpConstants.ENDPOINT_CONFIG_PORT)).intValue(),
                                             httpServicesRegistry, listener.getMapValue(SERVICE_ENDPOINT_CONFIG));
        return null;
    }
}
