package org.wso2.ballerinalang.compiler.tree;

import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;

import java.util.TreeMap;

/***
 * @since 2.0
 */
public class OCEDynamicEnvironmentData {
    public SymbolEnv capturedClosureEnv;
    public TreeMap<Integer, BVarSymbol> enclMapSymbols;
    public BVarSymbol mapSymbol;
    public BLangTypeInit typeInit;

    public boolean closureDesugared = false;

     public OCEDynamicEnvironmentData(SymbolEnv env, BLangTypeInit typeInit) {
         this.capturedClosureEnv = env;
         this.typeInit = typeInit;
     }
}
