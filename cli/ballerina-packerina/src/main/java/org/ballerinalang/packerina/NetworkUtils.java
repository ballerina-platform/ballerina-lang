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
package org.ballerinalang.packerina;

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
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Util class for network calls.
 *
 * @since 0.964
 */
public class NetworkUtils {
    private static PrintStream outStream = System.err;
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
     * Pull/Downloads packages from the package repository.
     *
     * @param resourceName        package name to be pulled
     * @param ballerinaCentralURL URL of ballerina central
     */
    public static void pullPackages(String resourceName, String ballerinaCentralURL) {
        Path baloFilePath = null;
        try {
            URI uri = NetworkUtils.class.getResource("/META-INF/ballerina/pull/main.bal.balx").toURI();
            initFileSystem(uri);
            baloFilePath = Paths.get(uri);
        } catch (URISyntaxException e) {
            // Ignore
        }
        if (baloFilePath != null) {
            ProgramFile programFile = readExecutableProgram(baloFilePath);
            String host = getHost(ballerinaCentralURL);
            Path cacheDir = Paths.get("caches").resolve(host);

            // target directory path to .ballerina/cache
            Path targetDirectoryPath = UserRepositoryUtils.initializeUserRepository().resolve(cacheDir);

            int indexOfOrgName = resourceName.indexOf("/");
            if (indexOfOrgName != -1) {
                String orgName = resourceName.substring(0, indexOfOrgName);
                String pkgNameWithVersion = resourceName.substring(indexOfOrgName + 1);

                int indexOfColon = pkgNameWithVersion.indexOf(":");
                String pkgVersion, pkgName;
                if (indexOfColon != -1) {
                    pkgVersion = pkgNameWithVersion.substring(indexOfColon + 1);
                    pkgName = pkgNameWithVersion.substring(0, indexOfColon);
                } else {
                    pkgVersion = "*";
                    pkgName = pkgNameWithVersion;
                }
                Path fullPathOfPkg = Paths.get(orgName).resolve(pkgName).resolve(pkgVersion);

                targetDirectoryPath = targetDirectoryPath.resolve(fullPathOfPkg);
                String dstPath = targetDirectoryPath.toString();

                // Get the current dir path to check if the user is pulling a package from inside a project dir
                Path currentDirPath = Paths.get(".").toAbsolutePath().normalize();
                String currentProjectPath = null;
                if (ballerinaTomlExists(currentDirPath)) {
                    Path projectDestDirectoryPath = currentDirPath.resolve(".ballerina").resolve(cacheDir)
                            .resolve(fullPathOfPkg);
                    currentProjectPath = projectDestDirectoryPath.toString();
                }

                String pkgPath = Paths.get(orgName).resolve(pkgName).resolve(pkgVersion).toString();
                String resourcePath = ballerinaCentralURL + pkgPath;
                String[] arguments = new String[]{resourcePath, dstPath, pkgName, currentProjectPath, resourceName,
                        pkgVersion};

                invokeFunction(programFile, "pull", arguments);
                // TODO: Pull the dependencies of the pulled package
            } else {
                outStream.println("No org-name provided for the package to be pulled. Please provide an org-name");
            }
        } else {
            outStream.println("Path to the balx file inside ballerina-packerina jar is incorrect");
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
        } catch (FileSystemAlreadyExistsException | IOException ignore) {
        }
    }

    /**
     * Check if Ballerina.toml exists in the current directory that the pull command is executed, to
     * verify that its from a project directory
     *
     * @param currentDirPath path of the current directory
     * @return true if Ballerina.toml exists, else false
     */
    private static boolean ballerinaTomlExists(Path currentDirPath) {
        return Files.isRegularFile(currentDirPath.resolve("Ballerina.toml"));
    }

    /**
     * Extract the host name from ballerina central URL.
     *
     * @param ballerinaCentralURL URL of ballerina central
     * @return host
     */
    private static String getHost(String ballerinaCentralURL) {
        try {
            return new URL(ballerinaCentralURL).getHost();
        } catch (MalformedURLException e) {
            return ballerinaCentralURL.replaceAll("[^A-Za-z0-9.]", "");
        }
    }

    /**
     * Get program file after reading the executable program i.e. balo file.
     *
     * @param baloFilePath path of the balo file inside the packerina jar
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
