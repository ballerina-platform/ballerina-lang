/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.hover;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import org.ballerinalang.langserver.common.utils.SymbolUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Resolves a hover symbol.
 *
 * @since 2201.2.0
 */
public class HoverSymbolResolver extends NodeTransformer<Optional<Symbol>> {

    private final List<Node> visitedNodes = new ArrayList<>();

    private final SemanticModel semanticModel;
    private HoverConstructKind constructKind;

    public HoverSymbolResolver(SemanticModel semanticModel) {
        this.semanticModel = semanticModel;
    }

    @Override
    protected Optional<Symbol> transformSyntaxNode(Node node) {
        return this.visit(node.parent());
    }

    private Optional<Symbol> visit(Node node) {
        if (node == null || visitedNodes.contains(node)) {
            return Optional.empty();
        }
        visitedNodes.add(node);
        return node.apply(this);
    }

    @Override
    public Optional<Symbol> transform(QualifiedNameReferenceNode qualifiedNameReferenceNode) {
        Optional<Symbol> symbol = semanticModel.symbol(qualifiedNameReferenceNode);
        symbol.ifPresent(this::findAndSetSymbolConstructKind);
        return symbol;
    }

    private void findAndSetSymbolConstructKind(Symbol symbol) {
        switch (symbol.kind()) {
            case TYPE:
                TypeSymbol typeSymbol = ((TypeReferenceTypeSymbol) symbol).typeDescriptor();
                TypeDescKind typeDescKind = typeSymbol.typeKind();
                if (typeDescKind == TypeDescKind.OBJECT) {
                    if (SymbolUtil.isListener(symbol) && typeSymbol.kind() == SymbolKind.CLASS) {
                        this.constructKind = HoverConstructKind.Listener;
                        break;
                    }
                    if (SymbolUtil.isClient(symbol) && typeSymbol.kind() == SymbolKind.CLASS) {
                        this.constructKind = HoverConstructKind.Client;
                        break;
                    }
                    if (typeSymbol.kind() == SymbolKind.CLASS) {
                        this.constructKind = HoverConstructKind.Class;
                        break;
                    }
                    this.constructKind = HoverConstructKind.ObjectType;
                    break;
                }
                if (typeDescKind == TypeDescKind.RECORD) {
                    this.constructKind = HoverConstructKind.Record;
                    break;
                }
                if (typeDescKind == TypeDescKind.ERROR) {
                    this.constructKind = HoverConstructKind.Error;
                    break;
                }
            case TYPE_DEFINITION:
                this.constructKind = HoverConstructKind.Type;
                break;
            case CLASS:
                if (SymbolUtil.isListener(symbol)) {
                    this.constructKind = HoverConstructKind.Listener;
                    break;
                } else if (SymbolUtil.isClient(symbol)) {
                    this.constructKind = HoverConstructKind.Client;
                    break;
                }
                this.constructKind = HoverConstructKind.Class;
                break;
            case FUNCTION:
                this.constructKind = HoverConstructKind.Function;
                break;
            case CONSTANT:
                this.constructKind = HoverConstructKind.Constant;
                break;
            case ANNOTATION:
                this.constructKind = HoverConstructKind.Annotation;
                break;
            case VARIABLE:
                this.constructKind = HoverConstructKind.Variable;
                break;
            case ENUM:
                //Blocked by 
                //this.constructKind = HoverConstructKind.Enum;
                //break;
            default:
                //ignore
        }
    }

    public Optional<HoverConstructKind> getConstructKind() {
        return Optional.ofNullable(this.constructKind);
    }
}
