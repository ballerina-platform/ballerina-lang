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
package org.ballerinalang.test.utils;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.launcher.LauncherUtils;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ProgramFileReader;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
import org.ballerinalang.util.program.BLangFunctions;
import org.ballerinalang.util.program.BLangPrograms;
import org.testng.Assert;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.programfile.ProgramFileWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    private static Path resourceDir = Paths.get("src/test/resources").toAbsolutePath();

    /**
     * Compile and return the semantic errors.
     * 
     * @param sourceFilePath Path to source package/file
     * @return Semantic errors
     */
    public static CompileResult compile(String sourceFilePath) {
        return compile(sourceFilePath, CompilerPhase.CODE_GEN);
    }

    /**
     * Compile and return the semantic errors.
     * 
     * @param sourceFilePath Path to source package/file
     * @param compilerPhase Compiler phase
     * @return Semantic errors
     */
    public static CompileResult compile(String sourceFilePath, CompilerPhase compilerPhase) {
        Path sourcePath = Paths.get(sourceFilePath);
        String sourceFile = sourcePath.getFileName().toString();
        Path sourceRoot = resourceDir.resolve(sourcePath.getParent());

        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(SOURCE_ROOT, sourceRoot.toString());
        options.put(COMPILER_PHASE, compilerPhase.toString());
        options.put(PRESERVE_WHITESPACE, "false");

        CompileResult comResult = new CompileResult();

        // catch errors
        DiagnosticListener listener = diagnostic -> comResult.addDiagnostic(diagnostic);
        context.put(DiagnosticListener.class, listener);

        // compile
        Compiler compiler = Compiler.getInstance(context);
        compiler.compile(sourceFile);
        org.wso2.ballerinalang.programfile.ProgramFile programFile = compiler.getCompiledProgram();

        if (programFile != null) {
            comResult.setProgFile(getExecutableProgram(programFile));
        }

        return comResult;
    }

    /**
     * Invoke a ballerina function.
     *
     * @param compileResult CompileResult instance
     * @param functionName Name of the function to invoke
     * @param args Input parameters for the function
     * @return return values of the function
     */
    public static BValue[] invoke(CompileResult compileResult, String functionName, BValue[] args) {
        if (compileResult.getErrorCount() > 0) {
            throw new IllegalStateException("compilation contains errors.");
        }
        ProgramFile programFile = compileResult.getProgFile();
        return BLangFunctions.invokeNew(programFile, programFile.getEntryPkgName(), functionName, args);
    }

    /**
     * Compile and run a ballerina file.
     * 
     * @param sourceFilePath Path to the ballerina file.
     */
    public static void run(String sourceFilePath) {
        // TODO: improve. How to get the output
        CompileResult result = compile(sourceFilePath);
        ProgramFile programFile = result.getProgFile();

        // If there is no main or service entry point, throw an error
        if (!programFile.isMainEPAvailable() && !programFile.isServiceEPAvailable()) {
            throw new RuntimeException("main function not found in '" + programFile.getProgramFilePath() + "'");
        }

        if (programFile.isMainEPAvailable()) {
            LauncherUtils.runMain(programFile, new String[0]);
        } else {
            LauncherUtils.runServices(programFile);
        }
    }

    /**
     * Assert an error.
     * 
     * @param result Result from compilation
     * @param errorIndex Index of the error in the result
     * @param expectedErrMsg Expected error message
     * @param expectedErrLine Expected line number of the error
     * @param expectedErrCol Expected column number of the error
     */
    public static void validateError(CompileResult result, int errorIndex, String expectedErrMsg, int expectedErrLine,
                                   int expectedErrCol) {
        Diagnostic diag = result.getDiagnostics()[errorIndex];
        Assert.assertEquals(diag.getMessage(), expectedErrMsg, "incorrect error message:");
        Assert.assertEquals(diag.getPosition().getStartLine(), expectedErrLine, "incorrect line number:");
        Assert.assertEquals(diag.getPosition().startColumn(), expectedErrCol, "incorrect column position:");
    }

    private static ProgramFile getExecutableProgram(org.wso2.ballerinalang.programfile.ProgramFile programFile) {
        ByteArrayInputStream byteIS = null;
        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        try {
            ProgramFileWriter.writeProgram(programFile, byteOutStream);

            // Populate the global scope
            BLangPrograms.loadBuiltinTypes();

            // Populate the native function/actions
            BLangPrograms.populateNativeScope();

            ProgramFileReader reader = new ProgramFileReader();
            byteIS = new ByteArrayInputStream(byteOutStream.toByteArray());
            return reader.readProgram(byteIS);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (byteIS != null) {
                try {
                    byteIS.close();
                } catch (IOException ignore) {
                }
            }

            try {
                byteOutStream.close();
            } catch (IOException ignore) {
            }
        }
    }
}
