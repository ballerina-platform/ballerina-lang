package org.wso2.ballerinalang.compiler.tree;

import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;

import java.util.ArrayList;
import java.util.List;

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
    public List<BVarSymbol> functionMapClosures;

    public List<BLangLambdaFunction> lambdaFunctionsList;

    /*
     * Following fields will be used for AST Cloning.
     */
    public OCEDynamicEnvironmentData cloneRef;
    public int cloneAttempt;
    public BLangClassDefinition originalClass;

    public OCEDynamicEnvironmentData() {
        lambdaFunctionsList = new ArrayList<>(1);
        cloneAttempt = 0;
    }
}
