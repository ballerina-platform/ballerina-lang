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

import org.ballerinalang.model.Action;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.WhiteSpaceDescriptor;
import org.ballerinalang.model.types.BType;

/**
 * {@code ActionInvocationExpr} represents action invocation expression.
 *
 * @since 0.8.0
 */
public class ActionInvocationExpr extends AbstractExpression implements CallableUnitInvocationExpr<Action> {
    private String name;
    private String pkgName;
    private String pkgPath;
    private String connectorName;
    private Expression[] exprs;
    private Action action;
    private BType[] types = new BType[0];
    private int[] offsets;

    public ActionInvocationExpr(NodeLocation location,
                                WhiteSpaceDescriptor whiteSpaceDescriptor,
                                String name,
                                String pkgName,
                                String pkgPath,
                                String connectorName,
                                Expression[] exprs) {
        super(location, whiteSpaceDescriptor);
        this.name = name;
        this.pkgName = pkgName;
        this.pkgPath = pkgPath;
        this.connectorName = connectorName;
        this.exprs = exprs;
    }

    public String getConnectorName() {
        return connectorName;
    }

    public void setConnectorName(String connectorName) {
        this.connectorName = connectorName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPackageName() {
        return pkgName;
    }

    @Override
    public String getPackagePath() {
        return pkgPath;
    }

    @Override
    public Expression[] getArgExprs() {
        return exprs;
    }

    public void setArgExprs(Expression[] exprs) {
        this.exprs = exprs;
    }

    @Override
    public Action getCallableUnit() {
        return action;
    }

    @Override
    public void setCallableUnit(Action callableUnit) {
        this.action = callableUnit;
    }

    @Override
    public BType[] getTypes() {
        return types;
    }

    @Override
    public void setTypes(BType[] types) {
        this.types = types;

        multipleReturnsAvailable = types.length > 1;
        if (!multipleReturnsAvailable && types.length == 1) {
            this.type = types[0];
        }
    }

    public int[] getOffsets() {
        return offsets;
    }

    public void setOffsets(int[] offsets) {
        this.offsets = offsets;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

}
