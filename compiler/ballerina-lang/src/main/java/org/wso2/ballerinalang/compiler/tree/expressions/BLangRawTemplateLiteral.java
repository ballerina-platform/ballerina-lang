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
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.RawTemplateLiteralNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeModifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a raw template literal of the form: `Hello ${name}!`. This gets desugared to an object:RawTemplate object
 * or a subtype of this.
 *
 * @since 2.0.0
 */
public class BLangRawTemplateLiteral extends BLangExpression implements RawTemplateLiteralNode {

    public List<BLangLiteral> strings;
    public List<BLangExpression> insertions;

    public BLangRawTemplateLiteral() {
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
    public <T, R> R apply(BLangNodeModifier<T, R> modifier, T props) {
        return modifier.modify(this, props);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.RAW_TEMPLATE_LITERAL;
    }

    @Override
    public String toString() {
        return "BLangRawTemplateLiteral: strings " + strings + ", insertions " + insertions;
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
