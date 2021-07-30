/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.LambdaFunctionNode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.TreeMap;

/**
 * Implementation of {@link LambdaFunctionNode}.
 *
 * @since 0.94
 */
public class BLangLambdaFunction extends BLangExpression implements LambdaFunctionNode {

    public BLangFunction function;
    public SymbolEnv capturedClosureEnv;
    public TreeMap<Integer, BVarSymbol> paramMapSymbolsOfEnclInvokable = new TreeMap<>();
    public TreeMap<Integer, BVarSymbol>  enclMapSymbols = new TreeMap<>();

    @Override
    public FunctionNode getFunctionNode() {
        return function;
    }

    @Override
    public void setFunctionNode(FunctionNode functionNode) {
        this.function = (BLangFunction) functionNode;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.LAMBDA;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "LambdaRef:" + (function != null ? String.valueOf(function.getName()) : null);
    }
}
