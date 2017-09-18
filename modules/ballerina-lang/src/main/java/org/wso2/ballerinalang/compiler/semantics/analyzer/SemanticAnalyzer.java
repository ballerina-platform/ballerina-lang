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
import org.ballerinalang.model.tree.types.TypeNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
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
import java.util.List;
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
        BSymbol funcSymbol = funcNode.symbol;
        if (Symbols.isNative(funcSymbol)) {
            return;
        }
        SymbolEnv funcEnv = SymbolEnv.createPkgLevelSymbolEnv(funcNode, env, funcSymbol.scope);
        analyzeStmt(funcNode.body, funcEnv);
        // Process workers
        funcNode.workers.forEach(e -> this.symbolEnter.defineNode(e, funcEnv));
        funcNode.workers.forEach(e -> analyzeNode(e, funcEnv));
    }

    public void visit(BLangStruct structNode) {
        BSymbol structSymbol = structNode.symbol;
        SymbolEnv structEnv = SymbolEnv.createPkgLevelSymbolEnv(structNode, env, structSymbol.scope);
        structNode.fields.forEach(field -> analyzeDef(field, structEnv));
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
        blockNode.statements.forEach(stmt -> analyzeStmt(stmt, blockEnv));
    }

    public void visit(BLangVariableDef varDefNode) {
        analyzeDef(varDefNode.var, env);
    }

    public void visit(BLangAssignment assignNode) {
        // TODO This is a temporary workaround
        if (assignNode.declaredWithVar) {
            List<BType> expTypes = assignNode.varRefs.stream()
                    .map(varRef -> symTable.noType)
                    .collect(Collectors.toList());

            typeChecker.checkExpr(assignNode.expr, this.env, expTypes);
        }
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

    public void visit(BLangConnector connectorNode) {
    }

    public void visit(BLangService serviceNode) {
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
        this.typeChecker.checkNodeType(bLangCatch.param, symTable.errStructType, DiagnosticCode.INCOMPATIBLE_TYPES);
        analyzeStmt(bLangCatch.body, catchBlockEnv);
    }
    
    private boolean isJoinResultType(TypeNode type) {
        if (type instanceof BuiltInReferenceTypeNode) {
            return ((BuiltInReferenceTypeNode) type).getTypeKind() == TypeKind.MAP;
        }
        return false;
    }
    
    private BLangVariableDef createVarDef(BLangType type, BLangIdentifier name) {
        BLangVariable var = new BLangVariable();
        var.setTypeNode(type);
        var.setName(name);
        var.pos = type.pos;
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
        if (!this.isJoinResultType(forkJoin.joinResultsType)) {
            this.dlog.error(forkJoin.joinResultsType.pos, DiagnosticCode.INVALID_WORKER_JOIN_RESULT_TYPE);
        }
        /* create code black and environment for join result section, i.e. (map results) */
        BLangBlockStmt joinResultsBlock = this.generateCodeBlock(
                this.createVarDef(forkJoin.joinResultsType, forkJoin.joinResultsName));
        SymbolEnv joinResultsEnv = SymbolEnv.createBlockEnv(joinResultsBlock, this.env);
        this.analyzeNode(joinResultsBlock, joinResultsEnv);
        /* create an environment for the join body, making the enclosing environment the earlier 
         * join result's environment */
        SymbolEnv joinBodyEnv = SymbolEnv.createBlockEnv(forkJoin.joinedBody, joinResultsEnv);
        this.analyzeNode(forkJoin.joinedBody, joinBodyEnv);
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
        if (!this.isInTopLevelWorkerEnv()) {
            this.dlog.error(workerSendNode.pos, DiagnosticCode.INVALID_WORKER_SEND_POSITION);
        }
        String workerName = workerSendNode.workerIdentifier.getValue();
        if (!this.workerExists(this.env, workerName)) {
            this.dlog.error(workerSendNode.pos, DiagnosticCode.UNDEFINED_WORKER, workerName);
        }
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        if (!this.isInTopLevelWorkerEnv()) {
            this.dlog.error(workerReceiveNode.pos, DiagnosticCode.INVALID_WORKER_RECEIVE_POSITION);
        }
        String workerName = workerReceiveNode.workerIdentifier.getValue();
        if (!this.workerExists(this.env, workerName)) {
            this.dlog.error(workerReceiveNode.pos, DiagnosticCode.UNDEFINED_WORKER, workerName);
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
}
