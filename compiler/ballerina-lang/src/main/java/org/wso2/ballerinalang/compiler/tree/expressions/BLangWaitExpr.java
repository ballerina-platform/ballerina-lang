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
import org.ballerinalang.model.tree.expressions.WaitExpressionNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeModifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This represents the await expression implementation.
 * 
 * @since 0.965
 */
public class BLangWaitExpr extends BLangExpression implements WaitExpressionNode {

    private static final String WAIT_KEYWORD = "wait";

    public List<BLangExpression> exprList;

    @Override
    public NodeKind getKind() {
        return NodeKind.WAIT_EXPR;
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
    public String toString() {
        String expressions = exprList.stream().map(Object::toString).collect(Collectors.joining("|"));
        return WAIT_KEYWORD + " " + expressions;
    }

    @Override
    public BLangExpression getExpression() {
        return exprList.get(0);
    }
}
