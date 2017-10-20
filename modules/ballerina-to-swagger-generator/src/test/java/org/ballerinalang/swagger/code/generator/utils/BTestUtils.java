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
package org.ballerinalang.swagger.code.generator.utils;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.util.codegen.ProgramFileReader;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.programfile.ProgramFile;
import org.wso2.ballerinalang.programfile.ProgramFileWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.SOURCE_ROOT;

/**
 * Utility methods for Ballerina model unit tests.
 *
 * @since 0.8.0
 */
public class BTestUtils {

    public static org.ballerinalang.util.codegen.ProgramFile getProgramFile(String sourceFilePath) {
        Path programPath;
        try {
            programPath = Paths.get(BTestUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return getCompiledProgram(programPath, Paths.get(sourceFilePath));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("error while running test: " + e.getMessage());
        } 
    }

    private static org.ballerinalang.util.codegen.ProgramFile getCompiledProgram(Path sourceRoot, Path sourcePath) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(SOURCE_ROOT, sourceRoot.toString());
        options.put(COMPILER_PHASE, CompilerPhase.CODE_GEN.toString());
        options.put(PRESERVE_WHITESPACE, "false");

        // compile
        Compiler compiler = Compiler.getInstance(context);
        compiler.compile(sourcePath.toString());

        org.wso2.ballerinalang.programfile.ProgramFile programFile = compiler.getCompiledProgram();

        if (programFile == null) {
            throw new BallerinaException("compilation contains errors");
        }

        org.ballerinalang.util.codegen.ProgramFile progFile = getExecutableProgram(programFile);
        progFile.setProgramFilePath(sourcePath);

        return progFile;
    }

    public static org.ballerinalang.util.codegen.ProgramFile getExecutableProgram(ProgramFile programFile) {
        ByteArrayInputStream byteIS = null;
        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        try {
            ProgramFileWriter.writeProgram(programFile, byteOutStream);

            ProgramFileReader reader = new ProgramFileReader();
            byteIS = new ByteArrayInputStream(byteOutStream.toByteArray());
            return reader.readProgram(byteIS);
        } catch (Throwable e) {
            throw new BallerinaException("error: fail to compile file: " + makeFirstLetterLowerCase(e.getMessage()));
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

    private static String makeFirstLetterLowerCase(String s) {
        if (s == null) {
            return null;
        }
        char c[] = s.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }
}
