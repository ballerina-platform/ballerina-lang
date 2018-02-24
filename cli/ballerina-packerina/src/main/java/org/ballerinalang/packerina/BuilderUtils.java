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
import org.ballerinalang.util.BLangConstants;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.programfile.ProgramFile;
import org.wso2.ballerinalang.programfile.ProgramFileWriter;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinalang.compiler.CompilerOptionName.BUILD_COMPILED_PACKAGE;
import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.SOURCE_ROOT;
import static org.ballerinalang.compiler.CompilerOptionName.TARGET_BINARY_PATH;

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

        Path targetBinaryFilePath = getTargetBALXFilePath(packagePath, targetPath);
        options.put(TARGET_BINARY_PATH, targetBinaryFilePath.toAbsolutePath().toString());

        // compile
        Compiler compiler = Compiler.getInstance(context);
        compiler.compile(packagePath.toString());
        ProgramFile programFile = compiler.getCompiledProgram();

        try {
            ProgramFileWriter.writeProgram(programFile, targetBinaryFilePath);
        } catch (Throwable e) {
            throw new BLangRuntimeException("ballerina: error writing program file '" +
                    targetBinaryFilePath.toString() + "'", e);
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
                balxFilePath = Paths
                        .get(fileName.substring(0, fileName.length() - BLangConstants.BLANG_SRC_FILE_SUFFIX.length()));
            } else {
                balxFilePath = packagePath.getName(packagePath.getNameCount() - 1);
            }
        }

        String balxFilePathStr = balxFilePath.toString();
        if (!balxFilePathStr.endsWith(BLangConstants.BLANG_EXEC_FILE_SUFFIX)) {
            balxFilePath =
                    balxFilePath.resolveSibling(balxFilePath.getFileName() + BLangConstants.BLANG_EXEC_FILE_SUFFIX);
        }
        return balxFilePath;
    }
}
