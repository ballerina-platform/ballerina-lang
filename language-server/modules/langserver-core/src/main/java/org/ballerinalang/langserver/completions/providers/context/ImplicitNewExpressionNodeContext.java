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

import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Completion provider for {@link ImplicitNewExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ImplicitNewExpressionNodeContext extends AbstractCompletionProvider<ImplicitNewExpressionNode> {

    public ImplicitNewExpressionNodeContext() {
        super(ImplicitNewExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, ImplicitNewExpressionNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (this.withinArgs(context, node)) {
            /*
            Covers
            lhs = new(<cursor>)
             */
            completionItems.addAll(this.getCompletionsWithinArgs(context));
        } else {
            /*
            Supports the following
            (1) lhs = new <cursor>
            */
            Optional<ClassSymbol> classSymbol = getClassSymbol(context, node);
            completionItems.addAll(this.getModuleCompletionItems(context));
            classSymbol.ifPresent(symbol -> completionItems.add(this.getExplicitNewCompletionItem(symbol, context)));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    private Optional<ClassSymbol> getClassSymbol(BallerinaCompletionContext context, Node node) {
        Node typeDescriptor;

        switch (node.parent().kind()) {
            case LISTENER_DECLARATION:
                typeDescriptor = ((ListenerDeclarationNode) node.parent()).typeDescriptor().orElse(null);
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

        if (typeDescriptor == null) {
            return Optional.empty();
        }

        Optional<Symbol> nameReferenceSymbol = Optional.empty();
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        if (this.onQualifiedNameIdentifier(context, typeDescriptor)) {
            QualifiedNameReferenceNode nameReferenceNode = (QualifiedNameReferenceNode) typeDescriptor;

            Optional<ModuleSymbol> pkgSymbol = CommonUtil.searchModuleForAlias(context,
                    QNameReferenceUtil.getAlias(nameReferenceNode));
            if (pkgSymbol.isEmpty()) {
                return Optional.empty();
            }
            nameReferenceSymbol = pkgSymbol.get().allSymbols().stream()
                    .filter(symbol -> Objects
                            .equals(symbol.getName().orElse(null), nameReferenceNode.identifier().text()))
                    .findFirst();
        } else if (typeDescriptor.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            SimpleNameReferenceNode nameReferenceNode = (SimpleNameReferenceNode) typeDescriptor;
            nameReferenceSymbol = visibleSymbols.stream()
                    .filter(symbol -> Objects.equals(symbol.getName().orElse(null), nameReferenceNode.name().text()))
                    .findFirst();
        }

        if (nameReferenceSymbol.isEmpty() || !SymbolUtil.isObject(nameReferenceSymbol.get())) {
            return Optional.empty();
        }

        return Optional.of(SymbolUtil.getTypeDescForClassSymbol(nameReferenceSymbol.get()));
    }

    private Optional<ClassSymbol> getObjectTypeForVarRef(BallerinaCompletionContext context, Node varRefNode) {
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        if (varRefNode.kind() != SyntaxKind.SIMPLE_NAME_REFERENCE) {
            return Optional.empty();
        }
        String varName = ((SimpleNameReferenceNode) varRefNode).name().text();
        Optional<Symbol> varEntry = visibleSymbols.stream()
                .filter(symbol -> Objects.equals(symbol.getName().orElse(null), varName))
                .findFirst();

        if (varEntry.isEmpty() || !SymbolUtil.isObject(varEntry.get())) {
            return Optional.empty();
        }

        return Optional.of(SymbolUtil.getTypeDescForClassSymbol(varEntry.get()));
    }

    private boolean withinArgs(BallerinaCompletionContext context, ImplicitNewExpressionNode node) {
        if (node.parenthesizedArgList().isEmpty()) {
            return false;
        }
        ParenthesizedArgList parenthesizedArgList = node.parenthesizedArgList().get();
        int cursor = context.getCursorPositionInTree();

        return cursor > parenthesizedArgList.openParenToken().textRange().startOffset()
                && cursor < parenthesizedArgList.closeParenToken().textRange().endOffset();
    }

    private List<LSCompletionItem> getCompletionsWithinArgs(BallerinaCompletionContext ctx) {
        NonTerminalNode nodeAtCursor = ctx.getNodeAtCursor();
        if (this.onQualifiedNameIdentifier(ctx, nodeAtCursor)) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            return this.getCompletionItemList(QNameReferenceUtil.getExpressionContextEntries(ctx, qNameRef), ctx);
        }

        return this.expressionCompletions(ctx);
    }
}
