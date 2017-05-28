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
package org.ballerinalang;

import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.GlobalScope;
import org.ballerinalang.model.NativeScope;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.util.BLangDiagnosticListener;
import org.ballerinalang.util.codegen.CodeGenerator;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangPackages;
import org.ballerinalang.util.program.BLangPrograms;
import org.ballerinalang.util.repository.BLangProgramArchive;
import org.ballerinalang.util.repository.BuiltinPackageRepository;
import org.ballerinalang.util.repository.FileSystemPackageRepository;
import org.ballerinalang.util.repository.PackageRepository;
import org.ballerinalang.util.semantics.SemanticAnalyzer;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code BLangProgramLoader} creates in-memory representations of a Ballerina program from the given
 * list of files, packages or archives.
 *
 * @since 0.8.0
 */
public class BLangProgramLoader {
    private boolean disableSemanticAnalyzer = false;
    private PackageRepository packageRepository;
    private BLangDiagnosticListener diagnosticListener;

    @Deprecated
    public BLangProgram loadMain(Path programDirPath, Path sourcePath) {
        programDirPath = BLangPrograms.validateAndResolveProgramDirPath(programDirPath);

        // Get the global scope
        GlobalScope globalScope = BLangPrograms.populateGlobalScope();
        NativeScope nativeScope = BLangPrograms.populateNativeScope();

        BuiltinPackageRepository[] builtinPkgRepositories = BLangPrograms.populateBuiltinPackageRepositories();

        // Creates program scope for this Ballerina program
        BLangProgram bLangProgram = new BLangProgram(globalScope, nativeScope, BLangProgram.Category.MAIN_PROGRAM);
        bLangProgram.setProgramFilePath(sourcePath);

        BLangPackage[] bLangPackages = loadPackages(programDirPath, sourcePath,
                bLangProgram, builtinPkgRepositories);
        BLangPackage mainPackage = bLangPackages[0];

        bLangProgram.setMainPackage(mainPackage);
        bLangProgram.define(new SymbolName(mainPackage.getPackagePath()), mainPackage);


        // Analyze the semantic properties of the Ballerina program
        if (!disableSemanticAnalyzer) {
            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(bLangProgram);
            bLangProgram.accept(semanticAnalyzer);
        }

        return bLangProgram;
    }

    @Deprecated
    public BLangProgram loadService(Path programDirPath, Path servicePath) {
        programDirPath = BLangPrograms.validateAndResolveProgramDirPath(programDirPath);

        // Get the global scope
        GlobalScope globalScope = BLangPrograms.populateGlobalScope();
        NativeScope nativeScope = BLangPrograms.populateNativeScope();

        BuiltinPackageRepository[] builtinPkgRepositories = BLangPrograms.populateBuiltinPackageRepositories();

        // Creates program scope for this Ballerina program
        BLangProgram bLangProgram = new BLangProgram(globalScope, nativeScope, BLangProgram.Category.SERVICE_PROGRAM);
        bLangProgram.setProgramFilePath(servicePath);

        BLangPackage[] servicePackages = loadPackages(programDirPath, servicePath,
                bLangProgram, builtinPkgRepositories);
        for (BLangPackage servicePkg : servicePackages) {
            bLangProgram.addServicePackage(servicePkg);
            bLangProgram.define(new SymbolName(servicePkg.getPackagePath()), servicePkg);
        }

        // Analyze the semantic properties of the Ballerina program
        if (!disableSemanticAnalyzer) {
            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(bLangProgram);
            bLangProgram.accept(semanticAnalyzer);
        }

        return bLangProgram;
    }

