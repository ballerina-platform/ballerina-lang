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
package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.ballerinalang.model.tree.types.BuiltInReferenceTypeNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransform;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticLog;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @since 0.94
 */
public class SemanticAnalyzer extends BLangNodeVisitor {

    private static final CompilerContext.Key<SemanticAnalyzer> SYMBOL_ANALYZER_KEY =
            new CompilerContext.Key<>();

    private SymbolTable symTable;
    private SymbolEnter symbolEnter;
    private Names names;
    private SymbolResolver symResolver;
    private TypeChecker typeChecker;
    private Types types;
    private DiagnosticLog dlog;

    private SymbolEnv env;
    private BType expType;
    private DiagnosticCode diagCode;
    private BType resType;

    public static SemanticAnalyzer getInstance(CompilerContext context) {
        SemanticAnalyzer semAnalyzer = context.get(SYMBOL_ANALYZER_KEY);
        if (semAnalyzer == null) {
            semAnalyzer = new SemanticAnalyzer(context);
        }

        return semAnalyzer;
    }

    public SemanticAnalyzer(CompilerContext context) {
        context.put(SYMBOL_ANALYZER_KEY, this);

        this.symTable = SymbolTable.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
        this.names = Names.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.typeChecker = TypeChecker.getInstance(context);
        this.types = Types.getInstance(context);
        this.dlog = DiagnosticLog.getInstance(context);
    }

    public BLangPackage analyze(BLangPackage pkgNode) {
        pkgNode.accept(this);
        return pkgNode;
    }


    // Visitor methods

    public void visit(BLangPackage pkgNode) {
        SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);

        // Visit all the imported packages
        pkgNode.imports.forEach(importNode -> analyzeDef(importNode, pkgEnv));

