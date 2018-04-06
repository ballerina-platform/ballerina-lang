/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.ballerinalang.compiler.tree.statements;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.VariableReferenceNode;
import org.ballerinalang.model.tree.statements.TupleDestructureNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link TupleDestructureNode}
 *
 * @since 0.966.0
 */
public class BLangTupleDestructure extends BLangStatement implements TupleDestructureNode {
    public List<BLangExpression> varRefs;
    public BLangExpression expr;
    public boolean declaredWithVar;

    public BLangTupleDestructure() {
        this.varRefs = new ArrayList<>();
    }

    @Override
    public List<BLangExpression> getVariableRefs() {
        return this.varRefs;
    }

    @Override
    public void addVariableRef(VariableReferenceNode variableReferenceNode) {
        varRefs.add((BLangVariableReference) variableReferenceNode);
    }

    @Override
    public BLangExpression getExpression() {
        return expr;
    }

    @Override
    public void setExpression(ExpressionNode expression) {
        this.expr = (BLangExpression) expression;
    }

    @Override
    public boolean isDeclaredWithVar() {
        return this.declaredWithVar;
    }

    @Override
    public void setDeclaredWithVar(boolean isDeclaredWithVar) {
        this.declaredWithVar = isDeclaredWithVar;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.TUPLE_DESTRUCTURE;
    }
}
