/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.model.builder;

import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.expressions.ActionInvocationExpr;
import org.ballerinalang.model.expressions.Expression;
import org.ballerinalang.model.expressions.FunctionInvocationExpr;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder class for building function/action invocation expressions.
 *
 * @since 0.8.0
 */
class CallableUnitInvocationExprBuilder {
    protected NodeLocation location;
    protected String name;
    protected String pkgName;
    protected String pkgPath;
    protected String connectorName;
    protected List<Expression> expressionList = new ArrayList<>();

    void setNodeLocation(NodeLocation location) {
        this.location = location;
    }

    void setName(String name) {
        this.name = name;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public void setPkgPath(String pkgPath) {
        this.pkgPath = pkgPath;
    }

    public void setConnectorName(String connectorName) {
        this.connectorName = connectorName;
    }

    void setExpressionList(List<Expression> expressionList) {
        this.expressionList = expressionList;
    }

    FunctionInvocationExpr buildFuncInvocExpr() {
        return new FunctionInvocationExpr(
                location,
                name,
                pkgName,
                pkgPath,
                expressionList.toArray(new Expression[expressionList.size()]));
    }

    ActionInvocationExpr buildActionInvocExpr() {
        return new ActionInvocationExpr(
                location,
                name,
                pkgName,
                pkgPath,
                connectorName,
                expressionList.toArray(new Expression[expressionList.size()]));
    }
}
