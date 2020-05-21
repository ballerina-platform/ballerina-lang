/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerinalang.compiler.internal.parser.tree;

import io.ballerinalang.compiler.syntax.tree.ConditionalExpressionNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STConditionalExpressionNode extends STExpressionNode {
    public final STNode lhsExpression;
    public final STNode questionMarkToken;
    public final STNode middleExpression;
    public final STNode colonToken;
    public final STNode endExpression;

    STConditionalExpressionNode(
            STNode lhsExpression,
            STNode questionMarkToken,
            STNode middleExpression,
            STNode colonToken,
            STNode endExpression) {
        super(SyntaxKind.CONDITIONAL_EXPRESSION);
        this.lhsExpression = lhsExpression;
        this.questionMarkToken = questionMarkToken;
        this.middleExpression = middleExpression;
        this.colonToken = colonToken;
        this.endExpression = endExpression;

        addChildren(
                lhsExpression,
                questionMarkToken,
                middleExpression,
                colonToken,
                endExpression);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ConditionalExpressionNode(this, position, parent);
    }
}
