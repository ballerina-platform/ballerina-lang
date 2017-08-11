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
import org.ballerinalang.util.program.BLangPackages;
import org.ballerinalang.util.program.BLangPrograms;
import org.ballerinalang.util.repository.BLangProgramArchive;
import org.ballerinalang.util.repository.BuiltinPackageRepository;
import org.ballerinalang.util.repository.PackageRepository;
import org.ballerinalang.util.repository.ProgramDirRepository;
import org.ballerinalang.util.semantics.SemanticAnalyzer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code BLangProgramLoader} creates in-memory representations of a Ballerina program from a given
 * list of source files of packages.
 *
 * @since 0.8.0
 */
public class BLangASTBuilder {
    private boolean disableSemanticAnalyzer = false;
    private PackageRepository packageRepository;
    private BLangDiagnosticListener diagnosticListener;

    public BLangProgram build(Path sourceRootPath, Path packagePath) {
        sourceRootPath = BLangPrograms.validateAndResolveProgramDirPath(sourceRootPath);

        // Get the global scope
        GlobalScope globalScope = BLangPrograms.populateGlobalScope();
        NativeScope nativeScope = BLangPrograms.populateNativeScope();

        ProgramDirRepository programDirRepo = BLangPrograms.initProgramDirRepository(sourceRootPath);

        // Creates program scope for this Ballerina program
        BLangProgram bLangProgram = new BLangProgram(globalScope, nativeScope);
        bLangProgram.setProgramFilePath(packagePath);

        BLangPackage bLangPackage = BLangPackages.loadEntryPackage(sourceRootPath,
                packagePath, bLangProgram, programDirRepo);
        bLangProgram.setEntryPackage(bLangPackage);
        bLangProgram.define(new SymbolName(bLangPackage.getPackagePath()), bLangPackage);

        // Analyze the semantic properties of the Ballerina program
        if (!disableSemanticAnalyzer) {
            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(bLangProgram);
            bLangProgram.accept(semanticAnalyzer);
        }

        return bLangProgram;
    }

    public BLangASTBuilder disableSemanticAnalyzer() {
        this.disableSemanticAnalyzer = true;
        return this;
    }

    public BLangASTBuilder setPackageRepository(PackageRepository packageRepository) {
        this.packageRepository = packageRepository;
        return this;
    }

    public BLangASTBuilder addDiagnosticListener(BLangDiagnosticListener diagnosticListener) {
        this.diagnosticListener = diagnosticListener;
        return this;
    }

    @Deprecated
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
