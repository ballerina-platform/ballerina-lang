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
import org.ballerinalang.util.BLangConstants;
import org.ballerinalang.util.codegen.CodeGenerator;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ProgramFileWriter;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.program.BLangFunctions;
import org.ballerinalang.util.program.BLangPackages;
import org.ballerinalang.util.program.BLangPrograms;
import org.ballerinalang.util.repository.ProgramDirRepository;
import org.ballerinalang.util.semantics.SemanticAnalyzer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Ballerina compiler implementation. This class compiles ballerina sources and produces a {@code ProgramFile}
 * which is the in-memory representation of a compiled Ballerina program.
 *
 * @since 0.90
 */
public class BLangCompiler {

    public static ProgramFile compile(Path sourceRootPath, Path packagePath) {
        sourceRootPath = BLangPrograms.validateAndResolveProgramDirPath(sourceRootPath);

        // Get the global scope
        GlobalScope globalScope = BLangPrograms.populateGlobalScope();
        NativeScope nativeScope = BLangPrograms.populateNativeScope();

        // Create program repository
        ProgramDirRepository programDirRepo = BLangPrograms.initProgramDirRepository(sourceRootPath);

        // Creates program scope for this Ballerina program
        BLangProgram bLangProgram = new BLangProgram(globalScope, nativeScope);
        bLangProgram.setProgramFilePath(packagePath);

        // Load entry package
        BLangPackage entryPackage = BLangPackages.loadEntryPackage(sourceRootPath,
                packagePath, bLangProgram, programDirRepo);
        bLangProgram.setEntryPackage(entryPackage);
        bLangProgram.define(new SymbolName(entryPackage.getPackagePath()), entryPackage);

        // Analyze the semantic properties of the Ballerina program
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(bLangProgram);
        bLangProgram.accept(semanticAnalyzer);

        CodeGenerator codeGenerator = new CodeGenerator();
        bLangProgram.accept(codeGenerator);
        ProgramFile programFile = codeGenerator.getProgramFile();
        programFile.setProgramFilePath(packagePath);

        // Resolve main and service packages
        setEntryPoints(programFile);
        return programFile;
    }

    public static void compileAndWrite(Path sourceRootPath, Path packagePath, Path targetPath) {
        ProgramFile compiledProgram = compile(sourceRootPath, packagePath);

        Path balxFilePath = getTargetBALXFilePath(packagePath, targetPath);

        try {
            ProgramFileWriter.writeProgram(compiledProgram, balxFilePath);
        } catch (Throwable e) {
            throw new BLangRuntimeException("ballerina: error writing program file '" +
                    balxFilePath.toString() + "'", e);
        }
    }

    private static void setEntryPoints(ProgramFile programFile) {
        PackageInfo entryPackage = programFile.getEntryPackage();
        FunctionInfo mainFunc = BLangFunctions.getMainFunction(entryPackage);
        if (mainFunc != null) {
            programFile.setMainEPAvailable(true);
        }

        if (entryPackage.getServiceInfoEntries().length != 0) {
            programFile.setServiceEPAvailable(true);
        }
    }

    private static Path getTargetBALXFilePath(Path packagePath, Path targetPath) {
        Path balxFilePath;
        if (targetPath != null) {
            balxFilePath = targetPath;
        } else {
            Path lastName = packagePath.getFileName();
            // lastName cannot be null here.
            String fileName = lastName != null ? lastName.toString() : "";
            if (fileName.endsWith(BLangConstants.BLANG_SRC_FILE_SUFFIX)) {
                balxFilePath = Paths.get(fileName.substring(0, fileName.length() -
                        BLangConstants.BLANG_SRC_FILE_SUFFIX.length()));
            } else {
                balxFilePath = packagePath.getName(packagePath.getNameCount() - 1);
            }
        }

        String balxFilePathStr = balxFilePath.toString();
        if (!balxFilePathStr.endsWith(BLangConstants.BLANG_EXEC_FILE_SUFFIX)) {
            balxFilePath = balxFilePath.resolveSibling(balxFilePath.getFileName() +
                    BLangConstants.BLANG_EXEC_FILE_SUFFIX);
        }
        return balxFilePath;
    }
}
