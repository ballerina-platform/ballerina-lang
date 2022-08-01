package org.ballerinalang.langserver.completions.builder;

import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.PathParameterSymbol;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.resourcepath.PathRestParam;
import io.ballerina.compiler.api.symbols.resourcepath.PathSegmentList;
import io.ballerina.compiler.api.symbols.resourcepath.ResourcePath;
import io.ballerina.compiler.api.symbols.resourcepath.util.NamedPathSegment;
import io.ballerina.compiler.api.symbols.resourcepath.util.PathSegment;
import io.ballerina.compiler.syntax.tree.ClientResourceAccessActionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.DefaultValueGenerationUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.TextEdit;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Builder for ClientResourceAccessAction completion items.
 *
 * @since 2201.2.1
 */
public class ResourcePathCompletionUtil {

    /**
     * Creates and returns a completion item.
     *
     * @param functionSymbol BSresourceMethodSymbol
     * @param context        LS context
     * @return {@link CompletionItem}
     */
    public static CompletionItem build(ResourceMethodSymbol functionSymbol, BallerinaCompletionContext context) {
        Pair<String, String> functionSignature = ResourcePathCompletionUtil
                .getResourceAccessSignature(functionSymbol, context);
        CompletionItem item = build(functionSymbol, functionSignature, context);
        item.setFilterText(getFilterTextForResourceMethod(functionSymbol));
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
        item.setFilterText(getFilterTextForResourceMethod(resourceMethodSymbol, segments));
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
        checkAndSetAdditionalTextEdits(item, context.getNodeAtCursor(), context);
        return item;
    }

    public static boolean isInMethodCallContext(ClientResourceAccessActionNode node,
                                                BallerinaCompletionContext context) {
        return node.dotToken().isPresent()
                && node.dotToken().get().textRange().endOffset() <= context.getCursorPositionInTree();
    }

