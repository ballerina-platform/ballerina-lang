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
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.WhiteSpaceDescriptor;
import org.ballerinalang.model.types.BType;

/**
 * <p>
 * {@code FieldAccessExpr} represents struct field access operation.
 * {@link FieldAccessExpr} is a chain of {@link FieldAccessExpr}s, similar to a
 * linked list, with references to the parent expression and child expressions, that are in either
 * side of this expression.
 * </p>
 * eg:
 * <p>
 * In the expression <b>person[3].name.firstName</b> each variable reference separated by '.', is
 * represented by a {@link FieldAccessExpr}, with links to the nearest neighbor.
 * </p>
 * @since 1.0.0
 */
public class FieldAccessExpr extends UnaryExpression implements ReferenceExpr {

    private String pkgName;

    private String pkgPath;

    /**
     * Unique identifier or this expression.
     */
    private SymbolName symbolName;

    /**
     * Holds a reference to the actual variable, stated in the expression.
     */
    private Expression varRefExpr;

    /**
     * Expression of the child field of the current expression.
     * Is null for the last child in the chain.
     */
    private FieldAccessExpr fieldRefExpr;

    /**
     * Expression precedes the current expression in the chain.
     * Is null for the root of the chain.
     */

    /**
     * Flag indicating whether the entire expression is a left hand side expression.
     */
    private boolean isLHSExpr;
    
    /**
     * Flag indicating whether the varRef represented by this {@link FieldAccessExpr}
     * is a static key or not.
     */
    private boolean isStaticField = false;

    /**
     * Flag indicating whether the expression is an array or not.
     */
    private boolean isArrayIndexExpr = false;

    /**
     * Creates a field access expression.
     *
     * @param location location of the expression in the source file
     * @param whiteSpaceDescriptor Holds whitespace region data
     * @param symbolName Symbol Name of the current field
     * @param varRefExpr Variable reference represented by the current field
     */
    @Deprecated
    public FieldAccessExpr(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, SymbolName symbolName,
                           Expression varRefExpr) {
        super(location, whiteSpaceDescriptor, null, varRefExpr);
        this.symbolName = symbolName;
        this.varRefExpr = varRefExpr;
    }
    
    /**
     * Creates a field access expression.
     * 
     * @param location Location of the expression in the source file
     * @param whiteSpaceDescriptor Holds whitespace region data
     * @param varRefExpr Variable reference represented by the current field
     */
    public FieldAccessExpr(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, Expression varRefExpr) {
        super(location, whiteSpaceDescriptor, null, varRefExpr);
        this.varRefExpr = varRefExpr;
        
        if (varRefExpr instanceof ReferenceExpr) {
            this.symbolName = ((ReferenceExpr) varRefExpr).getSymbolName();
        }
    }

    /**
     * Creates a field access expression.
     *
     * @param location File name and the line number of the field access expression
     * @param whiteSpaceDescriptor Holds whitespace region data
     * @param varRefExpr Variable reference represented by the current field
     * @param fieldRefExpr Reference to the child field of the current field
     */
    public FieldAccessExpr(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, Expression varRefExpr,
                           FieldAccessExpr fieldRefExpr) {
        super(location, whiteSpaceDescriptor, null, fieldRefExpr);
        this.varRefExpr = varRefExpr;
        this.fieldRefExpr = fieldRefExpr;
        
        if (varRefExpr instanceof ReferenceExpr) {
            this.symbolName = ((ReferenceExpr) varRefExpr).getSymbolName();
        }
    }
    
    /**
     * Creates a field access expression.
     *
     * @param location File name and the line number of the field access expression
     * @param varRefExpr Variable reference represented by the current field
     * @param fieldRefExpr Reference to the child field of the current field
     */
    /**
     *
     * @param location File name and the line number of the field access expression
     * @param whiteSpaceDescriptor Holds whitespace region data
     * @param pkgName package name of the expression
     * @param pkgPath package path the expression
     * @param varRefExpr Variable reference represented by the current field
     * @param fieldRefExpr Reference to the child field of the current field
     */
    public FieldAccessExpr(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, String pkgName,
                           String pkgPath, Expression varRefExpr,
            FieldAccessExpr fieldRefExpr) {
        super(location, whiteSpaceDescriptor, null, fieldRefExpr);
        this.varRefExpr = varRefExpr;
        this.fieldRefExpr = fieldRefExpr;
        this.pkgName = pkgName;
        this.pkgPath = pkgPath;
        
        if (varRefExpr instanceof ReferenceExpr) {
            this.symbolName = ((ReferenceExpr) varRefExpr).getSymbolName();
        }
    }

    @Override
    public String getVarName() {
        if (varRefExpr instanceof ReferenceExpr) {
            return ((ReferenceExpr) varRefExpr).getVarName();
        }
        return null;
    }

    @Override
    public String getPkgName() {
        return pkgName;
    }

    @Override
    public String getPkgPath() {
        return pkgPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SymbolName getSymbolName() {
        return symbolName;
    }

    /**
     * Get the variable reference expression represented by this {@link FieldAccessExpr}.
     *
     * @return Variable reference expression represented by this {@link FieldAccessExpr}.
     */
    public FieldAccessExpr getFieldExpr() {
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

        // Set the property recursively
        if (fieldRefExpr != null) {
            fieldRefExpr.setLHSExpr(isLhsExpr);
        }
    }

    /**
     * Set the child field of this field expression.
     *
     * @param fieldExpr Child field of this field expression.
     */
    public void setFieldExpr(FieldAccessExpr fieldExpr) {
        this.fieldRefExpr = fieldExpr;
    }

    /**
     * Get the variable reference represented by this field expression.
     *
     * @return Variable reference represented by this field expression.
     */
    public Expression getVarRef() {
        return varRefExpr;
    }

    /**
     * Set the variable reference represented by this field expression.
     * @param varRefExpr variable reference
     */
    public void setVarRef(Expression varRefExpr) {
        this.varRefExpr = varRefExpr;
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
     * Get the type of the current expression.
     *
     * @return Type of this expression
     */
    public BType getExpressionType() {
        return this.type;
    }
    
    @Override
    public boolean hasTemporaryValues() {
        return varRefExpr.hasTemporaryValues();
    }
    
    public void setIsStaticField(boolean isStaticField) {
        this.isStaticField = isStaticField;
    }
    
    public boolean isStaticField() {
        return isStaticField;
    }

    public boolean isArrayIndexExpr() {
        return isArrayIndexExpr;
    }

    public void setIsArrayIndexExpr(boolean isArrayIndexExpr) {
        this.isArrayIndexExpr = isArrayIndexExpr;
    }
}
