/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction;

import io.ballerina.compiler.syntax.tree.DoStatementNode;
import io.ballerina.compiler.syntax.tree.ForEachStatementNode;
import io.ballerina.compiler.syntax.tree.LockStatementNode;
import io.ballerina.compiler.syntax.tree.MatchStatementNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.WhileStatementNode;

/**
 * This visitor is used to resolve the regular compound statement node of a given node.
 * 
 * @since 2.0.0
 */
public class FailStatementResolver extends NodeTransformer<Node> {
    
    public Node getRegComNSmtNode(Node node) {
        return node.apply(this);
    }

    @Override
    public Node transform(WhileStatementNode whileStatementNode) {
        Node whileStatementConditionNode = whileStatementNode.condition();
        if (whileStatementConditionNode.kind() == SyntaxKind.CHECK_EXPRESSION 
                || whileStatementConditionNode.kind() == SyntaxKind.FAIL_STATEMENT) {
            return whileStatementNode.whileKeyword();
        }
        return whileStatementNode;
    }
    
    @Override
    public Node transform(DoStatementNode doStatementNode) {
        return doStatementNode;
    }
    
    @Override
    public Node transform(MatchStatementNode matchStatementNode) {
        return matchStatementNode;
    }
    
    @Override
    public Node transform(ForEachStatementNode forEachStatementNode) {
        return forEachStatementNode;
    }
    
    @Override
    public Node transform(LockStatementNode lockStatementNode) {
        return lockStatementNode;
    }
    
    @Override
    protected Node transformSyntaxNode(Node node) {
        if (node.parent() != null) {
            return node.parent().apply(this);
        }
        return null;
    }
}
