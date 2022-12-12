/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.architecturemodelgenerator.model.service;

import io.ballerina.architecturemodelgenerator.model.ElementLocation;
import io.ballerina.architecturemodelgenerator.model.ModelElement;

/**
 * Represent the dependency of another service.
 *
 * @since 2201.4.0
 */
public class Dependency extends ModelElement {
    private final String serviceId;
    private final String connectorType;

    public Dependency(String serviceId, String connectorType, ElementLocation elementLocation) {
        super(elementLocation);
        this.serviceId = serviceId;
        this.connectorType = connectorType;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getConnectorType() {
        return connectorType;
    }
}
