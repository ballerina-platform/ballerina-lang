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
package org.ballerinalang.core.model.expressions.variablerefs;

import org.ballerinalang.core.model.NodeLocation;
import org.ballerinalang.core.model.WhiteSpaceDescriptor;
import org.ballerinalang.core.model.expressions.AbstractExpression;

/**
 * {@code SimpleVarRefExpr} represents a variable reference expression with just the variable name.
 * e.g. name;
 *
 * @since 0.89
 */
public class SimpleVarRefExpr extends AbstractExpression implements VariableReferenceExpr {

    private boolean isLHSExpr;

    // Parent in the node tree
    private VariableReferenceExpr parentVarRefExpr;

    public SimpleVarRefExpr(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor) {
        super(location, whiteSpaceDescriptor);
    }

    @Override
    public boolean isLHSExpr() {
        return isLHSExpr;
    }

    @Override
    public void setLHSExpr(boolean lhsExpr) {
        this.isLHSExpr = lhsExpr;
    }

    @Override
    public VariableReferenceExpr getParentVarRefExpr() {
        return parentVarRefExpr;
    }

    @Override
    public void setParentVarRefExpr(VariableReferenceExpr varRefExpr) {
        this.parentVarRefExpr = varRefExpr;
    }
}
