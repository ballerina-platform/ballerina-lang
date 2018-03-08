/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerinalang.compiler.tree.statements;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.expressions.VariableReferenceNode;
import org.ballerinalang.model.tree.statements.PostIncrementNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

/**
 * @since 0.965.0
 */
public class BLangPostIncrement extends BLangStatement implements PostIncrementNode {

    public BLangExpression varRef;
    public OperatorKind opKind;
    public BLangExpression modifiedExpr;
    public BLangExpression increment;

    public BLangPostIncrement() {
    }

    public BLangPostIncrement(DiagnosticPos pos, BLangExpression varRef) {
        this.pos = pos;
        this.varRef = varRef;
    }

    @Override
    public OperatorKind getOperatorKind() {
        return opKind;
    }

    @Override
    public BLangExpression getVariable() {
        return varRef;
    }

    @Override
    public void setVariable(VariableReferenceNode variableReferenceNode) {
        this.varRef = (BLangVariableReference) variableReferenceNode;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.POST_INCREMENT;
    }

    @Override
    public String toString() {
        return "BLangPostIncrement: " + (this.varRef != null ? this.varRef : "")
                + String.valueOf(opKind) + String.valueOf(opKind);
    }

}
