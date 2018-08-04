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
package org.ballerinalang.test.utils.debug;

import org.ballerinalang.BLangProgramRunner;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.debugger.dto.BreakPointDTO;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.ballerinalang.BLangProgramRunner.MAIN;

/**
 * {@link DebuggerExecutor} represents executor class which runs the main program when debugging.
 *
 * @since 0.88
 */
public class DebuggerExecutor implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(DebuggerExecutor.class);

    private CompileResult result;
    private String functionName;
    private String[] args;
    private TestDebugger debugger;
    private List<BreakPointDTO> breakPoints;
    private PackageInfo entryPkgInfo;

    DebuggerExecutor(CompileResult result, String functionName, String[] args, TestDebugger debugger,
                     List<BreakPointDTO> breakPoints) {
        this.result = result;
        this.functionName = functionName;
        this.args = args;
        this.debugger = debugger;
        this.breakPoints = breakPoints;
        init();
    }

    private void init() {
        ProgramFile programFile = result.getProgFile();

        if ((MAIN.equals(functionName) && !programFile.isMainEPAvailable())) {
            throw new BallerinaException("main function not found in  '" + programFile.getProgramFilePath() + "'");
        }

        // Get the main entry package
        entryPkgInfo = programFile.getEntryPackage();
        if (entryPkgInfo == null) {
            throw new BallerinaException("entry package not found in  '" + programFile.getProgramFilePath() + "'");
        }
    }

    @Override
    public void run() {
        ProgramFile programFile = result.getProgFile();

        programFile.setDebugger(debugger);
        debugger.init();
        debugger.addDebugPoints(breakPoints);
        debugger.releaseLock();
        debugger.waitTillDebuggeeResponds();

        // Prepare main function arguments

        BStringArray arrayArgs = new BStringArray();
        for (int i = 0; i < args.length; i++) {
            arrayArgs.add(i, args[i]);
        }

        // Invoke package init function
        FunctionInfo funcInfo = BLangProgramRunner.getEntryFunctionInfo(entryPkgInfo, functionName);
        try {
            BLangFunctions.invokeEntrypointCallable(programFile, funcInfo, new BValue[]{arrayArgs});
        } catch (Exception e) {
            log.debug("error occurred, invoking the function - " + e.getMessage(), e);
        } finally {
            BLangFunctions.invokeVMUtilFunction(entryPkgInfo.getStopFunctionInfo());
        }

        debugger.notifyExit();
    }
}
