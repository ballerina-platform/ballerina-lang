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
package org.ballerinalang.langserver.codeaction;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticProperty;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;

import java.util.List;
import java.util.Optional;

/**
 * This class holds position details for the diagnostics-based code actions.
 *
 * @since 2.0.0
 */
public class DiagBasedPositionDetailsImpl implements DiagBasedPositionDetails {
    private final NonTerminalNode matchedNode;
    private final Symbol matchedSymbol;
    private final Diagnostic diagnostic;

    private DiagBasedPositionDetailsImpl(NonTerminalNode matchedNode,
                                         Symbol matchedSymbol,
                                         Diagnostic diagnostic) {
        this.matchedNode = matchedNode;
        this.matchedSymbol = matchedSymbol;
        this.diagnostic = diagnostic;
    }

    public static DiagBasedPositionDetailsImpl from(NonTerminalNode matchedNode,
                                                    Symbol matchedSymbol,
                                                    Diagnostic diagnostic) {
        return new DiagBasedPositionDetailsImpl(matchedNode, matchedSymbol, diagnostic);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NonTerminalNode matchedNode() {
        return matchedNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Symbol matchedSymbol() {
        return matchedSymbol;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Optional<T> diagnosticProperty(int propertyIndex) {
        List<DiagnosticProperty<?>> props = diagnostic.properties();
        if (props.size() < (propertyIndex + 1)) {
            return Optional.empty();
        }

        DiagnosticProperty<?> diagnosticProperty = props.get(propertyIndex);
        // Nullable static API used for safety
        return Optional.ofNullable((T) diagnosticProperty.value());
    }
}
