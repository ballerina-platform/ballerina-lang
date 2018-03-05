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
import org.wso2.ballerinalang.CompiledBinaryFile;
import org.wso2.ballerinalang.compiler.codegen.CodeGenerator;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.programfile.PackageFileWriter;
import org.wso2.ballerinalang.programfile.ProgramFileWriter;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Write a compiled executable program(.balx) or a compiled package(balo.) to a file.
 *
 * @since 0.965.0
 */
public class BinaryFileWriter {

    private static final CompilerContext.Key<BinaryFileWriter> BINARY_FILE_WRITER_KEY =
            new CompilerContext.Key<>();

    private CompilerOptions options;
    private CodeGenerator codeGenerator;

    public static final String BLANG_SRC_FILE_EXT = "bal";
    public static final String BLANG_SRC_FILE_SUFFIX = "." + BLANG_SRC_FILE_EXT;

    public static final String BLANG_EXEC_FILE_EXT = "balx";
    public static final String BLANG_EXEC_FILE_SUFFIX = "." + BLANG_EXEC_FILE_EXT;

    public static final String BLANG_COMPILED_PACKAGE_FILE_EXT = "balo";
    public static final String BLANG_COMPILED_PACKAGE_FILE_SUFFIX = "." + BLANG_COMPILED_PACKAGE_FILE_EXT;

    private String projectDir;

    public static BinaryFileWriter getInstance(CompilerContext context) {
        BinaryFileWriter binaryFileWriter = context.get(BINARY_FILE_WRITER_KEY);
        if (binaryFileWriter == null) {
            binaryFileWriter = new BinaryFileWriter(context);
        }
        return binaryFileWriter;
    }

    private BinaryFileWriter(CompilerContext context) {
        context.put(BINARY_FILE_WRITER_KEY, this);

        this.options = CompilerOptions.getInstance(context);
        this.codeGenerator = CodeGenerator.getInstance(context);

        this.projectDir = options.get(CompilerOptionName.PROJECT_DIR);
    }

    public void writeLibraryPackage(BLangPackage bLangPackage) {
    }

    public void writeExecutableBinary(BLangPackage bLangPackage) {
        CompiledBinaryFile compiledBinaryFile;
        boolean buildCompiledPkg = Boolean.parseBoolean(options.get(CompilerOptionName.BUILD_COMPILED_PACKAGE));
        if (buildCompiledPkg) {
            compiledBinaryFile = this.codeGenerator.generateBALO(bLangPackage);
        } else {
            compiledBinaryFile = this.codeGenerator.generateBALX(bLangPackage);
        }

        Path targetBinaryFilePath = getTargetFilePath(bLangPackage.loadedFilePath,
                Paths.get(projectDir), buildCompiledPkg);

        if (buildCompiledPkg) {
            writeBALO(targetBinaryFilePath, (CompiledBinaryFile.PackageFile) compiledBinaryFile);
        } else {
            writeBALX(targetBinaryFilePath, (CompiledBinaryFile.ProgramFile) compiledBinaryFile);
        }

    }

    private static void writeBALX(Path targetBinaryFilePath, CompiledBinaryFile.ProgramFile programFile) {
        try {
            ProgramFileWriter.writeProgram(programFile, targetBinaryFilePath);
        } catch (Throwable e) {
            throw new RuntimeException("error writing program file '" + targetBinaryFilePath + "'", e);
        }
    }

    private static void writeBALO(Path targetBinaryFilePath, CompiledBinaryFile.PackageFile packageFile) {
        try {
            PackageFileWriter.writePackage(packageFile, targetBinaryFilePath);
        } catch (Throwable e) {
            throw new RuntimeException("error writing package file '" + targetBinaryFilePath.toString() + "'", e);
        }
    }

    private static Path getTargetFilePath(Path packagePath, Path targetPath, boolean buildCompiledPackageFile) {
        if (buildCompiledPackageFile) {
            return getTargetFilePath(packagePath, targetPath, BLANG_COMPILED_PACKAGE_FILE_SUFFIX);
        } else {
            return getTargetFilePath(packagePath, targetPath, BLANG_EXEC_FILE_SUFFIX);
        }
    }

    private static Path getTargetFilePath(Path packagePath, Path targetPath, final String fileExt) {
        Path filePath = packagePath.getFileName();

        if (filePath == null) {
            throw new RuntimeException("invalid target file path");
        }

        String fileName = filePath.toString();
        if (fileName.endsWith(BLANG_SRC_FILE_EXT)) {
            filePath = Paths.get(fileName.substring(0, fileName.length() - BLANG_SRC_FILE_SUFFIX.length()));
        }

        if (!filePath.toString().endsWith(fileExt)) {
            filePath = targetPath.resolve(filePath.getFileName() + fileExt);

        }
        return filePath;
    }
}
