/*
 * Copyright (c) 2024, WSO2 LLC. (http://wso2.com)
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

package io.ballerina.runtime.api;

import io.ballerina.runtime.api.types.ServiceType;
import io.ballerina.runtime.internal.RuntimeRepository;
import io.ballerina.runtime.internal.types.BServiceType;
import io.ballerina.runtime.internal.values.ObjectValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * External API to be used by the runtime-management module to provide Ballerina runtime artifacts.
 *
 * @since 2201.9.0
 */
public class Repository {

    private Repository() {
    }

    public static List<Artifact> getArtifacts() {
        Map<ObjectValue, ServiceType> serviceTypes = RuntimeRepository.getServiceTypes();
        if (serviceTypes.isEmpty()) {
            return new ArrayList<>();
        }

        List<Artifact> artifacts = new ArrayList<>();
        for (Map.Entry<ObjectValue, ServiceType> entry : serviceTypes.entrySet()) {
            BServiceType serviceType = (BServiceType) entry.getValue();
            Artifact artifact = new Artifact(serviceType.toString(), Artifact.ArtifactType.SERVICE);
            artifact.addDetail("attachPoint", serviceType.attachPoint);
            artifact.addDetail("listener", entry.getKey());
            artifact.addDetail("resources", serviceType.getResourceMethods());
            artifacts.add(artifact);
        }
        return artifacts;
    }
}
