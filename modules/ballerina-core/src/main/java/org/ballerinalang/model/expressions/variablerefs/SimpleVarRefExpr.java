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

import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.VariableDef;
import org.ballerinalang.model.WhiteSpaceDescriptor;
import org.ballerinalang.model.expressions.AbstractExpression;
import org.ballerinalang.model.types.BType;

/**
 * {@code SimpleVarRefExpr} represents a variable reference expression with just the variable name.
 * e.g. name;
 *
 * @since 0.89
 */
public class SimpleVarRefExpr extends AbstractExpression implements VariableReferenceExpr {
    private String varName;
    private String pkgName;
    private String pkgPath;

    private SymbolName symbolName;
    private VariableDef variableDef;
    private boolean isLHSExpr;

    // Parent in the node tree
    private VariableReferenceExpr parentVarRefExpr;

    public SimpleVarRefExpr(NodeLocation location,
                            WhiteSpaceDescriptor whiteSpaceDescriptor,
                            String varName,
                            String pkgName,
                            String pkgPath) {
        super(location, whiteSpaceDescriptor);
        this.varName = varName;
        this.pkgName = pkgName;
        this.pkgPath = pkgPath;
        this.symbolName = new SymbolName(varName, pkgPath);
    }

    public SimpleVarRefExpr(NodeLocation location,
                            WhiteSpaceDescriptor whiteSpaceDescriptor,
                            String varName) {
        this(location, whiteSpaceDescriptor, varName, null, null);
    }

    public String getVarName() {
        return varName;
    }

    public String getPkgName() {
        return pkgName;
    }

    public String getPkgPath() {
        return pkgPath;
    }

    public SymbolName getSymbolName() {
        return symbolName;
    }

    public VariableDef getVariableDef() {
        return variableDef;
    }

    public void setVariableDef(VariableDef variableDef) {
        this.variableDef = variableDef;
    }

    public BType getType() {
        return variableDef.getType();
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

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
