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

import io.ballerinalang.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerinalang.compiler.text.LineRange;
import io.ballerinalang.compiler.text.TextRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Completion provider for {@link ModuleVariableDeclarationNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class ModuleVariableDeclarationNodeContext extends VariableDeclarationProvider<ModuleVariableDeclarationNode> {
    public ModuleVariableDeclarationNodeContext() {
        super(Kind.MODULE_MEMBER);
        this.attachmentPoints.add(ModuleVariableDeclarationNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, ModuleVariableDeclarationNode node) {
        NonTerminalNode nodeAtCursor = context.get(CompletionKeys.NODE_AT_CURSOR_KEY);
        if (this.withinInitializerContext(context, node)) {
            return this.initializerContextCompletions(context, node.typedBindingPattern().typeDescriptor(),
                    node.initializer());
        }

        if (nodeAtCursor.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            Predicate<Scope.ScopeEntry> predicate = scopeEntry -> scopeEntry.symbol instanceof BTypeSymbol;
            List<Scope.ScopeEntry> types = QNameReferenceUtil.getModuleContent(context,
                    (QualifiedNameReferenceNode) nodeAtCursor, predicate);
            return this.getCompletionItemList(types, context);
        }

        if (withinServiceOnKeywordContext(context, node)) {
            return Collections.singletonList(new SnippetCompletionItem(context, Snippet.KW_ON.get()));
        }

        List<LSCompletionItem> completionItems = new ArrayList<>();
        completionItems.addAll(addTopLevelItems(context));
        completionItems.addAll(this.getTypeItems(context));
        completionItems.addAll(this.getPackagesCompletionItems(context));

        return completionItems;
    }

    private boolean withinServiceOnKeywordContext(LSContext context, ModuleVariableDeclarationNode node) {
        TypedBindingPatternNode typedBindingPattern = node.typedBindingPattern();
        TypeDescriptorNode typeDescriptor = typedBindingPattern.typeDescriptor();

        if (typeDescriptor.kind() != SyntaxKind.SERVICE_TYPE_DESC || typedBindingPattern.bindingPattern().isMissing()) {
            return false;
        }

        Position position = context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        LineRange lineRange = typedBindingPattern.bindingPattern().lineRange();
        return (position.getLine() == lineRange.endLine().line()
                && position.getCharacter() > lineRange.endLine().offset())
                || position.getLine() > lineRange.endLine().line();
    }

    private boolean withinInitializerContext(LSContext context, ModuleVariableDeclarationNode node) {
        if (node.equalsToken() == null || node.equalsToken().isMissing()) {
            return false;
        }
        Integer textPosition = context.get(CompletionKeys.TEXT_POSITION_IN_TREE);
        TextRange equalTokenRange = node.equalsToken().textRange();
        return equalTokenRange.endOffset() <= textPosition;
    }
}
