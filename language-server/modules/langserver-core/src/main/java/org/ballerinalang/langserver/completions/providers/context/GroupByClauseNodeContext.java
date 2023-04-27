/*
 * Copyright (c) 2023, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import io.ballerina.compiler.syntax.tree.*;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.List;

/**
 * Completion provider for {@link GroupByClauseNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class GroupByClauseNodeContext extends IntermediateClauseNodeContext<GroupByClauseNode> {

    public GroupByClauseNodeContext() {
        super(GroupByClauseNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, GroupByClauseNode node) {

        List<LSCompletionItem> completionItems = new ArrayList<>();
        int cursor = context.getCursorPositionInTree();
        SeparatedNodeList<Node> groupingKey = node.groupingKey();
        Node lastNode = groupingKey.get(groupingKey.separatorSize());

        if (lastNode.kind() != SyntaxKind.GROUPING_KEY_VAR_DECLARATION || this.onBindingPatternContext(cursor, lastNode)) {
            /*
            Covers the case where the cursor is within the binding pattern context.
            Eg:
            (1) group by var <cursor>
            (2) group by var cu<cursor>
            (3) group by var item = foo(), var <cursor>
            In these cases no suggestions are provided
             */
            return completionItems;
        }
        
        if (this.onTypedBindingPatternContext(cursor, lastNode)) {
            /*
            Covers the case where the cursor is within the typed binding pattern context.
            Eg:
            (1) group by <cursor>
            (2) group by v<cursor>
            (3) group by var item = foo(), v<cursor>
             */
            NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
            if (nodeAtCursor.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
                return this.getCompletionItemList(QNameRefCompletionUtil.getTypesInModule(context, qNameRef), context);
            }
            completionItems.addAll(this.expressionCompletions(context));
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_VAR.get()));
        } else if (this.onExpressionContext(cursor, lastNode)) {
            /*
            Covers the case where the cursor is within the expression context.
            Eg:
            (1) group by var item = <cursor>
             */
            completionItems.addAll(this.expressionCompletions(context));
        }
//        this.sort(context, node, completionItems);
        return completionItems;
    }
//
//    public static boolean isSymbolWithinNodeAndCursor(BallerinaCompletionContext context,
//                                                      Symbol symbol, Node startNode) {
//
//        return symbol.getLocation()
//                .filter(location -> startNode.textRange().startOffset() < location.textRange().startOffset())
//                .filter(location -> location.textRange().endOffset() < context.getCursorPositionInTree())
//                .isPresent();
//    }
//
//    private boolean afterGroupByKeywords(BallerinaCompletionContext context, GroupByClauseNode node) {
//        return true;
//    }

    private boolean onBindingPatternContext(int cursorPos, Node lastNode) {
        GroupingKeyVarDeclarationNode groupingKeyVarDeclNode = (GroupingKeyVarDeclarationNode) lastNode;
        return cursorPos >= groupingKeyVarDeclNode.simpleBindingPattern().textRange().startOffset()
                && cursorPos < groupingKeyVarDeclNode.equalsToken().textRange().startOffset();
    }

    private boolean onTypedBindingPatternContext(int cursorPos, Node lastNode) {
        TypeDescriptorNode typeDescriptorNode = ((GroupingKeyVarDeclarationNode) lastNode).typeDescriptor();
        return cursorPos < typeDescriptorNode.textRange().startOffset() ||
                cursorPos >= typeDescriptorNode.textRange().startOffset()
                        && cursorPos <= typeDescriptorNode.textRange().endOffset();
    }

    private boolean onExpressionContext(int cursorPos, Node lastNode) {
        return cursorPos > ((GroupingKeyVarDeclarationNode) lastNode).equalsToken().textRange().endOffset();
    }


}

//    @Override
//    public void sort(BallerinaCompletionContext context,
//                     OrderByClauseNode node,
//                     List<LSCompletionItem> completionItems) {
//        
//        List<TypeDescKind> basicTypes = Arrays.asList(
//                TypeDescKind.STRING, TypeDescKind.INT,
//                TypeDescKind.BOOLEAN, TypeDescKind.FLOAT,
//                TypeDescKind.DECIMAL);
//
//        Optional<QueryExpressionNode> queryExprNode =  SortingUtil.getTheOutermostQueryExpressionNode(node);
//        if (queryExprNode.isEmpty()) {
//            return;
//        }
//        completionItems.forEach(lsCItem -> {
//            int rank = 3;
//            if (SortingUtil.isSymbolCItemWithinNodeAndCursor(context, lsCItem, queryExprNode.get())) {
//                rank = 1;
//            } else if (CommonUtil.isCompletionItemOfType(lsCItem, basicTypes)) {
//                rank = 2;
//            }
//            lsCItem.getCompletionItem().setSortText(SortingUtil.genSortText(rank) +
//                    SortingUtil.genSortText(SortingUtil.toRank(context, lsCItem)));
//        });
//    }