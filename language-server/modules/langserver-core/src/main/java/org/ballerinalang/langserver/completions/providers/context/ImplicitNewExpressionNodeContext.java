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

import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.types.ObjectTypeDescriptor;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.QNameReferenceUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;

import java.util.ArrayList;
import java.util.List;
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
    public List<LSCompletionItem> getCompletions(LSContext context, ImplicitNewExpressionNode node) {
        /*
        Supports the following
        (1) lhs = new <cursor>
        */
        Optional<ObjectTypeDescriptor> objectTypeSymbol = getObjectTypeDescriptor(context, node);
        List<LSCompletionItem> completionItems = new ArrayList<>(this.getModuleCompletionItems(context));
        objectTypeSymbol.ifPresent(bSymbol -> completionItems.add(this.getExplicitNewCompletionItem(bSymbol, context)));

        return completionItems;
    }

    private Optional<ObjectTypeDescriptor> getObjectTypeDescriptor(LSContext context, Node node) {
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

        Optional<Symbol> nameReferenceSymbol = Optional.empty();
        List<Symbol> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        if (this.onQualifiedNameIdentifier(context, typeDescriptor)) {
            QualifiedNameReferenceNode nameReferenceNode = (QualifiedNameReferenceNode) typeDescriptor;

            Optional<ModuleSymbol> pkgSymbol = CommonUtil.searchModuleForAlias(context,
                    QNameReferenceUtil.getAlias(nameReferenceNode));
            if (pkgSymbol.isEmpty()) {
                return Optional.empty();
            }
            nameReferenceSymbol = pkgSymbol.get().allSymbols().stream()
                    .filter(symbol -> symbol.name().equals(nameReferenceNode.identifier().text()))
                    .findFirst();
        } else if (typeDescriptor.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            SimpleNameReferenceNode nameReferenceNode = (SimpleNameReferenceNode) typeDescriptor;
            nameReferenceSymbol = visibleSymbols.stream()
                    .filter(symbol -> symbol.name().equals(nameReferenceNode.name().text()))
                    .findFirst();
        }

        if (nameReferenceSymbol.isEmpty() || !SymbolUtil.isObject(nameReferenceSymbol.get())) {
            return Optional.empty();
        }

        return Optional.of(SymbolUtil.getTypeDescForObjectSymbol(nameReferenceSymbol.get()));
    }

    private Optional<ObjectTypeDescriptor> getObjectTypeForVarRef(LSContext context, Node varRefNode) {
        List<Symbol> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        if (varRefNode.kind() != SyntaxKind.SIMPLE_NAME_REFERENCE) {
            return Optional.empty();
        }
        String varName = ((SimpleNameReferenceNode) varRefNode).name().text();
        Optional<Symbol> varEntry = visibleSymbols.stream()
                .filter(symbol -> symbol.name().equals(varName))
                .findFirst();

        if (varEntry.isEmpty() || !SymbolUtil.isObject(varEntry.get())) {
            return Optional.empty();
        }

        return Optional.of(SymbolUtil.getTypeDescForObjectSymbol(varEntry.get()));
    }
}
