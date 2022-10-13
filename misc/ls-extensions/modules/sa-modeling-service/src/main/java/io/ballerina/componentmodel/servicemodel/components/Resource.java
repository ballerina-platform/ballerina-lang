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

import java.util.List;

/**
 * Represents resource details.
 *
 * @since 2201.2.2
 */
public class Resource {

    private final String identifier;
    private final ResourceId resourceId;
    private final List<ResourceParameter> parameters;
    private final List<String> returns;
    private final List<Interaction> interactions;

    public Resource(String identifier, ResourceId resourceId, List<ResourceParameter> parameters, List<String> returns,
                    List<Interaction> interactions) {

        this.identifier = identifier;
        this.resourceId = resourceId;
        this.parameters = parameters;
        this.returns = returns;
        this.interactions = interactions;
    }

    public String getIdentifier() {
        return identifier;
    }

    public ResourceId getResourceId() {
        return resourceId;
    }

    public List<ResourceParameter> getParameters() {
        return parameters;
    }

    public List<String> getReturns() {
        return returns;
    }

    public List<Interaction> getInteractions() {
        return interactions;
    }
}
