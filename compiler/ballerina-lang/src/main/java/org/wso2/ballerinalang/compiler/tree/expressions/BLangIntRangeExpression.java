/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.IntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

/**
 * Implementation of Int range expression.
 *
 * @since 0.96.0
 */
public class BLangIntRangeExpression extends BLangExpression implements IntRangeExpression {

    public boolean includeStart = true, includeEnd = true;
    public BLangExpression startExpr, endExpr;

    @Override
    public ExpressionNode getStartExpression() {
        return startExpr;
    }

    @Override
    public ExpressionNode getEndExpression() {
        return endExpr;
    }

    @Override
    public void setStartExpression(ExpressionNode expression) {
        this.startExpr = (BLangExpression) expression;
    }

    @Override
    public void setEndExpression(ExpressionNode expression) {
        this.endExpr = (BLangExpression) expression;
    }

    @Override
    public boolean includeStart() {
        return includeStart;
    }

    @Override
    public boolean includeEnd() {
        return includeEnd;
    }

    @Override
    public void setIncludeStart(boolean include) {
        this.includeStart = include;
    }

    @Override
    public void setIncludeEnd(boolean include) {
        this.includeEnd = include;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.INT_RANGE_EXPR;
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
}
