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

import org.ballerinalang.model.tree.expressions.LiteralNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.ballerinalang.util.diagnostic.Diagnostic.Kind;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.NodeUtils;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnostic;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnosticSource;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticLog;

/**
 * This represents the code analyzing pass of semantic analysis. 
 * 
 * The following validations are done here:-
 * 
 * (*) Loop continuation statement validation.
 * (*) Function return path existence and unreachable code validation.
 * (*) Dead code detection.
 * (*) Worker send/receive validation.
 */
public class CodeAnalyzer extends BLangNodeVisitor {
    
    private static final CompilerContext.Key<CodeAnalyzer> CODE_ANALYZER_KEY =
            new CompilerContext.Key<>();
    
    private DiagnosticListener diagListener;
    private Names names;
    private int loopCount;
    private Name pkgName;
    private Name pkgVerion;
    private BLangCompilationUnit compUnitNode;
    private boolean statementReturns;
    private boolean deadCode;
    private boolean unreachableCodeCheckDone;
    private DiagnosticLog dlog;
    private SymbolResolver symResolver;
    private SymbolTable symTable;

    public static CodeAnalyzer getInstance(CompilerContext context) {
        CodeAnalyzer codeGenerator = context.get(CODE_ANALYZER_KEY);
        if (codeGenerator == null) {
            codeGenerator = new CodeAnalyzer(context);
        }
        return codeGenerator;
    }

