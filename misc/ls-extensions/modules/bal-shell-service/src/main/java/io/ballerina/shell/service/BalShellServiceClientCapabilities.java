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

import org.ballerinalang.langserver.commons.registration.BallerinaClientCapability;

/**
 * Client capabilities for the balShell service.
 *
 * @since 2201.1.1
 */
public class BalShellServiceClientCapabilities extends BallerinaClientCapability {
    private boolean isGetResult;
    private boolean isGetShellFileSource;
    private boolean isGetVariableValues;
    private boolean isDeleteDeclarations;
    private boolean isRestartNotebook;

    public boolean isGetResult() {
        return isGetResult;
    }

    public void setGetResult(boolean getResult) {
        isGetResult = getResult;
    }

    public boolean isGetShellFileSource() {
        return isGetShellFileSource;
    }

    public void setGetShellFileSource(boolean getShellFileSource) {
        isGetShellFileSource = getShellFileSource;
    }

    public boolean isGetVariableValues() {
        return isGetVariableValues;
    }

    public void setGetVariableValues(boolean getVariableValues) {
        isGetVariableValues = getVariableValues;
    }

    public boolean isDeleteDeclarations() {
        return isDeleteDeclarations;
    }

    public void setDeleteDeclarations(boolean deleteDeclarations) {
        isDeleteDeclarations = deleteDeclarations;
    }

    public boolean isRestartNotebook() {
        return isRestartNotebook;
    }

    public void setRestartNotebook(boolean restartNotebook) {
        isRestartNotebook = restartNotebook;
    }

    public BalShellServiceClientCapabilities() {
        super(Constants.CAPABILITY_NAME);
    }
}
