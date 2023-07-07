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
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.FunctionTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.Minutiae;
import io.ballerina.compiler.syntax.tree.MinutiaeList;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SortingUtil;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Completion provider for {@link FunctionTypeDescriptorNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class FunctionTypeDescriptorNodeContext extends AbstractCompletionProvider<FunctionTypeDescriptorNode> {

    public FunctionTypeDescriptorNodeContext() {
        super(FunctionTypeDescriptorNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, FunctionTypeDescriptorNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        RuleContext ruleContext = RuleContext.OTHER;
        if (onSuggestionsAfterQualifiers(context, node)) {
            /*
             * Covers the following
             * isolated <cursor> function
             */
            completionItems.addAll(getCompletionItemsOnQualifiers(node, context));
        } else if (this.withinParameterContext(context, node)) {
            /*
            Covers the completions when the cursor is within the parameter context
             */
            if (QNameRefCompletionUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
                List<Symbol> typesInModule = QNameRefCompletionUtil.getTypesInModule(context,
                        ((QualifiedNameReferenceNode) nodeAtCursor));
                completionItems.addAll(this.getCompletionItemList(typesInModule, context));
            } else {
                completionItems.addAll(this.getTypeDescContextItems(context));
            }
            ruleContext = RuleContext.PARAMETER_CTX;
        } else if (this.withinReturnKWContext(context, node)) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_RETURNS.get()));
        } else if (node.parent().kind() == SyntaxKind.OBJECT_FIELD) {
            /* Covers the completions for init function in object constructor or class definition
             * eg: object { function <cursor> }
             *     object { public function <cursor> }
             */
            SnippetCompletionItem initFuncCompletionItem =
                    new SnippetCompletionItem(context, Snippet.DEF_INIT_FUNCTION.get());
            Token funcKW = node.functionKeyword();
            int endOffset = getEndPosWithoutNewLine(funcKW);
            Range range = PositionUtil.toRange(funcKW.textRange().startOffset(), endOffset,
                    context.currentDocument().get().textDocument());
            TextEdit textEdit = new TextEdit(range, "");
            initFuncCompletionItem.getCompletionItem().setAdditionalTextEdits(List.of(textEdit));
            completionItems.add(initFuncCompletionItem);
            completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_FUNCTION.get()));
        }
        this.sort(context, node, completionItems, ruleContext);

        return completionItems;
    }

    private int getEndPosWithoutNewLine(Token token) {
        /* Purpose of this method to handle the following scenario specially
         * eg: object { function<cursor> }
         * If we have single trailing minutiae, and it is a new line, then we need to consider `function` keyword as
         * an existing token.
         */
        int end = token.textRangeWithMinutiae().endOffset();
        MinutiaeList minutiaeList = token.trailingMinutiae();
        int size = minutiaeList.size();
        if (size == 0) {
            return end;
        }
        Minutiae lastMinutiae = minutiaeList.get(size - 1);
        if (lastMinutiae.kind() == SyntaxKind.END_OF_LINE_MINUTIAE) {
            return size == 1 ? token.textRange().startOffset() : end - 1;
        }
        return end;
    }

    @Override
    protected List<LSCompletionItem> getCompletionItemsOnQualifiers(Node node, BallerinaCompletionContext context) {
        List<LSCompletionItem> completionItems = new ArrayList<>(super.getCompletionItemsOnQualifiers(node, context));
        List<Token> qualifiers = CommonUtil.getQualifiersOfNode(context, node);
        if (qualifiers.isEmpty()) {
            return completionItems;
        }
        Token lastQualifier = qualifiers.get(qualifiers.size() - 1);
        if (lastQualifier.kind() == SyntaxKind.ISOLATED_KEYWORD) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FUNCTION.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_OBJECT_TYPE_DESC_SNIPPET.get()));
        }
        return completionItems;
    }

    private boolean withinParameterContext(BallerinaCompletionContext context, FunctionTypeDescriptorNode node) {
        if (node.functionSignature().isEmpty()) {
            return false;
        }
        FunctionSignatureNode functionSignatureNode = node.functionSignature().get();
        int txtPosInTree = context.getCursorPositionInTree();
        TextRange openParanRange = functionSignatureNode.openParenToken().textRange();
        TextRange closeParanRange = functionSignatureNode.closeParenToken().textRange();

        return openParanRange.endOffset() <= txtPosInTree && txtPosInTree <= closeParanRange.startOffset();
    }

    private boolean withinReturnKWContext(BallerinaCompletionContext context, FunctionTypeDescriptorNode node) {
        if (node.functionSignature().isEmpty()) {
            return false;
        }
        FunctionSignatureNode functionSignatureNode = node.functionSignature().get();
        int txtPosInTree = context.getCursorPositionInTree();
        TextRange closeParanRange = functionSignatureNode.closeParenToken().textRange();
        Optional<ReturnTypeDescriptorNode> returnTypeDescNode = functionSignatureNode.returnTypeDesc();

        return closeParanRange.startOffset() <= txtPosInTree && (!returnTypeDescNode.isPresent()
                || returnTypeDescNode.get().returnsKeyword().isMissing());
    }

    @Override
    protected boolean onSuggestionsAfterQualifiers(BallerinaCompletionContext context, Node node) {
        int cursor = context.getCursorPositionInTree();
        Token functionKeyword = ((FunctionTypeDescriptorNode) node).functionKeyword();
        return super.onSuggestionsAfterQualifiers(context, node) &&
                cursor < functionKeyword.textRange().startOffset();
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, FunctionTypeDescriptorNode node) {
        return !node.functionKeyword().isMissing() &&
                context.getCursorPositionInTree() > node.functionKeyword().textRange().startOffset();
    }

    @Override
    public void sort(BallerinaCompletionContext context, FunctionTypeDescriptorNode node,
                     List<LSCompletionItem> completionItems, Object... metaData) {
        if (metaData.length == 1 && metaData[0] instanceof RuleContext && metaData[0] == RuleContext.PARAMETER_CTX) {
            for (LSCompletionItem lsCItem : completionItems) {
                String sortText = SortingUtil.genSortTextForTypeDescContext(context, lsCItem);
                lsCItem.getCompletionItem().setSortText(sortText);
            }
            return;
        }
        super.sort(context, node, completionItems);
    }

    private enum RuleContext {
        PARAMETER_CTX,
        OTHER
    }
}
