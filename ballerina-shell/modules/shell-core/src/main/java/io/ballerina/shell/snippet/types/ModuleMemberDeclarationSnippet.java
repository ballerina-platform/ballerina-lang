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
import io.ballerina.shell.utils.Identifier;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Module level declarations. These are not active or runnable.
 * Any undefined variable in these declarations are ignored.
 *
 * @since 2.0.0
 */
public class ModuleMemberDeclarationSnippet extends AbstractSnippet<ModuleMemberDeclarationNode>
        implements TopLevelDeclarationSnippet {
    private static final AtomicInteger unnamedModuleNameIndex = new AtomicInteger(0);

    public ModuleMemberDeclarationSnippet(SnippetSubKind subKind, ModuleMemberDeclarationNode rootNode) {
        super(subKind, rootNode);
    }

    /**
     * @return the name associated with the module level declaration.
     * If the module declaration has no name, this will return null.
     */
    public Identifier name() {
        if (rootNode instanceof ClassDefinitionNode classDefinitionNode) {
            String className = classDefinitionNode.className().text();
            return new Identifier(className);
        } else if (rootNode instanceof ConstantDeclarationNode constantDeclarationNode) {
            String constName = constantDeclarationNode.variableName().text();
            return new Identifier(constName);
        } else if (rootNode instanceof EnumDeclarationNode enumDeclarationNode) {
            String enumName = enumDeclarationNode.identifier().text();
            return new Identifier(enumName);
        } else if (rootNode instanceof FunctionDefinitionNode functionDefinitionNode) {
            String funcName = functionDefinitionNode.functionName().text();
            return new Identifier(funcName);
        } else if (rootNode instanceof ListenerDeclarationNode listenerDeclarationNode) {
            String listenerName = listenerDeclarationNode.variableName().text();
            return new Identifier(listenerName);
        } else if (rootNode instanceof ModuleXMLNamespaceDeclarationNode namespaceNode) {
            return namespaceNode.namespacePrefix().map(Token::text).map(Identifier::new)
                    .orElseGet(this::createAnonModuleName);
        } else if (rootNode instanceof TypeDefinitionNode typeDefinitionNode) {
            String typeName = typeDefinitionNode.typeName().text();
            return new Identifier(typeName);
        } else {
            return createAnonModuleName();
        }
    }

    /**
     * Creates an unused module name.
     * The prefix will follow the format $I.
     */
    private Identifier createAnonModuleName() {
        return new Identifier("$" + unnamedModuleNameIndex.getAndIncrement());
    }
}
