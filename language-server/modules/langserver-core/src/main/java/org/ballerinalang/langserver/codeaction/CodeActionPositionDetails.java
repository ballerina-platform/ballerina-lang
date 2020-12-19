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
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.Node;
import org.ballerinalang.langserver.commons.codeaction.spi.PositionDetails;

/**
 * This class holds position details for the code actions.
 *
 * @since 2.0.0
 */
public class CodeActionPositionDetails implements PositionDetails {
    private final Node matchedNode;
    private final Symbol matchedSymbol;
    private final TypeSymbol matchedExprType;

    private CodeActionPositionDetails(Node matchedNode,
                                      Symbol matchedSymbol,
                                      TypeSymbol matchedExprType) {
        this.matchedNode = matchedNode;
        this.matchedSymbol = matchedSymbol;
        this.matchedExprType = matchedExprType;
    }

    public static PositionDetails from(Node matchedNode,
                                       Symbol matchedSymbol,
                                       TypeSymbol optTypeDesc) {
        return new CodeActionPositionDetails(matchedNode, matchedSymbol, optTypeDesc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node matchedNode() {
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
    public TypeSymbol matchedExprType() {
        return matchedExprType;
    }
}
