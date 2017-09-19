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
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.Diagnostic.Kind;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnostic;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.SOURCE_ROOT;

/**
 * Utility methods for unit tests
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
    public static String[] compile(String sourceFilePath) {
        return compile(sourceFilePath, CompilerPhase.CODE_ANALYZE);
    }

    /**
     * Compile and return the semantic errors.
     * 
     * @param sourceFilePath Path to source package/file
     * @param compilerPhase Compiler phase
     * @return Semantic errors
     */
    public static String[] compile(String sourceFilePath, CompilerPhase compilerPhase) {
        Path sourcePath = Paths.get(sourceFilePath);
        String sourceFile = sourcePath.getFileName().toString();
        Path sourceRoot = resourceDir.resolve(sourcePath.getParent());

        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(SOURCE_ROOT, sourceRoot.toString());
        options.put(COMPILER_PHASE, compilerPhase.toString());
        options.put(PRESERVE_WHITESPACE, "false");

        // catch errors
        List<String> errors = new ArrayList<>();
        DiagnosticListener listener = diagnostic -> errors.add(getErrorMessage(diagnostic));
        context.put(DiagnosticListener.class, listener);

        Compiler compiler = Compiler.getInstance(context);
        compiler.compile(sourceFile);

        return errors.toArray(new String[errors.size()]);
    }

    private static String getErrorMessage(Diagnostic diagnostic) {
        BDiagnostic diag = (BDiagnostic) diagnostic;
        DiagnosticPos pos = ((BDiagnostic) diagnostic).pos;
        Kind kind = diag.kind;
        switch (kind) {
            case ERROR:
                return "error: " + pos + " " + diag.msg;
            case WARNING:
                return "warning: " + pos + " " + diag.msg;
            case NOTE:
                break;
        }
        return "";
    }
}
