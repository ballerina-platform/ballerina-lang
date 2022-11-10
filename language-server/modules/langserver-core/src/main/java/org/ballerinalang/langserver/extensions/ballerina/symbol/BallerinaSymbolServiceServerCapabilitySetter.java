/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License a
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.extensions.ballerina.symbol;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.registration.BallerinaServerCapabilitySetter;

import java.util.Optional;

/**
 * Capability setter for the {@link BallerinaSymbolService}.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.registration.BallerinaServerCapabilitySetter")
public class BallerinaSymbolServiceServerCapabilitySetter extends
        BallerinaServerCapabilitySetter<BallerinaSymbolServerCapabilities> {

    @Override
    public Optional<BallerinaSymbolServerCapabilities> build() {
        BallerinaSymbolServerCapabilities capabilities = new BallerinaSymbolServerCapabilities();
        capabilities.setEndpoints(true);
        capabilities.setType(true);
        capabilities.setGetSymbol(true);
        capabilities.setGetTypeFromExpression(true);
        capabilities.setGetTypeFromSymbol(true);
        capabilities.setGetTypesFromFnDefinition(true);
        return Optional.of(capabilities);
    }

    @Override
    public String getCapabilityName() {
        return Constants.CAPABILITY_NAME;
    }

    @Override
    public Class<BallerinaSymbolServerCapabilities> getCapability() {
        return BallerinaSymbolServerCapabilities.class;
    }
}
