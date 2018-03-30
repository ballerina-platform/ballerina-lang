/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.code.generator.model;

import org.ballerinalang.code.generator.exception.CodeGeneratorException;
import org.ballerinalang.model.tree.EndpointNode;
import org.ballerinalang.model.tree.ResourceNode;
import org.ballerinalang.model.tree.ServiceNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Context holder for ballerina client generation.
 * This class will hold the context information required for ballerina code generation
 */
public class ClientContextHolder {
    private String name;
    private List<ResourceContextHolder> resources;
    private List<EndpointContextHolder> endpoints;

    /**
     * Build a parsable context from a Ballerina {@link ServiceNode}.
     *
     * @param service   {@code ServiceNode} for a valid ballerina source file
     * @param endpoints list of endpoints to be used as service endpoints for generated client
     * @return A parsable data model for provided ballerina {@code service}
     */
    public static ClientContextHolder buildContext(ServiceNode service, List<EndpointNode> endpoints)
            throws CodeGeneratorException {
        ClientContextHolder context = new ClientContextHolder();
        context.name = service.getName().getValue();
        context.resources = new ArrayList<>();
        context.endpoints = new ArrayList<>();

        // Extract bound endpoint details
        for (EndpointNode ep : endpoints) {
            EndpointContextHolder epContext = EndpointContextHolder.buildContext(service, ep);
            if (epContext != null) {
                context.endpoints.add(EndpointContextHolder.buildContext(service, ep));
            }
        }

        // Extract ballerina resource nodes as parsable resources
        for (ResourceNode resource: service.getResources()) {
            ResourceContextHolder operation = ResourceContextHolder.buildContext(resource);
            context.resources.add(operation);
        }

        return context;
    }

    public String getName() {
        return name;
    }

    public List<ResourceContextHolder> getResources() {
        return resources;
    }

    public List<EndpointContextHolder> getEndpoints() {
        return endpoints;
    }
}
