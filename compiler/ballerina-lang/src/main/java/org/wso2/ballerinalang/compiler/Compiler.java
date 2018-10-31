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

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.parser.ManifestProcessor;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile.ProgramFile;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @since 0.94
 */
public class Compiler {

    private static final CompilerContext.Key<Compiler> COMPILER_KEY =
            new CompilerContext.Key<>();
    private static PrintStream outStream = System.out;

    private final SourceDirectoryManager sourceDirectoryManager;
    private final CompilerDriver compilerDriver;
    private final BinaryFileWriter binaryFileWriter;
    private final LockFileWriter lockFileWriter;
    private final DependencyTree dependencyTree;
    private final BLangDiagnosticLog dlog;
    private final PackageLoader pkgLoader;
    private final Manifest manifest;

    private Compiler(CompilerContext context) {
        context.put(COMPILER_KEY, this);
        this.sourceDirectoryManager = SourceDirectoryManager.getInstance(context);
        this.compilerDriver = CompilerDriver.getInstance(context);
        this.binaryFileWriter = BinaryFileWriter.getInstance(context);
        this.lockFileWriter = LockFileWriter.getInstance(context);
        this.dependencyTree = DependencyTree.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.pkgLoader = PackageLoader.getInstance(context);
        this.manifest = ManifestProcessor.getInstance(context).getManifest();
    }

    public static Compiler getInstance(CompilerContext context) {
        Compiler compiler = context.get(COMPILER_KEY);
        if (compiler == null) {
            compiler = new Compiler(context);
        }
        return compiler;
    }

    public BLangPackage compile(String sourcePackage) {
        return compile(sourcePackage, false);
    }

    public BLangPackage compile(String sourcePackage, boolean isBuild) {
        if (!isBuild && !this.sourceDirectoryManager.checkIfSourcesExists(sourcePackage)) {
            throw new BLangCompilerException("no ballerina source files found in module " + sourcePackage);
        }
        PackageID packageID = this.sourceDirectoryManager.getPackageID(sourcePackage);
        if (packageID == null) {
            throw ProjectDirs.getPackageNotFoundError(sourcePackage);
        }

        return compilePackage(packageID, isBuild);
    }

    public List<BLangPackage> build() {
        return compilePackages();
    }

    public BLangPackage build(String sourcePackage) {
        if (!this.sourceDirectoryManager.checkIfSourcesExists(sourcePackage)) {
            throw new BLangCompilerException("no ballerina source files found in module " + sourcePackage);
        }
        outStream.println("Compiling source");
        BLangPackage bLangPackage = compile(sourcePackage, true);
        if (bLangPackage.diagCollector.hasErrors()) {
            throw new BLangCompilerException("compilation contains errors");
        }
        return bLangPackage;
    }

    public void write(List<BLangPackage> packageList) {
        if (packageList.stream().anyMatch(bLangPackage -> bLangPackage.symbol.entryPointExists)) {
            outStream.println("Generating executables");
        }
        packageList.forEach(this.binaryFileWriter::write);
        packageList.forEach(bLangPackage -> lockFileWriter.addEntryPkg(bLangPackage.symbol));
        this.lockFileWriter.writeLockFile(this.manifest);
    }

    public void write(BLangPackage bLangPackage, String targetFileName) {
        this.binaryFileWriter.write(bLangPackage, targetFileName);
        this.lockFileWriter.addEntryPkg(bLangPackage.symbol);
        this.lockFileWriter.writeLockFile(this.manifest);
    }

    public void list() {
        compilePackages().forEach(this.dependencyTree::listDependencyPackages);
    }

    public void list(String sourcePackage) {
        BLangPackage bLangPackage = compile(sourcePackage);
        if (bLangPackage.diagCollector.hasErrors()) {
            throw new BLangCompilerException("compilation contains errors");
        }

        this.dependencyTree.listDependencyPackages(bLangPackage);
    }

    public ProgramFile getExecutableProgram(BLangPackage entryPackageNode) {
        if (dlog.errorCount > 0) {
            return null;
        }
        return this.binaryFileWriter.genExecutable(entryPackageNode);
    }


    // private methods

    private List<BLangPackage> compilePackages(Stream<PackageID> pkgIdStream, boolean isBuild) {
        // TODO This is hack to load the builtin package. We will fix this with BALO support
        this.compilerDriver.loadBuiltinPackage();

        // 1) Load all source packages. i.e. source-code -> BLangPackageNode
        // 2) Define all package level symbols for all the packages including imported packages in the AST
        List<BLangPackage> packages = pkgIdStream
                .filter(p -> !SymbolTable.BUILTIN.equals(p))
                .map((PackageID pkgId) -> this.pkgLoader.loadEntryPackage(pkgId, null, isBuild))
                .filter(pkgNode -> pkgNode != null) // skip the packages that were not loaded properly
                .collect(Collectors.toList());

        // 3) Invoke compiler phases. e.g. type_check, code_analyze, taint_analyze, desugar etc.
        packages.stream()
//                .filter(pkgNode -> !pkgNode.diagCollector.hasErrors())
                .filter(pkgNode -> pkgNode.symbol != null)
                .forEach(this.compilerDriver::compilePackage);
        return packages;
    }

    private List<BLangPackage> compilePackages() {
        List<PackageID> pkgList = this.sourceDirectoryManager.listSourceFilesAndPackages().collect(Collectors.toList());
        if (pkgList.size() == 0) {
            return new ArrayList<>();
        }
        outStream.println("Compiling source");
        List<BLangPackage> compiledPackages = compilePackages(pkgList.stream(), true);
        if (this.dlog.errorCount > 0) {
            throw new BLangCompilerException("compilation contains errors");
        }
        return compiledPackages;
    }

    private BLangPackage compilePackage(PackageID packageID, boolean isBuild) {
        List<BLangPackage> compiledPackages = compilePackages(Stream.of(packageID), isBuild);
        // TODO: this should check for dlog.errorCount > 0. But currently some errors are
        // not getting added to dlog, hence cannot check for error count. Issue #10454.
        if (compiledPackages.isEmpty()) {
            throw new BLangCompilerException("compilation contains errors");
        }
        return compiledPackages.get(0);
    }
}
