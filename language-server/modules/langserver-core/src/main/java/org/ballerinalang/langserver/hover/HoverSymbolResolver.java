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

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import org.ballerinalang.langserver.commons.HoverContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Resolves a hover symbol.
 *
 * @since 2201.2.0
 */
public class HoverSymbolResolver extends NodeTransformer<Optional<Symbol>> {

    //Todo: use a set instead
    private final List<Node> visitedNodes = new ArrayList<>();

    private final SemanticModel semanticModel;
    private final HoverContext context;
    private boolean isSymbolReferable = true;

    public HoverSymbolResolver(HoverContext context, SemanticModel semanticModel) {
        this.semanticModel = semanticModel;
        this.context = context;
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
        symbol.ifPresent(this::setSymbolReferable);
        
        return symbol;
    }

    public void setSymbolReferable(Symbol symbol) {
        Optional<Package> currentPackage = context.workspace().project(context.filePath()).map(Project::currentPackage);
        Optional<ModuleID> moduleID = symbol.getModule().map(ModuleSymbol::id);
        if (currentPackage.isEmpty() || moduleID.isEmpty()) {
            this.isSymbolReferable = false;
            return;
        }

        if (moduleID.get().packageName().equals(currentPackage.get().packageName().toString())
                && moduleID.get().orgName().equals(currentPackage.get().packageOrg().value())) {
            this.isSymbolReferable = false;
            return;
        }
        this.isSymbolReferable = true;
    }

    public boolean isSymbolReferable() {
        return Boolean.TRUE.equals(this.isSymbolReferable);
    }

}
