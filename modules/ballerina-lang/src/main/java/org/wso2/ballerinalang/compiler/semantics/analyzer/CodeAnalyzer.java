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

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.Diagnostic.Kind;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.util.BDiagnostic;
import org.wso2.ballerinalang.compiler.util.BDiagnosticSource;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

/**
 * This represents the code analyzing pass of semantic analysis. 
 * 
 * The following validations are done here:-
 * 
 * (*) Loop continuation statement validation.
 * (*) Function return path existence and unreachable code validation.
 */
public class CodeAnalyzer extends BLangNodeVisitor {
    
    private static final CompilerContext.Key<CodeAnalyzer> CODE_ANALYZER_KEY =
            new CompilerContext.Key<>();
    
    private DiagnosticListener diagListener;
    
    private int loopCount;
    
    private PackageID pkgId;
    
    private BLangCompilationUnit compUnitNode;
    
    private boolean statementReturns;
    
    private boolean unreachableCodeCheckDone;

    public static CodeAnalyzer getInstance(CompilerContext context) {
        CodeAnalyzer codeGenerator = context.get(CODE_ANALYZER_KEY);
        if (codeGenerator == null) {
            codeGenerator = new CodeAnalyzer(context);
        }
        return codeGenerator;
    }

    public CodeAnalyzer(CompilerContext context) {
        context.put(CODE_ANALYZER_KEY, this);
    }
    
    private void resetPackage() {
        this.pkgId = null;
        this.compUnitNode = null;
        this.loopCount = 0;
    }
    
    private void resetFunction() {
        this.unreachableCodeCheckDone = false;
        this.resetStatementReturns();
    }
    
    private void resetStatementReturns() {
        this.statementReturns = false;
    }
    
    public void analyze(BLangPackage pkgNode, DiagnosticListener diagListener) {
        this.resetPackage();
        this.diagListener = diagListener;
        pkgNode.accept(this);
    }
    
    @Override
    public void visit(BLangPackage pkgNode) {
        this.pkgId = pkgNode.pkgDecl.pkgId;
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
    }
    
    private void checkUnreachableCode(BLangStatement stmt) {
        if (this.statementReturns && !this.unreachableCodeCheckDone) {
            this.diagListener.received(this.generateUnreachableCodeDiagnostic(stmt));
            /* to make sure we don't give the same error again to following statements */
            this.unreachableCodeCheckDone = true;
        }
    }
    
    @Override
    public void visit(BLangBlockStmt blockNode) {
        blockNode.statements.forEach(e -> {
            this.checkUnreachableCode(blockNode);
            e.accept(this);
        });
    }
    
    @Override
    public void visit(BLangReturn returnStmt) {
        this.statementReturns = true;
    }
    
    @Override
    public void visit(BLangIf ifStmt) {
        this.checkUnreachableCode(ifStmt);
        ifStmt.body.accept(this);
        if (ifStmt.elseStmt != null) {
            boolean ifStatementReturns = this.statementReturns;
            this.resetStatementReturns();
            ifStmt.elseStmt.accept(this);
            this.statementReturns = ifStatementReturns && this.statementReturns;
        }
    }
        
    @Override
    public void visit(BLangWhile whileNode) {
        this.checkUnreachableCode(whileNode);
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
    
    private Diagnostic generateInvalidContinueDiagnostic(BLangContinue continueNode) {
        BDiagnostic diag = new BDiagnostic();
        diag.source = new BDiagnosticSource(this.pkgId.name.value, this.pkgId.version.value, this.compUnitNode.name);
        diag.kind = Kind.ERROR;
        diag.pos = continueNode.pos;
        diag.msg = "next cannot be used outside of a loop";
        return diag;
    }
    
    private Diagnostic generateUnreachableCodeDiagnostic(BLangStatement stmt) {
        BDiagnostic diag = new BDiagnostic();
        diag.source = new BDiagnosticSource(this.pkgId.name.value, this.pkgId.version.value, this.compUnitNode.name);
        diag.kind = Kind.ERROR;
        diag.pos = stmt.pos;
        diag.msg = "Unreachable code";
        return diag;
    }
    
    private Diagnostic generateFunctionMustReturn(BLangFunction funcNode) {
        BDiagnostic diag = new BDiagnostic();
        diag.source = new BDiagnosticSource(this.pkgId.name.value, this.pkgId.version.value, this.compUnitNode.name);
        diag.kind = Kind.ERROR;
        diag.pos = funcNode.pos;
        diag.msg = "This function must return a result of type " + funcNode.getReturnParameters();
        return diag;
    }

}
