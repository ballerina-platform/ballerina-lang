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
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.launcher.LauncherUtils;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.program.BLangFunctions;

import java.io.File;
import java.nio.file.Path;

import static org.ballerinalang.util.BLangConstants.USER_REPO_ARTIFACTS_DIRNAME;
import static org.ballerinalang.util.BLangConstants.USER_REPO_SRC_DIRNAME;

/**
 * Util class for network calls.
 */
public class NetworkUtils {
    private static CompileResult compileResult;

    /**
     * Pull/Downloads packages from the package repository.
     *
     * @param resourceName package name to be pulled
     */
    public static void pullPackages(String resourceName) {
        compileResult = compilePullCmdBalFile("ballerina.pull");
        Path targetDirectoryPath = UserRepositoryUtils.initializeUserRepository()
                .resolve(USER_REPO_ARTIFACTS_DIRNAME).resolve(USER_REPO_SRC_DIRNAME);
        String ballerinaCentralRepoURL = "http://packages.ballerina.io/";
        String dstPath = targetDirectoryPath + File.separator;
        String resourcePath = ballerinaCentralRepoURL + File.separator + resourceName;

        String[] arguments = new String[]{resourcePath, dstPath};
        LauncherUtils.runMain(compileResult.getProgFile(), arguments);
    }

    /**
     * Compile the bal file.
     *
     * @return compile result after compiling the bal file
     */
    private static CompileResult compilePullCmdBalFile(String packageName) {
        CompileResult compileResult = BCompileUtil.compile("src", packageName, CompilerPhase.CODE_GEN);
        ProgramFile programFile = compileResult.getProgFile();
        PackageInfo packageInfo = programFile.getPackageInfo(compileResult.getProgFile().getEntryPkgName());
        Context context = new Context(programFile);
        Debugger debugger = new Debugger(programFile);
        programFile.setDebugger(debugger);
        compileResult.setContext(context);
        BLangFunctions.invokePackageInitFunction(programFile, packageInfo.getInitFunctionInfo(), context);
        return compileResult;
    }

    /**
     * Push/Uploads packages to the central repository
     * @param resourceName path of the package folder to be pushed
     */
    public static void pushPackages(String resourceName) {
        compileResult = compilePullCmdBalFile("ballerina.push");
        String ballerinaCentralRepoURL = "http://packages.ballerina.io/";
        String[] arguments = new String[]{ballerinaCentralRepoURL, resourceName};
        LauncherUtils.runMain(compileResult.getProgFile(), arguments);
    }
}
