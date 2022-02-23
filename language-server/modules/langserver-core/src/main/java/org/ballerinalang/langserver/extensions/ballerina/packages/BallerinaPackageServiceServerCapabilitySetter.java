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
package org.ballerinalang.langserver.extensions.ballerina.packages;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.registration.BallerinaServerCapabilitySetter;

import java.util.Optional;

/**
 * Capability setter for the {@link BallerinaPackageService}.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.registration.BallerinaServerCapabilitySetter")
public class BallerinaPackageServiceServerCapabilitySetter extends
        BallerinaServerCapabilitySetter<BallerinaPackageServerCapabilities> {

    @Override
    public Optional<BallerinaPackageServerCapabilities> build() {
        BallerinaPackageServerCapabilities capabilities = new BallerinaPackageServerCapabilities();
        capabilities.setMetadata(true);
        capabilities.setComponents(true);
        capabilities.setConfigSchema(true);
        return Optional.of(capabilities);
    }

    @Override
    public String getCapabilityName() {
        return PackageServiceConstants.CAPABILITY_NAME;
    }

    @Override
    public Class<BallerinaPackageServerCapabilities> getCapability() {
        return BallerinaPackageServerCapabilities.class;
    }
}
