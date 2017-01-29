/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

import org.wso2.ballerina.core.model.symbols.VariableRefSymbol;
import org.wso2.ballerina.core.model.types.BType;
import org.wso2.ballerina.core.model.types.SimpleTypeName;

/**
 * {@code VariableDef} represent a Variable definition.
 * <p>
 * Ballerina has variables of various types. The type system includes built-in primitives,
 * a collection of built-in structured types and array and record type constructors.
 *
 * @since 0.8.0
 */
public class VariableDef implements Node {
    protected NodeLocation location;
    protected String name;
    protected SimpleTypeName typeName;
    protected VariableRefSymbol varRefSymbol;

    private BType type;
    private SymbolName symbolName;

    public VariableDef(NodeLocation location, String name, SimpleTypeName typeName, VariableRefSymbol varRefSymbol) {
        this.location = location;
        this.name = name;
        this.typeName = typeName;
        this.varRefSymbol = varRefSymbol;
    }

    public VariableDef(NodeLocation location, BType type, SymbolName symbolName) {
        this.location = location;
        this.type = type;
        this.symbolName = symbolName;
    }

    public SymbolName getName() {
        return symbolName;
    }

    public SimpleTypeName getTypeName() {
        return typeName;
    }

    public BType getType() {
        return type;
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
