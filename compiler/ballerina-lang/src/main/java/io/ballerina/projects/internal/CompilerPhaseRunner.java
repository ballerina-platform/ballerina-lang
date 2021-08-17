/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects.internal;

import io.ballerina.runtime.internal.util.RuntimeUtils;
import org.ballerinalang.compiler.CompilerPhase;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.bir.BIRGen;
import org.wso2.ballerinalang.compiler.bir.emit.BIREmitter;
import org.wso2.ballerinalang.compiler.desugar.ConstantPropagation;
import org.wso2.ballerinalang.compiler.desugar.Desugar;
import org.wso2.ballerinalang.compiler.diagnostic.CompilerBadSadDiagnostic;
import org.wso2.ballerinalang.compiler.semantics.analyzer.CodeAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.analyzer.CompilerPluginRunner;
import org.wso2.ballerinalang.compiler.semantics.analyzer.DataflowAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.analyzer.DocumentationAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.analyzer.IsolationAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.analyzer.ObservabilitySymbolCollectorRunner;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SemanticAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.spi.ObservabilitySymbolCollector;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import static org.ballerinalang.compiler.CompilerOptionName.TOOLING_COMPILATION;

/**
 * This class drives the compilation of packages through various phases
 * such as symbol enter, semantic analysis, type checking, code analysis,
 * desugar and code generation.
 *
 * @since 2.0.0
 */
public class CompilerPhaseRunner {

    private static final CompilerContext.Key<CompilerPhaseRunner> COMPILER_DRIVER_KEY =
            new CompilerContext.Key<>();

    private final CompilerOptions options;
    private final PackageCache pkgCache;
    private final SymbolTable symbolTable;
    private final SymbolEnter symbolEnter;
    private final SymbolResolver symResolver;
    private final SemanticAnalyzer semAnalyzer;
    private final CodeAnalyzer codeAnalyzer;
    private final ConstantPropagation constantPropagation;
    private final DocumentationAnalyzer documentationAnalyzer;
    private final CompilerPluginRunner compilerPluginRunner;
    private final ObservabilitySymbolCollector observabilitySymbolCollector;
    private final Desugar desugar;
    private final BIRGen birGenerator;
    private final BIREmitter birEmitter;
    private final CompilerPhase compilerPhase;
    private final DataflowAnalyzer dataflowAnalyzer;
    private final IsolationAnalyzer isolationAnalyzer;
    private boolean isToolingCompilation;


    public static CompilerPhaseRunner getInstance(CompilerContext context) {
        CompilerPhaseRunner compilerDriver = context.get(COMPILER_DRIVER_KEY);
        if (compilerDriver == null) {
            compilerDriver = new CompilerPhaseRunner(context);
        }
        return compilerDriver;
    }

    private CompilerPhaseRunner(CompilerContext context) {
        context.put(COMPILER_DRIVER_KEY, this);

        this.options = CompilerOptions.getInstance(context);
        this.pkgCache = PackageCache.getInstance(context);
        this.symbolTable = SymbolTable.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
        this.semAnalyzer = SemanticAnalyzer.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.codeAnalyzer = CodeAnalyzer.getInstance(context);
        this.documentationAnalyzer = DocumentationAnalyzer.getInstance(context);
        this.constantPropagation = ConstantPropagation.getInstance(context);
        this.compilerPluginRunner = CompilerPluginRunner.getInstance(context);
        this.observabilitySymbolCollector = ObservabilitySymbolCollectorRunner.getInstance(context);
        this.desugar = Desugar.getInstance(context);
        this.birGenerator = BIRGen.getInstance(context);
        this.birEmitter = BIREmitter.getInstance(context);
        this.compilerPhase = this.options.getCompilerPhase();
        this.dataflowAnalyzer = DataflowAnalyzer.getInstance(context);
        this.isolationAnalyzer = IsolationAnalyzer.getInstance(context);
        this.isToolingCompilation = this.options.isSet(TOOLING_COMPILATION)
                && Boolean.parseBoolean(this.options.get(TOOLING_COMPILATION));
    }

