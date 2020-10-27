/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.compiler.api;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;

import java.util.List;
import java.util.Optional;

/**
 * Represents an Abstract semantic model.
 *
 * @since 2.0.0
 */
public interface SemanticModel {

    /**
     * Lookup the visible symbols at the given location.
     *
     * @param fileName  path for the file in which we need to look up symbols, relative to the source root path
     * @param position text position in the source
     * @return {@link List} of visible symbols in the given location
     */
    List<Symbol> visibleSymbols(String fileName, LinePosition position);

    /**
     * Lookup the symbol at the given location.
     *
     * @param fileName  path for the file in which we need to look up symbols, relative to the source root path
     * @param position text position in the source
     * @return {@link Symbol} in the given location
     */
    Optional<Symbol> symbol(String fileName, LinePosition position);

    /**
     * Retrieves the symbols of module-scoped constructs in the semantic model.
     *
     * @return A list of module-scoped symbols
     */
    List<Symbol> moduleLevelSymbols();

    /**
     * Get the diagnostics within the given text Span.
     *
     * @param range Line range to filter the diagnostics
     * @return {@link List} of extracted diagnostics
     */
    List<Diagnostic> diagnostics(LineRange range);


    /**
     * Retrieves the diagnostics of the module.
     *
     * @return {@link List} of diagnostics for the module
     */
    List<Diagnostic> diagnostics();
}
