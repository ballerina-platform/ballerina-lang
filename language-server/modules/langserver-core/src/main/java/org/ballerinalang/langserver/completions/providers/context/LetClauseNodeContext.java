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
import io.ballerina.compiler.syntax.tree.LetClauseNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.List;

/**
 * Completion provider for {@link LetClauseNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class LetClauseNodeContext extends AbstractCompletionProvider<LetClauseNode> {

    public LetClauseNodeContext() {
        super(LetClauseNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, LetClauseNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();

        if (nodeAtCursor.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            /*
            Covers the cases where the cursor is within the expression context
             */
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            List<Symbol> typesInModule = QNameReferenceUtil.getTypesInModule(context, qNameRef);

            completionItems.addAll(this.getCompletionItemList(typesInModule, context));
        } else {
            completionItems.addAll(this.getTypeItems(context));
            completionItems.addAll(this.getModuleCompletionItems(context));
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_VAR.get()));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, LetClauseNode node) {
        return !node.letKeyword().isMissing();
    }
}
