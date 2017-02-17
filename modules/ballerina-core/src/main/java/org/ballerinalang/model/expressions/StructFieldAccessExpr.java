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
import org.ballerinalang.model.values.BValue;

/**
 * {@code StructfieldAccessExpr} represents struct field access operation.
 * {@link StructFieldAccessExpr} is a chain of {@link StructFieldAccessExpr}s, similar to a
 * linked list, with references to the parent expression and child expressions, that are in either
 * side of this expression.
 * <br/>
 * eg:
 * <br/>
 * In the expression <b>person[3].name.firstName<b> each variable reference separated by '.', is
 * represented by a {@link StructFieldAccessExpr}, with links to the nearest neighbor.
 *
 * @since 1.0.0
 */
public class StructFieldAccessExpr extends UnaryExpression implements ReferenceExpr {

    /**
     * Name of the variable reference
     */
    private String varName;

    /**
     * Unique identifier or this expression
     */
    private SymbolName symbolName;

    /**
     * Holds a reference to the actual variable, stated in the expression.
     */
    private ReferenceExpr varRefExpr;

    /**
     * Expression of the child field of the current expression.
     * Is null for the last child in the chain.
     */
    private StructFieldAccessExpr fieldRefExpr;

    /**
     * Expression precedes the current expression in the chain.
     * Is null for the root of the chain.
     */
//    private StructFieldAccessExpr parentExpr;

    /**
     * Flag indicating whether the entire expression is a left hand side expression.
     */
    private boolean isLHSExpr;

    /**
     * Creates a Struct field access expression.
     *
     * @param symbolName       Symbol Name of the current field
     * @param structVarRefExpr Variable reference represented by the current field
     */
    public StructFieldAccessExpr(NodeLocation location, SymbolName symbolName, ReferenceExpr structVarRefExpr) {
        super(location, null, structVarRefExpr);
        this.symbolName = symbolName;
        this.varRefExpr = structVarRefExpr;
    }

    /**
     * Creates a Struct field access expression.
     *
     * @param location         file name and the line number of the field access expression
     * @param varRefExpr       Variable reference of the Struct
     * @param fieldRefExpr       field variable reference of the Struct
     */
    public StructFieldAccessExpr(NodeLocation location, ReferenceExpr varRefExpr, StructFieldAccessExpr fieldRefExpr) {
        super(location, null, fieldRefExpr);
        this.varRefExpr = varRefExpr;
        this.fieldRefExpr = fieldRefExpr;
    }

    @Override
    public String getVarName() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SymbolName getSymbolName() {
        return symbolName;
    }

    /**
     * Get the variable reference expression represented by this {@link StructFieldAccessExpr}.
     *
     * @return Variable reference expression represented by this {@link StructFieldAccessExpr}.
     */
    public StructFieldAccessExpr getFieldExpr() {
        return fieldRefExpr;
    }

    /**
     * Check whether this expression is a left hand side expression in an assignment.
     *
     * @return Flag indicating whether this expression is a left hand side expression in an assignment.
     */
    public boolean isLHSExpr() {
        return isLHSExpr;
    }

    /**
     * Set the flag indicating whether this expression is a left hand side expression in an assignment.
     *
     * @param isLhsExpr Flag indicating whether this expression is a left hand side expression in an assignment.
     */
    public void setLHSExpr(boolean isLhsExpr) {
        isLHSExpr = isLhsExpr;
    }

    /**
     * Set the parent of this field expression.
     *
     * @param parent Parent of this field expression.
     */
    public void setParent(StructFieldAccessExpr parent) {
//        this.parentExpr = parent;
    }

    /**
     * Set the child field of this field expression.
     *
     * @param fieldAccessExpr Child field of this field expression.
     */
    public void setFieldExpr(StructFieldAccessExpr fieldAccessExpr) {
        this.fieldRefExpr = fieldAccessExpr;
    }

    /**
     * Get the parent of this field expression.
     *
     * @return Parent of this field expression.
     */
    public StructFieldAccessExpr getParent() {
        return null;
    }

    /**
     * Get the variable reference represented by this field expression.
     *
     * @return Variable reference represented by this field expression.
     */
    public ReferenceExpr getVarRef() {
        return varRefExpr;
    }

    /**
     * Get the type of the variable represented by this field expression.
     *
     * @return Type of the variable represented by this field expression .
     */
    public BType getRefVarType() {
        return varRefExpr.getType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BValue execute(NodeExecutor executor) {
        return executor.visit(this);
    }

    /**
     * Get the type to which this entire expression chain evaluates to.
     * Type of a struct field access expression chain, is the type of the field expression at the chain.
     */
    @Override
    public BType getType() {
        // if the current expression has a child field, then get the type of that
        if (fieldRefExpr != null) {
            return fieldRefExpr.getType();
        }

        // if the current field is the last child, get the type of the variable that is referenced by
        // this field expression
        return varRefExpr.getType();
    }

    /**
     * Get the type of the current expression
     *
     * @return Type of this expression
     */
    public BType getExpressionType() {
        return this.type;
    }
}
