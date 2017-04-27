/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.model.expressions;

import org.ballerinalang.model.NodeExecutor;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BValue;

/**
 * <p>
 * {@code VariableRefTypeAccessExpr} represents variable reference type access operation.
 * </p>
 * eg:
 * <p>
 * Sample usage <b>variableRef.type</b> returns type of variable referenced by variableRef.
 * </p>
 * @since 1.0.0
 */
public class VariableRefTypeAccessExpr extends UnaryExpression implements ReferenceExpr {

    private ReferenceExpr varRefExpr;

    public VariableRefTypeAccessExpr(NodeLocation location, ReferenceExpr varRefTypeAccessExpr) {
        super(location, null, varRefTypeAccessExpr);
        this.varRefExpr = varRefTypeAccessExpr;
    }

    @Override
    public String getVarName() {
        return null;
    }


    @Override
    public SymbolName getSymbolName() {
        return null;
    }


    public ReferenceExpr getVarRef() {
        return varRefExpr;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public BValue execute(NodeExecutor executor) {
        return executor.visit(this);
    }

    @Override
    public BType getType() {
        return BTypes.typeInt;
    }

}
