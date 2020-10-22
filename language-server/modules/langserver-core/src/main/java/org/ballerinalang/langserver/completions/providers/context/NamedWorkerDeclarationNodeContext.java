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

import io.ballerina.compiler.syntax.tree.NamedWorkerDeclarationNode;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.CompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Completion provider for {@link NamedWorkerDeclarationNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class NamedWorkerDeclarationNodeContext extends AbstractCompletionProvider<NamedWorkerDeclarationNode> {
    public NamedWorkerDeclarationNodeContext() {
        super(NamedWorkerDeclarationNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, NamedWorkerDeclarationNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        boolean inReturnContext = this.withinReturnTypeContext(context, node);

        if (inReturnContext && !node.returnTypeDesc().isPresent()) {
            return Collections.singletonList(new SnippetCompletionItem(context, Snippet.KW_RETURNS.get()));
        }

        if (inReturnContext && node.returnTypeDesc().isPresent()) {
            return CompletionUtil.route(context, node.returnTypeDesc().get());
        }

        return completionItems;
    }

    private boolean withinReturnTypeContext(LSContext context, NamedWorkerDeclarationNode node) {
        Integer textPosition = context.get(CompletionKeys.TEXT_POSITION_IN_TREE);
        TextRange nameRange = node.workerName().textRange();
        TextRange bodyStart = node.workerBody().openBraceToken().textRange();
        return nameRange.endOffset() < textPosition && textPosition < bodyStart.startOffset();
    }
}
