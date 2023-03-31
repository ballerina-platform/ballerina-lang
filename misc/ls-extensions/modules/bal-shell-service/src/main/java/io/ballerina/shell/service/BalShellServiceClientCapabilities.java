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
    private boolean getResult;
    private boolean getShellFileSource;
    private boolean getVariableValues;
    private boolean deleteDeclarations;
    private boolean restartNotebook;

    public boolean isGetResult() {
        return getResult;
    }

    public void setGetResult(boolean getResult) {
        this.getResult = getResult;
    }

    public boolean isGetShellFileSource() {
        return getShellFileSource;
    }

    public void setGetShellFileSource(boolean getShellFileSource) {
        this.getShellFileSource = getShellFileSource;
    }

    public boolean isGetVariableValues() {
        return getVariableValues;
    }

    public void setGetVariableValues(boolean getVariableValues) {
        this.getVariableValues = getVariableValues;
    }

    public boolean isDeleteDeclarations() {
        return deleteDeclarations;
    }

    public void setDeleteDeclarations(boolean deleteDeclarations) {
        this.deleteDeclarations = deleteDeclarations;
    }

    public boolean isRestartNotebook() {
        return restartNotebook;
    }

    public void setRestartNotebook(boolean restartNotebook) {
        this.restartNotebook = restartNotebook;
    }

    public BalShellServiceClientCapabilities() {
        super(Constants.CAPABILITY_NAME);
    }
}
