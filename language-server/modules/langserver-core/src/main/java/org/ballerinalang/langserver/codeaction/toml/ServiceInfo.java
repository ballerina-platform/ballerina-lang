/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction.toml;

import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Service of a ballerina document.
 *
 * @since 2.0.0
 */
public class ServiceInfo {
    private final ServiceDeclarationNode node;
    private final String serviceName;
    private final ListenerInfo listener;
    private final List<ResourceInfo> resourceInfo;

    public ServiceInfo(ListenerInfo listener, ServiceDeclarationNode node, String serviceName) {
        this.listener = listener;
        this.node = node;
        this.serviceName = serviceName;
        this.resourceInfo = new ArrayList<>();
    }

    public ServiceDeclarationNode getNode() {
        return node;
    }

    public String getServiceName() {
        return serviceName;
    }

    public ListenerInfo getListener() {
        return listener;
    }

    public List<ResourceInfo> getResourceInfo() {
        return resourceInfo;
    }

    public void addResource(ResourceInfo resourceInfo) {
        this.resourceInfo.add(resourceInfo);
    }

    @Override
    public String toString() {
        return "ServiceInfo{" +
                ", serviceName='" + serviceName + '\'' +
                ", listener=" + listener +
                ", resourceInfo=" + resourceInfo +
                '}';
    }
}
