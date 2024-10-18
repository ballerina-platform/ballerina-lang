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

import org.ballerinalang.langserver.commons.registration.BallerinaServerCapability;

/**
 * Server capabilities for the ballerina runner service.
 *
 * @since 2201.11.0
 */
public class BallerinaRunnerServiceServerCapabilities extends BallerinaServerCapability {

    private boolean diagnostics;
    private boolean mainFunctionParams;

    public BallerinaRunnerServiceServerCapabilities() {
        super(BallerinaRunnerServiceConstants.CAPABILITY_NAME);
    }

    public boolean isDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(boolean diagnostics) {
        this.diagnostics = diagnostics;
    }

    public boolean isMainFunctionParams() {
        return mainFunctionParams;
    }

    public void setMainFunctionParams(boolean mainFunctionParams) {
        this.mainFunctionParams = mainFunctionParams;
    }
}