    public ProgramFile loadMainProgramFile(Path programDirPath, Path sourcePath) {
        programDirPath = BLangPrograms.validateAndResolveProgramDirPath(programDirPath);

        // Get the global scope
        GlobalScope globalScope = BLangPrograms.populateGlobalScope();
        NativeScope nativeScope = BLangPrograms.populateNativeScope();

        BuiltinPackageRepository[] builtinPkgRepositories = BLangPrograms.populateBuiltinPackageRepositories();

        // Creates program scope for this Ballerina program
        BLangProgram bLangProgram = new BLangProgram(globalScope, nativeScope, BLangProgram.Category.MAIN_PROGRAM);
        bLangProgram.setProgramFilePath(sourcePath);

        BLangPackage[] bLangPackages = loadPackages(programDirPath, sourcePath,
                bLangProgram, builtinPkgRepositories);
        BLangPackage mainPackage = bLangPackages[0];
        bLangProgram.setMainPackage(mainPackage);
        bLangProgram.define(new SymbolName(mainPackage.getPackagePath()), mainPackage);

        // Analyze the semantic properties of the Ballerina program
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(bLangProgram);
        bLangProgram.accept(semanticAnalyzer);

        CodeGenerator codeGenerator = new CodeGenerator();
        bLangProgram.accept(codeGenerator);

        ProgramFile programFile = codeGenerator.getProgramFile();
        programFile.setProgramFilePath(bLangProgram.getProgramFilePath());
        return programFile;
    }

    public ProgramFile loadServiceProgramFile(Path programDirPath, Path servicePath) {
        programDirPath = BLangPrograms.validateAndResolveProgramDirPath(programDirPath);

        // Get the global scope
        GlobalScope globalScope = BLangPrograms.populateGlobalScope();
        NativeScope nativeScope = BLangPrograms.populateNativeScope();

        BuiltinPackageRepository[] builtinPkgRepositories = BLangPrograms.populateBuiltinPackageRepositories();

        // Creates program scope for this Ballerina program
        BLangProgram bLangProgram = new BLangProgram(globalScope, nativeScope, BLangProgram.Category.SERVICE_PROGRAM);
        bLangProgram.setProgramFilePath(servicePath);

        BLangPackage[] servicePackages = loadPackages(programDirPath, servicePath,
                bLangProgram, builtinPkgRepositories);
        for (BLangPackage servicePkg : servicePackages) {
            bLangProgram.addServicePackage(servicePkg);
            bLangProgram.define(new SymbolName(servicePkg.getPackagePath()), servicePkg);
        }

        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(bLangProgram);
        bLangProgram.accept(semanticAnalyzer);

        CodeGenerator codeGenerator = new CodeGenerator();
        bLangProgram.accept(codeGenerator);

        ProgramFile programFile = codeGenerator.getProgramFile();
        programFile.setProgramFilePath(bLangProgram.getProgramFilePath());
        return programFile;
    }

    public ProgramFile loadProgramFile(Path programDirPath, Path sourcePath) {
        programDirPath = BLangPrograms.validateAndResolveProgramDirPath(programDirPath);

        // Get the global scope
        GlobalScope globalScope = BLangPrograms.populateGlobalScope();
        NativeScope nativeScope = BLangPrograms.populateNativeScope();

        BuiltinPackageRepository[] builtinPkgRepositories = BLangPrograms.populateBuiltinPackageRepositories();

        // Creates program scope for this Ballerina program
        BLangProgram bLangProgram = new BLangProgram(globalScope, nativeScope, BLangProgram.Category.LIBRARY_PROGRAM);
        bLangProgram.setProgramFilePath(sourcePath);

        BLangPackage[] bLangPackages = loadPackages(programDirPath, sourcePath, bLangProgram, builtinPkgRepositories);

        for (BLangPackage bLangPackage : bLangPackages) {
            bLangProgram.addLibraryPackage(bLangPackage);
            bLangProgram.define(new SymbolName(bLangPackage.getPackagePath()), bLangPackage);
        }

        // Analyze the semantic properties of the Ballerina program
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(bLangProgram);
        bLangProgram.accept(semanticAnalyzer);

        CodeGenerator codeGenerator = new CodeGenerator();
        bLangProgram.accept(codeGenerator);

        ProgramFile programFile = codeGenerator.getProgramFile();
        programFile.setProgramFilePath(bLangProgram.getProgramFilePath());
        return programFile;
    }

