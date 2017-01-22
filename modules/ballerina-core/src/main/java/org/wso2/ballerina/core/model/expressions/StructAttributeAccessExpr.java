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
package org.wso2.ballerina.core.model.expressions;

import org.wso2.ballerina.core.interpreter.MemoryLocation;
import org.wso2.ballerina.core.model.NodeExecutor;
import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.types.BIndexedType;
import org.wso2.ballerina.core.model.types.BType;
import org.wso2.ballerina.core.model.values.BValue;

/**
 * {@code StructAttributeAccessExpr} represents struct attribute access operation.
 * {@link StructAttributeAccessExpr} is a chain of {@link StructAttributeAccessExpr}s, similar to a
 * linked list, with references to the parent expression and child expressions, that are in either
 * side of this expression.
 * <br/>
 * eg:
 * <br/>
 * In the expression <b>person[3].name.firstName<b> each variable reference separated by '.', is
 * represented by a {@link StructAttributeAccessExpr}, with links to the nearest neighbor.
 *
 * @since 1.0.0
 */
public class StructAttributeAccessExpr extends AbstractExpression implements ReferenceExpr {

    /**
     * Unique identifier or this expression
     */
    private SymbolName symbolName;
    
    /**
     * Holds a reference to the actual variable, stated in the expression.
     */
    private ReferenceExpr structVarRefExpr;
    
    /**
     * Expression of the child attribute of the current expression.
     * Is null for the last child in the chain.
     */
    private StructAttributeAccessExpr attributeExpr;
    
    /**
     * Expression precedes the current expression in the chain.
     * Is null for the root of the chain.
     */
    private StructAttributeAccessExpr parentExpr;
    
    /**
     * Flag indicating whether the entire expression is a left hand side expression.
     */
    private boolean isLHSExpr;
    
    /**
     * Memory location of the variable represented by the current expression.
     */
    private MemoryLocation memoryLocation;
    
    /**
     * Creates a Struct attribute access expression.
     *      
     * @param symbolName        Symbol Name of the current attribute
     * @param structVarRefExpr  Variable reference represented by the current attribute
     */
    public StructAttributeAccessExpr(SymbolName symbolName,  ReferenceExpr structVarRefExpr) {
        this.symbolName = symbolName;
        this.structVarRefExpr = structVarRefExpr;
    }    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SymbolName getSymbolName() {
        return symbolName;
    }

    /**
     * Get the memory location of the attribute represented by this {@link StructAttributeAccessExpr}.
     * 
     * @return  Memory location of the attribute represented by this {@link StructAttributeAccessExpr}
     */
    public MemoryLocation getMemoryLocation() {
        return memoryLocation;
    }

    /**
     * Set the memory location of the attribute represented by this {@link StructAttributeAccessExpr}.
     * 
     * @param location  Memory location of the attribute represented by this {@link StructAttributeAccessExpr}
     */
    public void setMemoryLocation(MemoryLocation location) {
        this.memoryLocation = location;
    }
    
    /**
     * Get the variable reference expression represented by this {@link StructAttributeAccessExpr}.
     * 
     * @return  Variable reference expression represented by this {@link StructAttributeAccessExpr}.
     */
    public StructAttributeAccessExpr getAttributeExpr() {
        return attributeExpr;
    }

    /**
     * Check whether this expression is a left hand side expression in an assignment.
     * 
     * @return  Flag indicating whether this expression is a left hand side expression in an assignment.
     */
    public boolean isLHSExpr() {
        return isLHSExpr;
    }

    /**
     * Set the flag indicating whether this expression is a left hand side expression in an assignment.
     * 
     * @param isLhsExpr   Flag indicating whether this expression is a left hand side expression in an assignment.
     */
    public void setLHSExpr(boolean isLhsExpr) {
        isLHSExpr = isLhsExpr;
    }
    
    /**
     * Set the parent of this attribute expression.
     * 
     * @param parent    Parent of this attribute expression.
     */
    public void setParent(StructAttributeAccessExpr parent) {
        this.parentExpr = parent;
    }
    
    /**
     * Set the child attribute of this attribute expression.
     * 
     * @param attributeAccessExpr    Child attribute of this attribute expression.
     */
    public void setAttributeExpr(StructAttributeAccessExpr attributeAccessExpr) {
        this.attributeExpr = attributeAccessExpr;
    }
    
    /**
     * Get the parent of this attribute expression.
     * 
     * @return  Parent of this attribute expression.
     */
    public StructAttributeAccessExpr getParent() {
        return parentExpr;
    }
    
    /**
     * Get the variable reference represented by this attribute expression.
     * 
     * @return  Variable reference represented by this attribute expression.
     */
    public ReferenceExpr getVarRef() {
        return structVarRefExpr;
    }
    
    /**
     * Get the index expression of the current attribute expression.
     * 
     * @return  Index expression of the current attribute expression. If the current expression does not
     *          represents a array/map variable, then this will return null.
     */
    public Expression getIndexExpr() {
        if (structVarRefExpr instanceof ArrayMapAccessExpr) {
            return ((ArrayMapAccessExpr) structVarRefExpr).getIndexExpr();
        }
        return null;
    }
    
    /**
     * Get the type of the variable represented by this attribute expression.
     * 
     * @return  Type of the variable represented by this attribute expression .
     */
    public BType getRefVarType() {
        if (structVarRefExpr instanceof ArrayMapAccessExpr) {
            return structVarRefExpr.getType();
        }
        return null;
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
     * Type of a struct attribute access expression chain, is the type of the attribute expression at the chain.
     */
    @Override
    public BType getType() {
        if (this.attributeExpr != null) {
            return attributeExpr.getType();
        } else {
            // If the type is an array type, then return the element type
            if (getIndexExpr() != null && this.type instanceof BIndexedType) {
                return ((BIndexedType) this.type).getElementType();
            }
            return this.type;
        }
    }
    
    /**
     * Get the type of this expression
     * 
     * @return  Type of this expression
     */
    public BType getExpressionType() {
        return this.type;
    }
}
