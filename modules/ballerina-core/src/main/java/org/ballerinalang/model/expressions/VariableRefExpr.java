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

import org.ballerinalang.bre.MemoryLocation;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.VariableDef;
import org.ballerinalang.model.WhiteSpaceDescriptor;
import org.ballerinalang.model.types.BType;

/**
 * {@code VariableRefExpr} represents a variable reference in Ballerina.
 *
 * @since 0.8.0
 */
public class VariableRefExpr extends AbstractExpression implements ReferenceExpr {
    private String varName;
    private String pkgName;
    private String pkgPath;
    private SymbolName symbolName;
    private VariableDef variableDef;
    private boolean isLHSExpr;

    public VariableRefExpr(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, String varName) {
        super(location, whiteSpaceDescriptor);
        this.varName = varName;
        this.symbolName = new SymbolName(varName);
    }

    public VariableRefExpr(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, SymbolName symbolName) {
        super(location, whiteSpaceDescriptor);
        this.symbolName = symbolName;
    }

    public VariableRefExpr(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, String varName,
                           String pkgName, String pkgPath) {
        super(location, whiteSpaceDescriptor);
        this.varName = varName;
        this.pkgName = pkgName;
        this.pkgPath = pkgPath;
        this.symbolName = new SymbolName(varName, pkgPath);
    }

    @Override
    public String getVarName() {
        return varName;
    }

    @Override
    public String getPkgName() {
        return pkgName;
    }

    @Override
    public String getPkgPath() {
        return pkgPath;
    }

    public SymbolName getSymbolName() {
        return symbolName;
    }

    @Override
    public boolean isLHSExpr() {
        return isLHSExpr;
    }

    @Override
    public void setLHSExpr(boolean lhsExpr) {
        this.isLHSExpr = lhsExpr;
    }

    public BType getType() {
        return variableDef.getType();
    }

    public MemoryLocation getMemoryLocation() {
        return variableDef.getMemoryLocation();
    }

    public VariableDef getVariableDef() {
        return variableDef;
    }

    public void setVariableDef(VariableDef variableDef) {
        this.variableDef = variableDef;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
