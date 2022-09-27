/*
 * Copyright (c) 2022, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.builder;

import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.resourcepath.util.PathSegment;
import io.ballerina.compiler.syntax.tree.ClientResourceAccessActionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.TextEdit;

import java.util.List;
import java.util.Optional;

/**
 * This class is being used to build resource access completion item.
 *
 * @since 2201.2.0
 */
public class ResourcePathCompletionItemBuilder {

    /**
     * Creates and returns a completion item.
     *
     * @param functionSymbol resourceMethodSymbol
     * @param context        LS context
     * @return {@link CompletionItem}
     */
    public static CompletionItem build(ResourceMethodSymbol functionSymbol, BallerinaCompletionContext context) {
        Pair<String, String> functionSignature = ResourcePathCompletionUtil
                .getResourceAccessSignature(functionSymbol, context);
        CompletionItem item = build(functionSymbol, functionSignature, context);
        item.setFilterText(ResourcePathCompletionUtil.getFilterTextForClientResourceAccessAction(functionSymbol));
        return item;
    }

    /**
     * Creates and returns a completion item.
     *
     * @param resourceMethodSymbol resource method symbol.
     * @param segments             path segments.
     * @param context              LS context
     * @return {@link CompletionItem}
     */
    public static CompletionItem build(ResourceMethodSymbol resourceMethodSymbol,
                                       List<PathSegment> segments,
                                       BallerinaCompletionContext context) {
        Pair<String, String> functionSignature = ResourcePathCompletionUtil
                .getResourceAccessSignature(resourceMethodSymbol, context, segments);
        CompletionItem item = build(resourceMethodSymbol, functionSignature, context);
        item.setFilterText(ResourcePathCompletionUtil
                .getFilterTextForClientResourceAccessAction(resourceMethodSymbol, segments));
        return item;
    }

    private static CompletionItem build(ResourceMethodSymbol resourceMethodSymbol,
                                        Pair<String, String> functionSignature,
                                        BallerinaCompletionContext context) {

        CompletionItem item = new CompletionItem();
        FunctionCompletionItemBuilder.setMeta(item, resourceMethodSymbol, context);
        item.setLabel(functionSignature.getRight());
        item.setInsertText(functionSignature.getLeft());

        //Add additional text edits
        checkAndSetAdditionalTextEdits(item, context);
        return item;
    }

    /**
     * Creates and returns a completion item corresponding to the
     * method call expression of the resource function.
     *
     * @param resourceMethodSymbol resource method symbol.
     * @param context              LS context
     * @return {@link CompletionItem}
     */
    public static CompletionItem buildMethodCallExpression(ResourceMethodSymbol resourceMethodSymbol,
                                                           BallerinaCompletionContext context) {
        CompletionItem item = new CompletionItem();
        FunctionCompletionItemBuilder.setMeta(item, resourceMethodSymbol, context);
        String functionName = resourceMethodSymbol.getName().orElse("");
        String escapedFunctionName = CommonUtil.escapeEscapeCharsInIdentifier(functionName);
        StringBuilder signature = new StringBuilder();
        StringBuilder insertText = new StringBuilder();
        ResourcePathCompletionUtil.addResourceMethodCallSignature(resourceMethodSymbol, context, 
                escapedFunctionName, signature, insertText, 1);
        item.setLabel(signature.toString());
        item.setInsertText(insertText.toString());
        item.setFilterText(resourceMethodSymbol.getName().orElse(""));
        checkAndSetAdditionalTextEdits(item, context);
        return item;
    }

    private static void checkAndSetAdditionalTextEdits(CompletionItem item,
                                                       BallerinaCompletionContext context) {
        //Check and replace preceding slash token and dot
        Token token = null;
        Optional<ClientResourceAccessActionNode> node = 
                ResourcePathCompletionUtil.findClientResourceAccessActionNode(context);
        if (node.isPresent()) {
            if (ResourcePathCompletionUtil.isInMethodCallContext(node.get(), context)) {
                if (node.get().resourceAccessPath().isEmpty()
                        && !node.get().slashToken().isMissing()) {
                    //Covers /.<cursor>
                    item.setAdditionalTextEdits(
                            List.of(new TextEdit(PositionUtil.toRange(node.get().slashToken().lineRange()), ""),
                                    new TextEdit(PositionUtil.toRange(node.get().dotToken().get().lineRange()), "")));
                    return;
                }
                //dot token's presence is ensured at this point.
                token = node.get().dotToken().get();
            } else {
                //else replace the last slash token
                SeparatedNodeList<Node> nodes = node.get().resourceAccessPath();
                if (nodes.separatorSize() > 0
                        && nodes.getSeparator(nodes.separatorSize() - 1).textRange().endOffset()
                        <= context.getCursorPositionInTree()) {
                    token = nodes.getSeparator(nodes.separatorSize() - 1);
                } else if (nodes.separatorSize() == 0 && !node.get().slashToken().isMissing()) {
                    token = node.get().slashToken();
                }
            }
        }
        /*Avoid replacing if there are any path parameters specified by the
         user after the token. */
        Token finalToken = token;
        if (finalToken != null && node.get().resourceAccessPath().stream()
                .noneMatch(child -> child.kind() == SyntaxKind.COMPUTED_RESOURCE_ACCESS_SEGMENT
                        && finalToken.textRange().startOffset() <= child.textRange().startOffset())) {
            TextEdit edit = new TextEdit();
            edit.setNewText("");
            edit.setRange(PositionUtil.toRange(token.lineRange()));
            item.setAdditionalTextEdits(List.of(edit));
        }
    }
}
