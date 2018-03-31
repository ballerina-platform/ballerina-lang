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

import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;
import org.wso2.ballerinalang.compiler.util.CompilerUtils;

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

        boolean distributedTxEnabled = CompilerUtils.isDistributedTransactionsEnabled();
        programFile.setDistributedTransactionEnabled(distributedTxEnabled);

        // Invoke package init function
        BLangFunctions.invokePackageInitFunction(servicesPackage.getInitFunctionInfo());

        BLangFunctions.invokeVMUtilFunction(servicesPackage.getStartFunctionInfo());
    }

    public static void runMain(ProgramFile programFile, String[] args) {
        if (!programFile.isMainEPAvailable()) {
            throw new BallerinaException("main function not found in  '" + programFile.getProgramFilePath() + "'");
        }
        PackageInfo mainPkgInfo = programFile.getEntryPackage();
        if (mainPkgInfo == null) {
            throw new BallerinaException("main function not found in  '" + programFile.getProgramFilePath() + "'");
        }
        Debugger debugger = new Debugger(programFile);
        initDebugger(programFile, debugger);

        boolean distributedTxEnabled = CompilerUtils.isDistributedTransactionsEnabled();
        programFile.setDistributedTransactionEnabled(distributedTxEnabled);

        FunctionInfo mainFuncInfo = getMainFunction(mainPkgInfo);
        try {
            BLangFunctions.invokeEntrypointCallable(programFile, mainPkgInfo, mainFuncInfo, extractMainArgs(args));
        } finally {
            if (debugger.isDebugEnabled()) {
                debugger.notifyExit();
            }
            BLangFunctions.invokeVMUtilFunction(mainPkgInfo.getStopFunctionInfo());
        }
    }

    private static BValue[] extractMainArgs(String[] args) {
        BStringArray arrayArgs = new BStringArray();
        for (int i = 0; i < args.length; i++) {
            arrayArgs.add(i, args[i]);
        }
        return new BValue[] {arrayArgs};
    }

    private static void initDebugger(ProgramFile programFile, Debugger debugger) {
        programFile.setDebugger(debugger);
        if (debugger.isDebugEnabled()) {
            debugger.init();
            debugger.waitTillDebuggeeResponds();
        }
    }

    public static FunctionInfo getMainFunction(PackageInfo mainPkgInfo) {
        String errorMsg = "main function not found in  '" +
                mainPkgInfo.getProgramFile().getProgramFilePath() + "'";

        FunctionInfo mainFuncInfo = mainPkgInfo.getFunctionInfo("main");
        if (mainFuncInfo == null) {
            throw new BallerinaException(errorMsg);
        }

        BType[] paramTypes = mainFuncInfo.getParamTypes();
        BType[] retParamTypes = mainFuncInfo.getRetParamTypes();
        BArrayType argsType = new BArrayType(BTypes.typeString);
        if (paramTypes.length != 1 || !paramTypes[0].equals(argsType) || retParamTypes.length != 0) {
            throw new BallerinaException(errorMsg);
        }

        return mainFuncInfo;
    }
}
