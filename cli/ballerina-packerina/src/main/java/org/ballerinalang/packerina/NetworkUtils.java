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
import org.ballerinalang.launcher.toml.model.Manifest;
import org.ballerinalang.launcher.toml.model.Proxy;
import org.ballerinalang.launcher.toml.parser.ManifestProcessor;
import org.ballerinalang.launcher.toml.parser.ProxyProcessor;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.program.BLangFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.ballerinalang.util.BLangConstants.USER_REPO_ARTIFACTS_DIRNAME;
import static org.ballerinalang.util.BLangConstants.USER_REPO_SRC_DIRNAME;

/**
 * Util class for network calls.
 */
public class NetworkUtils {
    private static final Logger log = LoggerFactory.getLogger(NetworkUtils.class);
    private static final String BALLERINA_CENTRAL_REPO_URL = "http://0.0.0.0:9090/p/";
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
     * @param resourceName package name to be pulled
     */
    public static void pullPackages(String resourceName) {
        compileResult = compileBalFile("ballerina.pull");
        Path targetDirectoryPath = UserRepositoryUtils.initializeUserRepository()
                .resolve(USER_REPO_ARTIFACTS_DIRNAME).resolve(USER_REPO_SRC_DIRNAME);

        // Make directories
        String[] resourceArr = resourceName.split("/");
        for (String aResourceArr : resourceArr) {
            targetDirectoryPath = targetDirectoryPath.resolve(aResourceArr);
            if (!Files.exists(targetDirectoryPath)) {
                try {
                    Files.createDirectories(targetDirectoryPath);
                } catch (IOException e) {
                    log.debug("I/O Exception when creating the directory ", e);
                    log.error("I/O Exception when creating the directory" + e.getMessage());
                }
            }
        }
        int index = resourceName.lastIndexOf('/');
        String pkgName = resourceName.substring(0, index);

        String dstPath = targetDirectoryPath + File.separator;
        String resourcePath = BALLERINA_CENTRAL_REPO_URL + resourceName;
        String[] proxyConfigs = readProxyConfigurations();
        String[] arguments = new String[]{resourcePath, dstPath, pkgName};
        arguments = Stream.concat(Arrays.stream(arguments), Arrays.stream(proxyConfigs))
                .toArray(String[]::new);
        LauncherUtils.runMain(compileResult.getProgFile(), arguments);
    }

    /**
     * Push/Uploads packages to the central repository.
     *
     * @param packageName path of the package folder to be pushed
     */
    public static void pushPackages(String packageName) {
        compileResult = compileBalFile("ballerina.push");

        // Get the org-name and version by reading Settings.toml
        Manifest manifest = readManifestConfigurations();
        if (manifest != null && manifest.getName() != null && manifest.getVersion() != null) {
            String orgName = removeQuotationsFromValue(manifest.getName());
            String version = removeQuotationsFromValue(manifest.getVersion());
            String resourcePath = BALLERINA_CENTRAL_REPO_URL + Paths.get(orgName).resolve(packageName).resolve(version);
            String[] proxyConfigs = readProxyConfigurations();
            String[] arguments = new String[]{resourcePath, packageName};
            arguments = Stream.concat(Arrays.stream(arguments), Arrays.stream(proxyConfigs))
                    .toArray(String[]::new);
            LauncherUtils.runMain(compileResult.getProgFile(), arguments);
        } else {
            log.debug("An org-name and package version is required when pushing. This is not specified in " +
                    "Settings.toml inside the project");
            log.error("An org-name and package version is required when pushing. This is not specified in " +
                            "Settings.toml inside the project",
                    "An org-name and package version is required when pushing");
        }
    }

    /**
     * Read the manifest.
     *
     * @return manifest configuration object
     */
    private static Manifest readManifestConfigurations() {
        String tomlFilePath = Paths.get(".").toAbsolutePath().normalize().resolve("Settings.toml").toString();
        Manifest manifest = null;
        try {
            manifest = ManifestProcessor.parseTomlContentFromFile(tomlFilePath);
        } catch (IOException e) {
            log.debug("I/O Exception when processing Settings.toml ", e);
            log.error("I/O Exception when processing Settings.toml " + e.getMessage());
        }
        return manifest;
    }

    /**
     * Read proxy configurations from the Settings.toml file
     *
     * @return array with proxy configurations
     */
    private static String[] readProxyConfigurations() {
        File cliTomlFile = new File(UserRepositoryUtils.initializeUserRepository().toString()
                + File.separator + "Settings.toml");
        String host = "", port = "", username = "", password = "";
        String proxyConfigArr[] = new String[]{host, port, username, password};
        if (cliTomlFile.exists()) {
            try {
                Proxy proxy = ProxyProcessor.parseTomlContentFromFile(cliTomlFile.toString());

                if (proxy.getHost() != null) {
                    host = removeQuotationsFromValue(proxy.getHost());
                    proxyConfigArr[0] = host;
                }
                if (proxy.getPort() != null) {
                    port = removeQuotationsFromValue(proxy.getPort());
                    proxyConfigArr[1] = port;
                }
                if (proxy.getUserName() != null) {
                    username = removeQuotationsFromValue(proxy.getUserName());
                    proxyConfigArr[2] = username;
                }
                if (proxy.getPassword() != null) {
                    password = removeQuotationsFromValue(proxy.getPassword());
                    proxyConfigArr[3] = password;
                }
            } catch (IOException e) {
                log.debug("I/O Exception when processing the toml file ", e);
                log.error("I/O Exception when processing the toml file " + e.getMessage());
            }
        }
        return proxyConfigArr;
    }

    /**
     * Remove enclosing quotation from the string value.
     *
     * @param value string value with enclosing quotations
     * @return string value after removing the enclosing quotations
     */
    private static String removeQuotationsFromValue(String value) {
        return value.replace("\"", "");
    }
}
