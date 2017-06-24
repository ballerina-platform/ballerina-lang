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

import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.WhiteSpaceDescriptor;

/**
 * {@code IndexBasedVarRefExpr} represents a variable reference expression with an array index.
 * e.g. tokens[0];
 *
 * @since 0.89
 */
public class IndexBasedVarRefExpr extends UnaryExpression implements VariableReferenceExpr {
    // Variable reference expression
    private VariableReferenceExpr varRefExpr;

    // Array index expression
    private Expression indexExpr;
    private boolean isLHSExpr;

    public IndexBasedVarRefExpr(NodeLocation location,
                                WhiteSpaceDescriptor whiteSpaceDescriptor,
                                VariableReferenceExpr varRefExpr,
                                Expression indexExpr) {
        super(location, whiteSpaceDescriptor, null, varRefExpr);
        this.varRefExpr = varRefExpr;
        this.indexExpr = indexExpr;
    }

    public VariableReferenceExpr getVarRefExpr() {
        return varRefExpr;
    }

    public Expression getIndexExpr() {
        return indexExpr;
    }

    @Override
    public boolean isLHSExpr() {
        return isLHSExpr;
    }

    @Override
    public void setLHSExpr(boolean lhsExpr) {
        isLHSExpr = lhsExpr;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
