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
package org.ballerinalang.langserver;

import com.google.gson.JsonObject;
import org.ballerinalang.langserver.commons.registration.BallerinaClientCapability;
import org.ballerinalang.langserver.commons.registration.BallerinaClientCapabilitySetter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Extended server capability builder.
 * This utility is responsible for setting the capabilities
 *
 * @since 2.0.0
 */
public class ExtendedClientCapabilityBuilder {
    private static List<BallerinaClientCapabilitySetter<? extends BallerinaClientCapability>> capabilitySetters;

    private ExtendedClientCapabilityBuilder() {
    }

    /**
     * Get the extended server capabilities supported by the sever.
     *
     * @return {@link List}
     */
    public static List<BallerinaClientCapability> get(List<Object> configs) {

        if (capabilitySetters == null) {
            capabilitySetters = new ArrayList<>();

            ServiceLoader<BallerinaClientCapabilitySetter> loader
                    = ServiceLoader.load(BallerinaClientCapabilitySetter.class);
            for (BallerinaClientCapabilitySetter<?> capabilitySetter : loader) {
                capabilitySetters.add(capabilitySetter);
            }
        }
        
        List<BallerinaClientCapability> clientCapabilities = new ArrayList<>();

        for (Object config : configs) {
            if (!(config instanceof JsonObject jConfig)) {
                continue;
            }
            String capabilityName = jConfig.get("name").getAsString();
            Optional<BallerinaClientCapabilitySetter<? extends BallerinaClientCapability>> setter =
                    capabilitySetters.stream()
                            .filter(s -> s.getCapabilityName().equals(capabilityName))
                            .findFirst();
            if (setter.isEmpty()) {
                continue;
            }
            setter.get().build(jConfig).ifPresent(clientCapabilities::add);
        }

        return clientCapabilities;
    }
}
