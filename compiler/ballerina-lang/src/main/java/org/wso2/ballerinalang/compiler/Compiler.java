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

import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.parser.ManifestProcessor;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import org.wso2.ballerinalang.util.Lists;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @since 0.94
 */
public class Compiler {

    private static final CompilerContext.Key<Compiler> COMPILER_KEY =
            new CompilerContext.Key<>();

    private final SourceDirectoryManager sourceDirectoryManager;
    private final CompilerDriver compilerDriver;
    private final BinaryFileWriter binaryFileWriter;
    private final DependencyTree dependencyTree;
    private final BLangDiagnosticLog dlog;
    private final PackageLoader pkgLoader;
    private final Manifest manifest;
    private boolean langLibsLoaded;
    private PrintStream outStream;
    private PrintStream errorStream;

    private Compiler(CompilerContext context) {
        context.put(COMPILER_KEY, this);
        this.sourceDirectoryManager = SourceDirectoryManager.getInstance(context);
        this.compilerDriver = CompilerDriver.getInstance(context);
        this.binaryFileWriter = BinaryFileWriter.getInstance(context);
        this.dependencyTree = DependencyTree.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.pkgLoader = PackageLoader.getInstance(context);
        this.manifest = ManifestProcessor.getInstance(context).getManifest();
        this.outStream = System.out;
        this.errorStream = System.err;
        this.langLibsLoaded = false;
    }

    public static Compiler getInstance(CompilerContext context) {
        Compiler compiler = context.get(COMPILER_KEY);
        if (compiler == null) {
            compiler = new Compiler(context);
        }
        return compiler;
    }

    public void setOutStream(PrintStream outStream) {
        this.outStream = outStream;
    }

    public void setErrorStream(PrintStream errorStream) {
        this.errorStream = errorStream;
    }

    public BLangPackage compile(String sourcePackage) {
        return compile(sourcePackage, false);
    }

    public BLangPackage compile(String sourcePackage, boolean isBuild) {
        if (!isBuild && !this.sourceDirectoryManager.checkIfSourcesExists(sourcePackage)) {
            throw new BLangCompilerException("no ballerina source files found in module '" + sourcePackage + "'");
        }
        PackageID packageID = this.sourceDirectoryManager.getPackageID(sourcePackage);
        if (packageID == null) {
            throw ProjectDirs.getPackageNotFoundError(sourcePackage);
        }

        return compilePackage(packageID);
    }

    public BLangPackage build(String sourcePackage) {
        if (!this.sourceDirectoryManager.checkIfSourcesExists(sourcePackage)) {
            throw new BLangCompilerException("no ballerina source files found in module '" + sourcePackage + "'");
        }
        this.outStream.println("Compiling source");
        BLangPackage bLangPackage = compile(sourcePackage, true);
        if (bLangPackage.getErrorCount() > 0) {
            throw new BLangCompilerException("compilation contains errors");
        }
        return bLangPackage;
    }

    public void write(List<BLangPackage> packageList) {
        if (packageList.stream().anyMatch(bLangPackage -> bLangPackage.symbol.entryPointExists)) {
            this.outStream.println("Generating executables");
        }
        packageList.forEach(this.binaryFileWriter::write);
    }

    public void write(BLangPackage bLangPackage, String targetFileName) {
        this.binaryFileWriter.write(bLangPackage, targetFileName);
    }

    public void list() {
        compilePackages(true).forEach(this.dependencyTree::listDependencyPackages);
    }

    public void list(String sourcePackage) {
        BLangPackage bLangPackage = compile(sourcePackage);
        if (bLangPackage.hasErrors()) {
            throw new BLangCompilerException("compilation contains errors");
        }

        this.dependencyTree.listDependencyPackages(bLangPackage);
    }

    public List<BLangPackage> compilePackages(boolean isBuild) {
        List<PackageID> pkgList = this.sourceDirectoryManager.listSourceFilesAndPackages().collect(Collectors.toList());
        if (pkgList.size() == 0) {
            return new ArrayList<>();
        }
        
        this.outStream.println("Compiling source");
        List<BLangPackage> compiledPackages = compilePackages(pkgList);
        // If it is a build and dlog is not empty, compilation should fail
        if (isBuild && this.dlog.errorCount() > 0) {
            throw new BLangCompilerException("compilation contains errors");
        }
        return compiledPackages;
    }
    // private methods

    private List<BLangPackage> compilePackages(List<PackageID> pkgIdList) {
        if (!this.langLibsLoaded) {
            this.compilerDriver.loadLangModules(pkgIdList);
            this.langLibsLoaded = true;
        }

        // 1) Load all source packages. i.e. source-code -> BLangPackageNode
        // 2) Define all package level symbols for all the packages including imported packages in the AST
        List<BLangPackage> packages = new ArrayList<>();
        for (PackageID pkgId : pkgIdList) {
            BLangPackage bLangPackage = this.pkgLoader.loadEntryPackage(pkgId, null, this.outStream);
            if (bLangPackage != null) {
                // skip the packages that were not loaded properly
                packages.add(bLangPackage);
            }
        }

        // 3) Invoke compiler phases. e.g. type_check, code_analyze, taint_analyze, desugar etc.
        for (BLangPackage pkgNode : packages) {
            if (pkgNode.symbol != null) {
                this.compilerDriver.compilePackage(pkgNode);
                logDiagnostics(pkgNode);
                dlog.resetErrorCount();
            }
        }
        return packages;
    }

    /**
     * Log the diagnostics in the package to the output stream.
     * 
     * @param pkgNode Package node
     */
    private void logDiagnostics(BLangPackage pkgNode) {
        for (Diagnostic diagnostic : pkgNode.getDiagnostics()) {
            String strPos = "";
            LineRange lineRange = diagnostic.location().lineRange();
            String fileName = lineRange.filePath();
            if (pkgNode.packageID != PackageID.DEFAULT && fileName != null && !fileName.isEmpty()) {
                strPos = strPos + pkgNode.packageID + "::" + fileName + ":";
            } else if (pkgNode.packageID != PackageID.DEFAULT) {
                strPos = strPos + pkgNode.packageID + ":";
            } else {
                strPos = strPos + fileName + ":";
            }

            LinePosition line = lineRange.startLine();
            // Add +1 since it's 0-based
            strPos = strPos + (line.line() + 1) + ":" + (line.offset() + 1) + ":";

            DiagnosticSeverity kind = diagnostic.diagnosticInfo().severity();
            switch (kind) {
                case ERROR:
                    this.errorStream.println("error: " + strPos + " " + diagnostic.message());
                    break;
                case WARNING:
                    this.errorStream.println("warning: " + strPos + " " + diagnostic.message());
                    break;
                default:
                    break;
            }
        }
    }

    private BLangPackage compilePackage(PackageID packageID) {
        List<BLangPackage> compiledPackages = compilePackages(Lists.of(packageID));
        // TODO: this should check for dlog.errorCount > 0. But currently some errors are
        // not getting added to dlog, hence cannot check for error count. Issue #10454.
        if (compiledPackages.isEmpty()) {
            throw new BLangCompilerException("compilation contains errors");
        }
        return compiledPackages.get(0);
    }
}
