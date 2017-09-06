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
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.util.BDiagnostic;
import org.wso2.ballerinalang.compiler.util.BDiagnosticSource;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

/**
 * This represents the code analyzing pass of semantic analysis. 
 */
public class CodeAnalyzer extends BLangNodeVisitor {
    
    private static final CompilerContext.Key<CodeAnalyzer> CODE_ANALYZER_KEY =
            new CompilerContext.Key<>();
    
    private DiagnosticListener diagListener;
    
    private int loopCount;
    
    private PackageID pkgId;
    
    private BLangCompilationUnit compUnitNode;

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
    
    private void reset() {
        this.pkgId = null;
        this.compUnitNode = null;
        this.loopCount = 0;
    }
    
    public void analyze(BLangPackage pkgNode, DiagnosticListener diagListener) {
        this.reset();
        this.diagListener = diagListener;
        pkgNode.accept(this);
    }
    
    public void visit(BLangPackage pkgNode) {
        this.pkgId = pkgNode.pkgDecl.pkgId;
        pkgNode.compUnits.forEach(e -> e.accept(this));
    }
    
    public void visit(BLangCompilationUnit compUnitNode) {
        this.compUnitNode = compUnitNode;
        compUnitNode.topLevelNodes.forEach(e -> ((BLangNode) e).accept(this));
    }
    
    public void visit(BLangFunction funcNode) {
        funcNode.body.accept(this);
    }
    
    public void visit(BLangBlockStmt blockNode) {
        blockNode.statements.forEach(e -> e.accept(this));
    }
    
    public void visit(BLangWhile whileNode) {
        this.loopCount++;
        whileNode.body.statements.forEach(e -> e.accept(this));
        this.loopCount--;
    }
    
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
        diag.msg = "Invalid 'next' statement";
        return diag;
    }

}
