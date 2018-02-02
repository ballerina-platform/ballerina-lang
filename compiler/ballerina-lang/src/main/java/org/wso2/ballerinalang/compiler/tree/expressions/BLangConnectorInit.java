/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ConnectorInitNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.types.UserDefinedTypeNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of {@link ConnectorInitNode}.
 *
 * @since 0.94
 */
public class BLangConnectorInit extends BLangExpression implements ConnectorInitNode {

    public BLangUserDefinedType connectorType;
    public List<BLangExpression> argsExpr;
    public List<ConnectorInitNode> filterConnectors;


    public BLangConnectorInit() {
        argsExpr = new ArrayList<>();
        filterConnectors = new ArrayList<>();
    }

    @Override
    public UserDefinedTypeNode getConnectorType() {
        return connectorType;
    }

    @Override
    public List<? extends ExpressionNode> getExpressions() {
        return this.argsExpr;
    }

    @Override
    public List<ConnectorInitNode> getFilterConnectos() {
        return this.filterConnectors;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.CONNECTOR_INIT_EXPR;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "BLangConnectorInit: " + connectorType +
                " (" + (argsExpr != null ? Arrays.toString(argsExpr.toArray()) : "") + ") " +
                (!filterConnectors.isEmpty() ? " with " + Arrays.toString(filterConnectors.toArray()) : "");
    }
}
