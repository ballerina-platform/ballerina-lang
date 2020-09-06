/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerina.compiler.api;

import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextRange;
import org.ballerina.compiler.api.symbols.Symbol;
import org.ballerinalang.util.diagnostic.Diagnostic;

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
     * @param position text position in the source
     * @return {@link List} of visible symbols in the given location
     */
    List<Symbol> visibleSymbols(LinePosition position);

    /**
     * Lookup the symbol at the given location.
     *
     * @param position text position in the source
     * @return {@link Symbol} in the given location
     */
    Optional<Symbol> symbol(LinePosition position);

    /**
     * Get the diagnostics within the given text Span.
     *
     * @param textRange Text range to filter the diagnostics
     * @return {@link List} of extracted diagnostics
     */
    List<Diagnostic> diagnostics(TextRange textRange);
}
