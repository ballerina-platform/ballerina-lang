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
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.expressions.CheckedExpressionNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.List;

/**
 * {@code BLangCheckedExpr} represents a unary expression which forces return-on-error behaviour.
 * <p>
 * If the enclosing function does not have an error return, this expression forces a throw-on-error.
 * e.g.
 * function openFile(string name) returns File | error;
 * <p>
 * File f = check openFile("foo.txt");
 *
 * @since 0.970.0
 */
public class BLangCheckedExpr extends BLangExpression implements CheckedExpressionNode {

    // BLangNodes
    public BLangExpression expr;

    // Semantic Data
    // This list caches types that are equivalent to the error type which are returned by the rhs expression.
    public List<BType> equivalentErrorTypeList;

    @Override
    public BLangExpression getExpression() {
        return expr;
    }

    @Override
    public OperatorKind getOperatorKind() {
        return OperatorKind.CHECK;
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
    public <T, R> R apply(BLangNodeTransformer<T, R> modifier, T props) {
        return modifier.transform(this, props);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.CHECK_EXPR;
    }
}
