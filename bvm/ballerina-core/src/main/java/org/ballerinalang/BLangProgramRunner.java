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

import org.ballerinalang.model.values.BValue;
import org.ballerinalang.persistence.RecoveryTask;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.exceptions.BLangUsageException;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;

import static org.ballerinalang.util.BLangConstants.MAIN_FUNCTION_NAME;
import static org.ballerinalang.util.cli.ArgumentParser.extractEntryFuncArgs;
/**
 * This class contains utilities to execute Ballerina main and service programs.
 *
 * @since 0.8.0
 */
public class BLangProgramRunner {

    public static void runService(ProgramFile programFile) {
        if (!programFile.isServiceEPAvailable()) {
            throw new BallerinaException("no services found in '" + programFile.getProgramFilePath() + "'");
        }

        // Get the service package
        PackageInfo servicesPackage = programFile.getEntryPackage();
        if (servicesPackage == null) {
            throw new BallerinaException("no services found in '" + programFile.getProgramFilePath() + "'");
        }

        Debugger debugger = new Debugger(programFile);
        initDebugger(programFile, debugger);

        BLangFunctions.invokePackageInitFunctions(programFile);
        BLangFunctions.invokePackageStartFunctions(programFile);
    }

    public static void resumeStates(ProgramFile programFile) {
        new Thread(new RecoveryTask(programFile)).start();
    }

    public static BValue[] runEntryFunc(ProgramFile programFile, String functionName, String[] args) {
        BValue[] entryFuncResult;
        if (MAIN_FUNCTION_NAME.equals(functionName) && !programFile.isMainEPAvailable()) {
            throw new BallerinaException("main function not found in  '" + programFile.getProgramFilePath() + "'");
        }
        PackageInfo entryPkgInfo = programFile.getEntryPackage();
        if (entryPkgInfo == null) {
            throw new BallerinaException("entry module not found in  '" + programFile.getProgramFilePath() + "'");
        }
        Debugger debugger = new Debugger(programFile);
        initDebugger(programFile, debugger);

        FunctionInfo functionInfo = getEntryFunctionInfo(entryPkgInfo, functionName);
        try {
            entryFuncResult = BLangFunctions.invokeEntrypointCallable(programFile, functionInfo,
                                                                      extractEntryFuncArgs(functionInfo, args));
        } finally {
            if (!programFile.isServiceEPAvailable()) {
                if (debugger.isDebugEnabled()) {
                    debugger.notifyExit();
                }
                BLangFunctions.invokePackageStopFunctions(programFile);
            }
        }
        return entryFuncResult;
    }

    private static void initDebugger(ProgramFile programFile, Debugger debugger) {
        programFile.setDebugger(debugger);
        if (debugger.isDebugEnabled()) {
            debugger.init();
            debugger.waitTillDebuggeeResponds();
        }
    }

    public static FunctionInfo getEntryFunctionInfo(PackageInfo entryPkgInfo, String functionName) {
        String errorMsg = "'" + functionName + "' function not found in '"
                            + entryPkgInfo.getProgramFile().getProgramFilePath() + "'";

        FunctionInfo functionInfo = entryPkgInfo.getFunctionInfo(functionName);
        if (functionInfo == null) {
            throw new BLangUsageException(errorMsg);
        }

        if (!functionInfo.isPublic()) {
            throw new BLangUsageException("non public function '" + functionName + "' not allowed as entry function");
        }

        return functionInfo;
    }
}
