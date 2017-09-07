/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ArrayLiteralNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.Arrays;
import java.util.List;

/**
 * Implementation of ArrayLiteralNode.
 *
 * @since 0.94
 */
public class BLangArrayLiteral extends BLangExpression implements ArrayLiteralNode {

    public List<ExpressionNode> expressionNodes;

    @Override
    public NodeKind getKind() {
        return NodeKind.ARRAY_LITERAL;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
    }

    @Override
    public List<? extends ExpressionNode> getExpressions() {
        return expressionNodes;
    }

    @Override
    public String toString() {
        String s = Arrays.toString(expressionNodes.toArray());
        return "[" + s.substring(1, s.length() - 1) + ']';
    }
}
