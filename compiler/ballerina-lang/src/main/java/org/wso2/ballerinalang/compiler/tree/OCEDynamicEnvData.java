/*
 * Copyright (c) (2022), WSO2 Inc. (http://www.wso2.org).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.tree;

import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
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
 * This class is a placeholder for dynamic environment data for object ctor expression. While `capturedClosureEnv`
 * provide env data other
 * @since 2.0
 */
public class OCEDynamicEnvData {

    private static final int DEFAULT_CLOSURE_VAR_STORAGE_CAPACITY = 4;
    public boolean blockMapUpdatedInInitMethod;
    public boolean functionMapUpdatedInInitMethod;
    public SymbolEnv capturedClosureEnv;

    public BLangTypeInit typeInit;
    public BObjectType objectType;
    public BLangInvocation.BLangAttachedFunctionInvocation attachedFunctionInvocation;

    public SymbolEnv objMethodsEnv;
    public SymbolEnv fieldEnv;

    public BVarSymbol classEnclosedFunctionMap;
    public BVarSymbol blockLevelMapSymbol;
    public BVarSymbol functionLevelMapSymbol;

    public Set<BSymbol> closureBlockSymbols;
    public Set<BSymbol> closureFuncSymbols;
    public Set<BSymbol> closureSymbols;
    public Map<BLangFunction, SymbolEnv> functionEnvs;

    /*
     * Following fields will be used for AST Cloning.
     */
    public OCEDynamicEnvData cloneRef;
    public int cloneAttempts;
    public BLangClassDefinition originalClass;

    // These can be removed by an proper design
    public BLangClassDefinition parent;
    public boolean fieldClosureDesugaringInProgress;
    public boolean isDirty;
    public List<BLangSimpleVarRef> desugaredClosureVars;
    public BLangExpression initInvocation;


    // ************************ new approach START
    public Map<BLangFunction, Set<BSymbol>> functionClosures; // functions which has closure variables inside them
//    public BLangFunction parentFunction;
    public Set<BSymbol> closureVarOriginalSymbols;
    public Set<BSymbol> closureSymbolsFields;
    // ************************ new approach END

    public OCEDynamicEnvData(int functions) {
        closureBlockSymbols = new HashSet<>(DEFAULT_CLOSURE_VAR_STORAGE_CAPACITY);
        closureFuncSymbols = new HashSet<>(DEFAULT_CLOSURE_VAR_STORAGE_CAPACITY);
        closureSymbols = new HashSet<>(DEFAULT_CLOSURE_VAR_STORAGE_CAPACITY);
        closureVarOriginalSymbols = new HashSet<>(DEFAULT_CLOSURE_VAR_STORAGE_CAPACITY);
        desugaredClosureVars = new ArrayList<>(DEFAULT_CLOSURE_VAR_STORAGE_CAPACITY);
        functionEnvs = new HashMap<>(functions);
        cloneAttempts = 0;

        // new approach
        functionClosures = new HashMap<>(functions);
    }

    public void cleanUp() {
        closureFuncSymbols = null;
        closureBlockSymbols = null;
        closureSymbols = null;
        desugaredClosureVars = null;
        functionEnvs = null;
        functionLevelMapSymbol = null;
        blockLevelMapSymbol = null;
        cloneAttempts = 0;
        throw new UnsupportedOperationException("Not tested");
    }
}
