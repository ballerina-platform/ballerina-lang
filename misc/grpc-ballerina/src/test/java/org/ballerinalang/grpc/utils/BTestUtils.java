/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.grpc.utils;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.launcher.LauncherUtils;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.SOURCE_ROOT;

/**
 * Utility methods for unit tests.
 *
 * @since 0.94
 */
public class BTestUtils {

    private static Path resourceDir = Paths.get(
            BTestUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath());

    /**
     * Compile and return the semantic errors.
     *
     * @param sourceFilePath Path to source package/file
     * @return Semantic errors
     */
    public static org.ballerinalang.grpc.utils.CompileResult compile(String sourceFilePath) {
        return compile(sourceFilePath, CompilerPhase.CODE_GEN);
    }
    

    /**
     * Compile and return the semantic errors.
     *
     * @param sourceFilePath Path to source package/file
     * @param compilerPhase  Compiler phase
     * @return Semantic errors
     */
    public static CompileResult compile(String sourceFilePath, CompilerPhase compilerPhase) {
        Path sourcePath = Paths.get(sourceFilePath);
        String packageName = sourcePath.getFileName().toString();
        Path sourceRoot = resourceDir.resolve(sourcePath.getParent());
        return compile(sourceRoot.toString(), packageName, compilerPhase);
    }

    /**
     * Compile and return the semantic errors.
     *
     * @param sourceRoot    root path of the source packages
     * @param packageName   name of the package to compile
     * @param compilerPhase Compiler phase
     * @return Semantic errors
     */
    public static CompileResult compile(String sourceRoot, String packageName, CompilerPhase compilerPhase) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(SOURCE_ROOT, resourceDir.resolve(sourceRoot).toString());
        options.put(COMPILER_PHASE, compilerPhase.toString());
        options.put(PRESERVE_WHITESPACE, "false");

        CompileResult comResult = new CompileResult();

        // catch errors
        DiagnosticListener listener = comResult::addDiagnostic;
        context.put(DiagnosticListener.class, listener);

        // compile
        Compiler compiler = Compiler.getInstance(context);
        compiler.compile(packageName);
        org.wso2.ballerinalang.programfile.ProgramFile programFile = compiler.getCompiledProgram();
        if (programFile != null) {
            comResult.setProgFile(LauncherUtils.getExecutableProgram(programFile));
        }

        return comResult;
    }
}
