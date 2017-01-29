/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.ballerina.core.model.values.BValue;

/**
 * {@code VariableRefExpr} represents a variable reference in Ballerina.
 *
 * @since 0.8.0
 */
public class VariableRefExpr extends AbstractExpression implements ReferenceExpr {

    private SymbolName symbolName;

    private MemoryLocation memoryLocation;

    public VariableRefExpr(NodeLocation location, SymbolName symbolName) {
        super(location);
        this.symbolName = symbolName;
    }

    public SymbolName getSymbolName() {
        return symbolName;
    }

    public MemoryLocation getMemoryLocation() {
        return memoryLocation;
    }

    public void setMemoryLocation(MemoryLocation location) {
        this.memoryLocation = location;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public BValue execute(NodeExecutor executor) {
        return executor.visit(this);
    }
}
