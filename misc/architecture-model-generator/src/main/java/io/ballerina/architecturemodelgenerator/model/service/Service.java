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

import java.util.List;

/**
 * Provides service related information.
 *
 * @since 2201.2.2
 */
public class Service extends ModelElement {

    private final String path;
    private final String serviceId;
    private final String serviceType;
    private final List<Resource> resources;
    private final ServiceAnnotation annotation;
    private final List<RemoteFunction> remoteFunctions;
    private final List<Dependency> dependencies;

    public Service(String path, String serviceId, String serviceType, List<Resource> resources,
                   ServiceAnnotation annotation, List<RemoteFunction> remoteFunctions, List<Dependency> dependencies,
                   ElementLocation elementLocation) {
        super(elementLocation);
        this.path = path;
        this.serviceId = serviceId;
        this.serviceType = serviceType;
        this.resources = resources;
        this.annotation = annotation;
        this.remoteFunctions = remoteFunctions;
        this.dependencies = dependencies;
    }

    public String getPath() {
        return path;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public ServiceAnnotation getAnnotation() {
        return annotation;
    }

    public List<RemoteFunction> getRemoteFunctions() {
        return remoteFunctions;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }
}
