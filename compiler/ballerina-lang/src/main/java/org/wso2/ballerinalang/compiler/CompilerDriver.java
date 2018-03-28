/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.ballerinalang.compiler.desugar.Desugar;
import org.wso2.ballerinalang.compiler.semantics.analyzer.CodeAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.analyzer.CompilerPluginRunner;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SemanticAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.analyzer.TaintAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;

/**
 * This class drives the compilation of packages through various phases
 * such as symbol enter, semantic analysis, type checking, code analysis,
 * desugar and code generation.
 *
 * @since 0.965.0
 */
public class CompilerDriver {

    private static final CompilerContext.Key<CompilerDriver> COMPILER_DRIVER_KEY =
            new CompilerContext.Key<>();

    private final CompilerOptions options;
    private final BLangDiagnosticLog dlog;
    private final PackageLoader pkgLoader;
    private final SymbolTable symbolTable;
    private final SymbolEnter symbolEnter;
    private final SemanticAnalyzer semAnalyzer;
    private final CodeAnalyzer codeAnalyzer;
    private final TaintAnalyzer taintAnalyzer;
    private final CompilerPluginRunner compilerPluginRunner;
    private final Desugar desugar;

    private final CompilerPhase compilerPhase;

    public static CompilerDriver getInstance(CompilerContext context) {
        CompilerDriver compilerDriver = context.get(COMPILER_DRIVER_KEY);
        if (compilerDriver == null) {
            compilerDriver = new CompilerDriver(context);
        }
        return compilerDriver;
    }

    private CompilerDriver(CompilerContext context) {
        context.put(COMPILER_DRIVER_KEY, this);

        this.options = CompilerOptions.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.pkgLoader = PackageLoader.getInstance(context);
        this.symbolTable = SymbolTable.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
        this.semAnalyzer = SemanticAnalyzer.getInstance(context);
        this.codeAnalyzer = CodeAnalyzer.getInstance(context);
        this.taintAnalyzer = TaintAnalyzer.getInstance(context);
        this.compilerPluginRunner = CompilerPluginRunner.getInstance(context);
        this.desugar = Desugar.getInstance(context);
        this.compilerPhase = getCompilerPhase();
    }

    public BLangPackage compilePackage(BLangPackage bLangPackage) {
        // execute all the phases up to the desugar phase.
        return compile(bLangPackage);
    }


    // Private methods

    private BLangPackage compile(BLangPackage bLangPackage) {
        BLangPackage pkgNode = bLangPackage;

        // "ballerina/built-in" packages is only the pre-known package by the Ballerina compiler. So load it first.
        BLangPackage builtInPackage = loadBuiltInPackage();
        if (this.stopCompilation(pkgNode, CompilerPhase.DEFINE)) {
            return pkgNode;
        }

        pkgNode = define(pkgNode);
        if (this.stopCompilation(pkgNode, CompilerPhase.TYPE_CHECK)) {
            return pkgNode;
        }

        pkgNode = typeCheck(pkgNode);
        if (this.stopCompilation(pkgNode, CompilerPhase.CODE_ANALYZE)) {
            return pkgNode;
        }

        pkgNode = codeAnalyze(pkgNode);
        if (this.stopCompilation(pkgNode, CompilerPhase.TAINT_ANALYZE)) {
            return pkgNode;
        }

        pkgNode = taintAnalyze(pkgNode);
        if (this.stopCompilation(pkgNode, CompilerPhase.COMPILER_PLUGIN)) {
            return pkgNode;
        }

        pkgNode = annotationProcess(pkgNode);
        if (this.stopCompilation(pkgNode, CompilerPhase.DESUGAR)) {
            return pkgNode;
        }

        // TODO : Improve this.
        desugar(builtInPackage);
        return desugar(pkgNode);
    }

    private BLangPackage define(BLangPackage pkgNode) {
        return this.symbolEnter.definePackage(pkgNode);
    }

    private BLangPackage typeCheck(BLangPackage pkgNode) {
        return this.semAnalyzer.analyze(pkgNode);
    }

    private BLangPackage codeAnalyze(BLangPackage pkgNode) {
        return this.codeAnalyzer.analyze(pkgNode);
    }

    private BLangPackage taintAnalyze(BLangPackage pkgNode) {
        return this.taintAnalyzer.analyze(pkgNode);
    }

    private BLangPackage annotationProcess(BLangPackage pkgNode) {
        return this.compilerPluginRunner.runPlugins(pkgNode);
    }

    private BLangPackage desugar(BLangPackage pkgNode) {
        return this.desugar.perform(pkgNode);
    }

    private CompilerPhase getCompilerPhase() {
        String phaseName = options.get(CompilerOptionName.COMPILER_PHASE);
        if (phaseName == null || phaseName.isEmpty()) {
            return CompilerPhase.CODE_GEN;
        }

        return CompilerPhase.fromValue(phaseName);
    }

    private boolean stopCompilation(BLangPackage pkgNode, CompilerPhase nextPhase) {
        if (compilerPhase.compareTo(nextPhase) < 0) {
            return true;
        }

        return (nextPhase == CompilerPhase.TAINT_ANALYZE ||
                nextPhase == CompilerPhase.COMPILER_PLUGIN ||
                nextPhase == CompilerPhase.DESUGAR)
                && (dlog.errorCount > 0 || pkgNode.getCompilationUnits().isEmpty());
    }

    private BLangPackage getBuiltInPackage(Name orgName, Name name) {
        return taintAnalyze(codeAnalyze(semAnalyzer.analyze(pkgLoader.loadAndDefinePackage(orgName.getValue(),
                name.getValue()))));
    }

    private BLangPackage loadBuiltInPackage() {
        // Load built-in packages.
        BLangPackage builtInPkg = getBuiltInPackage(Names.BUILTIN_ORG, Names.BUILTIN_PACKAGE);
        symbolTable.builtInPackageSymbol = builtInPkg.symbol;
        return builtInPkg;
    }
}
