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
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.PathParameterSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.resourcepath.PathRestParam;
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
import io.ballerina.tools.text.LinePosition;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SymbolCompletionItem;
import org.ballerinalang.langserver.completions.builder.ResourcePathCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.ResourcePathCompletionUtil;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.SortingUtil;
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
        if (onSuggestClients(node, context)) {
            // Covers following:
            // 1. cl<cursor>->/path1/path2
            completionItems.addAll(this.expressionCompletions(context));
            this.sort(context, node, completionItems);
            return completionItems;
        }

        LinePosition linePosition = node.expression().location().lineRange().endLine();
        Optional<TypeSymbol> expressionType = Optional.empty();
        if (context.currentSemanticModel().isPresent() && context.currentDocument().isPresent()) {
            expressionType = context.currentSemanticModel().get()
                    .expectedType(context.currentDocument().get(), linePosition);
        }

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
            List<Symbol> clientActions = this.getClientActions(expressionType.get());
            List<ResourceMethodSymbol> resourceMethodSymbols = clientActions.stream()
                    .filter(symbol -> symbol.kind() == SymbolKind.RESOURCE_METHOD)
                    .map(symbol -> (ResourceMethodSymbol) symbol).collect(Collectors.toList());
            List<MethodSymbol> remoteMethods = clientActions.stream()
                    .filter(symbol -> symbol.kind() == SymbolKind.METHOD
                            && ((MethodSymbol) symbol).qualifiers().contains(Qualifier.REMOTE))
                    .map(symbol -> (MethodSymbol) symbol).collect(Collectors.toList());

            if (node.slashToken().isMissing()) {
                completionItems.addAll(this.getCompletionItemList(remoteMethods, context));
            }
            
            /*
            Covers the following case where "a" is a client object, and we suggest the client resource access actions 
            and remote methods.
             a -> /path/p<cursor>
             */
            List<Node> resourcePathSegments = node.resourceAccessPath().stream()
                    .filter(segmentNode -> !segmentNode.isMissing()).collect(Collectors.toList());

            if (!resourcePathSegments.isEmpty()
                    || ResourcePathCompletionUtil.isInMethodCallContext(node, context)) {
                //Covers /path<cursor> and /.<cursor>
                completionItems.addAll(getPathSegmentCompletionItems(node, context, resourceMethodSymbols,
                        resourcePathSegments));
            } else if (!ResourcePathCompletionUtil.isInMethodCallContext(node, context)) {
                //Covers /<cursor>
                completionItems.addAll(this.getCompletionItemList(resourceMethodSymbols, context));
            }
        }
        this.sort(context, node, completionItems);
        return completionItems;
    }

    @Override
    public void sort(BallerinaCompletionContext context, ClientResourceAccessActionNode node,
                     List<LSCompletionItem> completionItems) {
        // At expression of the remote method call action, suggest clients first
        if (onSuggestClients(node, context)) {
            completionItems.forEach(completionItem -> {
                Optional<TypeSymbol> typeSymbol = SortingUtil.getSymbolFromCompletionItem(completionItem);
                String sortText;
                // Prioritize clients
                if (typeSymbol.isPresent() && SymbolUtil.isClient(typeSymbol.get())) {
                    sortText = SortingUtil.genSortText(1);
                } else {
                    sortText = SortingUtil.genSortText(2);
                }
                sortText += SortingUtil.genSortText(SortingUtil.toRank(context, completionItem));
                completionItem.getCompletionItem().setSortText(sortText);
            });
            return;
        }

        super.sort(context, node, completionItems);
    }

    private List<LSCompletionItem> getPathSegmentCompletionItems(ClientResourceAccessActionNode node,
                                                                 BallerinaCompletionContext context,
                                                                 List<ResourceMethodSymbol> resourceMethods,
                                                                 List<Node> resourcePathSegments) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        for (ResourceMethodSymbol resourceMethod : resourceMethods) {
            ResourcePath resourcePath = resourceMethod.resourcePath();

            List<PathSegment> segments;
            if (resourcePath.kind() == ResourcePath.Kind.PATH_SEGMENT_LIST) {
                segments = ((PathSegmentList) resourcePath).list();
            } else if (resourcePath.kind() == ResourcePath.Kind.PATH_REST_PARAM) {
                PathParameterSymbol parameterSymbol = ((PathRestParam) resourcePath).parameter();
                segments = List.of(parameterSymbol);
            } else {
                segments = new ArrayList<>();
            }

            Pair<List<PathSegment>, Boolean> completablePathSegments =
                    completableSegmentList(resourceMethod, segments,
                            resourcePathSegments, context, node);
            if (completablePathSegments.getRight() && !completablePathSegments.getLeft().isEmpty()) {
                CompletionItem completionItem = ResourcePathCompletionItemBuilder.build(resourceMethod,
                        completablePathSegments.getLeft(), context);
                completionItems.add(new SymbolCompletionItem(context, resourceMethod, completionItem));
            } else if (completablePathSegments.getRight() && ResourcePathCompletionUtil
                    .isInMethodCallContext(node, context)) {
                //suggest method call expressions
                if (!isGetMethodWithoutParams(resourceMethod)) {
                    CompletionItem completionItem =
                            ResourcePathCompletionItemBuilder.buildMethodCallExpression(resourceMethod, context);
                    completionItems.add(new SymbolCompletionItem(context, resourceMethod, completionItem));
                }
            }
        }
        return completionItems;
    }

    /**
     * Check if a resource methods signature is matching with the provided client resource access path and returns
     * the path segments that can be used to complete the resource path and whether the resource method's
     * signature is a match.
     *
     * @param segments        path segment symbols of the resource method
     * @param currentSegments path segment nodes of the client resource access action node
     * @param context         completion context
     * @return completable list of path segments(left) and whether the given resource method is matching(right)
     */
    private Pair<List<PathSegment>, Boolean> completableSegmentList(ResourceMethodSymbol resourceMethodSymbol,
                                                                    List<PathSegment> segments,
                                                                    List<Node> currentSegments,
                                                                    BallerinaCompletionContext context,
                                                                    ClientResourceAccessActionNode accNode) {
        Optional<PathSegment> pathRestParam =
                segments.stream().filter(pathSegment ->
                        pathSegment.pathSegmentKind() == PathSegment.Kind.PATH_REST_PARAMETER).findFirst();
        if (segments.size() < currentSegments.size() && pathRestParam.isEmpty()) {
            return Pair.of(Collections.emptyList(), false);
        }

        if (currentSegments.isEmpty() && ResourcePathCompletionUtil.isInMethodCallContext(accNode, context)) {
            return Pair.of(Collections.emptyList(),
                    resourceMethodSymbol.resourcePath().kind() == ResourcePath.Kind.DOT_RESOURCE_PATH);
        }

        int completableSegmentStartIndex = 0;
        int numOfProvidedSegments = currentSegments.size();
        for (int i = 0; i < numOfProvidedSegments; i++) {
            completableSegmentStartIndex += 1;
            Node node = currentSegments.get(i);
            PathSegment segment;
            if (i > segments.size() - 1) {
                if (pathRestParam.isEmpty()) {
                    return Pair.of(Collections.emptyList(), false);
                }
                segment = pathRestParam.get();
            } else {
                segment = segments.get(i);
            }
            if (node.kind() == SyntaxKind.IDENTIFIER_TOKEN
                    && segment.pathSegmentKind() == PathSegment.Kind.NAMED_SEGMENT) {
                String currentPath = ((IdentifierToken) node).text().strip();
                String expectedPath = ((NamedPathSegment) segment).name();
                if (i < numOfProvidedSegments - 1 && currentPath.equals(expectedPath)) {
                    continue;
                } else if (i == numOfProvidedSegments - 1) {
                    if (currentPath.equals(expectedPath)) {
                        if (context.getCursorPositionInTree() == node.textRange().endOffset()) {
                            //covers /path1<cursor>, excluding /path1/<cursor> and /path1.<cursor>
                            completableSegmentStartIndex -= 1;
                        }
                        continue;
                    } else if (expectedPath.startsWith(currentPath)
                            && context.getCursorPositionInTree() == node.textRange().endOffset()) {
                        //suggest only when /pat<cursor> excluding /pat/<cursor>
                        completableSegmentStartIndex -= 1;
                        continue;
                    }
                }
                //Covers named segments that are not matching
                return Pair.of(Collections.emptyList(), false);
            } else if (segment.pathSegmentKind() == PathSegment.Kind.PATH_PARAMETER ||
                    segment.pathSegmentKind() == PathSegment.Kind.PATH_REST_PARAMETER) {
                if (node.kind() == SyntaxKind.COMPUTED_RESOURCE_ACCESS_SEGMENT) {
                    TypeSymbol typeSymbol = segment.pathSegmentKind() == PathSegment.Kind.PATH_REST_PARAMETER ?
                            ((ArrayTypeSymbol) (((PathParameterSymbol) segment).typeDescriptor()))
                                    .memberTypeDescriptor() : ((PathParameterSymbol) segment).typeDescriptor();
                    Optional<SemanticModel> semanticModel = context.currentSemanticModel();
                    if (semanticModel.isEmpty()) {
                        return Pair.of(Collections.emptyList(), false);
                    }
                    Optional<TypeSymbol> exprType =
                            semanticModel.get().typeOf(((ComputedResourceAccessSegmentNode) node).expression());

                    if (exprType.isEmpty() || !exprType.get().subtypeOf(typeSymbol)) {
                        return Pair.of(Collections.emptyList(), false);
                    }
                    continue;
                } else if (node.kind() == SyntaxKind.IDENTIFIER_TOKEN) {
                    continue;
                }
            }
            return Pair.of(Collections.emptyList(), false);
        }

        List<PathSegment> completableSegments =
                completableSegmentStartIndex <= segments.size() ?
                        segments.subList(completableSegmentStartIndex, segments.size()) : Collections.emptyList();
        return Pair.of(completableSegments, true);
    }

    private boolean onSuggestClients(ClientResourceAccessActionNode node, BallerinaCompletionContext context) {
        int cursor = context.getCursorPositionInTree();
        return cursor <= node.rightArrowToken().textRange().startOffset();
    }

    private boolean isInResourceMethodParameterContext(ClientResourceAccessActionNode node,
                                                       BallerinaCompletionContext context) {
        Optional<ParenthesizedArgList> arguments = node.arguments();
        int cursor = context.getCursorPositionInTree();
        return arguments.isPresent() && arguments.get().openParenToken().textRange().startOffset() <= cursor
                && cursor <= arguments.get().closeParenToken().textRange().endOffset();
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

    private boolean isGetMethodWithoutParams(ResourceMethodSymbol resourceMethodSymbol) {
        Optional<String> name = resourceMethodSymbol.getName();
        FunctionTypeSymbol functionTypeSymbol = resourceMethodSymbol.typeDescriptor();
        if (name.isPresent() && !"get".equals(name.get())) {
            return false;
        }
        return (functionTypeSymbol.params().isEmpty()
                || functionTypeSymbol.params().get().size() == 0)
                && functionTypeSymbol.restParam().isEmpty();
    }
}
