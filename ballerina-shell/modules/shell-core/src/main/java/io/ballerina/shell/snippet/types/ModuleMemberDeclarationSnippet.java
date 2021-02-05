/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.shell.snippet.types;

import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleXMLNamespaceDeclarationNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.shell.snippet.SnippetSubKind;

import java.util.Optional;

/**
 * Module level declarations. These are not active or runnable.
 * Any undefined variable in these declarations are ignored.
 *
 * @since 2.0.0
 */
public class ModuleMemberDeclarationSnippet extends AbstractSnippet implements DeclarationSnippet {
    public ModuleMemberDeclarationSnippet(SnippetSubKind subKind, ModuleMemberDeclarationNode rootNode) {
        super(subKind, rootNode);
    }

    /**
     * @return the name associated with the module level declaration.
     * If the module declaration has no name, this will return null.
     */
    public Optional<String> name() {
        if (rootNode instanceof ClassDefinitionNode) {
            return Optional.ofNullable(((ClassDefinitionNode) rootNode).className().text());
        } else if (rootNode instanceof ConstantDeclarationNode) {
            return Optional.ofNullable(((ConstantDeclarationNode) rootNode).variableName().text());
        } else if (rootNode instanceof EnumDeclarationNode) {
            return Optional.ofNullable(((EnumDeclarationNode) rootNode).identifier().text());
        } else if (rootNode instanceof FunctionDefinitionNode) {
            return Optional.ofNullable(((FunctionDefinitionNode) rootNode).functionName().text());
        } else if (rootNode instanceof ListenerDeclarationNode) {
            return Optional.ofNullable(((ListenerDeclarationNode) rootNode).variableName().text());
        } else if (rootNode instanceof ModuleXMLNamespaceDeclarationNode) {
            ModuleXMLNamespaceDeclarationNode namespaceNode = (ModuleXMLNamespaceDeclarationNode) rootNode;
            return namespaceNode.namespacePrefix().map(Token::text);
        } else if (rootNode instanceof TypeDefinitionNode) {
            return Optional.ofNullable(((TypeDefinitionNode) rootNode).typeName().text());
        } else {
            return Optional.empty();
        }
    }
}
