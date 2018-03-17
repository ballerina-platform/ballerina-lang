/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.util;

import org.ballerinalang.BLangProgramRunner;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVM;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.ControlStack;
import org.ballerinalang.bre.bvm.StackFrame;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.runtime.threadpool.ThreadPoolFactory;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ProgramFileReader;
import org.ballerinalang.util.codegen.WorkerInfo;
import org.ballerinalang.util.debugger.DebugContext;
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Util class for packaging both pull and push.
 *
 * @since 0.964
 */
public class ExecutorUtils {

    private static boolean packageInit = false;

    /**
     * Invoke function.
     *
     * @param programFile bal program file
     * @param methodName  method to be invoked
     * @param args        arguments passed to the method
     */
    private static void invokeFunction(ProgramFile programFile, String methodName, String[] args) {
        runFunction(programFile, methodName, args);
        try {
            ThreadPoolFactory.getInstance().getWorkerExecutor().shutdown();
            ThreadPoolFactory.getInstance().getWorkerExecutor().awaitTermination(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignore) {
            // Ignore the error
        }
        Runtime.getRuntime().exit(0);
    }

    /**
     * Invoke package init.
     *
     * @param programFile program file
     * @param packageInfo package info object
     * @param bContext    context object
     */
    private static void initPackage(ProgramFile programFile, PackageInfo packageInfo, Context bContext) {
        if (!packageInit) {
            BLangFunctions.invokePackageInitFunction(programFile, packageInfo.getInitFunctionInfo(), bContext);
            packageInit = true;
        }
    }

    /**
     * Run function from the bal file.
     *
     * @param programFile bal program file
     * @param methodName  method to be invoked
     * @param args        arguments passed to the method
     */
    private static void runFunction(ProgramFile programFile, String methodName, String[] args) {
        PackageInfo pkgInfo = programFile.getEntryPackage();
        Context bContext = new Context(programFile);

        Debugger debugger = new Debugger(programFile);
        initDebugger(bContext, debugger);
        FunctionInfo mainFuncInfo = getFunctionInfo(pkgInfo, methodName);
        initPackage(programFile, pkgInfo, bContext);

        // Prepare function arguments
        BStringArray arrayArgs = new BStringArray();
        for (int i = 0; i < args.length; i++) {
            arrayArgs.add(i, args[i]);
        }

        WorkerInfo defaultWorkerInfo = mainFuncInfo.getDefaultWorkerInfo();

        // Execute workers
        StackFrame callerSF = new StackFrame(pkgInfo, -1, new int[0]);
        callerSF.setRefRegs(new BRefType[1]);
        callerSF.getRefRegs()[0] = arrayArgs;

        StackFrame stackFrame = new StackFrame(mainFuncInfo, defaultWorkerInfo, -1, new int[0]);
        stackFrame.getRefRegs()[0] = arrayArgs;
        ControlStack controlStack = bContext.getControlStack();
        controlStack.pushFrame(stackFrame);
        bContext.startTrackWorker();
        bContext.setStartIP(defaultWorkerInfo.getCodeAttributeInfo().getCodeAddrs());

        BLangVM bLangVM = new BLangVM(programFile);
        bLangVM.run(bContext);
        bContext.await();
        if (debugger.isDebugEnabled()) {
            debugger.notifyExit();
        }
        if (bContext.getError() != null) {
            String stackTraceStr = BLangVMErrors.getPrintableStackTrace(bContext.getError());
            throw new BLangRuntimeException("error: " + stackTraceStr);
        }
    }

    /**
     * Init debugger context.
     *
     * @param bContext context
     * @param debugger debugger
     */
    private static void initDebugger(Context bContext, Debugger debugger) {
        bContext.getProgramFile().setDebugger(debugger);
        if (debugger.isDebugEnabled()) {
            DebugContext debugContext = new DebugContext();
            bContext.setDebugContext(debugContext);
            debugger.init();
            debugger.addDebugContextAndWait(debugContext);
        }
    }

    /**
     * Get function info.
     *
     * @param pkgInfo    package in which the function exists
     * @param methodName name of the method to be invoked
     * @return functionInfo object
     */
    private static FunctionInfo getFunctionInfo(PackageInfo pkgInfo, String methodName) {
        String errorMsg = "function not found in  '" +
                pkgInfo.getProgramFile().getProgramFilePath() + "'";

        FunctionInfo mainFuncInfo = pkgInfo.getFunctionInfo(methodName);
        if (mainFuncInfo == null) {
            throw new BallerinaException(errorMsg);
        }
        return mainFuncInfo;
    }

    /**
     * Run balx that lives within jars
     */
    public static void execute(URI balxResource, String... args) {
        initFileSystem(balxResource);
        Path baloFilePath = Paths.get(balxResource);
        if (baloFilePath != null) {
            ProgramFile programFile = readExecutableProgram(baloFilePath);
            BLangProgramRunner.runMain(programFile, args);
        }
    }

    /**
     * Init file system from jar.
     *
     * @param uri URI of the file
     */

    private static void initFileSystem(URI uri) {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        try {
            FileSystems.newFileSystem(uri, env);
        } catch (Exception ignore) {
        }
    }

    /**
     * Get program file after reading the executable program i.e. balo file.
     *
     * @param baloFilePath path of the balo file
     * @return program file
     */
    private static ProgramFile readExecutableProgram(Path baloFilePath) {
        ByteArrayInputStream byteIS = null;
        try {
            byte[] byteArray = Files.readAllBytes(baloFilePath);
            ProgramFileReader reader = new ProgramFileReader();
            byteIS = new ByteArrayInputStream(byteArray);
            return reader.readProgram(byteIS);
        } catch (IOException ignore) {
        } finally {
            if (byteIS != null) {
                try {
                    byteIS.close();
                } catch (IOException ignore) {
                }
            }
        }
        return null;
    }
}
