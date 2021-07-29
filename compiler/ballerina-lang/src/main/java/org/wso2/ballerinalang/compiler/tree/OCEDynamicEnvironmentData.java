package org.wso2.ballerinalang.compiler.tree;

import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.util.ClosureVarSymbol;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeMap;

/***
 * @since 2.0
 */
public class OCEDynamicEnvironmentData {

    public SymbolEnv capturedClosureEnv;
    public TreeMap<Integer, BVarSymbol> paramMapSymbolsOfEnclInvokable = new TreeMap<>();
    public TreeMap<Integer, BVarSymbol> enclMapSymbols;
    public BVarSymbol mapSymbol;
    public BLangTypeInit typeInit;
    public BObjectType objectType;
    public BLangInvocation.BLangAttachedFunctionInvocation attachedFunctionInvocation;
    public Set<ClosureVarSymbol> closureVarSymbols = new LinkedHashSet<>();

    public boolean closureDesugared = false;

    public OCEDynamicEnvironmentData(SymbolEnv env, BLangTypeInit typeInit) {
        this.capturedClosureEnv = env;
        this.typeInit = typeInit;
    }

    public OCEDynamicEnvironmentData() {}
}
