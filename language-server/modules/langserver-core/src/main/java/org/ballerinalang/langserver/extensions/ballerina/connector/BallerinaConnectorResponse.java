/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langserver.extensions.ballerina.connector;

import com.google.gson.JsonElement;

/**
 * Represents with connector AST.
 *
 * @since 2.0.0
 */
public class BallerinaConnectorResponse {

    private final String org;
    private final String module;
    private final String version;
    private final String name;
    private final String displayName;
    private final String error;
    private JsonElement ast;
    private final Boolean beta;

    public BallerinaConnectorResponse(String org, String module, String version, String name, String displayName,
                                      JsonElement ast, String error, Boolean beta) {
        this.org = org;
        this.module = module;
        this.version = version;
        this.name = name;
        this.displayName = displayName;
        this.ast = ast;
        this.error = error;
        this.beta = beta;
    }

    public String getOrg() {
        return org;
    }

    public String getModule() {
        return module;
    }

    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public JsonElement getAst() {
        return ast;
    }

    public String getError() {
        return error;
    }

    public Boolean getBeta() {
        return beta;
    }
}
