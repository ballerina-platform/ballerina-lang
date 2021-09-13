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
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.util.ClosureVarSymbol;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @since 0.94
 */
public class BLangFunction extends BLangInvokableNode implements FunctionNode {

    public BLangSimpleVariable receiver;

    //TODO remove this and use ATTACHED flag instead
    // TODO remove when removing struct
    public boolean attachedFunction;
    public boolean objInitFunction;

    public boolean interfaceFunction;

    public TreeMap<Integer, BVarSymbol> paramClosureMap = new TreeMap<>();
    public BVarSymbol mapSymbol;
    public Map<BSymbol, BLangStatement> initFunctionStmts = new LinkedHashMap<>();

    // Used to track uninitialized closure variables in DataFlowAnalyzer.
    public Set<ClosureVarSymbol> closureVarSymbols = new LinkedHashSet<>();

    public BInvokableSymbol originalFuncSymbol;

    public LinkedHashSet<String> sendsToThis = new LinkedHashSet<>();

    // This only set when we encounter worker inside a fork statement.
    public String anonForkName;

    public SimpleVariableNode getReceiver() {
        return receiver;
    }

    public void setReceiver(SimpleVariableNode receiver) {
        this.receiver = (BLangSimpleVariable) receiver;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> void accept(BLangNodeAnalyzer<T> analyzer, T props) {
        analyzer.visit(this, props);
    }

    @Override
    public <T, R> R accept(BLangNodeTransformer<T, R> transformer, T props) {
        return transformer.transform(this, props);
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
