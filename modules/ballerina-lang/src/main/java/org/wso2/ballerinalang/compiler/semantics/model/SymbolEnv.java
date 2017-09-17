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
package org.wso2.ballerinalang.compiler.semantics.model;

import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;

/**
 * @since 0.94
 */
public class SymbolEnv {

    public Scope scope;

    public BLangNode node;

    public BLangPackage enclPkg;

    public BLangConnector enclConnector;

    public BLangService enclService;

    public BLangInvokableNode enclInvokable;

    public BVarSymbol enclVarSym;

    public SymbolEnv enclEnv;

    public SymbolEnv(BLangNode node, Scope scope) {
        this.scope = scope;
        this.node = node;
        this.enclPkg = null;
        this.enclConnector = null;
        this.enclService = null;
        this.enclInvokable = null;
        this.enclEnv = null;
        this.enclVarSym = null;
    }

    public void copyTo(SymbolEnv target) {
        target.enclPkg = this.enclPkg;
        target.enclConnector = this.enclConnector;
        target.enclService = this.enclService;
        target.enclInvokable = this.enclInvokable;
        target.enclVarSym = this.enclVarSym;
        target.enclEnv = this;
    }

    public static SymbolEnv createPkgEnv(BLangPackage node,
                                         Scope scope,
                                         BLangPackage rootPkgNode) {
        SymbolEnv env = new SymbolEnv(node, scope);
        env.enclPkg = rootPkgNode;
        return env;
    }

    public static SymbolEnv createPkgLevelSymbolEnv(BLangNode node,
                                                    SymbolEnv pkgEnv,
                                                    Scope scope) {
        SymbolEnv symbolEnv = new SymbolEnv(node, scope);
        pkgEnv.copyTo(symbolEnv);
        symbolEnv.enclPkg = (BLangPackage) pkgEnv.node;
        return symbolEnv;
    }

    public static SymbolEnv createBlockEnv(BLangBlockStmt block, SymbolEnv env) {
        // Create a scope for the block node if one doesn't exists
        Scope scope = block.scope;
        if (scope == null) {
            scope = new Scope(env.scope.owner);
            block.scope = scope;
        }

        SymbolEnv symbolEnv = new SymbolEnv(block, scope);
        env.copyTo(symbolEnv);
        return symbolEnv;
    }

    public static SymbolEnv createVarInitEnv(BLangVariable node, SymbolEnv env, BVarSymbol enclVarSym) {
        SymbolEnv symbolEnv = new SymbolEnv(node, env.scope);
        env.copyTo(symbolEnv);
        symbolEnv.enclVarSym = enclVarSym;
        return symbolEnv;
    }
    
    public static SymbolEnv createWorkerEnv(BLangWorker worker, SymbolEnv env, BLangInvokableNode enclInv) {
        SymbolEnv symbolEnv = new SymbolEnv(worker, env.scope);
        env.copyTo(symbolEnv);
        symbolEnv.enclInvokable = enclInv;
        return symbolEnv;
    }
    
}
