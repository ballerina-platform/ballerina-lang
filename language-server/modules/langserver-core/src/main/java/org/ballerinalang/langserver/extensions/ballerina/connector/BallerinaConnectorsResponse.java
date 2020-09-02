/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langserver.extensions.ballerina.connector;

import java.util.ArrayList;
import java.util.List;

/**
 * Response to sending connector information.
 *
 * @since 2.0.0
 */
public class BallerinaConnectorsResponse {

    private List<BallerinaConnectorInfo> connectors = new ArrayList<BallerinaConnectorInfo>();

    public BallerinaConnectorsResponse() {
    }

    public BallerinaConnectorsResponse(List<BallerinaConnectorInfo> connectors) {
        this.connectors = connectors;
    }

    public List<BallerinaConnectorInfo> getConnectors() {
        return connectors;
    }

    @Override
    public String toString() {
        return "BallerinaConnectorsResponse{" +
                "ballerinaConnectors=" + connectors +
                '}';
    }
}
