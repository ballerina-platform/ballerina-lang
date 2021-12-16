package org.wso2.ballerinalang.compiler.tree;

import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/***
 * @since 2.0
 */
public class OCEDynamicEnvironmentData {

    public SymbolEnv capturedClosureEnv;
    public BVarSymbol mapBlockMapSymbol;
    public BVarSymbol mapFunctionMapSymbol;
    public BLangTypeInit typeInit;
    public BObjectType objectType;
    public BLangInvocation.BLangAttachedFunctionInvocation attachedFunctionInvocation;
//    public BLangClassDefinition classDefinition;

    public SymbolEnv objMethodsEnv;
    public SymbolEnv fieldEnv;

    public boolean mapUpdatedInInitMethod;
    public boolean functionMapUpdatedInInitMethod;
    public BVarSymbol blockMap;
    public BVarSymbol classEnclosedFunctionMap;

    public List<BLangLambdaFunction> lambdaFunctionsList;
    public Set<BSymbol> closureBlockSymbols;
    public Set<BSymbol> closureFuncSymbols;


    /*
     * Following fields will be used for AST Cloning.
     */
    public OCEDynamicEnvironmentData cloneRef;
    public int cloneAttempt;
    public BLangClassDefinition originalClass;
    public LinkedList<BLangClassDefinition> parents;
    public LinkedList<BLangClassDefinition> siblings;

    public OCEDynamicEnvironmentData() {
        lambdaFunctionsList = new ArrayList<>(1);
        closureBlockSymbols = new HashSet<>();
        closureFuncSymbols = new HashSet<>();
        parents = new LinkedList<>();
        siblings = new LinkedList<>();
        cloneAttempt = 0;
    }
}
