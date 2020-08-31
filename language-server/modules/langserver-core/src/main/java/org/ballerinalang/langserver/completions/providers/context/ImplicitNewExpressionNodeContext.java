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
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerinalang.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerinalang.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.VariableDeclarationNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Completion provider for {@link ImplicitNewExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class ImplicitNewExpressionNodeContext extends AbstractCompletionProvider<ImplicitNewExpressionNode> {

    public ImplicitNewExpressionNodeContext() {
        super(ImplicitNewExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext ctx) {

        return new ArrayList<>();
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, ImplicitNewExpressionNode node) {
        /*
        Supports the following
        (1) lhs = new <cursor>
        */
        Optional<BObjectTypeSymbol> objectTypeSymbol = getObjectTypeSymbol(context, node);
        List<LSCompletionItem> completionItems = new ArrayList<>(this.getPackagesCompletionItems(context));
        objectTypeSymbol.ifPresent(bSymbol -> completionItems.add(this.getExplicitNewCompletionItem(bSymbol, context)));

        return completionItems;
    }

    private Optional<BObjectTypeSymbol> getObjectTypeSymbol(LSContext context, Node node) {
        Node typeDescriptor;

        switch (node.parent().kind()) {
            case LISTENER_DECLARATION:
                typeDescriptor = ((ListenerDeclarationNode) node.parent()).typeDescriptor();
                break;
            case LOCAL_VAR_DECL:
                typeDescriptor = ((VariableDeclarationNode) node.parent()).typedBindingPattern().typeDescriptor();
                break;
            case ASSIGNMENT_STATEMENT:
                Node varRef = ((AssignmentStatementNode) node.parent()).varRef();
                return this.getObjectTypeForVarRef(context, varRef);
            default:
                return Optional.empty();
        }

        Scope.ScopeEntry scopeEntry = null;
        List<Scope.ScopeEntry> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
            if (this.onQualifiedNameIdentifier(context, typeDescriptor)) {
            QualifiedNameReferenceNode nameReferenceNode = (QualifiedNameReferenceNode) typeDescriptor;

            Optional<Scope.ScopeEntry> pkgSymbol = CommonUtil.packageSymbolFromAlias(context,
                    QNameReferenceUtil.getAlias(nameReferenceNode));
            if (!pkgSymbol.isPresent()) {
                return Optional.empty();
            }
            scopeEntry = ((BPackageSymbol) pkgSymbol.get().symbol).scope.entries.entrySet().stream()
                    .filter(entry -> entry.getKey().value.equals(nameReferenceNode.identifier().text()))
                    .map(Map.Entry::getValue)
                    .findAny()
                    .orElse(null);
        } else if (typeDescriptor.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            SimpleNameReferenceNode nameReferenceNode = (SimpleNameReferenceNode) typeDescriptor;
            scopeEntry = visibleSymbols.stream()
                    .filter(entry -> entry.symbol.name.value.equals(nameReferenceNode.name().text()))
                    .findAny()
                    .orElse(null);
        }

        return scopeEntry == null || scopeEntry.symbol.kind != SymbolKind.OBJECT
                ? Optional.empty() : Optional.of((BObjectTypeSymbol) scopeEntry.symbol);
    }
    
    private Optional<BObjectTypeSymbol> getObjectTypeForVarRef(LSContext context, Node varRefNode) {
        List<Scope.ScopeEntry> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        if (varRefNode.kind() != SyntaxKind.SIMPLE_NAME_REFERENCE) {
            return Optional.empty();
        }
        String varName = ((SimpleNameReferenceNode) varRefNode).name().text();
        Optional<Scope.ScopeEntry> varEntry = visibleSymbols.stream()
                .filter(entry -> entry.symbol.name.value.equals(varName))
                .findAny();
        
        if (!varEntry.isPresent() || !(varEntry.get().symbol.type.tsymbol instanceof BObjectTypeSymbol)) {
            return Optional.empty();
        }
        
        return Optional.of((BObjectTypeSymbol) varEntry.get().symbol.type.tsymbol);
    }
}
