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

import io.ballerina.compiler.syntax.tree.ForEachStatementNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.eclipse.lsp4j.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Completion provider for {@link ForEachStatementNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class ForEachStatementNodeContext extends AbstractCompletionProvider<ForEachStatementNode> {
    public ForEachStatementNodeContext() {
        super(ForEachStatementNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, ForEachStatementNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();

        if (withinTypeDescContext(context, node)) {
            completionItems.addAll(this.getModuleCompletionItems(context));
            completionItems.addAll(this.getTypeItems(context));
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_VAR.get()));
        } else if (node.inKeyword().isMissing()) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_IN.get()));
        } else if (withinActionOrExpressionContext(context, node)) {
            completionItems.addAll(this.actionKWCompletions(context));
            completionItems.addAll(this.expressionCompletions(context));
        }

        return completionItems;
    }

    private boolean withinTypeDescContext(LSContext context, ForEachStatementNode node) {
        /*
        Check whether the following is satisfied
        (1) foreach <cursor>typedesc ...
        (2) foreach <cursor>
        (3) foreach t<cursor>
         */
        TypedBindingPatternNode typedBindingPatternNode = node.typedBindingPattern();
        if (typedBindingPatternNode.isMissing() || typedBindingPatternNode.typeDescriptor().isMissing()) {
            return false;
        }
        TypeDescriptorNode typeDescriptorNode = typedBindingPatternNode.typeDescriptor();
        LinePosition typeDescEnd = typeDescriptorNode.lineRange().endLine();
        Position cursor = context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        return (cursor.getLine() < typeDescEnd.line())
                || (cursor.getLine() == typeDescEnd.line() && cursor.getCharacter() <= typeDescEnd.offset());
    }

    private boolean withinActionOrExpressionContext(LSContext context, ForEachStatementNode node) {
        /*
        Check whether the following is satisfied
        (1) foreach typedesc name in <cursor> ...
        (2) foreach typedesc name in a<cursor> ...
         */
        Token inKeyword = node.inKeyword();
        if (inKeyword.isMissing()) {
            return false;
        }
        LinePosition inKWEnd = inKeyword.lineRange().endLine();
        Position cursor = context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        return (cursor.getLine() < inKWEnd.line())
                || (cursor.getLine() == inKWEnd.line() && cursor.getCharacter() > inKWEnd.offset());
    }
}
