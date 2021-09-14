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

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.context.util.ModulePartNodeContextUtil;
import org.ballerinalang.langserver.completions.util.CompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SortingUtil;
import org.eclipse.lsp4j.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Completion provider for {@link ModuleVariableDeclarationNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ModuleVariableDeclarationNodeContext extends
        NodeWithRHSInitializerProvider<ModuleVariableDeclarationNode> {
    public ModuleVariableDeclarationNodeContext() {
        super(ModuleVariableDeclarationNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext ctx, ModuleVariableDeclarationNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        ResolvedContext resolvedContext;
        TypeDescriptorNode tDescNode = node.typedBindingPattern().typeDescriptor();
        if (this.withinInitializerContext(ctx, node)) {
            completionItems.addAll(this.initializerContextCompletions(ctx, tDescNode));
            resolvedContext = ResolvedContext.INITIALIZER;
        } else if (tDescNode.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE
                && ModulePartNodeContextUtil.onServiceTypeDescContext(((SimpleNameReferenceNode) tDescNode).name(),
                ctx)) {
            /*
            Covers the following cases
            Eg
            (1) service m<cursor>
            (2) isolated service m<cursor>
            
            Bellow cases are being handled by ModulePartNodeContext
            Eg:
            (1) service <cursor>
            (2) isolated service <cursor>
             */
            List<Symbol> objectSymbols = ModulePartNodeContextUtil.serviceTypeDescContextSymbols(ctx);
            completionItems.addAll(this.getCompletionItemList(objectSymbols, ctx));
            completionItems.addAll(this.getModuleCompletionItems(ctx));
            completionItems.add(new SnippetCompletionItem(ctx, Snippet.KW_ON.get()));
            resolvedContext = ResolvedContext.SERVICE_TYPEDESC;
        } else if (withinServiceOnKeywordContext(ctx, node)) {
            completionItems.add(new SnippetCompletionItem(ctx, Snippet.KW_ON.get()));
            resolvedContext = ResolvedContext.SERVICE_TYPEDESC;
        } else {
            // Type descriptor completions and the keyword completions are suggested via the ModulePartNodeContext.
            return CompletionUtil.route(ctx, node.parent());
        }
        this.sort(ctx, node, completionItems, resolvedContext);

        return completionItems;
    }

    @Override
    public void sort(BallerinaCompletionContext context, ModuleVariableDeclarationNode node,
                     List<LSCompletionItem> completionItems, Object... metaData) {
        ResolvedContext resolvedContext = (ResolvedContext) metaData[0];
        if (resolvedContext == ResolvedContext.INITIALIZER) {
            // Calls the NodeWithRHSInitializerProvider's sorting logic to 
            // make it consistent throughout the implementation
            super.sort(context, node, completionItems);
            return;
        }

        // Captures the ResolvedContext.SERVICE_TYPEDESC
        for (LSCompletionItem lsCItem : completionItems) {
            String sortingText;
            if (lsCItem.getType() != LSCompletionItem.CompletionItemType.SNIPPET) {
                sortingText = SortingUtil.genSortText(1);
            } else {
                sortingText = SortingUtil.genSortText(2)
                        + SortingUtil.genSortText(SortingUtil.toRank(context, lsCItem));
            }
            lsCItem.getCompletionItem().setSortText(sortingText);
        }
    }

    private boolean withinServiceOnKeywordContext(BallerinaCompletionContext context,
                                                  ModuleVariableDeclarationNode node) {
        TypedBindingPatternNode typedBindingPattern = node.typedBindingPattern();
        TypeDescriptorNode typeDescriptor = typedBindingPattern.typeDescriptor();

        if (typeDescriptor.kind() != SyntaxKind.SERVICE_TYPE_DESC
                || typedBindingPattern.bindingPattern().isMissing()) {
            return false;
        }

        Position position = context.getCursorPosition();
        LineRange lineRange = typedBindingPattern.bindingPattern().lineRange();
        return (position.getLine() == lineRange.endLine().line()
                && position.getCharacter() > lineRange.endLine().offset())
                || position.getLine() > lineRange.endLine().line();
    }

    private boolean withinInitializerContext(BallerinaCompletionContext context, ModuleVariableDeclarationNode node) {
        if (node.equalsToken().isEmpty()) {
            return false;
        }
        int textPosition = context.getCursorPositionInTree();
        TextRange equalTokenRange = node.equalsToken().get().textRange();
        return equalTokenRange.endOffset() <= textPosition;
    }

    enum ResolvedContext {
        INITIALIZER,
        SERVICE_TYPEDESC
    }
}
