/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.PathParameterSymbol;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.resourcepath.PathSegmentList;
import io.ballerina.compiler.api.symbols.resourcepath.ResourcePath;
import io.ballerina.compiler.api.symbols.resourcepath.util.NamedPathSegment;
import io.ballerina.compiler.api.symbols.resourcepath.util.PathSegment;
import io.ballerina.compiler.syntax.tree.ClientResourceAccessActionNode;
import io.ballerina.compiler.syntax.tree.ComputedResourceAccessSegmentNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SymbolCompletionItem;
import org.ballerinalang.langserver.completions.builder.ResourcePathCompletionItemBuilder;
import org.ballerinalang.langserver.completions.util.ContextTypeResolver;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link ClientResourceAccessActionNode} context.
 *
 * @since 2201.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ClientResourceAccessActionNodeContext
        extends RightArrowActionNodeContext<ClientResourceAccessActionNode> {

    public ClientResourceAccessActionNodeContext() {
        super(ClientResourceAccessActionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context,
                                                 ClientResourceAccessActionNode node) throws LSCompletionException {

        List<LSCompletionItem> completionItems = new ArrayList<>();
        ContextTypeResolver resolver = new ContextTypeResolver(context);
        Optional<TypeSymbol> expressionType = node.expression().apply(resolver);

        if (expressionType.isEmpty() || !SymbolUtil.isClient(expressionType.get())) {
            return Collections.emptyList();
        }
        if (isInResourceMethodParameterContext(node, context)) {
            /*
             * Covers the following cases:
             * 1. a->/path/.post(<cursor>)
             * 2. a->/path/.post(mod1:<cursor>)
             */
            if (QNameRefCompletionUtil.onQualifiedNameIdentifier(context, context.getNodeAtCursor())) {
                QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) context.getNodeAtCursor();
                List<Symbol> exprEntries = QNameRefCompletionUtil.getExpressionContextEntries(context, qNameRef);
                List<LSCompletionItem> items = this.getCompletionItemList(exprEntries, context);
                completionItems.addAll(items);
            } else {
                completionItems.addAll(this.actionKWCompletions(context));
                completionItems.addAll(this.expressionCompletions(context));
                completionItems.addAll(this.getNamedArgExpressionCompletionItems(context, node));
            }
        } else {
            /*
            Covers the following case where "a" is a client object and we suggest the client resource access actions 
            and remote methods.
             a -> /path/p<cursor>
             */

            //suggest complete resource path
            List<Node> resourcePathSegments = node.resourceAccessPath().stream()
                    .filter(segmentNode -> !segmentNode.isMissing()).collect(Collectors.toList());
            List<Symbol> clientActions = this.getClientActions(expressionType.get());
            if (resourcePathSegments.isEmpty()) {
                //to cover . resource path and rest param resource path
                completionItems.addAll(this.getCompletionItemList(clientActions, context));
            } else {
                //Suggest partial path segments
                List<ResourceMethodSymbol> resourceMethodSymbols =
                        clientActions.stream().filter(symbol -> symbol.kind() == SymbolKind.RESOURCE_METHOD)
                                .map(symbol -> (ResourceMethodSymbol) symbol).collect(Collectors.toList());
                completionItems.addAll(getPathSegmentCompletionItems(node, context, resourceMethodSymbols,
                        resourcePathSegments));
            }
        }
        this.sort(context, node, completionItems);
        return completionItems;
    }

    private List<LSCompletionItem> getPathSegmentCompletionItems(ClientResourceAccessActionNode node,
                                                                 BallerinaCompletionContext context,
                                                                 List<ResourceMethodSymbol> resourceMethods,
                                                                 List<Node> resourcePathSegments) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        for (ResourceMethodSymbol resourceMethod : resourceMethods) {
            ResourcePath resourcePath = resourceMethod.resourcePath();
            if (resourcePath.kind() == ResourcePath.Kind.PATH_SEGMENT_LIST) {
                Pair<List<PathSegment>, Boolean> completablePathSegments =
                        completableSegmentList(((PathSegmentList) resourcePath).list(),
                                resourcePathSegments, context, node);
                if (completablePathSegments.getRight() && !completablePathSegments.getLeft().isEmpty()) {
                    CompletionItem completionItem = ResourcePathCompletionItemBuilder.build(resourceMethod,
                            completablePathSegments.getLeft(), context);
                    completionItems.add(new SymbolCompletionItem(context, resourceMethod, completionItem));
                } else if (completablePathSegments.getRight() && isInMethodCallContext(node, context)) {
                    //suggest method call expressions
                    CompletionItem completionItem =
                            ResourcePathCompletionItemBuilder.buildMethodCallExpression(resourceMethod, context);
                    completionItems.add(new SymbolCompletionItem(context, resourceMethod, completionItem));
                }
            }
        }
        return completionItems;
    }

    /**
     * Check if a set of path segments are matching with the current segments (resource path) and returns
     * the path segments that can be used to complete the resource path.
     *
     * @param segments        path segment symbols of the resource function
     * @param currentSegments path segment nodes of the client resouce access acion node
     * @param context         completion context.
     * @return
     */
    private Pair<List<PathSegment>, Boolean> completableSegmentList(List<PathSegment> segments,
                                                                    List<Node> currentSegments,
                                                                    BallerinaCompletionContext context,
                                                                    ClientResourceAccessActionNode accNode) {
        if (segments.size() < currentSegments.size()) {
            return Pair.of(Collections.emptyList(), false);
        }
        int index = 0;
        for (int i = 0; i < currentSegments.size(); i++) {
            index += 1;
            Node node = currentSegments.get(i);
            PathSegment segment = segments.get(i);
            if (node.kind() == SyntaxKind.IDENTIFIER_TOKEN
                    && segment.pathSegmentKind() == PathSegment.Kind.NAMED_SEGMENT) {
                String currentPath = ((IdentifierToken) node).text().strip();
                String expectedPath = ((NamedPathSegment) segment).name();
                if (i < currentSegments.size() - 1 && currentPath.equals(expectedPath)) {
                    continue;
                } else if (i == currentSegments.size() - 1) {
                    if (currentPath.equals(expectedPath)) {
                        if (!isInMethodCallContext(accNode, context)) {
                            index -= 1;
                        }
                        continue;
                    } else if (expectedPath.startsWith(currentPath)
                            && !isInMethodCallContext(accNode, context)) {
                        index -= 1;
                        continue;
                    }
                }
                return Pair.of(Collections.emptyList(), false);
            }
            if (node.kind() == SyntaxKind.COMPUTED_RESOURCE_ACCESS_SEGMENT
                    && segment.pathSegmentKind() == PathSegment.Kind.PATH_PARAMETER) {
                Optional<SemanticModel> semanticModel = context.currentSemanticModel();
                if (semanticModel.isEmpty()) {
                    return Pair.of(Collections.emptyList(), false);
                }
                Optional<TypeSymbol> exprType =
                        semanticModel.get().typeOf(((ComputedResourceAccessSegmentNode) node).expression());
                TypeSymbol typeSymbol = ((PathParameterSymbol) segment).typeDescriptor();
                if (exprType.isEmpty() || !exprType.get().subtypeOf(typeSymbol)) {
                    return Pair.of(Collections.emptyList(), false);
                }
                continue;
            }
            return Pair.of(Collections.emptyList(), false);
        }
        return Pair.of(segments.subList(index, segments.size()), true);
    }

    private boolean isInResourceMethodParameterContext(ClientResourceAccessActionNode node,
                                                       BallerinaCompletionContext context) {
        Optional<ParenthesizedArgList> arguments = node.arguments();
        int cursor = context.getCursorPositionInTree();
        return arguments.isPresent() && arguments.get().openParenToken().textRange().startOffset() <= cursor
                && cursor <= arguments.get().closeParenToken().textRange().endOffset();
    }

    private boolean isInMethodCallContext(ClientResourceAccessActionNode node, BallerinaCompletionContext context) {
        Optional<Token> token = node.dotToken();
        return token.isPresent() && token.get().textRange().endOffset() <= context.getCursorPositionInTree();
    }

    private List<LSCompletionItem> getNamedArgExpressionCompletionItems(BallerinaCompletionContext context,
                                                                        ClientResourceAccessActionNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        Optional<SemanticModel> semanticModel = context.currentSemanticModel();
        if (node.arguments().isEmpty() || semanticModel.isEmpty()) {
            return completionItems;
        }
        Optional<Symbol> symbol = semanticModel.get().symbol(node);
        if (symbol.isEmpty() || symbol.get().kind() != SymbolKind.METHOD) {
            return completionItems;
        }
        FunctionSymbol functionSymbol = (FunctionSymbol) symbol.get();
        return getNamedArgCompletionItems(context, functionSymbol,
                node.arguments().get().arguments());
    }
}
