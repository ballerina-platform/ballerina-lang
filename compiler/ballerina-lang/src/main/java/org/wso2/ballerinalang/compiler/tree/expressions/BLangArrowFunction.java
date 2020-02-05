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

import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ArrowFunctionNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.util.ClosureVarSymbol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


/**
 * Implementation of {@link ArrowFunctionNode}.
 *
 * @since 0.982.0
 */
public class BLangArrowFunction extends BLangExpression implements ArrowFunctionNode {

    public List<BLangSimpleVariable> params = new ArrayList<>();
    public BType funcType;
    public IdentifierNode functionName;
    public BLangInvokableNode function;
    public BLangExprFunctionBody body;

    // Used to track uninitialized closure variables in DataFlowAnalyzer.
    public Set<ClosureVarSymbol> closureVarSymbols = new LinkedHashSet<>();

    @Override
    public List<BLangSimpleVariable> getParameters() {
        return params;
    }

    @Override
    public BLangExpression getExprBody() {
        return body.expr;
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
                             Arrays.toString(params.stream().map(x -> x.name).toArray()), body.expr);
    }
}