    public CodeAnalyzer(CompilerContext context) {
        context.put(CODE_ANALYZER_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.names = Names.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.dlog = DiagnosticLog.getInstance(context);
    }
    
    private void resetPackage() {
        this.pkgName = null;
        this.pkgVerion = null;
        this.compUnitNode = null;
        this.deadCode = false;
    }
    
    private void resetFunction() {
        this.unreachableCodeCheckDone = false;
        this.resetStatementReturns();
    }
    
    private void resetStatementReturns() {
        this.statementReturns = false;
    }
    
    public BLangPackage analyze(BLangPackage pkgNode) {
        this.resetPackage();
        pkgNode.accept(this);
        return pkgNode;
    }
    
    @Override
    public void visit(BLangPackage pkgNode) {
        this.pkgName = NodeUtils.getName(names, pkgNode.pkgDecl.pkgNameComps);
        pkgNode.compUnits.forEach(e -> e.accept(this));
    }
    
    @Override
    public void visit(BLangCompilationUnit compUnitNode) {
        this.compUnitNode = compUnitNode;
        compUnitNode.topLevelNodes.forEach(e -> ((BLangNode) e).accept(this));
    }
    
    @Override
    public void visit(BLangFunction funcNode) {
        this.resetFunction();
        boolean functionReturns = funcNode.retParams.size() > 0;
        funcNode.body.accept(this);
        /* the function returns, but none of the statements surely returns */
        if (functionReturns && !this.statementReturns) {
            this.diagListener.received(this.generateFunctionMustReturn(funcNode));
        }
        funcNode.workers.forEach(e -> e.accept(this));
    }
    
    @Override
    public void visit(BLangWorker worker) {
        worker.body.accept(this);
    }
    
    private void checkUnreachableCode(BLangStatement stmt) {
        if (this.statementReturns && !this.unreachableCodeCheckDone) {
            this.diagListener.received(this.generateUnreachableCodeDiagnostic(stmt));
            /* to make sure we don't give the same error again to following statements */
            this.unreachableCodeCheckDone = true;
        }
    }
    
    private void checkDeadCode(BLangStatement stmt) {
        if (this.deadCode) {
            this.diagListener.received(this.generateDeadCodeDiagnostic(stmt));
        }
    }
    
    private void checkStatementExecutionValidity(BLangStatement stmt) {
        this.checkUnreachableCode(stmt);
        this.checkDeadCode(stmt);
    }
    
    @Override
    public void visit(BLangBlockStmt blockNode) {
        blockNode.statements.forEach(e -> {
            this.checkStatementExecutionValidity(blockNode);
            e.accept(this);
        });
    }
    
    @Override
    public void visit(BLangReturn returnStmt) {
        this.statementReturns = true;
    }
    
    @Override
    public void visit(BLangIf ifStmt) {
        boolean originalDeadCode = this.deadCode;
        this.checkStatementExecutionValidity(ifStmt);
        boolean ifTrue = this.isBooleanTrue(ifStmt.expr);
        boolean ifFalse = this.isBooleanFalse(ifStmt.expr);
        boolean extendedDeadCode = false;
        if (ifFalse) {
            this.deadCode = true;
        }
        ifStmt.body.accept(this);
        if (ifTrue && this.statementReturns) {
            extendedDeadCode = true;
        }
        if (ifStmt.elseStmt != null) {
            if (ifTrue) {
                this.deadCode = true;
            }
            boolean ifStatementReturns = this.statementReturns;
            this.resetStatementReturns();
            ifStmt.elseStmt.accept(this);
            if (ifFalse && this.statementReturns) {
                extendedDeadCode = true;
            }
            this.statementReturns = ifStatementReturns && this.statementReturns;
            /* if the whole if/else returns for sure, the following is not dead code,
             * but rather unreachable code */
            if (this.statementReturns) {
                extendedDeadCode = false;
            }
        }
        /* this is the case where if (true) return or if (false) else return,
         * where after the if/else, the following statements are also dead code */
        if (extendedDeadCode) {
            this.deadCode = true;
        } else {
            this.deadCode = originalDeadCode;
        }
    }
    
    private boolean isBooleanTrue(BLangExpression expr) {
        return (expr instanceof LiteralNode) && ((LiteralNode) expr).getValue().equals(Boolean.TRUE);
    }
    
    private boolean isBooleanFalse(BLangExpression expr) {
        return (expr instanceof LiteralNode) && ((LiteralNode) expr).getValue().equals(Boolean.FALSE);
    }
        
    @Override
    public void visit(BLangWhile whileNode) {
        this.checkStatementExecutionValidity(whileNode);
        this.loopCount++;
        whileNode.body.statements.forEach(e -> e.accept(this));
        this.loopCount--;
    }
    
    @Override
    public void visit(BLangContinue continueNode) {
        if (this.loopCount == 0) {
            this.diagListener.received(this.generateInvalidContinueDiagnostic(continueNode));
        }
    }
    
    private boolean workerExists(SymbolEnv env, String workerName) {
        BSymbol symbol = this.symResolver.lookupSymbol(env, new Name(workerName), SymTag.WORKER);
        return (symbol != this.symTable.notFoundSymbol);
    }
    
    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        String workerName = workerSendNode.workerIdentifier.getValue();
        if (!this.workerExists(workerSendNode.env, workerName)) {
            this.dlog.error(workerSendNode.pos, DiagnosticCode.UNDEFINED_WORKER, workerName);
        }
    }
    
    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        String workerName = workerReceiveNode.workerIdentifier.getValue();
        if (!this.workerExists(workerReceiveNode.env, workerName)) {
            this.dlog.error(workerReceiveNode.pos, DiagnosticCode.UNDEFINED_WORKER, workerName);
        }
    }
    
    private Diagnostic generateInvalidContinueDiagnostic(BLangContinue continueNode) {
        BDiagnostic diag = new BDiagnostic();
        diag.source = new BDiagnosticSource(this.pkgName.value, this.pkgVerion.value, this.compUnitNode.name);
        diag.kind = Kind.ERROR;
        diag.pos = continueNode.pos;
        diag.msg = "next cannot be used outside of a loop";
        return diag;
    }
    
    private Diagnostic generateUnreachableCodeDiagnostic(BLangStatement stmt) {
        BDiagnostic diag = new BDiagnostic();
        diag.source = new BDiagnosticSource(this.pkgName.value, this.pkgVerion.value, this.compUnitNode.name);
        diag.kind = Kind.ERROR;
        diag.pos = stmt.pos;
        diag.msg = "Unreachable code";
        return diag;
    }
    
    private Diagnostic generateDeadCodeDiagnostic(BLangStatement stmt) {
        BDiagnostic diag = new BDiagnostic();
        diag.source = new BDiagnosticSource(this.pkgName.value, this.pkgVerion.value, this.compUnitNode.name);
        diag.kind = Kind.WARNING;
        diag.pos = stmt.pos;
        diag.msg = "Dead code";
        return diag;
    }
    
    private Diagnostic generateFunctionMustReturn(BLangFunction funcNode) {
        BDiagnostic diag = new BDiagnostic();
        diag.source = new BDiagnosticSource(this.pkgName.value, this.pkgVerion.value, this.compUnitNode.name);
        diag.kind = Kind.ERROR;
        diag.pos = funcNode.pos;
        diag.msg = "This function must return a result of type " + funcNode.getReturnParameters();
        return diag;
    }

}
