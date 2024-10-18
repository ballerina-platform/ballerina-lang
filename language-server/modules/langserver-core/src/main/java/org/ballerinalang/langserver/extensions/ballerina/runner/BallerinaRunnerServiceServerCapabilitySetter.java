/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.langserver.extensions.ballerina.runner;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.registration.BallerinaServerCapabilitySetter;

import java.util.Optional;

/**
 * Server capability setter for the {@link BallerinaRunnerService}.
 *
 * @since 2201.11.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.registration.BallerinaServerCapabilitySetter")
public class BallerinaRunnerServiceServerCapabilitySetter extends
        BallerinaServerCapabilitySetter<BallerinaRunnerServiceServerCapabilities> {

    @Override
    public Optional<BallerinaRunnerServiceServerCapabilities> build() {
        BallerinaRunnerServiceServerCapabilities capabilities = new BallerinaRunnerServiceServerCapabilities();
        capabilities.setDiagnostics(true);
        capabilities.setMainFunctionParams(true);
        return Optional.of(capabilities);
    }

    @Override
    public String getCapabilityName() {
        return BallerinaRunnerServiceConstants.CAPABILITY_NAME;
    }

    @Override
    public Class<BallerinaRunnerServiceServerCapabilities> getCapability() {
        return BallerinaRunnerServiceServerCapabilities.class;
    }
}
