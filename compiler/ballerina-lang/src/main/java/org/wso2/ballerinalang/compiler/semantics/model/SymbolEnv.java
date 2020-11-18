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

import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnFailClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 0.94
 */
public class SymbolEnv {

    public Scope scope;

    public BLangNode node;

    public BLangPackage enclPkg;

    public BLangType enclType;

    public BLangAnnotation enclAnnotation;

    public BLangService enclService;

    public BLangInvokableNode enclInvokable;

    public BVarSymbol enclVarSym;

    public SymbolEnv enclEnv;

    public List<TypeParamEntry> typeParamsEntries;

    public boolean logErrors;

    public int envCount;

    public int relativeEnvCount;

    public boolean isModuleInit;

    public SymbolEnv(BLangNode node, Scope scope) {
        this.scope = scope;
        this.node = node;
        this.enclPkg = null;
        this.enclAnnotation = null;
        this.enclService = null;
        this.enclInvokable = null;
        this.enclEnv = null;
        this.enclVarSym = null;
        this.logErrors = true;
        this.typeParamsEntries = null;
        this.isModuleInit = false;
    }

    public void copyTo(SymbolEnv target) {
        target.enclPkg = this.enclPkg;
        target.enclType = this.enclType;
        target.enclAnnotation = this.enclAnnotation;
        target.enclService = this.enclService;
        target.enclInvokable = this.enclInvokable;
        target.enclVarSym = this.enclVarSym;
        target.logErrors = this.logErrors;
        target.enclEnv = this;
        target.envCount = this.envCount;
    }

    public static SymbolEnv createPkgEnv(BLangPackage node, Scope scope, SymbolEnv builtinEnv) {
        SymbolEnv env = new SymbolEnv(node, scope);
        env.envCount = 0;
        env.enclPkg = node;
        env.enclEnv = builtinEnv;
        env.logErrors = false;
        return env;
    }

    public static SymbolEnv createPkgLevelSymbolEnv(BLangNode node,
                                                    Scope scope, SymbolEnv pkgEnv) {
        SymbolEnv symbolEnv = duplicate(node, scope, pkgEnv);
        symbolEnv.envCount = 1;
        symbolEnv.enclPkg = pkgEnv.enclPkg;
        return symbolEnv;
    }

    public static SymbolEnv createFunctionEnv(BLangFunction node, Scope scope, SymbolEnv env) {
        SymbolEnv funcEnv = createPkgLevelSymbolEnv(node, scope, env);
        funcEnv.envCount = env.envCount + 1;
        funcEnv.relativeEnvCount = 0;
        funcEnv.enclInvokable = node;
        return funcEnv;
    }

    public static SymbolEnv createModuleInitFunctionEnv(BLangFunction node, Scope scope, SymbolEnv env) {
        SymbolEnv funcEnv = createFunctionEnv(node, scope, env);
        funcEnv.isModuleInit = true;
        return funcEnv;
    }

    public static SymbolEnv createTypeEnv(BLangType node, Scope scope, SymbolEnv env) {
        SymbolEnv objectEnv = createPkgLevelSymbolEnv(node, scope, env);
        objectEnv.envCount = env.envCount;
        objectEnv.enclType = node;
        return objectEnv;
    }

    public static SymbolEnv createClassEnv(BLangClassDefinition node, Scope scope, SymbolEnv env) {
        SymbolEnv objectEnv = createPkgLevelSymbolEnv(node, scope, env);
        objectEnv.envCount = env.envCount;
        return objectEnv;
    }

    public static SymbolEnv createObjectMethodsEnv(BLangObjectTypeNode node, BObjectTypeSymbol objSymbol,
                                                   SymbolEnv env) {
        SymbolEnv symbolEnv = createPkgLevelSymbolEnv(node, objSymbol.scope, env);
        symbolEnv.envCount = env.envCount + 1;
        env.copyTo(symbolEnv);
        return symbolEnv;
    }

    public static SymbolEnv createClassMethodsEnv(BLangClassDefinition node, BObjectTypeSymbol objSymbol,
                                                   SymbolEnv env) {
        SymbolEnv symbolEnv = createPkgLevelSymbolEnv(node, objSymbol.scope, env);
        symbolEnv.envCount = env.envCount + 1;
        env.copyTo(symbolEnv);
        return symbolEnv;
    }

