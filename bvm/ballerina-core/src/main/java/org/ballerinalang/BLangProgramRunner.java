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

import org.ballerinalang.bre.bvm.BVMExecutor;
import org.ballerinalang.bre.bvm.BVMScheduler;
import org.ballerinalang.connector.impl.ServerConnectorRegistry;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.persistence.RecoveryTask;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.exceptions.BLangUsageException;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.PrintStream;

import static org.ballerinalang.util.BLangConstants.MAIN_FUNCTION_NAME;
import static org.ballerinalang.util.cli.ArgumentParser.extractEntryFuncArgs;
/**
 * This class contains utilities to execute Ballerina main and service programs.
 *
 * @since 0.8.0
 */
public class BLangProgramRunner {

    public static BValue[] runProgram(ProgramFile programFile, FunctionInfo functionInfo, String[] args) {
        BValue[] bValArgs;
        if (functionInfo != null) {
            bValArgs = extractEntryFuncArgs(functionInfo, args);
        } else {
            bValArgs = new BValue[0];
        }
        return runProgram(programFile, functionInfo, bValArgs);
    }

    public static BValue[] runProgram(ProgramFile programFile, FunctionInfo functionInfo, BValue... args) {
        Debugger debugger = new Debugger(programFile);
        initDebugger(programFile, debugger);
        return runProgram(programFile, debugger, functionInfo, args);
    }

    public static BValue[] runProgram(ProgramFile programFile, Debugger debugger, FunctionInfo functionInfo,
                                      BValue... args) {
        BVMExecutor.invokePackageInitFunctions(programFile);

        BValue[] returnVal = null;

        if (functionInfo != null) {
            returnVal = runMainFunc(programFile, functionInfo, args, debugger);
        }

        if (returnVal != null && returnVal[0] != null && returnVal[0].getType().getTag() == TypeTags.ERROR_TAG) {
            return returnVal;
        }

        if (programFile.isServiceEPAvailable()) {
            executeListenPhase(programFile);
        }
        return returnVal;
    }

    public static BValue[] runProgram(ProgramFile programFile, String... args) {
        return runProgram(programFile, null, args);
    }

    public static BValue[] runProgram(ProgramFile programFile, BValue... args) {
        return runProgram(programFile, null, args);
    }

    public static BValue[] runProgram(ProgramFile programFile) {
        return runProgram(programFile, null, new BValue[0]);
    }

    public static void resumeStates(ProgramFile programFile) {
        new Thread(new RecoveryTask(programFile)).start();
    }

    public static BValue[] runMainFunc(ProgramFile programFile, FunctionInfo functionInfo, BValue[] args,
                                       Debugger debugger) {
        BValue[] entryFuncResult;
        boolean mainRunSuccessful = false;

        PackageInfo entryPkgInfo = programFile.getEntryPackage();
        if (entryPkgInfo == null) {
            throw new BallerinaException("entry module not found in  '" + programFile.getProgramFilePath() + "'");
        }

        try {
            entryFuncResult = BVMExecutor.executeFunction(programFile, functionInfo, args);
            BVMScheduler.waitForStrandCompletion();
            mainRunSuccessful = true;
        } finally {
            if (!mainRunSuccessful || !programFile.isServiceEPAvailable()) {
                if (debugger.isDebugEnabled()) {
                    debugger.notifyExit();
                }
                BVMExecutor.stopProgramFile(programFile);
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

    public static FunctionInfo getMainFunctionInfo(PackageInfo entryPkgInfo) {
        String errorMsg = "'main' function not found in '" + entryPkgInfo.getProgramFile().getProgramFilePath() + "'";

        FunctionInfo functionInfo = entryPkgInfo.getFunctionInfo(MAIN_FUNCTION_NAME);
        if (functionInfo == null) {
            throw new BLangUsageException(errorMsg);
        }
        return functionInfo;
    }

    private static void executeListenPhase(ProgramFile programFile) {
        PrintStream outStream = System.out;

        ServerConnectorRegistry serverConnectorRegistry = new ServerConnectorRegistry();
        programFile.setServerConnectorRegistry(serverConnectorRegistry);
        serverConnectorRegistry.initServerConnectors();

        outStream.println("Initiating service(s) in '" + programFile.getProgramFilePath() + "'");
        BVMExecutor.invokePackageStartFunctions(programFile);

        serverConnectorRegistry.deploymentComplete();
    }
}
