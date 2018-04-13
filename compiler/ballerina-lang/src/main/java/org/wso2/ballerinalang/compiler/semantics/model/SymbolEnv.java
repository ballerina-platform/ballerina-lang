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
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangObject;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;

/**
 * @since 0.94
 */
public class SymbolEnv {

    public Scope scope;

    public BLangNode node;

    public BLangPackage enclPkg;

    public BLangConnector enclConnector;

    public BLangObject enclObject;

    public BLangStruct enclStruct;

    public BLangAnnotation enclAnnotation;

    public BLangService enclService;

    public BLangInvokableNode enclInvokable;

    public BLangForkJoin forkJoin;

    public BVarSymbol enclVarSym;

    public SymbolEnv enclEnv;

    public SymbolEnv(BLangNode node, Scope scope) {
        this.scope = scope;
        this.node = node;
        this.enclPkg = null;
        this.enclConnector = null;
        this.enclObject = null;
        this.enclStruct = null;
        this.enclAnnotation = null;
        this.enclService = null;
        this.enclInvokable = null;
        this.forkJoin = null;
        this.enclEnv = null;
        this.enclVarSym = null;
    }

    public void copyTo(SymbolEnv target) {
        target.enclPkg = this.enclPkg;
        target.enclConnector = this.enclConnector;
        target.enclObject = this.enclObject;
        target.enclStruct = this.enclStruct;
        target.enclAnnotation = this.enclAnnotation;
        target.enclService = this.enclService;
        target.enclInvokable = this.enclInvokable;
        target.forkJoin = this.forkJoin;
        target.enclVarSym = this.enclVarSym;
        target.enclEnv = this;
    }

    public static SymbolEnv createPkgEnv(BLangPackage node, Scope scope, SymbolEnv builtinEnv) {
        SymbolEnv env = new SymbolEnv(node, scope);
        env.enclPkg = node;
        env.enclEnv = builtinEnv;
        return env;
    }

    public static SymbolEnv createPkgLevelSymbolEnv(BLangNode node,
                                                    Scope scope, SymbolEnv pkgEnv) {
        SymbolEnv symbolEnv = duplicate(node, scope, pkgEnv);
        symbolEnv.enclPkg = pkgEnv.enclPkg;
        return symbolEnv;
    }

    public static SymbolEnv createFunctionEnv(BLangFunction node, Scope scope, SymbolEnv env) {
        SymbolEnv funcEnv = createPkgLevelSymbolEnv(node, scope, env);
        funcEnv.enclInvokable = node;
        return funcEnv;
    }

    public static SymbolEnv createConnectorEnv(BLangConnector node, Scope scope, SymbolEnv env) {
        SymbolEnv connectorEnv = createPkgLevelSymbolEnv(node, scope, env);
        connectorEnv.enclConnector = node;
        return connectorEnv;
    }

    public static SymbolEnv createObjectEnv(BLangObject node, Scope scope, SymbolEnv env) {
        SymbolEnv objectEnv = createPkgLevelSymbolEnv(node, scope, env);
        objectEnv.enclObject = node;
        return objectEnv;
    }

    public static SymbolEnv createDummyEnv(BLangFunction node, Scope scope, SymbolEnv env) {
        SymbolEnv dummyEnv = createPkgLevelSymbolEnv(node, scope, env);
        return dummyEnv;
    }

    public static SymbolEnv createStructEnv(BLangStruct node, Scope scope, SymbolEnv env) {
        SymbolEnv objectEnv = createPkgLevelSymbolEnv(node, scope, env);
        objectEnv.enclStruct = node;
        return objectEnv;
    }

    public static SymbolEnv createAnnotationEnv(BLangAnnotation node, Scope scope, SymbolEnv env) {
        SymbolEnv annotationEnv = createPkgLevelSymbolEnv(node, scope, env);
        annotationEnv.enclAnnotation = node;
        return annotationEnv;
    }

    public static SymbolEnv createServiceEnv(BLangService node, Scope scope, SymbolEnv env) {
        SymbolEnv serviceEnv = createPkgLevelSymbolEnv(node, scope, env);
        serviceEnv.enclService = node;
        return serviceEnv;
    }

    public static SymbolEnv createResourceActionSymbolEnv(BLangInvokableNode node, Scope scope, SymbolEnv env) {
        SymbolEnv symbolEnv = duplicate(node, scope, env);
        symbolEnv.enclInvokable = node;
        return symbolEnv;
    }

    public static SymbolEnv createForkJoinSymbolEnv(BLangForkJoin node, SymbolEnv env) {
        SymbolEnv symbolEnv = new SymbolEnv(node, env.scope);
        env.copyTo(symbolEnv);
        symbolEnv.forkJoin = node;
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

    public static SymbolEnv createWorkerEnv(BLangWorker worker, SymbolEnv env) {
        SymbolEnv symbolEnv = new SymbolEnv(worker, worker.symbol.scope);
        env.copyTo(symbolEnv);
        return symbolEnv;
    }

    public static SymbolEnv createFolkJoinEnv(BLangForkJoin forkJoin, SymbolEnv env) {
        Scope scope = new Scope(env.scope.owner);
        SymbolEnv symbolEnv = new SymbolEnv(forkJoin, scope);
        env.copyTo(symbolEnv);
        return symbolEnv;
    }

    public static SymbolEnv getXMLElementEnv(BLangXMLElementLiteral node, SymbolEnv env) {
        Scope scope = node.scope;
        if (scope == null) {
            scope = new Scope(env.scope.owner);
            node.scope = scope;
        }
        SymbolEnv symbolEnv = new SymbolEnv(node, scope);
        env.copyTo(symbolEnv);
        return symbolEnv;
    }

    public static SymbolEnv getXMLAttributeEnv(BLangXMLAttribute node, SymbolEnv env) {
        SymbolEnv symbolEnv = new SymbolEnv(node, env.scope);
        env.copyTo(symbolEnv);
        return symbolEnv;
    }

    public static SymbolEnv createTransformerEnv(BLangTransformer node, Scope scope, SymbolEnv env) {
        SymbolEnv transformerEnv = createPkgLevelSymbolEnv(node, scope, env);
        transformerEnv.enclInvokable = node;
        return transformerEnv;
    }

    // Private functions

    private static SymbolEnv duplicate(BLangNode node, Scope scope, SymbolEnv env) {
        SymbolEnv symbolEnv = new SymbolEnv(node, scope);
        env.copyTo(symbolEnv);
        return symbolEnv;
    }

}