    public static SymbolEnv createDummyEnv(BLangNode node, Scope scope, SymbolEnv env) {
        SymbolEnv dummyEnv = createPkgLevelSymbolEnv(node, scope, env);
        dummyEnv.envCount = env.envCount + 1;
        return dummyEnv;
    }

    public static SymbolEnv createAnnotationEnv(BLangAnnotation node, Scope scope, SymbolEnv env) {
        SymbolEnv annotationEnv = createPkgLevelSymbolEnv(node, scope, env);
        annotationEnv.envCount = 0;
        annotationEnv.enclAnnotation = node;
        return annotationEnv;
    }

    public static SymbolEnv createServiceEnv(BLangService node, Scope scope, SymbolEnv env) {
        SymbolEnv serviceEnv = createPkgLevelSymbolEnv(node, scope, env);
        serviceEnv.envCount = 0;
        serviceEnv.enclService = node;
        return serviceEnv;
    }

    public static SymbolEnv createResourceActionSymbolEnv(BLangInvokableNode node, Scope scope, SymbolEnv env) {
        SymbolEnv symbolEnv = duplicate(node, scope, env);
        symbolEnv.envCount = 0;
        symbolEnv.enclInvokable = node;
        return symbolEnv;
    }

    public static SymbolEnv createArrowFunctionSymbolEnv(BLangArrowFunction node, SymbolEnv env) {
        SymbolEnv symbolEnv = cloneSymbolEnvForClosure(node, env);
        symbolEnv.enclEnv = env.enclEnv != null ? env.enclEnv.createClone() : null;
        symbolEnv.enclPkg = env.enclPkg;
        return symbolEnv;
    }

    public static SymbolEnv createTransactionEnv(BLangTransaction node, SymbolEnv env) {
        SymbolEnv symbolEnv = new SymbolEnv(node, new Scope(env.scope.owner));
        env.copyTo(symbolEnv);
        symbolEnv.envCount = env.envCount + 1;
        symbolEnv.enclEnv = env;
        symbolEnv.enclInvokable = env.enclInvokable;
        symbolEnv.node = node;
        symbolEnv.enclPkg = env.enclPkg;
        return symbolEnv;
    }

    public static SymbolEnv createRetryEnv(BLangRetry node, SymbolEnv env) {
        SymbolEnv symbolEnv = new SymbolEnv(node, new Scope(env.scope.owner));
        symbolEnv.enclEnv = env;
        symbolEnv.enclInvokable = env.enclInvokable;
        symbolEnv.node = node;
        symbolEnv.enclPkg = env.enclPkg;
        return symbolEnv;
    }

    public static SymbolEnv createOnFailEnv(BLangOnFailClause node, SymbolEnv env) {
        SymbolEnv symbolEnv = new SymbolEnv(node, new Scope(env.scope.owner));
        env.copyTo(symbolEnv);
        symbolEnv.envCount = env.envCount + 1;
        symbolEnv.enclEnv = env;
        symbolEnv.enclInvokable = env.enclInvokable;
        symbolEnv.node = node;
        symbolEnv.enclPkg = env.enclPkg;
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
        symbolEnv.envCount = env.envCount + 1;
        symbolEnv.relativeEnvCount = env.relativeEnvCount + 1;
        return symbolEnv;
    }

    public static SymbolEnv createFuncBodyEnv(BLangFunctionBody body, SymbolEnv env) {
        Scope scope = body.scope;
        if (scope == null) {
            scope = new Scope(env.scope.owner);
            body.scope = scope;
        }

        SymbolEnv symbolEnv = new SymbolEnv(body, scope);
        env.copyTo(symbolEnv);
        symbolEnv.envCount = env.envCount + 1;
        symbolEnv.relativeEnvCount = env.relativeEnvCount + 1;
        return symbolEnv;
    }

    public static SymbolEnv createExprEnv(BLangExpression expr, SymbolEnv env, BSymbol owner) {
        Scope scope = new Scope(owner);
        SymbolEnv symbolEnv = new SymbolEnv(expr, scope);
        env.copyTo(symbolEnv);
        return symbolEnv;
    }

    public static SymbolEnv createVarInitEnv(BLangVariable node, SymbolEnv env, BVarSymbol enclVarSym) {
        SymbolEnv symbolEnv = new SymbolEnv(node, env.scope);
        symbolEnv.envCount = 0;
        env.copyTo(symbolEnv);
        symbolEnv.enclVarSym = enclVarSym;
        return symbolEnv;
    }

