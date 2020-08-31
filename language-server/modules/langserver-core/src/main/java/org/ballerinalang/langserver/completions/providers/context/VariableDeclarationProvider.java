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
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.TypeDescriptorNode;
import org.ballerinalang.jvm.util.Flags;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Generic completion resolver for the Block Nodes.
 *
 * @param <T> block node type
 * @since 2.0.0
 */
public class VariableDeclarationProvider<T extends Node> extends AbstractCompletionProvider<T> {

    public VariableDeclarationProvider(Class<T> attachmentPoint) {
        super(attachmentPoint);
    }

    protected List<LSCompletionItem> initializerContextCompletions(LSContext context,
                                                                   TypeDescriptorNode typeDsc) {
        NonTerminalNode nodeAtCursor = context.get(CompletionKeys.NODE_AT_CURSOR_KEY);
        if (this.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            /*
            Captures the following cases
            (1) [module:]TypeName c = module:<cursor>
            (2) [module:]TypeName c = module:a<cursor>
             */
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            Predicate<Scope.ScopeEntry> filter = scopeEntry -> {
                BSymbol symbol = scopeEntry.symbol;
                return symbol instanceof BVarSymbol && (symbol.flags & Flags.PUBLIC) == Flags.PUBLIC;
            };
            List<Scope.ScopeEntry> moduleContent = QNameReferenceUtil.getModuleContent(context, qNameRef, filter);
            return this.getCompletionItemList(moduleContent, context);
        }
        
        /*
        Captures the following cases
        (1) [module:]TypeName c = <cursor>
        (2) [module:]TypeName c = a<cursor>
         */
        List<LSCompletionItem> completionItems = new ArrayList<>();
        completionItems.addAll(this.actionKWCompletions(context));
        completionItems.addAll(this.expressionCompletions(context));
        completionItems.addAll(getNewExprCompletionItems(context, typeDsc));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_IS.get()));

        return completionItems;
    }

    private List<LSCompletionItem> getNewExprCompletionItems(LSContext context, TypeDescriptorNode typeDescriptorNode) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        ArrayList<Scope.ScopeEntry> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        Optional<Scope.ScopeEntry> objectType;
        if (this.onQualifiedNameIdentifier(context, typeDescriptorNode)) {
            String modulePrefix = QNameReferenceUtil.getAlias(((QualifiedNameReferenceNode) typeDescriptorNode));
            Optional<Scope.ScopeEntry> module = CommonUtil.packageSymbolFromAlias(context, modulePrefix);
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
}
