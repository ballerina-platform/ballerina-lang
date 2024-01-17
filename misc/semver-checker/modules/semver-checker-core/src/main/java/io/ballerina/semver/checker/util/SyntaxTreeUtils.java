/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.semver.checker.util;

import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;

import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.semver.checker.util.PackageUtils.capitalize;

/**
 * Syntax tree API related utilities.
 *
 * @since 2201.2.0
 */
public class SyntaxTreeUtils {

    /**
     * Returns the node typename of the given {@link SyntaxKind} instance.
     *
     * @param nodeKind node kind
     * @return node typename
     */
    public static String getNodeKindName(SyntaxKind nodeKind) {
        return nodeKind.name().toLowerCase(Locale.ROOT).toLowerCase(Locale.ENGLISH).replaceAll("_", " ");
    }

    /**
     * Returns the identifier for a given service node.
     *
     * @param serviceNode service declaration syntax node
     * @return the service identifier
     */
    public static Optional<String> getServiceIdentifier(ServiceDeclarationNode serviceNode) {
        // Todo: refactor once the service identifier support is added to the language side
        if (serviceNode.absoluteResourcePath() != null && !serviceNode.absoluteResourcePath().isEmpty()) {
            return Optional.of(serviceNode.absoluteResourcePath().stream()
                    .map(Node::toSourceCode)
                    .collect(Collectors.joining("_")));
        }
        return Optional.empty();
    }

    /**
     * Returns the identifier for a given module variable declaration node.
     *
     * @param moduleVarNode module variable declaration syntax node
     * @return the service identifier
     */
    public static String getModuleVarIdentifier(ModuleVariableDeclarationNode moduleVarNode) {
        // Todo: implement a separate identifier capturing logics for each binding pattern type (high priority)
        return moduleVarNode.typedBindingPattern().bindingPattern().toSourceCode().trim();
    }

    /**
     * Returns the identifier for a given constant declaration node.
     *
     * @param constNode constant declaration syntax node
     * @return the service identifier
     */
    public static String getConstIdentifier(ConstantDeclarationNode constNode) {
        return constNode.variableName().text().trim();
    }

    /**
     * Returns the function identifier of the given function node.
     *
     * @param functionNode function syntax node
     * @return the service identifier
     */
    public static String getFunctionIdentifier(FunctionDefinitionNode functionNode) {
        String functionName = functionNode.functionName().toSourceCode().trim();
        if (functionNode.relativeResourcePath() == null || functionNode.relativeResourcePath().isEmpty()) {
            return functionName;
        }

        String resourcePaths = functionNode.relativeResourcePath().stream()
                .filter(node -> node.kind() == SyntaxKind.IDENTIFIER_TOKEN)
                .map(node -> capitalize(node.toSourceCode().trim()))
                .collect(Collectors.joining());
        return functionName + resourcePaths;
    }

    /**
     * Returns the identifier for a given class definition node.
     *
     * @param classNode class definition syntax node
     * @return the service identifier
     */
    public static String getClassIdentifier(ClassDefinitionNode classNode) {
        return classNode.className().text().trim();
    }

    /**
     * Returns the identifier for a given type definition node.
     *
     * @param typeDefNode type definition syntax node
     * @return the type definition identifier
     */
    public static String getTypeDefIdentifier(TypeDefinitionNode typeDefNode) {
        return typeDefNode.typeName().text().trim();
    }

    /**
     * Returns the identifier for a given enum declaration node.
     *
     * @param enumNode enum declaration syntax node
     * @return the type definition identifier
     */
    public static String getEnumIdentifier(EnumDeclarationNode enumNode) {
        return enumNode.identifier().text().trim();
    }

    /**
     * Returns the specified qualifier if it exist within the provided qualifier list.
     *
     * @param qualifierList qualifier list
     * @param qualifierType expected qualifier kind
     * @return the specified qualifier if it exist in the provided qualifier list
     */
    public static Optional<Token> lookupQualifier(NodeList<Token> qualifierList, SyntaxKind qualifierType) {
        return qualifierList.stream().filter(token -> token.kind() == qualifierType).findAny();
    }
}
