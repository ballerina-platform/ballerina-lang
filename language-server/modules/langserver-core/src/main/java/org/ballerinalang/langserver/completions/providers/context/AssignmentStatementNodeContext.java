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

import io.ballerinalang.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.jvm.util.Flags;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Handles the completions for {@link AssignmentStatementNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class AssignmentStatementNodeContext extends AbstractCompletionProvider<AssignmentStatementNode> {
    
    public AssignmentStatementNodeContext() {
        super(AssignmentStatementNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, AssignmentStatementNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (this.onQualifiedNameIdentifier(context, node.expression())) {
            /*
            Captures the following cases
            (1) [module:]TypeName c = module:<cursor>
            (2) [module:]TypeName c = module:a<cursor>
             */
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) node.expression();
            Predicate<Scope.ScopeEntry> filter = scopeEntry -> {
                BSymbol symbol = scopeEntry.symbol;
                return symbol instanceof BVarSymbol && (symbol.flags & Flags.PUBLIC) == Flags.PUBLIC;
            };
            List<Scope.ScopeEntry> moduleContent = QNameReferenceUtil.getModuleContent(context, qNameRef, filter);
            completionItems.addAll(this.getCompletionItemList(moduleContent, context));
        } else {
            /*
            Captures the following cases
            (1) [module:]TypeName c = <cursor>
            (2) [module:]TypeName c = a<cursor>
             */
            completionItems.addAll(this.actionKWCompletions(context));
            completionItems.addAll(this.expressionCompletions(context));
            completionItems.addAll(this.getNewExprCompletionItems(context, node));
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_IS.get()));
        }
        return completionItems;
    }

    @Override
    public boolean onPreValidation(LSContext context, AssignmentStatementNode node) {
        return !node.equalsToken().isMissing();
    }

    private List<LSCompletionItem> getNewExprCompletionItems(LSContext context, AssignmentStatementNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        ArrayList<Scope.ScopeEntry> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        Optional<BObjectTypeSymbol> objectType;
        Node varRef = node.varRef();
        if (varRef.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            String identifier = ((SimpleNameReferenceNode) varRef).name().text();
            objectType = visibleSymbols.stream()
                    .filter(scopeEntry -> scopeEntry.symbol.type.getKind() == TypeKind.OBJECT
                            && scopeEntry.symbol.getName().getValue().equals(identifier))
                    .map(scopeEntry -> (BObjectTypeSymbol) scopeEntry.symbol.type.tsymbol)
                    .findAny();
        } else {
            objectType = Optional.empty();
        }

        objectType.ifPresent(typeSymbol ->
                completionItems.add(this.getImplicitNewCompletionItem(typeSymbol, context)));

        return completionItems;
    }
}
