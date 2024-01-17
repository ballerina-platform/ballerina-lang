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
package org.ballerinalang.langserver.extensions.ballerina.symbol;

import org.ballerinalang.langserver.commons.registration.BallerinaServerCapability;

/**
 * Server capabilities for the ballerinaSymbol service.
 *
 * @since 2.0.0
 */
public class BallerinaSymbolServerCapabilities extends BallerinaServerCapability {

    private boolean endpoints;

    private boolean type;

    private boolean getSymbol;

    private boolean getTypeFromExpression;

    private boolean getTypeFromSymbol;

    private boolean getTypesFromFnDefinition;

    public boolean isGetSymbol() {
        return getSymbol;
    }

    public void setGetSymbol(boolean getSymbol) {
        this.getSymbol = getSymbol;
    }

    public boolean isEndpoints() {
        return endpoints;
    }

    public void setEndpoints(boolean endpoints) {
        this.endpoints = endpoints;
    }

    public boolean isType() {
        return type;
    }

    public void setType(boolean type) {
        this.type = type;
    }

    public boolean isGetTypeFromExpression() {
        return getTypeFromExpression;
    }

    public void setGetTypeFromExpression(boolean getTypeFromExpression) {
        this.getTypeFromExpression = getTypeFromExpression;
    }

    public boolean isGetTypeFromSymbol() {
        return getTypeFromSymbol;
    }

    public void setGetTypeFromSymbol(boolean getTypeFromSymbol) {
        this.getTypeFromSymbol = getTypeFromSymbol;
    }

    public boolean isGetTypesFromFnDefinition() {
        return getTypesFromFnDefinition;
    }

    public void setGetTypesFromFnDefinition(boolean getTypesFromFnDefinition) {
        this.getTypesFromFnDefinition = getTypesFromFnDefinition;
    }

    public BallerinaSymbolServerCapabilities() {
        super(Constants.CAPABILITY_NAME);
    }
}
