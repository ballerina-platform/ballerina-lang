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

import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.shell.snippet.SnippetSubKind;

import java.util.stream.Collectors;

/**
 * These will be variable declarations.
 * Currently only module level variable declarations are accepted.
 *
 * @since 2.0.0
 */
public class VariableDeclarationSnippet extends ExecutableSnippet {
    public VariableDeclarationSnippet(ModuleVariableDeclarationNode rootNode) {
        super(SnippetSubKind.VARIABLE_DECLARATION, rootNode);
    }

    /**
     * This will remove all the qualifiers from a declaration and
     * return the TYPE VAR = INIT; formatted declaration.
     *
     * @return The variable declarations without the qualifiers.
     */
    public String withoutQualifiers() {
        assert rootNode instanceof ModuleVariableDeclarationNode;
        return ((ModuleVariableDeclarationNode) rootNode).modify()
                .withMetadata(null)
                .withQualifiers(NodeFactory.createEmptyNodeList())
                .apply().toSourceCode();
    }

    /**
     * This will ONLY return the metadata and qualifiers of this node.
     * Whitespaces will be added between all qualifiers and metadata.
     *
     * @return Qualifiers/Metadata as string.
     */
    public String qualifiersAndMetadata() {
        assert rootNode instanceof ModuleVariableDeclarationNode;
        ModuleVariableDeclarationNode moduleVariableDeclarationNode = (ModuleVariableDeclarationNode) rootNode;
        String metadata = moduleVariableDeclarationNode.metadata().map(Node::toSourceCode).orElse("");
        String qualifiers = moduleVariableDeclarationNode.qualifiers().stream()
                .map(Node::toSourceCode).collect(Collectors.joining(" "));
        return metadata + qualifiers;
    }
}
