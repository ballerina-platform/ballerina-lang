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
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.DefaultValueGenerationUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Builder for ClientResourceAccessAction completion items.
 *
 * @since 2201.2.0
 */
public class ResourcePathCompletionUtil {

    /**
     * Check if the cursor is positioned within the method call context of client resource access action node.
     *
     * @param node    client resource access action node.
     * @param context completion context
     * @return boolean
     */
    public static boolean isInMethodCallContext(ClientResourceAccessActionNode node,
                                                BallerinaCompletionContext context) {
        return node.dotToken().isPresent()
                && node.dotToken().get().textRange().endOffset() <= context.getCursorPositionInTree();
    }

    /**
     * Finds the client resource access action node based on cursor position.
     *
     * @param context completion context.
     * @return client resource access action node
     */
    public static Optional<ClientResourceAccessActionNode> findClientResourceAccessActionNode(
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
     * Get the resource access action signature.
     *
     * @param resourceMethodSymbol ballerina resource method symbol instance
     * @param ctx                  Language Server Operation context
     * @return {@link Pair} of insert text(left-side) and signature label(right-side)
     */
    public static Pair<String, String> getResourceAccessSignature(ResourceMethodSymbol resourceMethodSymbol,
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
    public static Pair<String, String> getResourceAccessSignature(ResourceMethodSymbol resourceMethodSymbol,
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

    /**
     * Add the call expression part to a given client resource access action.
     *
     * @param resourceMethodSymbol resource method symbol
     * @param ctx                  completion context
     * @param escapedFunctionName  escaped function name
     * @param signature            signature of the completion item
     * @param insertText           insert text of the completion item
     * @param placeHolderIndex     next place holder index
     */
    public static void addResourceMethodCallSignature(ResourceMethodSymbol resourceMethodSymbol,
                                                      BallerinaCompletionContext ctx,
                                                      String escapedFunctionName, StringBuilder signature,
                                                      StringBuilder insertText, int placeHolderIndex) {
        /*
            Covers 
            cl-> /path1.<post()> part of the client resource access action.
         */
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
            insertText.append("(${").append(placeHolderIndex).append("})");
        }
    }

    /**
     * Generate filter text for client resource access action completion item.
     *
     * @param resourceMethodSymbol resource method symbol
     * @param segments             path segments of the client resource access action
     * @return {@link String}
     */
    public static String getFilterTextForClientResourceAccessAction(ResourceMethodSymbol resourceMethodSymbol,
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

    static String getFilterTextForClientResourceAccessAction(ResourceMethodSymbol resourceMethodSymbol) {
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

}
