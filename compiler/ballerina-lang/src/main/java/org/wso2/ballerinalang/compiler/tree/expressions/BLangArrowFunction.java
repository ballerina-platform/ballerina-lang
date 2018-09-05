/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ArrowFunctionNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;

import java.util.Arrays;
import java.util.List;

/**
 * Implementation of {@link ArrowFunctionNode}.
 *
 * @since 0.982.0
 */
public class BLangArrowFunction extends BLangExpression implements ArrowFunctionNode {

    public List<BLangVariable> params;
    public BLangExpression expression;
    public BLangLambdaFunction lambdaFunction;

    @Override
    public List<BLangVariable> getParameters() {
        return params;
    }

    @Override
    public BLangExpression getExpression() {
        return expression;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.ARROW_EXPR;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("ArrowExprRef:(%s) => %s",
                Arrays.toString(params.stream().map(x -> x.name).toArray()), expression);
    }
}
