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

import com.google.gson.JsonElement;
import org.ballerinalang.diagramutil.connector.models.connector.Connector;

import java.util.List;

/**
 * Request to get connectorList.
 *
 * @since 2.0.0
 */
public class BallerinaConnectorListResponse {

    private List<Connector> central;
    private List<Connector> local;

    public BallerinaConnectorListResponse() {
    }

    public BallerinaConnectorListResponse(List<Connector> central, List<Connector> local) {
        this.central = central;
        this.local = local;
    }

    public List<Connector> getCentralConnectors() {
        return central;
    }

    public void setCentralConnectors(List<Connector> central) {
        this.central = central;
    }

    public List<Connector> getLocalConnectors() {
        return local;
    }

    public void setLocalConnectors(List<Connector> local) {
        this.local = local;
    }
}
