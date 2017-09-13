/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.connector.api;

import org.ballerinalang.connector.impl.ServerConnectorRegistry;
import org.ballerinalang.connector.impl.StructHelper;
import org.ballerinalang.model.values.BStruct;

/**
 * {@code ConnectorUtil} Utilities related to connector processing.
 *
 * @since 0.94
 */
public class ConnectorUtils extends StructHelper {

    /**
     * This method is used to create a struct given the resource and required struct details.
     *
     * @param resource to get required details.
     * @param packageName package name of the struct definition.
     * @param structName struct name.
     * @return created struct.
     */
    public static BStruct createStruct(Resource resource, String packageName, String structName) {
        return createAndGetStruct(resource, packageName, structName);
    }

    /**
     * This method can be used to access the {@code BallerinaServerConnector} object which is at
     * Ballerina level.
     *
     * @param protocolPkgPath   of the server connector.
     * @return  ballerinaServerConnector object.
     */
    public static BallerinaServerConnector getBallerinaServerConnector(String protocolPkgPath) {
        return ServerConnectorRegistry.getInstance().getBallerinaServerConnector(protocolPkgPath);
    }
}
