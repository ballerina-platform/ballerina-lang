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
package org.wso2.ballerinalang.compiler;

import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.Name;
import org.ballerinalang.model.tree.PackageNode;
import org.wso2.ballerinalang.compiler.codegen.CodeGenerator;
import org.wso2.ballerinalang.compiler.desugar.Desugar;
import org.wso2.ballerinalang.compiler.parser.BLangParserException;
import org.wso2.ballerinalang.compiler.semantics.analyzer.CodeAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SemanticAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticLog;
import org.wso2.ballerinalang.programfile.ProgramFile;

/**
 * @since 0.94
 */
public class Compiler {

    private static final CompilerContext.Key<Compiler> COMPILER_KEY =
            new CompilerContext.Key<>();

    private CompilerOptions options;
    private DiagnosticLog dlog;
    private PackageLoader pkgLoader;
    private SymbolTable symbolTable;
    private SemanticAnalyzer semAnalyzer;
    private CodeAnalyzer codeAnalyzer;
    private Desugar desugar;
    private CodeGenerator codeGenerator;

    private CompilerPhase compilerPhase;
    private ProgramFile programFile;
    private BLangPackage pkgNode;

    // TODO: separate the 'parse' and 'define' phases to properly fix this
    private boolean containsSyntaxErrors = false;

    public static Compiler getInstance(CompilerContext context) {
        Compiler compiler = context.get(COMPILER_KEY);
        if (compiler == null) {
            compiler = new Compiler(context);
        }
        return compiler;
    }

    public Compiler(CompilerContext context) {
        context.put(COMPILER_KEY, this);

        this.options = CompilerOptions.getInstance(context);
        this.dlog = DiagnosticLog.getInstance(context);
        this.pkgLoader = PackageLoader.getInstance(context);
        this.symbolTable = SymbolTable.getInstance(context);
        this.semAnalyzer = SemanticAnalyzer.getInstance(context);
        this.codeAnalyzer = CodeAnalyzer.getInstance(context);
        this.desugar = Desugar.getInstance(context);
        this.codeGenerator = CodeGenerator.getInstance(context);

        this.compilerPhase = getCompilerPhase();
    }

    public void compile(String sourcePkg) {
        // "ballerina/built-in" packages is only the pre-known package by the Ballerina compiler. So load it first.
        BLangPackage builtInPackage = loadBuiltInPackage();
        if (this.stopCompilation(CompilerPhase.DEFINE)) {
            return;
        }

        pkgNode = define(sourcePkg);
        if (this.stopCompilation(CompilerPhase.TYPE_CHECK)) {
            return;
        }

        pkgNode = typeCheck(pkgNode);
        if (this.stopCompilation(CompilerPhase.CODE_ANALYZE)) {
            return;
        }

        pkgNode = codeAnalyze(pkgNode);
        if (this.stopCompilation(CompilerPhase.DESUGAR)) {
            return;
        }
        // TODO : Improve this.
        desugar(builtInPackage);
        pkgNode = desugar(pkgNode);
        if (this.stopCompilation(CompilerPhase.CODE_GEN)) {
            return;
        }

        loadRequiredPackagesForBVM();
        gen(pkgNode);
    }

    private BLangPackage loadBuiltInPackage() {
        // Load built-in packages.
        BLangPackage builtInPkg = getBuiltInPackage(Names.BUILTIN_PACKAGE);
        symbolTable.builtInPackageSymbol = builtInPkg.symbol;
        return builtInPkg;
    }

    private void loadRequiredPackagesForBVM() {
        // TODO : FIX this with Balo.
        // This is a temporary fix to load required packages for BVM. These should be loaded from Balo to BVM.
        symbolTable.runtimePackageSymbol = desugar(getBuiltInPackage(Names.RUNTIME_PACKAGE)).symbol;
    }

    public ProgramFile getCompiledProgram() {
        return programFile;
    }

    public ProgramFile getCompiledPackage() {
        // TODO
        return null;
    }

    public PackageNode getAST() {
        return pkgNode;
    }


    // private methods

    private BLangPackage define(String sourcePkg) {
        try {
            return pkgLoader.loadEntryPackage(sourcePkg);
        } catch (BLangParserException e) {
            containsSyntaxErrors = true;
            return null;
        }
    }

    private BLangPackage typeCheck(BLangPackage pkgNode) {
        return semAnalyzer.analyze(pkgNode);
    }

    private BLangPackage codeAnalyze(BLangPackage pkgNode) {
        return codeAnalyzer.analyze(pkgNode);
    }

    private BLangPackage desugar(BLangPackage pkgNode) {
        return desugar.perform(pkgNode);
    }

    private void gen(BLangPackage pkgNode) {
        programFile = this.codeGenerator.generate(pkgNode);
    }

    private CompilerPhase getCompilerPhase() {
        String phaseName = options.get(CompilerOptionName.COMPILER_PHASE);
        if (phaseName == null || phaseName.isEmpty()) {
            return CompilerPhase.CODE_GEN;
        }

        return CompilerPhase.fromValue(phaseName);
    }

    private boolean stopCompilation(CompilerPhase phase) {
        if (compilerPhase.compareTo(phase) < 0) {
            return true;
        }

        if (containsSyntaxErrors) {
            return true;
        }

        return (phase == CompilerPhase.DESUGAR ||
                phase == CompilerPhase.CODE_GEN) &&
                (dlog.errorCount > 0 || this.pkgNode.getCompilationUnits().isEmpty());
    }

    private BLangPackage getBuiltInPackage(Name name) {
        return codeAnalyze(semAnalyzer.analyze(pkgLoader.loadEntryPackage(name.getValue())));
    }
}
