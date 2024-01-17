/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.compiler.api.symbols.Documentation;

import java.util.List;

/**
 * Represents the documentation structure of getSymbol endpoint of BallerinaSymbolService.
 *
 *  @since 2201.1.0
 */
public class SymbolDocumentation {
    private final String description;
    private final List<ParameterInfo> parameters;
    private final String returnValueDescription;
    private final String deprecatedDocumentation;
    private final List<ParameterInfo> deprecatedParams;


    public SymbolDocumentation(Documentation documentation, List<ParameterInfo> parameters,
                               List<ParameterInfo> deprecatedParams) {
        this.description = documentation.description().isEmpty() ? null : documentation.description().get();
        this.parameters = parameters.isEmpty() ? null : parameters;
        this.returnValueDescription = documentation.returnDescription().isEmpty() ?
                null : documentation.returnDescription().get();
        this.deprecatedDocumentation = documentation.deprecatedDescription().isEmpty() ?
                null : documentation.deprecatedDescription().get();
        this.deprecatedParams = deprecatedParams.isEmpty() ? null : deprecatedParams;
    }

    public String getDescription() {
        return description;
    }

    public List<ParameterInfo> getParameters() {
        return parameters;
    }

    public String getReturnValueDescription() {
        return returnValueDescription;
    }

    public String getDeprecatedDocumentation() {
        return deprecatedDocumentation;
    }

    public List<ParameterInfo> getDeprecatedParams() {
        return deprecatedParams;
    }

    /**
     * Represents a parameter information.
     *
     * @since 2201.1.0
     */
    public static class ParameterInfo {

        public String name;
        public String description;
        public String kind;
        public String type;

        public ParameterInfo(String name, String description, String kind, String type) {
            this.name = name;
            this.description = description;
            this.kind = kind;
            this.type = type;
        }

        public ParameterInfo(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getKind() {
            return kind;
        }

        public String getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }
    }
}
