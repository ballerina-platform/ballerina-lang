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

package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.Artifact;
import io.ballerina.runtime.api.Repository;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.internal.types.BServiceType;
import io.ballerina.runtime.internal.values.ObjectValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API implementation for the runtime-management module to provide Ballerina runtime artifacts.
 *
 * @since 2201.9.0
 */
public class RepositoryImpl implements Repository {
    private static final Map<ObjectValue, ObjectValue> serviceListenerMap = new HashMap<>();
    private static final Map<ObjectValue, ObjectValue> listenerServiceMap = new HashMap<>();

    @Override
    public List<Artifact> getArtifacts() {
        List<Artifact> artifacts = new ArrayList<>();
        for (Map.Entry<ObjectValue, ObjectValue> entry : serviceListenerMap.entrySet()) {
            ObjectValue service = entry.getKey();
            ObjectValue listener = entry.getValue();
            Artifact artifact = createArtifact(service, listener);
            artifacts.add(artifact);
        }

        for (Map.Entry<ObjectValue, ObjectValue> entry : listenerServiceMap.entrySet()) {
            ObjectValue listener = entry.getKey();
            ObjectValue service = entry.getValue();

            if (!serviceListenerMap.containsKey(service)) {
                Artifact artifact = createArtifact(service, listener);
                artifacts.add(artifact);
            }
        }

        return artifacts;
    }

    private Artifact createArtifact(ObjectValue service, ObjectValue listener) {
        Artifact artifact = new Artifact(service.toString(), Artifact.ArtifactType.SERVICE);
        List<ObjectValue> listeners = (List<ObjectValue>) artifact.getDetail("listeners");
        if (listeners == null) {
            listeners = new ArrayList<>();
            artifact.addDetail("listeners", listeners);
        }
        listeners.add(listener);
        BServiceType serviceType = (BServiceType) TypeUtils.getImpliedType(service.getOriginalType());
        artifact.addDetail("attachPoint", serviceType.attachPoint);
        artifact.addDetail("resources", serviceType.getResourceMethods());
        artifact.addDetail("remotes", serviceType.getRemoteMethods());
        return artifact;
    }

    public static void addServiceListener(BObject listener, BObject service, Object attachPoint) {
        BServiceType serviceType = (BServiceType) service.getType();
        serviceType.attachPoint = attachPoint;
        serviceListenerMap.put((ObjectValue) service, (ObjectValue) listener);
        listenerServiceMap.put((ObjectValue) listener, (ObjectValue) service);
    }

}
