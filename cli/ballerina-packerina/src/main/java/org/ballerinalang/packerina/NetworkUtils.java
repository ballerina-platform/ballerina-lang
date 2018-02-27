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
import org.ballerinalang.launcher.toml.model.Settings;
import org.ballerinalang.launcher.toml.parser.ManifestProcessor;
import org.ballerinalang.launcher.toml.parser.SettingsProcessor;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Util class for network calls.
 */
public class NetworkUtils {
    private static final Logger log = LoggerFactory.getLogger(NetworkUtils.class);
    private static CompileResult compileResult;
    private static Settings settings = readSettings();

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
        Path targetDirectoryPath = UserRepositoryUtils.initializeUserRepository().resolve("caches").
                resolve(host);

        int indexOfSlash = resourceName.indexOf("/");
        String orgName = resourceName.substring(0, indexOfSlash);
        String pkgNameWithVersion = resourceName.substring(indexOfSlash + 1);

        int indexOfColon = pkgNameWithVersion.indexOf(":");
        String pkgName = pkgNameWithVersion.substring(0, indexOfColon);
        String pkgVersion = pkgNameWithVersion.substring(indexOfColon + 1);
        targetDirectoryPath = targetDirectoryPath.resolve(orgName).resolve(pkgName).resolve(pkgVersion);

        // Create the target directories
        createDirectories(targetDirectoryPath);
        String dstPath = targetDirectoryPath + File.separator;
        String pkgPath = Paths.get(orgName).resolve(pkgName).resolve(pkgVersion).toString();
        String resourcePath = ballerinaCentralURL + pkgPath;
        String[] proxyConfigs = readProxyConfigurations();
        String[] arguments = new String[]{resourcePath, dstPath, pkgName};
        arguments = Stream.concat(Arrays.stream(arguments), Arrays.stream(proxyConfigs))
                .toArray(String[]::new);
        LauncherUtils.runMain(compileResult.getProgFile(), arguments);
    }

    /**
     * Create target/output directories which contains the pulled packages.
     *
     * @param targetDirectoryPath target directory path
     */
    private static void createDirectories(Path targetDirectoryPath) {
        if (!Files.exists(targetDirectoryPath)) {
            try {
                Files.createDirectories(targetDirectoryPath);
            } catch (IOException e) {
                log.debug("I/O Exception when creating the directory ", e);
                log.error("I/O Exception when creating the directory" + e.getMessage());
            }
        }
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
     * Push/Uploads packages to the central repository.
     *
     * @param packageName         path of the package folder to be pushed
     * @param ballerinaCentralURL URL of ballerina central
     */
    public static void pushPackages(String packageName, String ballerinaCentralURL) {
        compileResult = compileBalFile("ballerina.push");
        // Get the org-name and version by reading Ballerina.toml
        Manifest manifest = readManifestConfigurations();
        if (manifest != null && manifest.getName() != null && manifest.getVersion() != null) {
            String orgName = removeQuotationsFromValue(manifest.getName());
            String version = removeQuotationsFromValue(manifest.getVersion());
            String resourcePath = ballerinaCentralURL + Paths.get(orgName).resolve(packageName)
                    .resolve(version);
            String[] proxyConfigs = readProxyConfigurations();
            String[] arguments = new String[]{resourcePath, packageName};
            arguments = Stream.concat(Arrays.stream(arguments), Arrays.stream(proxyConfigs))
                    .toArray(String[]::new);
            LauncherUtils.runMain(compileResult.getProgFile(), arguments);
        } else {
            log.debug("An org-name and package version is required when pushing. This is not specified in " +
                    "Ballerina.toml inside the project");
            log.error("An org-name and package version is required when pushing. This is not specified in " +
                            "Ballerina.toml inside the project",
                    "An org-name and package version is required when pushing");
        }
    }

    /**
     * Read the manifest.
     *
     * @return manifest configuration object
     */
    private static Manifest readManifestConfigurations() {
        String tomlFilePath = Paths.get(".").toAbsolutePath().normalize().resolve("Ballerina.toml").toString();
        Manifest manifest = null;
        try {
            manifest = ManifestProcessor.parseTomlContentFromFile(tomlFilePath);
        } catch (IOException e) {
            log.debug("I/O Exception when processing Ballerina.toml ", e);
            log.error("I/O Exception when processing Ballerina.toml " + e.getMessage());
        }
        return manifest;
    }

    /**
     * Read Settings.toml to populate the configurations.
     *
     * @return settings object
     */
    private static Settings readSettings() {
        File cliTomlFile = new File(UserRepositoryUtils.initializeUserRepository().toString()
                + File.separator + "Settings.toml");
        if (cliTomlFile.exists()) {
            try {
                settings = SettingsProcessor.parseTomlContentFromFile(cliTomlFile.toString());
            } catch (IOException e) {
                log.debug("I/O Exception when processing the toml file ", e);
                log.error("I/O Exception when processing the toml file " + e.getMessage());
            }
        }
        return settings;
    }

    /**
     * Read proxy configurations from the SettingHeaders.toml file.
     *
     * @return array with proxy configurations
     */
    private static String[] readProxyConfigurations() {
        String host = "", port = "", username = "", password = "";
        String proxyConfigArr[] = new String[]{host, port, username, password};
        if (settings != null) {
            if (settings.getProxy().getHost() != null) {
                host = removeQuotationsFromValue(settings.getProxy().getHost());
                proxyConfigArr[0] = host;
            }
            if (settings.getProxy().getPort() != null) {
                port = removeQuotationsFromValue(settings.getProxy().getPort());
                proxyConfigArr[1] = port;
            }
            if (settings.getProxy().getUserName() != null) {
                username = removeQuotationsFromValue(settings.getProxy().getUserName());
                proxyConfigArr[2] = username;
            }
            if (settings.getProxy().getPassword() != null) {
                password = removeQuotationsFromValue(settings.getProxy().getPassword());
                proxyConfigArr[3] = password;
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
