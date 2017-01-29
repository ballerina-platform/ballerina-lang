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
import org.wso2.ballerina.core.model.types.BType;
import org.wso2.ballerina.core.model.values.BValue;

/**
 * {@code Const} represents a Constant in Ballerina.
 *
 * @since 0.8.0
 */
public class Const implements CompilationUnit {

    private BType type;
    private SymbolName symbolName;
    private Expression rhsExpr;
    private BValue value;
    protected NodeLocation location;

    /**
     * Constructing a Ballerina Constant Node.
     *
     * @param type       Type of the constant
     * @param symbolName Identifier of the constant
     * @param value      bValueRef of the constant
     */
    public Const(BType type, SymbolName symbolName, BValue value) {
        this.type = type;
        this.symbolName = symbolName;
        this.value = value;
    }

    /**
     * @param type       Type of the constant
     * @param symbolName Identifier of the constant
     * @param rhsExpr    Rhs expression
     */
    public Const(NodeLocation location, BType type, SymbolName symbolName, Expression rhsExpr) {
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

    /**
     * This class is builds a {@code Constant} node from parser events.
     *
     * @since 0.8.0
     */
    public static class ConstBuilder {
        private NodeLocation location;
        private BType type;
        private SymbolName symbolName;
        private Expression valueExpr;

        public void setNodeLocation(NodeLocation location) {
            this.location = location;
        }

        public void setType(BType type) {
            this.type = type;
        }

        public void setSymbolName(SymbolName symbolName) {
            this.symbolName = symbolName;
        }

        public void setValueExpr(Expression valueExpr) {
            this.valueExpr = valueExpr;
        }

        public Const build() {
            return new Const(location, type, symbolName, valueExpr);
        }
    }
}
