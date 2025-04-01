/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org).
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
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.NaturalExpressionNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a natural expression.
 *
 * @since 2201.13.0
 */
public class BLangNaturalExpression extends BLangExpression implements NaturalExpressionNode {

    public List<BLangLiteral> strings;
    public List<BLangExpression> insertions;

    public BLangNaturalExpression() {
        strings = new ArrayList<>();
        insertions = new ArrayList<>();
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
        return NodeKind.NATURAL_EXPR;
    }

    @Override
    public String toString() {
        return "BLangNaturalExpression: strings " + strings + ", insertions " + insertions;
    }

    @Override
    public List<? extends ExpressionNode> getInsertions() {
        return insertions;
    }

    @Override
    public void addInsertion(ExpressionNode expression) {
        insertions.add((BLangExpression) expression);
    }

    @Override
    public List<? extends ExpressionNode> getStrings() {
        return strings;
    }

    @Override
    public void addString(ExpressionNode stringLiteral) {
        strings.add((BLangLiteral) stringLiteral);
    }
}
