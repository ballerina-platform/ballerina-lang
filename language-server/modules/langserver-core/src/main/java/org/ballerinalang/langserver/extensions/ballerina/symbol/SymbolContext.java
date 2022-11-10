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

import org.ballerinalang.langserver.commons.LSOperation;

/**
 * Represents a document context.
 *
 * @since 2.0.0
 */
public enum SymbolContext implements LSOperation {
    SC_TYPE_API("ballerinaSymbol/type"),
    SC_GET_TYPE_FROM_EXPRESSION_API("ballerinaSymbol/getTypeFromExpression"),
    SC_GET_SYMBOL_API("ballerinaSymbol/getSymbol"),
    SC_GET_TYPE_FROM_SYMBOL_API("ballerinaSymbol/getTypeFromSymbol"),
    SC_GET_TYPE_FROM_FN_DEFINITION_API("ballerinaSymbol/getTypesFromFnDefinition");
    private final String name;

    SymbolContext(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
