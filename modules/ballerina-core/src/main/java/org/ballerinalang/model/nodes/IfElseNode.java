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
package org.ballerinalang.model.nodes;

import org.ballerinalang.model.LinkedNode;
import org.ballerinalang.model.LinkedNodeVisitor;
import org.ballerinalang.model.expressions.Expression;

/**
 * Represents a If Else style Branching node. Target will be calculated at runtime.
 *
 * This node is used to model, If-ElseIf-Else, While Statements.
 */
public class IfElseNode extends AbstractLinkedNode {

    private Expression condition;

    private LinkedNode nextIfFalse;

    public IfElseNode(Expression expr) {
        this.condition = expr;
    }

    public LinkedNode nextAfterBreak() {
        return nextIfFalse;
    }

    public void setNextIfFalse(LinkedNode statement) {
        this.nextIfFalse = statement;
    }

    public Expression getCondition() {
        return condition;
    }

    @Override
    public void accept(LinkedNodeVisitor nodeVisitor) {
        nodeVisitor.visit(this);
    }
}
