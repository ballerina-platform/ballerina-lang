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
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
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

import java.util.ArrayList;
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
     * Get the resource access action insert-text and signature information given a resource method.
     *
     * @param resourceMethodSymbol ballerina resource method symbol instance
     * @param ctx                  Language Server Operation context
     * @param segments             path segments.
     * @return {@link Pair} of insert text(left-side) and signature label(right-side)
     */
    public static List<Pair<String, String>> getResourceAccessInfo(ResourceMethodSymbol resourceMethodSymbol,
                                                                   BallerinaCompletionContext ctx,
                                                                   List<PathSegment> segments) {
        String functionName = resourceMethodSymbol.getName().orElse("");
        String escapedFunctionName = CommonUtil.escapeEscapeCharsInIdentifier(functionName);
        if (functionName.isEmpty()) {
            return List.of(ImmutablePair.of(escapedFunctionName + "()", functionName + "()"));
        }

        StringBuilder signatureWithNamedSegments = new StringBuilder();
        StringBuilder insertTextWithNamedSegments = new StringBuilder();

        StringBuilder signatureWithComputedResourceSegments = new StringBuilder();
        StringBuilder insertTextWithComputedResourceSegments = new StringBuilder();

        int placeHolderIndex = 1;
        boolean isStringPathParamsAvailable = false;
        for (PathSegment pathSegment : segments) {
            ResourceAccessPathPart resourceAccessPart =
                    getResourceAccessPartForSegment(pathSegment, placeHolderIndex, ctx);
            signatureWithComputedResourceSegments.append("/").append(resourceAccessPart.computedPathSignature);
            insertTextWithComputedResourceSegments.append("/").append(resourceAccessPart.computedPathInsertText);

            if (resourceAccessPart.isStringPathParam) {
                isStringPathParamsAvailable = true;
                signatureWithNamedSegments.append("/").append(resourceAccessPart.namedPathSignature);
                insertTextWithNamedSegments.append("/").append(resourceAccessPart.namedPathInsertText);
                placeHolderIndex += 1;
                continue;
            }
            signatureWithNamedSegments.append("/").append(resourceAccessPart.computedPathSignature);
            insertTextWithNamedSegments.append("/").append(resourceAccessPart.computedPathInsertText);
            if (pathSegment.pathSegmentKind() != PathSegment.Kind.NAMED_SEGMENT) {
                placeHolderIndex += 1;
            }
        }

        List<Pair<String, String>> resourceAccessInfo = new ArrayList<>();
        addResourceMethodCallSignature(resourceMethodSymbol, ctx, escapedFunctionName,
                signatureWithComputedResourceSegments, insertTextWithComputedResourceSegments, placeHolderIndex);
        insertTextWithComputedResourceSegments.append(";");
        resourceAccessInfo.add(new ImmutablePair<>(insertTextWithComputedResourceSegments.toString(),
                signatureWithComputedResourceSegments.toString()));

        if (isStringPathParamsAvailable) {
            addResourceMethodCallSignature(resourceMethodSymbol, ctx, escapedFunctionName,
                    signatureWithNamedSegments, insertTextWithNamedSegments, placeHolderIndex);
            insertTextWithNamedSegments.append(";");
            resourceAccessInfo.add(new ImmutablePair<>(insertTextWithNamedSegments.toString(),
                    signatureWithNamedSegments.toString()));
        }
        return resourceAccessInfo;
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

    /**
     * Check if the given expression node is a valid type for the given parameter node.
     *
     * @param paramType Type of the parameter
     * @param exprType  Type of the expression
     * @param exprValue Value of the expression
     * @return Returns true if the expression is a valid type for the parameter
     */
    public static boolean checkSubtype(TypeSymbol paramType, TypeSymbol exprType, String exprValue) {
        if (exprType.subtypeOf(paramType)) {
            return true;
        }
        switch (paramType.typeKind()) {
            case UNION:
                for (TypeSymbol childSymbol : ((UnionTypeSymbol) paramType).memberTypeDescriptors()) {
                    if (checkSubtype(childSymbol, exprType, exprValue)) {
                        return true;
                    }
                }
                return false;
            case SINGLETON:
                return paramType.subtypeOf(exprType) && exprValue.equals(paramType.signature());
            case TYPE_REFERENCE:
                return paramType.subtypeOf(exprType);
            default:
                return false;
        }
    }


    private static ResourceAccessPathPart getResourceAccessPartForSegment(PathSegment segment, int placeHolderIndex,
                                                                          BallerinaCompletionContext context) {

        if (segment.pathSegmentKind() == PathSegment.Kind.NAMED_SEGMENT) {
            String name = ((NamedPathSegment) segment).name();
            return new ResourceAccessPathPart(name, name);
        }

        PathParameterSymbol pathParameterSymbol = (PathParameterSymbol) segment;
        Optional<String> defaultValue;
        TypeSymbol typeSymbol = pathParameterSymbol.typeDescriptor();
        if (segment.pathSegmentKind() == PathSegment.Kind.PATH_REST_PARAMETER
                && pathParameterSymbol.typeDescriptor().typeKind() == TypeDescKind.ARRAY) {
            typeSymbol = ((ArrayTypeSymbol) (pathParameterSymbol.typeDescriptor())).memberTypeDescriptor();
        }
        defaultValue = DefaultValueGenerationUtil
                .getDefaultValueForType(typeSymbol);
        String paramType = FunctionCompletionItemBuilder
                .getFunctionParameterSyntax(pathParameterSymbol, context).orElse("");
        String computedSignature = "[" + paramType + "]";
        String computedInsertText =
                typeSymbol.typeKind().equals(TypeDescKind.SINGLETON) ? "[" + defaultValue.orElse("") + "]" :
                        "[${" + placeHolderIndex + ":" + defaultValue.orElse("") + "}]";
        ResourceAccessPathPart resourceAccessPathPart =
                new ResourceAccessPathPart(computedInsertText, computedSignature);

        //  A resource method parameter can be a singleton or a union with a `string`. As the node "text" is always
        //  resolved to `string`, we need to check if typeSymbol is either a supertype or a subtype  of `string`.
        if (context.currentSemanticModel().isPresent() &&
                isStringSubtype(typeSymbol, context.currentSemanticModel().get().types().STRING)) {
            if (typeSymbol.typeKind().equals(TypeDescKind.SINGLETON)) {
                resourceAccessPathPart.namedPathSignature =
                        typeSymbol.signature().substring(1, typeSymbol.signature().length() - 1);
                resourceAccessPathPart.namedPathInsertText = resourceAccessPathPart.namedPathSignature;
            } else {
                resourceAccessPathPart.namedPathSignature = "<" +
                        (pathParameterSymbol.getName().isPresent() ? pathParameterSymbol.getName().get() : "path") +
                        ">";
                resourceAccessPathPart.namedPathInsertText = "${" + placeHolderIndex + ":" + "path" + "}";
                resourceAccessPathPart.computedPathInsertText = "[${" + placeHolderIndex + ":" + "\"path\"" + "}]";
            }
            resourceAccessPathPart.isStringPathParam = true;
        }
        return resourceAccessPathPart;
    }

    private static boolean isStringSubtype(TypeSymbol paramType, TypeSymbol stringType) {
        if (stringType.subtypeOf(paramType)) {
            return true;
        }
        switch (paramType.typeKind()) {
            case UNION:
                for (TypeSymbol childSymbol : ((UnionTypeSymbol) paramType).memberTypeDescriptors()) {
                    if (isStringSubtype(childSymbol, stringType)) {
                        return true;
                    }
                }
                return false;
            case SINGLETON:
            case TYPE_REFERENCE:
                return paramType.subtypeOf(stringType);
            default:
                return false;
        }
    }

    private static class ResourceAccessPathPart {

        private String computedPathInsertText;
        private String computedPathSignature;

        private String namedPathSignature;
        private String namedPathInsertText;

        boolean isStringPathParam = false;

        ResourceAccessPathPart(String computedPathInsertText, String computedPathSignature) {
            this.computedPathInsertText = computedPathInsertText;
            this.computedPathSignature = computedPathSignature;
        }
    }

}
