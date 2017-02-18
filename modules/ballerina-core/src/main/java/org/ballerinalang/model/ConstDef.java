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
package org.ballerinalang.model;

import org.ballerinalang.model.expressions.Expression;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.SimpleTypeName;
import org.ballerinalang.model.values.BValue;

/**
 * {@code ConstDef} represents a Constant in Ballerina.
 *
 * @since 0.8.0
 */
public class ConstDef extends VariableDef implements CompilationUnit {
    private Expression rhsExpr;
    private BValue value;

    public ConstDef(NodeLocation location,
                    String name,
                    SimpleTypeName typeName,
                    String pkgPath,
                    boolean isPublic,
                    SymbolName symbolName,
                    SymbolScope symbolScope,
                    Expression rhsExpr) {

        super(location, name, typeName, symbolName, symbolScope);
        this.pkgPath = pkgPath;
        this.isPublic = isPublic;
        this.rhsExpr = rhsExpr;
    }

    public ConstDef(BType type, SymbolName symbolName, BValue value) {
        super(null, "", null, null, null);
        this.type = type;
        this.symbolName = symbolName;
        this.value = value;
    }

    public ConstDef(NodeLocation location, BType type, SymbolName symbolName, Expression rhsExpr) {
        super(null, "", null, null, null);
        this.location = location;
        this.type = type;
        this.symbolName = symbolName;
        this.rhsExpr = rhsExpr;
    }

    public Expression getRhsExpr() {
        return rhsExpr;
    }

    public BValue getValue() {
        return value;
    }

    public void setValue(BValue value) {
        this.value = value;
    }


    // Methods in Node interface

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
