/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.commons.codeaction.spi;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.tools.diagnostics.DiagnosticProperty;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * This class holds position details for the diagnostics based code actions.
 *
 * @since 2.0.0
 */
public interface DiagBasedPositionDetails {

    int DIAG_PROP_INCOMPATIBLE_TYPES_EXPECTED_SYMBOL_INDEX = 0;
    int DIAG_PROP_INCOMPATIBLE_TYPES_FOUND_SYMBOL_INDEX = 1;
    int DIAG_PROP_VAR_ASSIGN_SYMBOL_INDEX = 0;
    int DIAG_PROP_INCOMPATIBLE_TYPES_FOR_ITERABLE_FOUND_SYMBOL_INDEX = 0;

    /**
     * Returns matched scoped node for the current position.
     *
     * @return {@link NonTerminalNode}
     */
    NonTerminalNode matchedNode();

    /**
     * Returns matched scoped symbol for the current position.
     *
     * @return {@link Symbol}
     */
    Symbol matchedSymbol();

    /**
     * Returns optional value of the diagnostic property.
     *
     * @param propertyIndex property index
     * @return Value of the property at given index
     */
    <T> Optional<T> diagnosticProperty(int propertyIndex);

    /**
     * Returns optional value of the diagnostic property.
     *
     * @param function Function to be invoked on the diagnostic properties.
     * @return Value of the property returned by the specified function.
     */
    <T> Optional<T> diagnosticProperty(Function<List<DiagnosticProperty<?>>, Optional<T>> function);
    
}
