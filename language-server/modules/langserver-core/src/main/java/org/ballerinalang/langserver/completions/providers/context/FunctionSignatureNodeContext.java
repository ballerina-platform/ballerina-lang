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

import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.CompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.eclipse.lsp4j.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Completion provider for {@link FunctionSignatureNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class FunctionSignatureNodeContext extends AbstractCompletionProvider<FunctionSignatureNode> {
    public FunctionSignatureNodeContext() {
        super(FunctionSignatureNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, FunctionSignatureNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();

        if (withinReturnTypeDescContext(context, node)) {
            if (!node.returnTypeDesc().isPresent()) {
                /*
                Covers the following cases.
                (1) function test() <cursor>
                */
                completionItems.add(new SnippetCompletionItem(context, Snippet.KW_RETURNS.get()));
            } else {
                /*
                Covers the following cases.
                (1) function test() returns <cursor>
                */
                completionItems.addAll(CompletionUtil.route(context, node.returnTypeDesc().get()));
            }
        } else if (this.withinParameterContext(context, node)) {
            NonTerminalNode nodeAtCursor = context.get(CompletionKeys.NODE_AT_CURSOR_KEY);

            // skip the node kind, REQUIRED_PARAM because that maps to the variable name
            if (nodeAtCursor.kind() == SyntaxKind.REQUIRED_PARAM) {
                return completionItems;
            }
            
            if (this.onQualifiedNameIdentifier(context, nodeAtCursor)) {
                /*
                Covers the Following
                (1) function(mod:<cursor>)
                (2) function(mod:T<cursor>)
                 */
                QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
                completionItems.addAll(this.getCompletionItemList(QNameReferenceUtil
                        .getTypesInModule(context, qNameRef), context));
            } else {
                /*
                Covers the Following
                (1) function(<cursor>)
                (2) function(T<cursor>)
                 */
                completionItems.addAll(this.getTypeItems(context));
                completionItems.addAll(this.getModuleCompletionItems(context));
            }
        }
        return completionItems;
    }

    private boolean withinReturnTypeDescContext(LSContext context, FunctionSignatureNode node) {
        Position cursor = context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        LinePosition closeParanPosition = node.closeParenToken().lineRange().endLine();

        return (closeParanPosition.line() == cursor.getLine() && closeParanPosition.offset() < cursor.getCharacter())
                || closeParanPosition.line() < cursor.getLine();
    }

    private boolean withinParameterContext(LSContext context, FunctionSignatureNode node) {
        Integer cursor = context.get(CompletionKeys.TEXT_POSITION_IN_TREE);
        int openParan = node.openParenToken().textRange().endOffset();
        int closeParan = node.closeParenToken().textRange().startOffset();

        return openParan <= cursor && cursor <= closeParan;
    }

    @Override
    public boolean onPreValidation(LSContext context, FunctionSignatureNode node) {
        // If the signature belongs to the function type descriptor, we skip this resolver
        return !node.openParenToken().isMissing() && !node.closeParenToken().isMissing()
                && node.parent().kind() != SyntaxKind.FUNCTION_TYPE_DESC;
    }
}
