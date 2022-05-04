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
import java.util.Optional;
/**
 * Represents the documentation structure of getSymbol endpoint of BallerinaSymbolService
 */
public class SymbolDocumentation {
    public String description;
    public List<ParameterInfo> parameters;
    public String returnValueDescription;
    public String deprecatedDocumentation;
    public List<ParameterInfo> deprecatedParams;


    public SymbolDocumentation(Optional<Documentation> documentation, List<ParameterInfo> parameters, List<ParameterInfo> deprecatedParams) {
        this.description = documentation.get().description().isEmpty() ?
                null : documentation.get().description().get();
        this.parameters = parameters.isEmpty() ? null : parameters;
        this.returnValueDescription = documentation.get().returnDescription().isEmpty() ?
                null : documentation.get().returnDescription().get();
        this.deprecatedDocumentation = documentation.get().deprecatedDescription().isEmpty() ?
                null : documentation.get().deprecatedDescription().get();
        this.deprecatedParams = deprecatedParams.isEmpty() ? null : deprecatedParams;
    }


    /**
     * Represents a parameter information
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
