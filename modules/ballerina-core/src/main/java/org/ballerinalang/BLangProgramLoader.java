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

import org.ballerinalang.util.program.BLangPackages;
import org.ballerinalang.util.repository.BLangProgramArchive;
import org.ballerinalang.util.repository.FileSystemPackageRepository;
import org.ballerinalang.util.repository.PackageRepository;
import org.wso2.ballerina.core.model.BLangPackage;
import org.wso2.ballerina.core.model.BLangProgram;
import org.wso2.ballerina.core.model.GlobalScope;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.types.BTypes;
import org.wso2.ballerina.core.runtime.internal.BuiltInNativeConstructLoader;
import org.wso2.ballerina.core.semantics.SemanticAnalyzer;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

/**
 * @since 0.8.0
 */
public class BLangProgramLoader {

    private boolean disableSemanticAnalyzer = false;

    public BLangProgram loadArchive(Path archivePath) {
        if (archivePath == null) {
            throw new IllegalStateException("program archive cannot be null");
        }

        if (!archivePath.getFileName().toString().endsWith(".bpz")) {
            throw new IllegalStateException("invalid ballerina program archive");
        }

        try {
            archivePath = archivePath.toRealPath(LinkOption.NOFOLLOW_LINKS);
        } catch (NoSuchFileException x) {
            System.out.println("No such file");
            throw new RuntimeException(x.getMessage(), x);
        } catch (IOException e) {
            // TODO
            throw new RuntimeException(e.getMessage(), e);
        }

        // Get the global scope
        GlobalScope globalScope = GlobalScope.getInstance();
        BTypes.loadBuiltInTypes(globalScope);
        BuiltInNativeConstructLoader.loadConstructs(globalScope);

        // Creates program scope for this Ballerina program
        BLangProgram bLangProgram = new BLangProgram(globalScope, archivePath);

        BLangPackage mainPackage;
        try (BLangProgramArchive programArchive = new BLangProgramArchive(archivePath)) {
            programArchive.loadArchive();
            String entryPoint = programArchive.getEntryPoint();
            if (entryPoint.endsWith(".bal")) {
                Path filePath = Paths.get(entryPoint);
                mainPackage = BLangPackages.loadFile(filePath, programArchive, bLangProgram);
                bLangProgram.setEntryPoint(filePath.getFileName().toString());
            } else {
                Path packagePath = Paths.get(entryPoint);
                mainPackage = BLangPackages.loadPackage(packagePath, programArchive, bLangProgram);
                bLangProgram.setEntryPoint(packagePath.toString());
            }

        } catch (Exception e) {
            // TODO Handle
            throw new RuntimeException(e.getMessage(), e);
        }

        bLangProgram.define(new SymbolName(mainPackage.getPackagePath()), mainPackage);

        // TODO Find cyclic dependencies
        bLangProgram.setMainPackage(mainPackage);

        // Analyze the semantic properties of the Ballerina program
        if (!disableSemanticAnalyzer) {
            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(bLangProgram);
            bLangProgram.accept(semanticAnalyzer);
        }

        return bLangProgram;
    }

    public BLangProgram load(Path programDirPath, Path sourcePath) {
        if (programDirPath == null) {
            throw new IllegalStateException("program directory cannot be null");
        }

        if (sourcePath == null) {
            throw new IllegalStateException("source path cannot be null");
        }


        try {
            programDirPath = programDirPath.toRealPath(LinkOption.NOFOLLOW_LINKS);
            sourcePath = programDirPath.resolve(sourcePath).toRealPath();

            if (!sourcePath.startsWith(programDirPath)) {
                // TODO Throw error  given source package or file should be inside the program directory
                throw new IllegalStateException("given source package or file should be inside the program directory");
            }

        } catch (NoSuchFileException x) {
            System.out.println("No such file");
            throw new RuntimeException(x.getMessage(), x);
        } catch (IOException e) {
            // TODO handle errors properly
            throw new RuntimeException(e.getMessage(), e);
        }

        // Get the global scope
        GlobalScope globalScope = GlobalScope.getInstance();
        BTypes.loadBuiltInTypes(globalScope);
        BuiltInNativeConstructLoader.loadConstructs(globalScope);

        PackageRepository packageRepository = new FileSystemPackageRepository(programDirPath);

        // Creates program scope for this Ballerina program
        BLangProgram bLangProgram = new BLangProgram(globalScope, programDirPath);

        BLangPackage mainPackage;
        if (Files.isDirectory(sourcePath, LinkOption.NOFOLLOW_LINKS)) {
            Path packagePath = programDirPath.relativize(sourcePath);
            mainPackage = BLangPackages.loadPackage(packagePath, packageRepository, bLangProgram);
            bLangProgram.setEntryPoint(packagePath.toString());

        } else {
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*.bal");
            if (!matcher.matches(sourcePath.getFileName())) {
                throw new RuntimeException("given source file is not a .bal file");
            }

            mainPackage = BLangPackages.loadFile(sourcePath, packageRepository, bLangProgram);
            bLangProgram.setEntryPoint(sourcePath.getFileName().toString());
        }

        bLangProgram.define(new SymbolName(mainPackage.getPackagePath()), mainPackage);

        // TODO Find cyclic dependencies
        bLangProgram.setMainPackage(mainPackage);

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

    public BLangProgramLoader addPackageRepository() {
        return this;
    }

    public BLangProgramLoader addErrorListener() {
        // TODO
        return this;
    }
}
