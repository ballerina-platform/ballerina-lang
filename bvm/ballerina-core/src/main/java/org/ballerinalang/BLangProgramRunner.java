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
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.persistence.RecoveryTask;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.Arrays;

import static org.ballerinalang.util.BLangConstants.MAIN_FUNCTION_NAME;
import static org.ballerinalang.util.cli.ArgumentParser.extractEntryFuncArgs;
/**
 * This class contains utilities to execute Ballerina main and service programs.
 *
 * @since 0.8.0
 */
public class BLangProgramRunner {

    /**
     * API for executing a Ballerina program with a function named main() executed as the main phase.
     *
     * @param programFile Program to be executed
     * @param args        Arguments to the program as a String array
     * @return The value returned from the main() function
     */
    public static BValue runProgram(ProgramFile programFile, String[] args) {
        PackageInfo entryPkgInfo = programFile.getEntryPackage();
        if (entryPkgInfo == null) {
            throw new BallerinaException("entry module not found in  '" + programFile.getProgramFilePath() + "'");
        }
        FunctionInfo mainFunction = entryPkgInfo.getFunctionInfo(MAIN_FUNCTION_NAME);
        BValue[] bValArgs = parseEntryFuncArgs(mainFunction, args);
        return runProgram(programFile, mainFunction, bValArgs);
    }

    /**
     * API for executing a Ballerina program with a function named main() executed as the main phase.
     *
     * @param programFile Program to be executed
     * @param args        Arguments to the program as a BValue array
     * @return The value returned from the main() function
     */
    public static BValue runProgram(ProgramFile programFile, BValue[] args) {
        PackageInfo entryPkgInfo = programFile.getEntryPackage();
        if (entryPkgInfo == null) {
            throw new BallerinaException("entry module not found in  '" + programFile.getProgramFilePath() + "'");
        }
        FunctionInfo mainFunction = entryPkgInfo.getFunctionInfo(MAIN_FUNCTION_NAME);
        return runProgram(programFile, mainFunction, args);
    }

    /**
     * API for executing a Ballerina program with the provided function as the function to be executed in the main
     * phase.
     *
     * @param programFile   Program to be executed
     * @param debugger      A Debugger instance to be used with the program
     * @param entryFunction Function to be executed in the main phase
     * @param args          Arguments for the program
     * @return Value returned from the entry function during main phase
     */
    public static BValue runProgram(ProgramFile programFile, Debugger debugger, FunctionInfo entryFunction,
                                    BValue... args) {
        BVMExecutor.invokePackageInitFunctions(programFile);

        BValue returnVal;
        boolean mainRunSuccessful = false;
        try {
            returnVal = runMainFunction(programFile, entryFunction, args);
            mainRunSuccessful = true;
        } finally {
            if (!mainRunSuccessful || !programFile.isServiceEPAvailable()) {
                stopProgram(programFile, debugger);
            }
        }

        if (returnVal != null && returnVal.getType().getTag() == TypeTags.ERROR_TAG) {
            stopProgram(programFile, debugger);
            return returnVal;
        }

        if (programFile.isServiceEPAvailable()) {
            executeListenPhase(programFile);
        }

        return returnVal;
    }

    // TODO: 4/22/19 Remove this/make this private
    @Deprecated
    public static BValue runProgram(ProgramFile programFile, FunctionInfo entryFunction, BValue... args) {
        Debugger debugger = new Debugger(programFile);
        initDebugger(programFile, debugger);
        return runProgram(programFile, debugger, entryFunction, args);
    }

    public static void resumeStates(ProgramFile programFile) {
        new Thread(new RecoveryTask(programFile)).start();
    }

    private static BValue runMainFunction(ProgramFile programFile, FunctionInfo entryFunction, BValue[] args) {
        if (entryFunction == null) {
            return null;
        }
        BValue[] entryFuncResult = BVMExecutor.executeFunction(programFile, entryFunction, args);
        BVMScheduler.waitForStrandCompletion();
        return entryFuncResult[0];
    }

    private static void initDebugger(ProgramFile programFile, Debugger debugger) {
        programFile.setDebugger(debugger);
        if (debugger.isDebugEnabled()) {
            debugger.init();
            debugger.waitTillDebuggeeResponds();
        }
    }

    private static void executeListenPhase(ProgramFile programFile) {
        ServerConnectorRegistry serverConnectorRegistry = new ServerConnectorRegistry();
        programFile.setServerConnectorRegistry(serverConnectorRegistry);
        serverConnectorRegistry.initServerConnectors();
        BVMExecutor.invokePackageStartFunctions(programFile);
        serverConnectorRegistry.deploymentComplete();
    }

    private static BValue[] parseEntryFuncArgs(FunctionInfo entryFunction, String[] args) {
        BValue[] bValArgs;
        if (entryFunction != null) {
            bValArgs = extractEntryFuncArgs(entryFunction, args);
        } else {
            bValArgs = Arrays.stream(args).map(BString::new).toArray(BValue[]::new);
        }
        return bValArgs;
    }

    private static void stopProgram(ProgramFile programFile, Debugger debugger) {
        if (debugger.isDebugEnabled()) {
            debugger.notifyExit();
        }
        BVMExecutor.stopProgramFile(programFile);
    }
}
