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

import io.ballerina.compiler.syntax.tree.IntermediateClauseNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.CompletionUtil;

import java.util.List;

/**
 * Completion provider for {@link TypedBindingPatternNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class TypedBindingPatternNodeContext extends AbstractCompletionProvider<TypedBindingPatternNode> {

    public TypedBindingPatternNodeContext() {
        super(TypedBindingPatternNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, TypedBindingPatternNode node)
            throws LSCompletionException {
        /*
        When comes to the typed binding pattern we route to the type descriptors to check whether there are resolvers
        associated with the type descriptor. Otherwise the router will go up the parent ladder.
         */
        if (withinTypeDesc(context, node) || node.typeDescriptor().kind() == SyntaxKind.TABLE_TYPE_DESC
                || node.typeDescriptor().kind() == SyntaxKind.FUNCTION_TYPE_DESC
                || node.parent() instanceof IntermediateClauseNode
                || node.parent().kind() == SyntaxKind.FOREACH_STATEMENT
                || node.parent().kind() == SyntaxKind.FROM_CLAUSE) {
            return CompletionUtil.route(context, node.typeDescriptor());
        }

        return CompletionUtil.route(context, node.parent());
    }

    private boolean withinTypeDesc(BallerinaCompletionContext context, TypedBindingPatternNode node) {
        int cursor = context.getCursorPositionInTree();
        TypeDescriptorNode tDesc = node.typeDescriptor();

        return cursor >= tDesc.textRange().startOffset() && cursor <= tDesc.textRange().endOffset();
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, TypedBindingPatternNode node) {
        int cursor = context.getCursorPositionInTree();
        TypeDescriptorNode typeDescriptorNode = node.typeDescriptor();
        
        return !typeDescriptorNode.isMissing() && cursor > typeDescriptorNode.textRange().endOffset();
    }
}
