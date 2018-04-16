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
package org.wso2.ballerinalang.compiler.tree;

import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.VariableNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @since 0.94
 */
public class BLangFunction extends BLangInvokableNode implements FunctionNode {

    public BLangVariable receiver;

    //TODO remove this and use ATTACHED flag instead
    // TODO remove when removing struct
    public boolean attachedFunction;
    public boolean attachedOuterFunction;

    public boolean interfaceFunction;

    public BLangBlockStmt enclBlockStmt;

    public Set<BVarSymbol> closureVarSymbols =  new LinkedHashSet<>();

    public Map<BSymbol, BLangStatement> initFunctionStmts = new LinkedHashMap<>();

    public VariableNode getReceiver() {
        return receiver;
    }

    public void setReceiver(VariableNode receiver) {
        this.receiver = (BLangVariable) receiver;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.FUNCTION;
    }
    
    @Override
    public String toString() {
        return "BLangFunction: " + super.toString();
    }
    
}
