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
package org.ballerinalang.packerina;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.wso2.ballerinalang.CompiledBinaryFile.PackageFile;
import org.wso2.ballerinalang.CompiledBinaryFile.ProgramFile;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.programfile.PackageFileWriter;
import org.wso2.ballerinalang.programfile.ProgramFileWriter;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinalang.compiler.CompilerOptionName.BUILD_COMPILED_PACKAGE;
import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.SOURCE_ROOT;
import static org.ballerinalang.compiler.CompilerOptionName.TARGET_BINARY_PATH;
import static org.ballerinalang.util.BLangConstants.BLANG_COMPILED_PACKAGE_FILE_SUFFIX;
import static org.ballerinalang.util.BLangConstants.BLANG_EXEC_FILE_SUFFIX;
import static org.ballerinalang.util.BLangConstants.BLANG_SRC_FILE_EXT;
import static org.ballerinalang.util.BLangConstants.BLANG_SRC_FILE_SUFFIX;

/**
 * This class provides util methods for building Ballerina programs and packages.
 *
 * @since 0.95.2
 */
public class BuilderUtils {

    public static void compileAndWrite(Path sourceRootPath, Path packagePath,
                                       Path targetPath, boolean buildCompiledPkg) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(SOURCE_ROOT, sourceRootPath.toString());
        options.put(COMPILER_PHASE, CompilerPhase.CODE_GEN.toString());
        options.put(PRESERVE_WHITESPACE, "false");
        options.put(BUILD_COMPILED_PACKAGE, Boolean.toString(buildCompiledPkg));

        Path targetBinaryFilePath = getTargetFilePath(packagePath, targetPath, buildCompiledPkg);
        options.put(TARGET_BINARY_PATH, targetBinaryFilePath.toAbsolutePath().toString());

        Compiler compiler = Compiler.getInstance(context);
        compiler.compile(packagePath.toString());
        if (buildCompiledPkg) {
            writeBALO(targetBinaryFilePath, compiler.getCompiledPackage());
        } else {
            writeBALX(targetBinaryFilePath, compiler.getCompiledProgram());
        }
    }

    private static void writeBALX(Path targetBinaryFilePath, ProgramFile programFile) {
        try {
            ProgramFileWriter.writeProgram(programFile, targetBinaryFilePath);
        } catch (Throwable e) {
            throw new BLangRuntimeException("ballerina: error writing program file '" +
                    targetBinaryFilePath + "'", e);
        }
    }

    private static void writeBALO(Path targetBinaryFilePath, PackageFile packageFile) {
        try {
            PackageFileWriter.writePackage(packageFile, targetBinaryFilePath);
        } catch (Throwable e) {
            throw new BLangRuntimeException("ballerina: error writing package file '" +
                    targetBinaryFilePath.toString() + "'", e);
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
        Path targetFilePath = targetPath;
        if (targetFilePath == null) {
            targetFilePath = packagePath.getFileName();
        }

        if (targetFilePath == null) {
            throw new BLangRuntimeException("ballerina: invalid target file path");
        }

        String fileName = targetFilePath.toString();
        if (fileName.endsWith(BLANG_SRC_FILE_EXT)) {
            targetFilePath = Paths.get(fileName.substring(0, fileName.length() - BLANG_SRC_FILE_SUFFIX.length()));
        }

        if (!targetFilePath.toString().endsWith(fileExt)) {
            targetFilePath = targetFilePath.resolveSibling(targetFilePath.getFileName() + fileExt);
        }
        return targetFilePath;
    }
}
