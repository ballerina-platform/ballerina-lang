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

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.TableTypeDescriptorNode;
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
 * Completion provider for {@link AnnotationNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class TableTypeDescriptorNodeContext extends AbstractCompletionProvider<TableTypeDescriptorNode> {

    public TableTypeDescriptorNodeContext() {
        super(TableTypeDescriptorNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, TableTypeDescriptorNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();

        if (this.onSuggestKeyKw(context, node)) {
            return Collections.singletonList(new SnippetCompletionItem(context, Snippet.KW_KEY.get()));
        }

        return completionItems;
    }

    private boolean onSuggestKeyKw(LSContext context, TableTypeDescriptorNode node) {
        int cursor = context.get(CompletionKeys.TEXT_POSITION_IN_TREE);
        Optional<Node> keyConstraint = node.keyConstraintNode();
        Node rowTypeParamNode = node.rowTypeParameterNode();

        return (keyConstraint.isPresent() && cursor >= rowTypeParamNode.textRange().endOffset())
                || (keyConstraint.isPresent() && cursor <= keyConstraint.get().textRange().startOffset()
                && cursor >= rowTypeParamNode.textRange().endOffset());
    }
}
