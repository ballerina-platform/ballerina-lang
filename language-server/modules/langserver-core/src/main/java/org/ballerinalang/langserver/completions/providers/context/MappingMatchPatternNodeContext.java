/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import io.ballerina.compiler.syntax.tree.FieldMatchPatternNode;
import io.ballerina.compiler.syntax.tree.MappingMatchPatternNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link MappingMatchPatternNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class MappingMatchPatternNodeContext extends MappingContextProvider<MappingMatchPatternNode> {

    public MappingMatchPatternNodeContext() {
        super(MappingMatchPatternNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, MappingMatchPatternNode node)
            throws LSCompletionException {

        List<LSCompletionItem> completionItems = new ArrayList<>();
        Optional<Node> evalNode = CommonUtil.getMappingContextEvalNode(context.getNodeAtCursor());
        if (evalNode.isEmpty()) {
            return completionItems;
        }
        if (this.withinValueExpression(context, evalNode.get())) {
            completionItems.addAll(this.getCompletionsInValueExpressionContext(context));
        } else {
            completionItems.addAll(this.getFieldCompletionItems(context, node, evalNode.get()));
        }
        this.sort(context, node, completionItems);
        return completionItems;
    }

    @Override
    protected List<String> getFields(MappingMatchPatternNode node) {
        return node.fieldMatchPatterns().stream()
                .filter(field -> !field.isMissing() && field.kind() == SyntaxKind.FIELD_MATCH_PATTERN
                        && ((FieldMatchPatternNode) field).fieldNameNode().kind() == SyntaxKind.IDENTIFIER_TOKEN)
                .map(field -> ((FieldMatchPatternNode) field).fieldNameNode().text())
                .collect(Collectors.toList());
    }

    @Override
    protected boolean withinValueExpression(BallerinaCompletionContext context, Node evalNodeAtCursor) {
        if (evalNodeAtCursor.kind() != SyntaxKind.FIELD_MATCH_PATTERN) {
            return false;
        }
        Token colon = ((FieldMatchPatternNode) evalNodeAtCursor).colonToken();
        int cursorPosInTree = context.getCursorPositionInTree();
        int colonStart = colon.textRange().startOffset();
        return cursorPosInTree > colonStart;
    }
}
