/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects;

/**
 * Represents a resource file.
 *
 * @since 2.0.0
 */
public class Resource {

    private final ResourceContext resourceContext;
    private final Package packageInstance;

    Resource(ResourceContext resourceContext, Package aPackage) {
        this.resourceContext = resourceContext;
        this.packageInstance = aPackage;
    }

    Resource from(ResourceConfig resourceConfig, Package aPackage) {
        ResourceContext resourceContext = ResourceContext.from(resourceConfig);
        return new Resource(resourceContext, aPackage);
    }

    public DocumentId documentId() {
        return resourceContext.documentId();
    }

    public String name() {
        return resourceContext.name();
    }

    public byte[] content() {
        return resourceContext.content();
    }

    @Deprecated(since = "2201.10.0", forRemoval = true)
    public Module module() {
        return packageInstance.getDefaultModule();
    }

    public Package packageInstance() {
        return packageInstance;
    }
}
