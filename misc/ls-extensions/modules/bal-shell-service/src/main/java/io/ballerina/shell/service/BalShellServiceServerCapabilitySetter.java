/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package io.ballerina.shell.service;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.registration.BallerinaServerCapabilitySetter;

import java.util.Optional;

/**
 * Capability setter for the {@link BalShellService}.
 *
 * @since 2201.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.registration.BallerinaServerCapabilitySetter")
public class BalShellServiceServerCapabilitySetter extends
        BallerinaServerCapabilitySetter<BalShellServiceServerCapabilities> {

    @Override
    public Optional<BalShellServiceServerCapabilities> build() {
        BalShellServiceServerCapabilities capabilities = new BalShellServiceServerCapabilities();
        capabilities.setGetResult(true);
        capabilities.setGetShellFileSource(true);
        capabilities.setGetVariableValues(true);
        capabilities.setDeleteDeclarations(true);
        capabilities.setRestartNotebook(true);
        return Optional.of(capabilities);
    }

    @Override
    public String getCapabilityName() {
        return Constants.CAPABILITY_NAME;
    }

    @Override
    public Class<BalShellServiceServerCapabilities> getCapability() {
        return BalShellServiceServerCapabilities.class;
    }
}
