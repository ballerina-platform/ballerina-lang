/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langserver.command.visitors;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;

import java.util.Optional;

/**
 * Finds if a provided {@link NonTerminalNode} node is contained within an isolated block.
 *
 * @since 2.0.0
 */
public class IsolatedBlockResolver extends NodeTransformer<Boolean> {

    public Boolean findIsolatedBlock(NonTerminalNode node) {
        if (node.kind() == SyntaxKind.LIST) {
            return node.parent().apply(this);
        }
        return node.apply(this);    }

    @Override
    public Boolean transform(ObjectFieldNode objectFieldNode) {
        return true;
    }

    @Override
    public Boolean transform(FunctionDefinitionNode functionDefinitionNode) {
        return hasIsolatedQualifier(functionDefinitionNode.qualifierList());
    }

    @Override
    protected Boolean transformSyntaxNode(Node node) {
        if (node.parent() != null) {
            return node.parent().apply(this);
        }
        return Boolean.FALSE;
    }

    private boolean hasIsolatedQualifier(NodeList<Token> tokens) {
        Optional<Token> isolatedQualifier = tokens.stream().filter(qualifier ->
                qualifier.text().equals(SyntaxKind.ISOLATED_KEYWORD.stringValue())).findAny();
        return isolatedQualifier.isPresent();
    }
}
