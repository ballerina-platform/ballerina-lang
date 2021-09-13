/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.VariableReferenceNode;
import org.ballerinalang.model.tree.statements.AssignmentNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;

/**
 * @since 0.94
 */
public class BLangAssignment extends BLangStatement implements AssignmentNode {
    public BLangExpression varRef;
    public BLangExpression expr;
    public boolean declaredWithVar;
    public boolean safeAssignment;

    public BLangAssignment() {
    }

    public BLangAssignment(Location pos, BLangExpression varRef,
                           BLangExpression expr, boolean declaredWithVar) {
        this.pos = pos;
        this.varRef = varRef;
        this.expr = expr;
        this.declaredWithVar = declaredWithVar;
    }

    @Override
    public ExpressionNode getVariable() {
        return varRef;
    }

    @Override
    public BLangExpression getExpression() {
        return expr;
    }

    @Override
    public boolean isDeclaredWithVar() {
        return declaredWithVar;
    }

    @Override
    public void setExpression(ExpressionNode expression) {
        this.expr = (BLangExpression) expression;
    }

    @Override
    public void setDeclaredWithVar(boolean isDeclaredWithVar) {
        this.declaredWithVar = isDeclaredWithVar;
    }

    @Override
    public void setVariable(VariableReferenceNode variableReferenceNode) {
        this.varRef = (BLangExpression) variableReferenceNode;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> void accept(BLangNodeAnalyzer<T> analyzer, T props) {
        analyzer.visit(this, props);
    }

    @Override
    public <T, R> R accept(BLangNodeTransformer<T, R> transformer, T props) {
        return transformer.transform(this, props);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.ASSIGNMENT;
    }

    @Override
    public String toString() {
        return (this.declaredWithVar ? "var " : "") + (this.varRef != null ? this.varRef : "") +
                (this.expr != null ? " = " + this.expr : ";");
    }
}
