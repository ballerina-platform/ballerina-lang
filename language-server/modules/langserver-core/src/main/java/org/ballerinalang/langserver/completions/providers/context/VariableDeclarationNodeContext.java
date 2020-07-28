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
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerinalang.compiler.text.LinePosition;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.jvm.util.Flags;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.CompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link VariableDeclarationNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class VariableDeclarationNodeContext extends AbstractCompletionProvider<VariableDeclarationNode> {
    public VariableDeclarationNodeContext() {
        super(Kind.MODULE_MEMBER);
        this.attachmentPoints.add(VariableDeclarationNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, VariableDeclarationNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (!node.initializer().isPresent()) {
            TypeDescriptorNode typeDescriptorNode = node.typedBindingPattern().typeDescriptor();
            if (onQualifiedNameIdentifier(context, typeDescriptorNode)) {
                /*
                Covers the following
                Ex: function test() {
                        module:<cursor>
                        module:a<cursor>
                    }
                 */
                QualifiedNameReferenceNode nameRef = (QualifiedNameReferenceNode) typeDescriptorNode;
                Predicate<Scope.ScopeEntry> filter = scopeEntry -> {
                    BSymbol symbol = scopeEntry.symbol;
                    return !CommonUtil.isInvalidSymbol(symbol)
                            && (symbol instanceof BTypeSymbol || symbol instanceof BInvokableSymbol)
                            && (symbol.flags & Flags.PUBLIC) == Flags.PUBLIC;
                };
                completionItems.addAll(this.getModuleContent(context, nameRef, filter));
            } else {
                /*
                Routes to the parent. Parent can be function block, and etc.
                Covers the following
                Ex: function test() {
                        i<cursor>
                    }
                 */
                completionItems.addAll(CompletionUtil.route(context, node.parent()));
            }
        } else if (this.onQualifiedNameIdentifier(context, node.initializer().get())) {
            /*
            Captures the following cases
            (1) [module:]TypeName c = module:<cursor>
            (2) [module:]TypeName c = module:a<cursor>
             */
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) node.initializer().get();
            Predicate<Scope.ScopeEntry> filter = scopeEntry -> {
                BSymbol symbol = scopeEntry.symbol;
                return symbol instanceof BVarSymbol && (symbol.flags & Flags.PUBLIC) == Flags.PUBLIC;
            };
            completionItems.addAll(this.getModuleContent(context, qNameRef, filter));
        } else {
            /*
            Captures the following cases
            (1) [module:]TypeName c = <cursor>
            (2) [module:]TypeName c = a<cursor>
             */
            completionItems.addAll(this.actionKWCompletions(context));
            completionItems.addAll(this.expressionCompletions(context));
            completionItems.addAll(getNewExprCompletionItems(context, node.typedBindingPattern().typeDescriptor()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_IS.get()));
        }

        return completionItems;
    }

    private List<LSCompletionItem> getNewExprCompletionItems(LSContext context, TypeDescriptorNode typeDescriptorNode) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        ArrayList<Scope.ScopeEntry> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        Optional<Scope.ScopeEntry> objectType;
        if (typeDescriptorNode.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            String modulePrefix = ((QualifiedNameReferenceNode) typeDescriptorNode).modulePrefix().text();
            Optional<Scope.ScopeEntry> module = this.getPackageSymbolFromAlias(context, modulePrefix);
            if (!module.isPresent()) {
                return completionItems;
            }
            String identifier = ((QualifiedNameReferenceNode) typeDescriptorNode).identifier().text();
            objectType = module.get().symbol.scope.entries.values().stream()
                    .filter(scopeEntry -> scopeEntry.symbol instanceof BObjectTypeSymbol
                            && scopeEntry.symbol.getName().getValue().equals(identifier))
                    .findAny();
        } else if (typeDescriptorNode.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            String identifier = ((SimpleNameReferenceNode) typeDescriptorNode).name().text();
            objectType = visibleSymbols.stream()
                    .filter(scopeEntry -> scopeEntry.symbol instanceof BObjectTypeSymbol
                            && scopeEntry.symbol.getName().getValue().equals(identifier))
                    .findAny();
        } else {
            objectType = Optional.empty();
        }

        objectType.ifPresent(scopeEntry ->
                completionItems.add(this.getImplicitNewCompletionItem((BObjectTypeSymbol) scopeEntry.symbol, context)));

        return completionItems;
    }

    private boolean onQualifiedNameIdentifier(LSContext context, Node node) {
        if (node.kind() != SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            return false;
        }
        LinePosition colonPos = ((QualifiedNameReferenceNode) node).colon().lineRange().endLine();
        Position cursor = context.get(DocumentServiceKeys.POSITION_KEY).getPosition();

        return colonPos.line() == cursor.getLine() && colonPos.offset() <= cursor.getCharacter();
    }

    private List<LSCompletionItem> getModuleContent(LSContext context,
                                                    QualifiedNameReferenceNode qNameRef,
                                                    Predicate<Scope.ScopeEntry> predicate) {
        Optional<Scope.ScopeEntry> module = this.getPackageSymbolFromAlias(context, qNameRef.modulePrefix().text());
        if (!module.isPresent()) {
            return new ArrayList<>();
        }

        List<Scope.ScopeEntry> filteredContent = module.get().symbol.scope.entries.values().stream()
                .filter(predicate)
                .collect(Collectors.toList());

        return this.getCompletionItemList(filteredContent, context);
    }
}