        // Then visit each top-level element sorted using the compilation unit
        pkgNode.topLevelNodes.forEach(topLevelNode -> analyzeDef((BLangNode) topLevelNode, pkgEnv));
    }

    public void visit(BLangImportPackage importPkgNode) {
        BPackageSymbol pkgSymbol = importPkgNode.symbol;
        SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgSymbol);
        analyzeDef(pkgEnv.node, pkgEnv);
    }

    public void visit(BLangXMLNS xmlnsNode) {
        throw new AssertionError();
    }

    public void visit(BLangFunction funcNode) {
        // Check for native functions
        if (Symbols.isNative(funcNode.symbol)) {
            return;
        }

        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, env);
        analyzeStmt(funcNode.body, funcEnv);

        // Process workers
        funcNode.workers.forEach(e -> this.symbolEnter.defineNode(e, funcEnv));
        funcNode.workers.forEach(e -> analyzeNode(e, funcEnv));
    }

    public void visit(BLangStruct structNode) {
        BSymbol structSymbol = structNode.symbol;
        SymbolEnv structEnv = SymbolEnv.createPkgLevelSymbolEnv(structNode, structSymbol.scope, env);
        structNode.fields.forEach(field -> analyzeDef(field, structEnv));
    }

    public void visit(BLangAnnotation annotationNode) {
    }

    public void visit(BLangVariable varNode) {
        int ownerSymTag = env.scope.owner.tag;
        if ((ownerSymTag & SymTag.INVOKABLE) == SymTag.INVOKABLE) {
            // This is a variable declared in a function, an action or a resource
            // If the variable is parameter then the variable symbol is already defined
            if (varNode.symbol == null) {
                symbolEnter.defineNode(varNode, env);
            }
        }

        // Analyze the init expression
        if (varNode.expr != null) {
            // Here we create a new symbol environment to catch self references by keep the current
            // variable symbol in the symbol environment
            // e.g. int a = x + a;
            SymbolEnv varInitEnv = SymbolEnv.createVarInitEnv(varNode, env, varNode.symbol);
            typeChecker.checkExpr(varNode.expr, varInitEnv, Lists.of(varNode.symbol.type));
        }
        varNode.type = varNode.symbol.type;
    }


    // Statements

    public void visit(BLangBlockStmt blockNode) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, env);
        blockNode.stmts.forEach(stmt -> analyzeStmt(stmt, blockEnv));
    }

    public void visit(BLangVariableDef varDefNode) {
        analyzeDef(varDefNode.var, env);
    }

    public void visit(BLangAssignment assignNode) {
        if (assignNode.isDeclaredWithVar()) {
            handleAssignNodeWithVar(assignNode);
            return;
        }
        List<BType> expTypes = new ArrayList<>();
        // Check each LHS expression.
        for (int i = 0; i < assignNode.varRefs.size(); i++) {
            BLangExpression varRef = assignNode.varRefs.get(i);
            // In assignment, lhs supports only simpleVarRef, indexBasedAccess, filedBasedAccess only.
            if (varRef.getKind() == NodeKind.INVOCATION) {
                dlog.error(varRef.pos, DiagnosticCode.INVALID_VARIABLE_ASSIGNMENT, varRef);
                expTypes.add(symTable.errType);
                continue;
            }
            ((BLangVariableReference) varRef).lhsVar = true;
            expTypes.add(typeChecker.checkExpr(varRef, env).get(0));
        }
        typeChecker.checkExpr(assignNode.expr, this.env, expTypes);
    }

    public void visit(BLangExpressionStmt exprStmtNode) {
        // Creates a new environment here.
        SymbolEnv stmtEnv = new SymbolEnv(exprStmtNode, this.env.scope);
        this.env.copyTo(stmtEnv);
        typeChecker.checkExpr(exprStmtNode.expr, stmtEnv, new ArrayList<>());
    }

    public void visit(BLangIf ifNode) {
        typeChecker.checkExpr(ifNode.expr, env, Lists.of(symTable.booleanType));
        analyzeStmt(ifNode.body, env);

        if (ifNode.elseStmt != null) {
            analyzeStmt(ifNode.elseStmt, env);
        }
    }

    public void visit(BLangWhile whileNode) {
        typeChecker.checkExpr(whileNode.expr, env, Lists.of(symTable.booleanType));
        analyzeStmt(whileNode.body, env);
    }

    public void visit(BLangTransform transformNode) {
        analyzeStmt(transformNode.body, env);
    }

    public void visit(BLangConnector connectorNode) {
    }

    public void visit(BLangService serviceNode) {
        BSymbol serviceSymbol = serviceNode.symbol;
        SymbolEnv serviceEnv = SymbolEnv.createPkgLevelSymbolEnv(serviceNode, serviceSymbol.scope, env);
        serviceNode.vars.forEach(v -> this.analyzeDef(v, serviceEnv));
        serviceNode.annAttachments.forEach(a -> this.analyzeDef(a, serviceEnv));
        serviceNode.resources.forEach(r -> this.analyzeDef(r, serviceEnv));
    }

    public void visit(BLangResource resourceNode) {
        BSymbol resourceSymbol = resourceNode.symbol;
        SymbolEnv resourceEnv = SymbolEnv.createResourceActionSymbolEnv(resourceNode, resourceSymbol.scope, env);
        resourceNode.annAttachments.forEach(a -> this.analyzeDef(a, resourceEnv));
        resourceNode.params.forEach(p -> this.analyzeDef(p, resourceEnv));
        resourceNode.workers.forEach(w -> this.analyzeDef(w, resourceEnv));
        analyzeStmt(resourceNode.body, resourceEnv);
    }

    public void visit(BLangTryCatchFinally tryCatchFinally) {
        analyzeStmt(tryCatchFinally.tryBody, env);
        tryCatchFinally.catchBlocks.forEach(c -> analyzeNode(c, env));
        if (tryCatchFinally.finallyBody != null) {
            analyzeStmt(tryCatchFinally.finallyBody, env);
        }
    }

    public void visit(BLangCatch bLangCatch) {
        SymbolEnv catchBlockEnv = SymbolEnv.createBlockEnv(bLangCatch.body, env);
        analyzeNode(bLangCatch.param, catchBlockEnv);
        this.types.checkType(bLangCatch.param.pos, bLangCatch.param.type, symTable.errStructType,
                DiagnosticCode.INCOMPATIBLE_TYPES);
        analyzeStmt(bLangCatch.body, catchBlockEnv);
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        analyzeStmt(transactionNode.transactionBody, env);
        if (transactionNode.failedBody != null) {
            analyzeStmt(transactionNode.failedBody, env);
        }
        if (transactionNode.committedBody != null) {
            analyzeStmt(transactionNode.committedBody, env);
        }
        if (transactionNode.abortedBody != null) {
            analyzeStmt(transactionNode.abortedBody, env);
        }
        if (transactionNode.retryCount != null) {
            typeChecker.checkExpr(transactionNode.retryCount, env, Lists.of(symTable.intType));
        }
    }

    @Override
    public void visit(BLangAbort abortNode) {
    }

    @Override
    public void visit(BLangRetry retryNode) {
    }

    private boolean isJoinResultType(BLangVariable var) {
        BLangType type = var.typeNode;
        if (type instanceof BuiltInReferenceTypeNode) {
            return ((BuiltInReferenceTypeNode) type).getTypeKind() == TypeKind.MAP;
        }
        return false;
    }

    private BLangVariableDef createVarDef(BLangVariable var) {
        BLangVariableDef varDefNode = new BLangVariableDef();
        varDefNode.var = var;
        varDefNode.pos = var.pos;
        return varDefNode;
    }

    private BLangBlockStmt generateCodeBlock(StatementNode... statements) {
        BLangBlockStmt block = new BLangBlockStmt();
        for (StatementNode stmt : statements) {
            block.addStatement(stmt);
        }
        return block;
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        SymbolEnv folkJoinEnv = SymbolEnv.createFolkJoinEnv(forkJoin, this.env);
        forkJoin.workers.forEach(e -> this.symbolEnter.defineNode(e, folkJoinEnv));
        forkJoin.workers.forEach(e -> this.analyzeDef(e, folkJoinEnv));
        if (!this.isJoinResultType(forkJoin.joinResultVar)) {
            this.dlog.error(forkJoin.joinResultVar.pos, DiagnosticCode.INVALID_WORKER_JOIN_RESULT_TYPE);
        }
        /* create code black and environment for join result section, i.e. (map results) */
        BLangBlockStmt joinResultsBlock = this.generateCodeBlock(this.createVarDef(forkJoin.joinResultVar));
        SymbolEnv joinResultsEnv = SymbolEnv.createBlockEnv(joinResultsBlock, this.env);
        this.analyzeNode(joinResultsBlock, joinResultsEnv);
        /* create an environment for the join body, making the enclosing environment the earlier 
         * join result's environment */
        SymbolEnv joinBodyEnv = SymbolEnv.createBlockEnv(forkJoin.joinedBody, joinResultsEnv);
        this.analyzeNode(forkJoin.joinedBody, joinBodyEnv);

        if (forkJoin.timeoutExpression != null) {
            /* create code black and environment for timeout section */
            BLangBlockStmt timeoutVarBlock = this.generateCodeBlock(this.createVarDef(forkJoin.timeoutVariable));
            SymbolEnv timeoutVarEnv = SymbolEnv.createBlockEnv(timeoutVarBlock, this.env);
            this.typeChecker.checkExpr(forkJoin.timeoutExpression,
                    timeoutVarEnv, Arrays.asList(symTable.intType));
            this.analyzeNode(timeoutVarBlock, timeoutVarEnv);
            /* create an environment for the timeout body, making the enclosing environment the earlier 
             * timeout var's environment */
            SymbolEnv timeoutBodyEnv = SymbolEnv.createBlockEnv(forkJoin.timeoutBody, timeoutVarEnv);
            this.analyzeNode(forkJoin.timeoutBody, timeoutBodyEnv);
        }
    }

    @Override
    public void visit(BLangWorker workerNode) {
        SymbolEnv workerEnv = SymbolEnv.createWorkerEnv(workerNode, this.env);
        this.analyzeNode(workerNode.body, workerEnv);
    }

    private boolean isInTopLevelWorkerEnv() {
        return this.env.enclEnv.node.getKind() == NodeKind.WORKER;
    }

    private boolean workerExists(SymbolEnv env, String workerName) {
        BSymbol symbol = this.symResolver.lookupSymbol(env, new Name(workerName), SymTag.WORKER);
        return (symbol != this.symTable.notFoundSymbol);
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        workerSendNode.exprs.forEach(e -> this.typeChecker.checkExpr(e, this.env));
        if (!this.isInTopLevelWorkerEnv()) {
            this.dlog.error(workerSendNode.pos, DiagnosticCode.INVALID_WORKER_SEND_POSITION);
        }
        if (!workerSendNode.isForkJoinSend) {
            String workerName = workerSendNode.workerIdentifier.getValue();
            if (!this.workerExists(this.env, workerName)) {
                this.dlog.error(workerSendNode.pos, DiagnosticCode.UNDEFINED_WORKER, workerName);
            }
        }
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        workerReceiveNode.exprs.forEach(e -> this.typeChecker.checkExpr(e, this.env));
        if (!this.isInTopLevelWorkerEnv()) {
            this.dlog.error(workerReceiveNode.pos, DiagnosticCode.INVALID_WORKER_RECEIVE_POSITION);
        }
        String workerName = workerReceiveNode.workerIdentifier.getValue();
        if (!this.workerExists(this.env, workerName)) {
            this.dlog.error(workerReceiveNode.pos, DiagnosticCode.UNDEFINED_WORKER, workerName);
        }
    }

    private boolean checkReturnValueCounts(BLangReturn returnNode) {
        boolean success = false;
        int expRetCount = this.env.enclInvokable.getReturnParameters().size();
        int actualRetCount = returnNode.exprs.size();
        if (expRetCount > 1 && actualRetCount <= 1) {
            this.dlog.error(returnNode.pos, DiagnosticCode.MULTI_VALUE_RETURN_EXPECTED);
        } else if (expRetCount == 1 && actualRetCount > 1) {
            this.dlog.error(returnNode.pos, DiagnosticCode.SINGLE_VALUE_RETURN_EXPECTED);
        } else if (expRetCount == 0 && actualRetCount >= 1) {
            this.dlog.error(returnNode.pos, DiagnosticCode.RETURN_VALUE_NOT_EXPECTED);
        } else if (expRetCount > actualRetCount) {
            this.dlog.error(returnNode.pos, DiagnosticCode.NOT_ENOUGH_RETURN_VALUES);
        } else if (expRetCount < actualRetCount) {
            this.dlog.error(returnNode.pos, DiagnosticCode.TOO_MANY_RETURN_VALUES);
        } else {
            success = true;
        }
        return success;
    }

    private boolean isInvocationExpr(BLangExpression expr) {
        return expr.getKind() == NodeKind.INVOCATION;
    }

    @Override
    public void visit(BLangReturn returnNode) {
        if (returnNode.exprs.size() == 1 && this.isInvocationExpr(returnNode.exprs.get(0))) {
            /* a single return expression can be expanded to match a multi-value return */
            this.typeChecker.checkExpr(returnNode.exprs.get(0), this.env,
                    this.env.enclInvokable.getReturnParameters().stream()
                            .map(e -> e.getTypeNode().type)
                            .collect(Collectors.toList()));
        } else {
            if (returnNode.exprs.size() == 0 && this.env.enclInvokable.getReturnParameters().size() > 0
                    && !this.env.enclInvokable.getReturnParameters().get(0).name.value.isEmpty()) {
                // Return stmt has no expressions, but function/action has returns and they are named returns.
                // Rewrite tree at desuger phase.
                returnNode.namedReturnVariables = this.env.enclInvokable.getReturnParameters();
                return;
            }
            if (this.checkReturnValueCounts(returnNode)) {
                for (int i = 0; i < returnNode.exprs.size(); i++) {
                    this.typeChecker.checkExpr(returnNode.exprs.get(i), this.env,
                            Arrays.asList(this.env.enclInvokable.getReturnParameters().get(i).getTypeNode().type));
                }
            }
        }
    }

    BType analyzeDef(BLangNode node, SymbolEnv env) {
        return analyzeNode(node, env);
    }

    BType analyzeStmt(BLangStatement stmtNode, SymbolEnv env) {
        return analyzeNode(stmtNode, env);
    }

    BType analyzeNode(BLangNode node, SymbolEnv env) {
        return analyzeNode(node, env, symTable.noType, null);
    }

    public void visit(BLangContinue continueNode) {
        /* ignore */
    }

    public void visit(BLangBreak breakNode) {
        /* ignore */
    }

    BType analyzeNode(BLangNode node, SymbolEnv env, BType expType, DiagnosticCode diagCode) {
        SymbolEnv prevEnv = this.env;
        BType preExpType = this.expType;
        DiagnosticCode preDiagCode = this.diagCode;

        // TODO Check the possibility of using a try/finally here
        this.env = env;
        this.expType = expType;
        this.diagCode = diagCode;
        node.accept(this);
        this.env = prevEnv;
        this.expType = preExpType;
        this.diagCode = preDiagCode;

        return resType;
    }

    // Private methods

    private void handleAssignNodeWithVar(BLangAssignment assignNode) {
        int ignoredCount = 0;
        int createdSymbolCount = 0;

        Map<Integer, BLangSimpleVarRef> newVariablesMap = new HashMap<>();

        List<BType> expTypes = new ArrayList<>();
        // Check each LHS expression.
        for (int i = 0; i < assignNode.varRefs.size(); i++) {
            BLangExpression varRef = assignNode.varRefs.get(i);
            // If the assignment is declared with "var", then lhs supports only simpleVarRef expressions only.
            if (varRef.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
                dlog.error(varRef.pos, DiagnosticCode.INVALID_VARIABLE_ASSIGNMENT, varRef);
                expTypes.add(symTable.errType);
                continue;
            }
            ((BLangVariableReference) varRef).lhsVar = true;
            // Check variable symbol if exists.
            BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) varRef;
            Name varName = names.fromIdNode(simpleVarRef.variableName);
            if (varName == Names.IGNORE) {
                ignoredCount++;
                simpleVarRef.type = this.symTable.noType;
                expTypes.add(symTable.noType);
                continue;
            }
            BSymbol symbol = symResolver.lookupSymbol(env, varName, SymTag.VARIABLE);
            if (symbol == symTable.notFoundSymbol) {
                createdSymbolCount++;
                newVariablesMap.put(i, simpleVarRef);
                expTypes.add(symTable.noType);
            } else {
                expTypes.add(symbol.type);
            }
        }

        if (ignoredCount == assignNode.varRefs.size() || createdSymbolCount == 0) {
            dlog.error(assignNode.pos, DiagnosticCode.NO_NEW_VARIABLES_VAR_ASSIGNMENT);
        }
        // Check RHS expressions with expected type list.
        final List<BType> rhsTypes = typeChecker.checkExpr(assignNode.expr, this.env, expTypes);

        // define new variables
        newVariablesMap.keySet().forEach(i -> {
            BType actualType = rhsTypes.get(i);
            BLangSimpleVarRef simpleVarRef = newVariablesMap.get(i);
            Name varName = names.fromIdNode(simpleVarRef.variableName);
            this.symbolEnter.defineVarSymbol(simpleVarRef.pos, Collections.emptySet(), actualType, varName, env);
            typeChecker.checkExpr(simpleVarRef, env);
        });
    }
}
