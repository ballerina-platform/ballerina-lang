/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.nativeimpl.util;

import org.ballerinalang.BLangASTBuilder;
import org.ballerinalang.BLangCompiler;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ProgramFileReader;
import org.ballerinalang.util.codegen.ProgramFileWriter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility methods for Ballerina Parser.
 *
 * @since 0.8.0
 */
public class BTestUtils {

    private BTestUtils() {
    }

    /**
     * Get parsed, analyzed and linked Ballerina object model.
     *
     * @param sourceFilePath Path to Bal file.
     * @return BLangProgram instance.
     */
    public static BLangProgram parseBalFile(String sourceFilePath) {
        Path programPath;
        try {
            programPath = Paths.get(BTestUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return new BLangASTBuilder().build(programPath,
                    Paths.get(sourceFilePath));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("error while running test: " + e.getMessage());
        }
    }

    public static ProgramFile getProgramFile(String sourceFilePath) {
        Path programPath;
        try {
            programPath = Paths.get(BTestUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            ProgramFile programFile = BLangCompiler.compile(programPath,
                    Paths.get(sourceFilePath));
            Path targetPath;
            Path sourcePath = programPath.resolve(sourceFilePath);
            if (sourcePath.endsWith(".bal")) {
                String sourcePathStr = sourcePath.toString();
                targetPath = Paths.get(sourcePathStr.substring(0, sourcePathStr.length() - 4) + ".balx");
            } else {
                targetPath = Paths.get(sourcePath.getName(sourcePath.getNameCount() - 1).toString() + ".balx");
            }

            targetPath = programPath.resolve(targetPath);
            ProgramFileWriter.writeProgram(programFile, targetPath);
            ProgramFileReader reader = new ProgramFileReader();
            return reader.readProgram(targetPath);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("error while running test: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