    public static SymbolEnv createWorkerEnv(BLangWorker worker, SymbolEnv env) {
        SymbolEnv symbolEnv = new SymbolEnv(worker, worker.symbol.scope);
        symbolEnv.envCount = 0;
        env.copyTo(symbolEnv);
        return symbolEnv;
    }

    public static SymbolEnv createFolkJoinEnv(BLangForkJoin forkJoin, SymbolEnv env) {
        Scope scope = new Scope(env.scope.owner);
        SymbolEnv symbolEnv = new SymbolEnv(forkJoin, scope);
        symbolEnv.envCount = 0;
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
        symbolEnv.envCount = 0;
        env.copyTo(symbolEnv);
        return symbolEnv;
    }

    public static SymbolEnv createInvocationEnv(BLangNode node, SymbolEnv env) {

        Scope scope = new Scope(env.scope.owner);
        SymbolEnv symbolEnv = new SymbolEnv(node, scope);
        symbolEnv.envCount = 0;
        env.copyTo(symbolEnv);
        symbolEnv.typeParamsEntries = new ArrayList<>();
        return symbolEnv;
    }

    public static SymbolEnv createTypeNarrowedEnv(BLangNode node, SymbolEnv env) {
        Scope scope = new Scope(env.scope.owner);
        SymbolEnv symbolEnv = new SymbolEnv(node, scope);
        symbolEnv.envCount = 0;
        env.copyTo(symbolEnv);
        return symbolEnv;
    }

    public static SymbolEnv getXMLAttributeEnv(BLangXMLAttribute node, SymbolEnv env) {
        SymbolEnv symbolEnv = new SymbolEnv(node, env.scope);
        symbolEnv.envCount = 0;
        env.copyTo(symbolEnv);
        return symbolEnv;
    }

    public static SymbolEnv createStreamingInputEnv(BLangNode node, SymbolEnv env) {
        return createEnv(node, env);
    }

    public static SymbolEnv createLockEnv(BLangNode node, SymbolEnv env) {
        return createEnv(node, env);
    }

    private static SymbolEnv createEnv(BLangNode node, SymbolEnv env) {
        SymbolEnv symbolEnv = new SymbolEnv(node, new Scope(env.scope.owner));
        symbolEnv.envCount = 0;
        env.copyTo(symbolEnv);
        return symbolEnv;
    }

    public SymbolEnv createClone() {
        SymbolEnv symbolEnv = cloneSymbolEnvForClosure(node, this);
        this.copyTo(symbolEnv);
        symbolEnv.enclEnv = this.enclEnv != null ? this.enclEnv.createClone() : null;
        symbolEnv.enclPkg = this.enclPkg;
        symbolEnv.envCount = this.envCount;
        return symbolEnv;
    }

    public SymbolEnv shallowClone() {
        SymbolEnv symbolEnv = cloneSymbolEnvForClosure(node, this);
        this.copyTo(symbolEnv);
        symbolEnv.enclEnv = this.enclEnv;
        return symbolEnv;
    }

    private static SymbolEnv cloneSymbolEnvForClosure(BLangNode node, SymbolEnv env) {
        Scope scope = new Scope(env.scope.owner);
        env.scope.entries.entrySet().stream()
                // skip the type narrowed symbols when taking the snapshot for closures.
                .filter(entry -> (entry.getValue().symbol.tag & SymTag.VARIABLE) != SymTag.VARIABLE ||
                        ((BVarSymbol) entry.getValue().symbol).originalSymbol == null)
                .forEach(entry -> scope.entries.put(entry.getKey(), entry.getValue()));
        return new SymbolEnv(node, scope);
    }

    /**
     * Data holder for storing typeParams.
     *
     * @since JB 1.0.0
     */
    public static class TypeParamEntry {

        public BType typeParam;
        public BType boundType;

        public TypeParamEntry(BType typeParam, BType boundType) {

            this.typeParam = typeParam;
            this.boundType = boundType;
        }
    }

    // Private functions

    private static SymbolEnv duplicate(BLangNode node, Scope scope, SymbolEnv env) {
        SymbolEnv symbolEnv = new SymbolEnv(node, scope);
        env.copyTo(symbolEnv);
        return symbolEnv;
    }
}
