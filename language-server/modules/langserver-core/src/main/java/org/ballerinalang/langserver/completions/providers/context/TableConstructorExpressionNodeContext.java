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

import io.ballerina.compiler.syntax.tree.KeySpecifierNode;
import io.ballerina.compiler.syntax.tree.TableConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Completion provider for {@link TableConstructorExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class TableConstructorExpressionNodeContext extends AbstractCompletionProvider<TableConstructorExpressionNode> {

    public TableConstructorExpressionNodeContext() {
        super(TableConstructorExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, TableConstructorExpressionNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();

        if (this.withinKeySpecifier(context, node)) {
            return Collections.singletonList(new SnippetCompletionItem(context, Snippet.KW_KEY.get()));
        }
        int cursor = context.get(CompletionKeys.TEXT_POSITION_IN_TREE);
        if (node.keySpecifier().isPresent() && node.keySpecifier().get().textRange().endOffset() < cursor) {
            /*
            Covers the following
            (1) var test = table key(id) f<cursor>
            (2) var test = stream f<cursor>
            This particular section hits only when (1) and (2) are being the last statement of a block (ex: in function)
             */
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FROM.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.CLAUSE_FROM.get()));
        }
        
        return completionItems;
    }

    private boolean withinKeySpecifier(LSContext context, TableConstructorExpressionNode node) {
        int cursor = context.get(CompletionKeys.TEXT_POSITION_IN_TREE);
        Optional<KeySpecifierNode> keySpecifier = node.keySpecifier();
        Token tableKeyword = node.tableKeyword();

        return cursor > tableKeyword.textRange().endOffset()
                && (!keySpecifier.isPresent() || cursor < keySpecifier.get().keyKeyword().textRange().startOffset());
    }
}
