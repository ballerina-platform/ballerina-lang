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
package org.ballerinalang.model.expressions.variablerefs;

import org.ballerinalang.model.Identifier;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.VariableDef;
import org.ballerinalang.model.WhiteSpaceDescriptor;
import org.ballerinalang.model.expressions.UnaryExpression;

/**
 * {@code FieldBasedVarRefExpr} represents a variable reference expression which ends with a field access.
 * e.g. person.name;
 *
 * @since 0.89
 */
public class FieldBasedVarRefExpr extends UnaryExpression implements VariableReferenceExpr {

    // Variable reference expression
    private VariableReferenceExpr varRefExpr;

    // Parent in the node tree
    private VariableReferenceExpr parentVarRefExpr;

    // Field identifier
    private Identifier fieldIdentifier;

    // Only for structs
    private VariableDef fieldDef;

    // Flag indicating whether the entire expression is a left hand side expression.
    private boolean isLHSExpr;

    public FieldBasedVarRefExpr(NodeLocation location,
                                WhiteSpaceDescriptor whiteSpaceDescriptor,
                                VariableReferenceExpr varRefExpr,
                                Identifier fieldIdentifier) {
        super(location, whiteSpaceDescriptor, null, varRefExpr);
        this.varRefExpr = varRefExpr;
        this.fieldIdentifier = fieldIdentifier;
    }

    public VariableReferenceExpr getVarRefExpr() {
        return varRefExpr;
    }

    public String getFieldName() {
        return fieldIdentifier.getName();
    }

    public VariableDef getFieldDef() {
        return fieldDef;
    }

    public void setFieldDef(VariableDef fieldDef) {
        this.fieldDef = fieldDef;
    }

    /**
     * Check whether this expression is a left hand side expression in an assignment.
     *
     * @return Flag indicating whether this expression is a left hand side expression in an assignment.
     */
    @Override
    public boolean isLHSExpr() {
        return isLHSExpr;
    }

    /**
     * Set the flag indicating whether this expression is a left hand side expression in an assignment.
     *
     * @param isLhsExpr Flag indicating whether this expression is a left hand side expression in an assignment.
     */
    @Override
    public void setLHSExpr(boolean isLhsExpr) {
        isLHSExpr = isLhsExpr;

//         Set the property recursively
//        if (fieldRefExpr != null) {
//            fieldRefExpr.setLHSExpr(isLhsExpr);
//        }
    }

    @Override
    public VariableReferenceExpr getParentVarRefExpr() {
        return parentVarRefExpr;
    }

    @Override
    public void setParentVarRefExpr(VariableReferenceExpr varRefExpr) {
        this.parentVarRefExpr = varRefExpr;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
