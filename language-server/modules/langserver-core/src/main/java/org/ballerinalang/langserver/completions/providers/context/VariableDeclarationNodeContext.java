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

import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.util.CompletionUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Completion provider for {@link VariableDeclarationNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class VariableDeclarationNodeContext extends NodeWithRHSInitializerProvider<VariableDeclarationNode> {

    public VariableDeclarationNodeContext() {
        super(VariableDeclarationNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, VariableDeclarationNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (node.initializer().isPresent() && onExpressionContext(context, node)) {
            completionItems.addAll(this.initializerContextCompletions(context,
                    node.typedBindingPattern().typeDescriptor(), node.initializer().get()));
            this.sort(context, node, completionItems);
            return completionItems;
        } else if (onSuggestionsAfterQualifiers(context, node)) {
            /*
                Covers following
                (1) <qualifier(s)> <cursor>
                (2) <qualifier(s)> x<cursor>
                currently the qualifier can be isolated/transactional.
            */
            completionItems.addAll(getCompletionItemsOnQualifiers(node, context));
            this.sort(context, node, completionItems);
            return completionItems;
        } else if (this.onVariableNameContext(context, node)) {
            return Collections.emptyList();
        }
        return CompletionUtil.route(context, node.parent());
    }

    private boolean onExpressionContext(BallerinaCompletionContext context, VariableDeclarationNode node) {
        if (node.equalsToken().isEmpty()) {
            return false;
        }
        int textPosition = context.getCursorPositionInTree();
        TextRange equalTokenRange = node.equalsToken().get().textRange();

        return equalTokenRange.endOffset() <= textPosition;
    }

    private boolean onVariableNameContext(BallerinaCompletionContext context, VariableDeclarationNode node) {
        int cursor = context.getCursorPositionInTree();
        TypedBindingPatternNode typedBindingPatternNode = node.typedBindingPattern();
        TypeDescriptorNode typeDescriptorNode = typedBindingPatternNode.typeDescriptor();
        Optional<Token> equalsToken = node.equalsToken();

        return cursor > typeDescriptorNode.textRange().endOffset()
                && (equalsToken.isEmpty() || cursor < equalsToken.get().textRange().startOffset());
    }
}
