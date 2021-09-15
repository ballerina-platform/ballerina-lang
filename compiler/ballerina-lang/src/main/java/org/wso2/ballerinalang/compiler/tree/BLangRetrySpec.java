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
package org.wso2.ballerinalang.compiler.tree;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.RetrySpecNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * @since 1.3.0
 */
public class BLangRetrySpec extends BLangNode implements RetrySpecNode {

    // BLangNodes
    public BLangType retryManagerType;
    public List<BLangExpression> argExprs = new ArrayList<>();

    public BLangType getRetryManagerType() {
        return retryManagerType;
    }

    public void setRetryManagerType(TypeNode retryManagerType) {
        this.retryManagerType = (BLangType) retryManagerType;
    }

    public List<BLangExpression> getArgExprs() {
        return argExprs;
    }

    public void addArgExpr(ExpressionNode argExpr) {
        this.argExprs.add((BLangExpression) argExpr);
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
        return NodeKind.RETRY_SPEC;
    }

    @Override
    public String toString() {
        StringJoiner stringJoiner = new StringJoiner(",");
        argExprs.forEach(arg -> stringJoiner.add(arg.toString()));
        return (retryManagerType != null ? "<" + retryManagerType + ">" : "") +
                (argExprs.size() > 0 ? "(" + stringJoiner.toString() + ")" : "");
    }
}