    @Deprecated
    public BLangProgram loadLibrary(Path programDirPath, Path sourcePath) {
        programDirPath = BLangPrograms.validateAndResolveProgramDirPath(programDirPath);

        // Get the global scope
        GlobalScope globalScope = BLangPrograms.populateGlobalScope();
        NativeScope nativeScope = BLangPrograms.populateNativeScope();

        BuiltinPackageRepository[] builtinPkgRepositories = BLangPrograms.populateBuiltinPackageRepositories();

        // Creates program scope for this Ballerina program
        BLangProgram bLangProgram = new BLangProgram(globalScope, nativeScope, BLangProgram.Category.LIBRARY_PROGRAM);
        bLangProgram.setProgramFilePath(sourcePath);

        BLangPackage[] bLangPackages = loadPackages(programDirPath, sourcePath, bLangProgram, builtinPkgRepositories);
        for (BLangPackage bLangPackage : bLangPackages) {
            bLangProgram.addLibraryPackage(bLangPackage);
            bLangProgram.define(new SymbolName(bLangPackage.getPackagePath()), bLangPackage);
        }

        // Analyze the semantic properties of the Ballerina program
        if (!disableSemanticAnalyzer) {
            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(bLangProgram);
            bLangProgram.accept(semanticAnalyzer);
        }

        return bLangProgram;
    }

    public BLangProgramLoader disableSemanticAnalyzer() {
        this.disableSemanticAnalyzer = true;
        return this;
    }

    public BLangProgramLoader setPackageRepository(PackageRepository packageRepository) {
        this.packageRepository = packageRepository;
        return this;
    }

    public BLangProgramLoader addDiagnosticListener(BLangDiagnosticListener diagnosticListener) {
        this.diagnosticListener = diagnosticListener;
        return this;
    }

    private BLangPackage[] loadPackages(Path programDirPath,
                                        Path sourcePath,
                                        BLangProgram bLangProgram,
                                        BuiltinPackageRepository[] builtinPackageRepositories) {

        sourcePath = BLangPrograms.validateAndResolveSourcePath(programDirPath, sourcePath,
                bLangProgram.getProgramCategory());

        PackageRepository packageRepository = new FileSystemPackageRepository(programDirPath,
                builtinPackageRepositories);
        if (Files.isDirectory(sourcePath, LinkOption.NOFOLLOW_LINKS)) {
            Path packagePath = programDirPath.relativize(sourcePath);
            BLangPackage bLangPackage = BLangPackages.loadPackage(packagePath, packageRepository, bLangProgram);
            bLangProgram.addEntryPoint(packagePath.toString());
            return new BLangPackage[]{bLangPackage};

        } else if (sourcePath.toString().endsWith(BLangPrograms.BSOURCE_FILE_EXT)) {
            BLangPackage bLangPackage = BLangPackages.loadFile(sourcePath, packageRepository, bLangProgram);
            bLangProgram.addEntryPoint(sourcePath.getFileName().toString());
            return new BLangPackage[]{bLangPackage};

        } else {
            return loadFromArchive(sourcePath, bLangProgram, builtinPackageRepositories);
        }
    }

    private BLangPackage[] loadFromArchive(Path archivePath, BLangProgram bLangProgram,
                                           BuiltinPackageRepository[] builtinPackageRepositories) {

        try (BLangProgramArchive programArchive = new BLangProgramArchive(archivePath, builtinPackageRepositories)) {

            // Load the program archive
            programArchive.loadArchive();

            String[] entryPoints = programArchive.getEntryPoints();
            if (entryPoints.length > 1 && bLangProgram.getProgramCategory() == BLangProgram.Category.MAIN_PROGRAM) {
                throw new IllegalArgumentException("invalid program archive: " +
                        bLangProgram.getProgramFilePath() + " : multiple entry points");
            }

            List<BLangPackage> bLangPackageList = new ArrayList<>();
            for (String entryPoint : entryPoints) {
                if (entryPoint.endsWith(".bal")) {
                    Path filePath = Paths.get(entryPoint);
                    BLangPackage bLangPackage = BLangPackages.loadFile(filePath, programArchive, bLangProgram);
                    bLangPackageList.add(bLangPackage);
                    bLangProgram.addEntryPoint(filePath.getFileName().toString());
                } else {
                    Path packagePath = Paths.get(entryPoint);
                    BLangPackage bLangPackage = BLangPackages.loadPackage(packagePath, programArchive, bLangProgram);
                    bLangPackageList.add(bLangPackage);
                    bLangProgram.addEntryPoint(packagePath.toString());
                }
            }

            return bLangPackageList.toArray(new BLangPackage[0]);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
