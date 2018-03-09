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
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.program.BLangFunctions;

import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Util class for network calls.
 *
 * @since 0.964
 */
public class NetworkUtils {
    private static PrintStream outStream = System.err;
    private static CompileResult compileResult;

    /**
     * Compile the bal file.
     *
     * @return compile result after compiling the bal file
     */
    private static CompileResult compileBalFile(String packageName) {
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
     * Pull/Downloads packages from the package repository.
     *
     * @param resourceName        package name to be pulled
     * @param ballerinaCentralURL URL of ballerina central
     */
    public static void pullPackages(String resourceName, String ballerinaCentralURL) {
        compileResult = compileBalFile("ballerina.pull");

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

            // Get the current directory path to check if the user is pulling a package from inside a project directory
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
            BRunUtil.invoke(compileResult, "pull", new BValue[]{buildStringArray(arguments)});
            // TODO: Pull the dependencies of the pulled package
            // if (((BBoolean) returns[0]).booleanValue()) { }
            Runtime.getRuntime().exit(0);
        } else {
            outStream.println("No org-name provided for the package to be pulled. Please provide an org-name");
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
     * Build BString array from string array.
     *
     * @param args string array
     * @return BString array
     */
    private static BStringArray buildStringArray(String[] args) {
        BStringArray valueArray = new BStringArray();

        for (int i = 0; i < args.length; i++) {
            valueArray.add(i, args[i]);
        }

        return valueArray;
    }
}
