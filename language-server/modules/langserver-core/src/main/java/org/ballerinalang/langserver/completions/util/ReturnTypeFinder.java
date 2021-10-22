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

package org.ballerinalang.langserver.completions.util;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.WorkerSymbol;
import io.ballerina.compiler.syntax.tree.ExplicitAnonymousFunctionExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.NamedWorkerDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;

import java.util.Optional;

/**
 * Finds the expected return type of a given node's context.
 * That is, this class will visit the parents until it finds a context to
 * which a return type is applicable and returns the expected type.
 *
 * @since 2.0.0
 */
public class ReturnTypeFinder extends NodeTransformer<Optional<TypeSymbol>> {

    private final SemanticModel semanticModel;

    public ReturnTypeFinder(SemanticModel semanticModel) {
        this.semanticModel = semanticModel;
    }

    public Optional<TypeSymbol> getTypeSymbol(Node node) {
        return node.apply(this);
    }

    @Override
    public Optional<TypeSymbol> transform(FunctionDefinitionNode functionDefinitionNode) {
        return semanticModel.symbol(functionDefinitionNode.functionName())
                .flatMap(symbol -> ((FunctionSymbol) symbol).typeDescriptor().returnTypeDescriptor());
    }

    @Override
    public Optional<TypeSymbol> transform(NamedWorkerDeclarationNode namedWorkerDeclarationNode) {
        Optional<Symbol> symbol = semanticModel.symbol(namedWorkerDeclarationNode);
        if (symbol.isPresent()) {
            WorkerSymbol workerSymbol = (WorkerSymbol) symbol.get();
            return Optional.of(workerSymbol.returnType());
        }
        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(ExplicitAnonymousFunctionExpressionNode
                                                      explicitAnonymousFunctionExpressionNode) {

        Optional<TypeSymbol> type = semanticModel.typeOf(explicitAnonymousFunctionExpressionNode);
        if (type.isPresent() && type.get().typeKind() == TypeDescKind.FUNCTION) {
            FunctionTypeSymbol functionTypeSymbol = (FunctionTypeSymbol) type.get();
            return functionTypeSymbol.returnTypeDescriptor();
        }
        return Optional.empty();
    }

    @Override
    protected Optional<TypeSymbol> transformSyntaxNode(Node node) {
        if (node.parent() != null) {
            return node.parent().apply(this);
        }
        return Optional.empty();
    }
}
