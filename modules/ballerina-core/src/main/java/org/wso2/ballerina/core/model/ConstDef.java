/*
*   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.model;

import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.symbols.VariableRefSymbol;
import org.wso2.ballerina.core.model.types.BType;
import org.wso2.ballerina.core.model.types.SimpleTypeName;
import org.wso2.ballerina.core.model.values.BValue;

/**
 * {@code ConstDef} represents a Constant in Ballerina.
 *
 * @since 0.8.0
 */
public class ConstDef extends VariableDef implements CompilationUnit {

    private BType type;
    private SymbolName symbolName;
    private Expression rhsExpr;
    private BValue value;
    protected NodeLocation location;

    private boolean publicConst;

    /**
     * @param name        Type of the constant
     * @param typeName    Identifier of the constant
     * @param rhsExpr     Rhs expression
     * @param publicConst if true, then this constant is visible to other packages
     * @param varRefSymbol {@code VariableRefSymbol} of this definition
     */
    public ConstDef(NodeLocation location,
                    String name,
                    SimpleTypeName typeName,
                    Expression rhsExpr,
                    boolean publicConst,
                    VariableRefSymbol varRefSymbol) {
        super(location, name, typeName, varRefSymbol);
        this.rhsExpr = rhsExpr;
        this.publicConst = publicConst;
    }

    /**
     * Constructing a Ballerina Constant Node.
     *
     * @param type       Type of the constant
     * @param symbolName Identifier of the constant
     * @param value      bValueRef of the constant
     */
    public ConstDef(BType type, SymbolName symbolName, BValue value) {
        super(null, "", null, null);
        this.type = type;
        this.symbolName = symbolName;
        this.value = value;
    }

    /**
     * @param type       Type of the constant
     * @param symbolName Identifier of the constant
     * @param rhsExpr    Rhs expression
     */
    public ConstDef(NodeLocation location, BType type, SymbolName symbolName, Expression rhsExpr) {
        super(null, "", null, null);
        this.location = location;
        this.type = type;
        this.symbolName = symbolName;
        this.rhsExpr = rhsExpr;
    }

    /**
     * Get the type of the constant.
     *
     * @return type of the constant
     */
    public BType getType() {
        return type;
    }

    /**
     * Get the identifier of the constant declaration.
     *
     * @return identifier of the constant declaration
     */
    public SymbolName getName() {
        return symbolName;
    }

    public Expression getRhsExpr() {
        return rhsExpr;
    }

    public boolean isPublic() {
        return publicConst;
    }

    /**
     * Get the bValue of the constant.
     *
     * @return bValue of the constant
     */
    public BValue getValue() {
        return value;
    }

    public void setValue(BValue value) {
        this.value = value;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeLocation getNodeLocation() {
        return location;
    }
}
