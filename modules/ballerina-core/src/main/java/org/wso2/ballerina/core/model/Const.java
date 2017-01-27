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
 * {@code Const} represent a Constant declaration.
 *
 * @since 0.8.0
 */
public class Const extends PositionAwareNode implements Node {

    private BType type;
    private SymbolName symbolName;
    private Expression valueExpr;
    private BValue value;
    protected Position sourceLocation;

    /**
     * Constructing a Ballerina Const Statement.
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

    public Const(BType type, SymbolName symbolName, Expression valueExpr) {
        this.type = type;
        this.symbolName = symbolName;
        this.valueExpr = valueExpr;
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

    public Expression getValueExpr() {
        return valueExpr;
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

    /**
     *
     */
    public static class ConstBuilder {
        private BType type;
        private SymbolName symbolName;
        private Expression valueExpr;

        public ConstBuilder() {
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
            return new Const(type, symbolName, valueExpr);
        }
    }
    
    /**
     * Get the source location of this constant.
     * Return the source file and the line number of this constant.
     * 
     * @return  Source location of this constant
     */
    public Position getLocation() {
        return sourceLocation;
    }

    /**
     * Set the source location of this constant.
     * 
     * @param location  Source location of this constant.
     */
    public void setLocation(Position location) {
        this.sourceLocation = location;
    }
}
