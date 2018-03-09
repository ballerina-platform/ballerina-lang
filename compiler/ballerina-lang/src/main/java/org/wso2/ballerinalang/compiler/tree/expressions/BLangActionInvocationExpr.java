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
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ActionInvocationNode;
import org.ballerinalang.model.tree.expressions.VariableReferenceNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.programfile.Instruction.RegIndex;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of Action invocation.
 *
 * @since 0.965.0
 */
public class BLangActionInvocationExpr extends BLangExpression implements ActionInvocationNode, MultiReturnExpr {

    public BLangInvocation invocationExpr;
    public BLangVariableReference connectorVarRef;
    public List<BType> types = new ArrayList<>(0);
    public BSymbol symbol;
    protected RegIndex[] regIndexes;

    @Override
    public BLangInvocation getInvocationExpr() {
        return invocationExpr;
    }

    @Override
    public VariableReferenceNode getConnectorVarRef() {
        return connectorVarRef;
    }

    @Override
    public List<BType> getTypes() {
        return types;
    }

    @Override
    public void setTypes(List<BType> types) {
        this.types = types;
    }

    public RegIndex[] getRegIndexes() {
        return regIndexes;
    }

    public void setRegIndexes(RegIndex[] regIndexes) {
        this.regIndexes = regIndexes;
        this.regIndex = regIndexes != null && regIndexes.length > 0 ? regIndexes[0] : null;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.ACTION_INVOCATION;
    }
}
