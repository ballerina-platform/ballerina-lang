/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/***
 * This class is a place holder for dynamic environment data for object ctor expression. While `capturedClosureEnv`
 * provide env data other
 * @since 2.0
 */
public class OCEDynamicEnvData {

    public SymbolEnv capturedClosureEnv;

    public BLangTypeInit typeInit;
    public BObjectType objectType;
    public BLangInvocation.BLangAttachedFunctionInvocation attachedFunctionInvocation;

    public SymbolEnv objMethodsEnv;
    public SymbolEnv fieldEnv;

    public boolean blockMapUpdatedInInitMethod;
    public boolean functionMapUpdatedInInitMethod;
    public BVarSymbol classEnclosedFunctionMap;
    public BVarSymbol mapBlockMapSymbol;
    public BVarSymbol mapFunctionMapSymbol;

    public Set<BSymbol> closureBlockSymbols;
    public Set<BSymbol> closureFuncSymbols;
    public Map<BLangFunction, SymbolEnv> functionEnvs;

    /*
     * Following fields will be used for AST Cloning.
     */
    public OCEDynamicEnvData cloneRef;
    public int cloneAttempt;
    public BLangClassDefinition originalClass;

    // These can be removed by an proper design
    public LinkedList<BLangClassDefinition> parents;
    public boolean closureDesugaringInProgress;
    public boolean isDirty;
    public List<BLangSimpleVarRef> desugaredClosureVars;

    public OCEDynamicEnvData(int functions) {
        closureBlockSymbols = new HashSet<>();
        closureFuncSymbols = new HashSet<>();
        parents = new LinkedList<>();
        desugaredClosureVars = new ArrayList<>();
        functionEnvs = new HashMap<>(functions);
        cloneAttempt = 0;
    }

    public void cleanUp() {
        parents.clear();
        closureFuncSymbols.clear();
        closureBlockSymbols.clear();
        desugaredClosureVars.clear();
        functionEnvs.clear();
        mapFunctionMapSymbol = null;
        mapBlockMapSymbol = null;
    }
}