    private static void checkAndSetAdditionalTextEdits(CompletionItem item, NonTerminalNode nodeAtCursor,
                                                       BallerinaCompletionContext context) {
        //Check and replace preceding slash token and dot
        Token token = null;
        Optional<ClientResourceAccessActionNode> node = findClientResourceAccessActionNode(context);
        if (node.isPresent()) {
            if (isInMethodCallContext(node.get(), context)) {
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
        if (token != null) {
            TextEdit edit = new TextEdit();
            edit.setNewText("");
            edit.setRange(PositionUtil.toRange(token.lineRange()));
            item.setAdditionalTextEdits(List.of(edit));
        }
    }

    private static Optional<ClientResourceAccessActionNode> findClientResourceAccessActionNode(
            BallerinaCompletionContext context) {
        Node evalNode = context.getNodeAtCursor();
        while (evalNode != null) {
            if (evalNode.kind() == SyntaxKind.CLIENT_RESOURCE_ACCESS_ACTION) {
                return Optional.of((ClientResourceAccessActionNode) evalNode);
            }
            evalNode = evalNode.parent();
        }
        return Optional.empty();
    }

    /**
     * Creates and returns a completion item.
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
        addResourceMethodCallSignature(resourceMethodSymbol, context, escapedFunctionName, signature, insertText, 1);
        item.setLabel(signature.toString());
        item.setInsertText(insertText.toString());
        item.setFilterText(resourceMethodSymbol.getName().orElse(""));
        checkAndSetAdditionalTextEdits(item, context.getNodeAtCursor(), context);
        return item;
    }

    /**
     * Get the resource access action signature.
     *
     * @param resourceMethodSymbol ballerina resource method symbol instance
     * @param ctx                  Language Server Operation context
     * @return {@link Pair} of insert text(left-side) and signature label(right-side)
     */
    private static Pair<String, String> getResourceAccessSignature(ResourceMethodSymbol resourceMethodSymbol,
                                                                   BallerinaCompletionContext ctx) {
        String functionName = resourceMethodSymbol.getName().orElse("");
        String escapedFunctionName = CommonUtil.escapeEscapeCharsInIdentifier(functionName);
        if (functionName.isEmpty()) {
            return ImmutablePair.of(escapedFunctionName + "()", functionName + "()");
        }
        ResourcePath resourcePath = resourceMethodSymbol.resourcePath();
        StringBuilder signature = new StringBuilder();
        StringBuilder insertText = new StringBuilder();
        int placeHolderIndex = addPathSegmentsToSignature(ctx, resourcePath, signature, insertText, 1);

        //functionName considered the resource accessor.
        addResourceMethodCallSignature(resourceMethodSymbol, ctx, escapedFunctionName, signature, insertText,
                placeHolderIndex);
        return new ImmutablePair<>(insertText.toString(), signature.toString());
    }

    /**
     * Get the resource access action signature.
     *
     * @param resourceMethodSymbol ballerina resource method symbol instance
     * @param ctx                  Language Server Operation context
     * @param segments             path segments.
     * @return {@link Pair} of insert text(left-side) and signature label(right-side)
     */
    private static Pair<String, String> getResourceAccessSignature(ResourceMethodSymbol resourceMethodSymbol,
                                                                   BallerinaCompletionContext ctx,
                                                                   List<PathSegment> segments) {
        String functionName = resourceMethodSymbol.getName().orElse("");
        String escapedFunctionName = CommonUtil.escapeEscapeCharsInIdentifier(functionName);
        if (functionName.isEmpty()) {
            return ImmutablePair.of(escapedFunctionName + "()", functionName + "()");
        }
        StringBuilder signature = new StringBuilder();
        StringBuilder insertText = new StringBuilder();
        int placeHolderIndex = 1;
        placeHolderIndex = addPathSegmentsToSignature(ctx, segments, signature, insertText, placeHolderIndex);

        //functionName considered the resource accessor.
        addResourceMethodCallSignature(resourceMethodSymbol, ctx, escapedFunctionName, signature, insertText,
                placeHolderIndex);
        return new ImmutablePair<>(insertText.toString(), signature.toString());
    }

    private static void addResourceMethodCallSignature(ResourceMethodSymbol resourceMethodSymbol,
                                                       BallerinaCompletionContext ctx,
                                                       String escapedFunctionName, StringBuilder signature,
                                                       StringBuilder insertText, int placeHolderIndex) {
        if (resourceMethodSymbol.resourcePath().kind() == ResourcePath.Kind.DOT_RESOURCE_PATH) {
            signature.append("/");
            insertText.append("/");
        }
        if (!escapedFunctionName.equals("get")) {
            signature.append(".").append(escapedFunctionName);
            insertText.append(".").append(escapedFunctionName);
        }

        List<String> funcArguments = CommonUtil.getFuncArguments(resourceMethodSymbol, ctx);
        if (!funcArguments.isEmpty()) {
            signature.append("(").append(String.join(", ", funcArguments)).append(")");
            insertText.append("(${" + placeHolderIndex + "})");
        }
    }

    private static int addPathSegmentsToSignature(BallerinaCompletionContext ctx,
                                                  ResourcePath resourcePath,
                                                  StringBuilder signature,
                                                  StringBuilder insertText,
                                                  int placeHolderIndex) {
        if (resourcePath.kind() == ResourcePath.Kind.PATH_SEGMENT_LIST) {
            PathSegmentList pathSegmentList = (PathSegmentList) resourcePath;
            List<PathSegment> pathSegments = pathSegmentList.list();
            for (PathSegment pathSegment : pathSegments) {
                Pair<String, String> resourceAccessPart =
                        getResourceAccessPartForSegment(pathSegment, placeHolderIndex, ctx);
                signature.append("/").append(resourceAccessPart.getLeft());
                insertText.append("/").append(resourceAccessPart.getRight());
                if (pathSegment.pathSegmentKind() != PathSegment.Kind.NAMED_SEGMENT) {
                    placeHolderIndex += 1;
                }
            }
        } else if (resourcePath.kind() == ResourcePath.Kind.PATH_REST_PARAM) {
            PathRestParam pathRestParam = (PathRestParam) resourcePath;
            Pair<String, String> resourceAccessPart =
                    getResourceAccessPartForSegment(pathRestParam.parameter(), placeHolderIndex, ctx);
            signature.append("/").append(resourceAccessPart.getLeft());
            insertText.append("/").append(resourceAccessPart.getRight());
            placeHolderIndex += 1;
        }
        //DOT_RESOURCE_PATH(".") is ignored.
        return placeHolderIndex;
    }

    private static int addPathSegmentsToSignature(BallerinaCompletionContext ctx,
                                                  List<PathSegment> segments,
                                                  StringBuilder signature,
                                                  StringBuilder insertText,
                                                  int placeHolderIndex) {
        for (PathSegment pathSegment : segments) {
            Pair<String, String> resourceAccessPart =
                    getResourceAccessPartForSegment(pathSegment, placeHolderIndex, ctx);
            signature.append("/").append(resourceAccessPart.getLeft());
            insertText.append("/").append(resourceAccessPart.getRight());
            if (pathSegment.pathSegmentKind() != PathSegment.Kind.NAMED_SEGMENT) {
                placeHolderIndex += 1;
            }
        }
        return placeHolderIndex;
    }

    private static Pair<String, String> getResourceAccessPartForSegment(PathSegment segment, int placeHolderIndex,
                                                                        BallerinaCompletionContext context) {
        switch (segment.pathSegmentKind()) {
            case NAMED_SEGMENT:
                String name = ((NamedPathSegment) segment).name();
                return Pair.of(name, name);
            case PATH_PARAMETER:
                PathParameterSymbol pathParameterSymbol = (PathParameterSymbol) segment;
                Optional<String> defaultValue = DefaultValueGenerationUtil
                        .getDefaultValueForType(pathParameterSymbol.typeDescriptor());
                String paramType = FunctionCompletionItemBuilder
                        .getFunctionParameterSyntax(pathParameterSymbol, context).orElse("");
                return Pair.of("[" + paramType + "]", "[${" + placeHolderIndex + ":"
                        + defaultValue.orElse("") + "}]");
            case PATH_REST_PARAMETER:
                PathParameterSymbol pathRestParam = (PathParameterSymbol) segment;
                ArrayTypeSymbol typeSymbol = (ArrayTypeSymbol) pathRestParam.typeDescriptor();
                Optional<String> defaultVal = DefaultValueGenerationUtil
                        .getDefaultValueForType(typeSymbol.memberTypeDescriptor());
                String param = FunctionCompletionItemBuilder.getFunctionParameterSyntax(pathRestParam, context)
                        .orElse("");
                return Pair.of("[" + param + "]",
                        "[${" + placeHolderIndex + ":" + defaultVal.orElse("\"\"") + "}]");
            default:
                //ignore
        }
        return Pair.of("", "");
    }

    private static String getFilterTextForResourceMethod(ResourceMethodSymbol resourceMethodSymbol) {
        ResourcePath resourcePath = resourceMethodSymbol.resourcePath();
        if (resourcePath.kind() == ResourcePath.Kind.DOT_RESOURCE_PATH
                || resourcePath.kind() == ResourcePath.Kind.PATH_REST_PARAM) {
            return resourceMethodSymbol.getName().orElse("");
        }
        List<PathSegment> pathSegmentList = ((PathSegmentList) resourcePath).list();
        return pathSegmentList.stream()
                .filter(pathSegment -> pathSegment.pathSegmentKind() == PathSegment.Kind.NAMED_SEGMENT)
                .map(pathSegment -> ((NamedPathSegment) pathSegment).name()).collect(Collectors.joining("|"))
                + "|" + resourceMethodSymbol.getName().orElse("");
    }

    private static String getFilterTextForResourceMethod(ResourceMethodSymbol resourceMethodSymbol,
                                                         List<PathSegment> segments) {
        ResourcePath resourcePath = resourceMethodSymbol.resourcePath();
        if (resourcePath.kind() == ResourcePath.Kind.DOT_RESOURCE_PATH
                || resourcePath.kind() == ResourcePath.Kind.PATH_REST_PARAM) {
            return resourceMethodSymbol.getName().orElse("");
        }
        return segments.stream()
                .filter(pathSegment -> pathSegment.pathSegmentKind() == PathSegment.Kind.NAMED_SEGMENT)
                .map(pathSegment -> ((NamedPathSegment) pathSegment).name()).collect(Collectors.joining("|"))
                + "|" + resourceMethodSymbol.getName().orElse("");
    }
}
