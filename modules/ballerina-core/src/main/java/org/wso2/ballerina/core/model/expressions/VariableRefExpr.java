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
import org.wso2.ballerina.core.model.NodeLocation;
import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.VariableDef;
import org.wso2.ballerina.core.model.types.BType;
import org.wso2.ballerina.core.model.values.BValue;

/**
 * {@code VariableRefExpr} represents a variable reference in Ballerina.
 *
 * @since 0.8.0
 */
public class VariableRefExpr extends AbstractExpression implements ReferenceExpr {
    private String varName;
    private SymbolName symbolName;
    private VariableDef variableDef;

    public VariableRefExpr(NodeLocation location, String varName) {
        super(location);
        this.varName = varName;
        this.symbolName = new SymbolName(varName);
    }

    public VariableRefExpr(NodeLocation location, SymbolName symbolName) {
        super(location);
        this.symbolName = symbolName;
    }

    @Override
    public String getVarName() {
        return varName;
    }

    public SymbolName getSymbolName() {
        return symbolName;
    }

    public BType getType() {
        return variableDef.getType();
    }

    public MemoryLocation getMemoryLocation() {
        return variableDef.getMemoryLocation();
    }

    public void setMemoryLocation(MemoryLocation location) {
        this.variableDef.setMemoryLocation(location);
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

    public BValue execute(NodeExecutor executor) {
        return executor.visit(this);
    }

}