    public void performTypeCheckPhases(BLangPackage pkgNode) {
        if (this.stopCompilation(pkgNode, CompilerPhase.TYPE_CHECK)) {
            return;
        }

        typeCheck(pkgNode);
        if (this.stopCompilation(pkgNode, CompilerPhase.CODE_ANALYZE)) {
            return;
        }

        codeAnalyze(pkgNode);
        if (this.stopCompilation(pkgNode, CompilerPhase.DATAFLOW_ANALYZE)) {
            return;
        }

        dataflowAnalyze(pkgNode);
        if (this.stopCompilation(pkgNode, CompilerPhase.DOCUMENTATION_ANALYZE)) {
            return;
        }

        isolationAnalyze(pkgNode);
        if (this.stopCompilation(pkgNode, CompilerPhase.ISOLATION_ANALYZE)) {
            return;
        }

        documentationAnalyze(pkgNode);
        if (this.stopCompilation(pkgNode, CompilerPhase.CONSTANT_PROPAGATION)) {
            return;
        }

        propagateConstants(pkgNode);
        if (this.stopCompilation(pkgNode, CompilerPhase.COMPILER_PLUGIN)) {
            return;
        }

        annotationProcess(pkgNode);
    }

    public void performBirGenPhases(BLangPackage pkgNode) {
        if (this.stopCompilation(pkgNode, CompilerPhase.DESUGAR)) {
            return;
        }

        desugar(pkgNode);
        if (this.stopCompilation(pkgNode, CompilerPhase.BIR_GEN)) {
            return;
        }

        birGen(pkgNode);
        if (this.stopCompilation(pkgNode, CompilerPhase.BIR_EMIT)) {
            return;
        }

        birEmit(pkgNode);
    }

    public void performLangLibTypeCheckPhases(BLangPackage pkgNode) {
        if (this.stopCompilation(pkgNode, CompilerPhase.TYPE_CHECK)) {
            return;
        }

        typeCheck(pkgNode);
        if (this.stopCompilation(pkgNode, CompilerPhase.CODE_ANALYZE)) {
            return;
        }

        codeAnalyze(pkgNode);
        if (this.stopCompilation(pkgNode, CompilerPhase.DOCUMENTATION_ANALYZE)) {
            return;
        }

        documentationAnalyze(pkgNode);
    }

    public void performLangLibBirGenPhases(BLangPackage pkgNode) {
        if (this.stopCompilation(pkgNode, CompilerPhase.DESUGAR)) {
            return;
        }

        desugar(pkgNode);
        if (this.stopCompilation(pkgNode, CompilerPhase.BIR_GEN)) {
            return;
        }

        birGen(pkgNode);
        if (this.stopCompilation(pkgNode, CompilerPhase.BIR_EMIT)) {
            return;
        }

        birEmit(pkgNode);
    }

    public BLangPackage define(BLangPackage pkgNode) {
        return this.symbolEnter.definePackage(pkgNode);
    }

    private BLangPackage typeCheck(BLangPackage pkgNode) {
        return this.semAnalyzer.analyze(pkgNode);
    }

    private BLangPackage documentationAnalyze(BLangPackage pkgNode) {
        return this.documentationAnalyzer.analyze(pkgNode);
    }

    private BLangPackage codeAnalyze(BLangPackage pkgNode) {
        return this.codeAnalyzer.analyze(pkgNode);
    }

    private BLangPackage dataflowAnalyze(BLangPackage pkgNode) {
        return this.dataflowAnalyzer.analyze(pkgNode);
    }

    private BLangPackage isolationAnalyze(BLangPackage pkgNode) {
        return this.isolationAnalyzer.analyze(pkgNode);
    }

    private BLangPackage propagateConstants(BLangPackage pkgNode) {
        return this.constantPropagation.perform(pkgNode);
    }

    private BLangPackage annotationProcess(BLangPackage pkgNode) {
        return this.compilerPluginRunner.runPlugins(pkgNode);
    }

    public BLangPackage desugar(BLangPackage pkgNode) {
        return this.desugar.perform(pkgNode);
    }

    public BLangPackage birGen(BLangPackage pkgNode) {
        return this.birGenerator.genBIR(pkgNode);
    }

    private BLangPackage birEmit(BLangPackage pkgNode) {
        return this.birEmitter.emit(pkgNode);
    }

    private boolean stopCompilation(BLangPackage pkgNode, CompilerPhase nextPhase) {
        if (compilerPhase.compareTo(nextPhase) < 0) {
            return true;
        }
        return (checkNextPhase(nextPhase) && pkgNode.getErrorCount() > 0);
    }

    private boolean checkNextPhase(CompilerPhase nextPhase) {
        return (!isToolingCompilation && nextPhase == CompilerPhase.CODE_ANALYZE) ||
                nextPhase == CompilerPhase.COMPILER_PLUGIN ||
                nextPhase == CompilerPhase.DESUGAR;
    }

    public void addDiagnosticForUnhandledException(BLangPackage pkgNode, Throwable throwable) {
        pkgNode.addDiagnostic(new CompilerBadSadDiagnostic(pkgNode.pos, throwable));
        RuntimeUtils.logBadSad(throwable);
    }
}
