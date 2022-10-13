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

package io.ballerina.componentmodel.servicemodel.components;

/**
 * Represent interaction with another service.
 *
 * @since 2201.2.2
 */
public class Interaction {

    private final ResourceId resourceId;
    private final String connectorType;

    public Interaction(ResourceId resourceId, String connectorType) {

        this.resourceId = resourceId;
        this.connectorType = connectorType;
    }

    public ResourceId getResourceId() {
        return resourceId;
    }

    public String getConnectorType() {
        return connectorType;
    }
}
