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

import io.ballerinalang.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.ReturnStatementNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;

import java.util.ArrayList;
import java.util.List;

/**
 * Completion provider for {@link ReturnStatementNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class ReturnStatementNodeContext extends AbstractCompletionProvider<ReturnStatementNode> {
    public ReturnStatementNodeContext() {
        super(Kind.OTHER);
        this.attachmentPoints.add(ReturnStatementNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, ReturnStatementNode node)
            throws LSCompletionException {
        if (node.expression().isPresent() && node.expression().get().kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            List<Scope.ScopeEntry> entries = QNameReferenceUtil.expressionContextEntries(context,
                    (QualifiedNameReferenceNode) node.expression().get());
            
            return this.getCompletionItemList(entries, context);
        }

        List<LSCompletionItem> completionItems = new ArrayList<>();
        completionItems.addAll(this.actionKWCompletions(context));
        completionItems.addAll(this.expressionCompletions(context));
        return completionItems;
    }
}
