/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.testerina.util;

import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * Utility methods.
 */
public class Utils {

    private static PrintStream errStream = System.err;


    public static void startService(ProgramFile programFile) {
        if (!programFile.isServiceEPAvailable()) {
            throw new BallerinaException(String.format("no services found in package: %s", programFile
                    .getEntryPkgName()));
        }
        PackageInfo servicesPackage = programFile.getEntryPackage();

        Debugger debugger = new Debugger(programFile);
        initDebugger(programFile, debugger);

        // Invoke package init function
        BLangFunctions.invokePackageInitFunction(servicesPackage.getInitFunctionInfo());

        BLangFunctions.invokeVMUtilFunction(servicesPackage.getStartFunctionInfo());
    }

    /**
     * Cleans up any remaining testerina metadata.
     * @param path The path of the Directory/File to be deleted
     */
    public static void cleanUpDir(Path path) {
        try {
            Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (IOException e) {
            // Do nothing
            errStream.println("Error occurred while deleting the dir : " + path.toString() + " with error : "
                              + e.getMessage());
        }
    }

    private static void initDebugger(ProgramFile programFile, Debugger debugger) {
        programFile.setDebugger(debugger);
        if (debugger.isDebugEnabled()) {
            debugger.init();
            debugger.waitTillDebuggeeResponds();
        }
    }
}
