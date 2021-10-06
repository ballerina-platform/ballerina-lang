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

import org.ballerinalang.langserver.commons.registration.BallerinaServerCapability;
import org.ballerinalang.langserver.commons.registration.BallerinaServerCapabilitySetter;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Extended server capability setter.
 * This utility is responsible for setting the capabilities
 *
 * @since 2.0.0
 */
public class ExtendedServerCapabilityBuilder {
    private static List<BallerinaServerCapabilitySetter<? extends BallerinaServerCapability>> capabilitySetters;

    private ExtendedServerCapabilityBuilder() {
    }

    /**
     * Get the extended server capabilities supported by the sever.
     *
     * @return {@link List}
     */
    public static List<BallerinaServerCapability> get() {
        if (capabilitySetters == null) {
            capabilitySetters = new ArrayList<>();

            ServiceLoader<BallerinaServerCapabilitySetter> loader
                    = ServiceLoader.load(BallerinaServerCapabilitySetter.class);
            for (BallerinaServerCapabilitySetter<?> capabilitySetter : loader) {
                capabilitySetters.add(capabilitySetter);
            }
        }

        List<BallerinaServerCapability> serverCapabilities = new ArrayList<>();

        for (BallerinaServerCapabilitySetter<? extends BallerinaServerCapability> capabilitySetter
                : capabilitySetters) {
            capabilitySetter.build().ifPresent(serverCapabilities::add);
        }
        
        return serverCapabilities;
    }
}
