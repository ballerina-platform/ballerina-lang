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
import org.ballerinalang.launcher.toml.model.Manifest;
import org.ballerinalang.launcher.toml.model.Settings;
import org.ballerinalang.launcher.toml.parser.ManifestProcessor;
import org.ballerinalang.launcher.toml.parser.SettingsProcessor;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.BLangConstants;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.program.BLangFunctions;

import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.stream.Stream;

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
            String[] proxyConfigs = readProxyConfigurations();
            String[] arguments = new String[]{resourcePath, dstPath, pkgName, currentProjectPath, resourceName,
                    pkgVersion};
            arguments = Stream.concat(Arrays.stream(arguments), Arrays.stream(proxyConfigs))
                    .toArray(String[]::new);
            BValue[] returns = BRunUtil.invoke(compileResult, "pull",
                    new BValue[]{buildStringArray(arguments)});
            if (((BBoolean) returns[0]).booleanValue()) {
                // TODO: Pull the dependencies of the pulled package
            }
            Runtime.getRuntime().exit(0);
        } else {
            outStream.println("No org-name provided for the package to be pulled. Please provide an org-name");
        }
    }

    /**
     * Push/Uploads packages to the central repository.
     *
     * @param packageName         path of the package folder to be pushed
     * @param ballerinaCentralURL URL of ballerina central
     */
    public static void pushPackages(String packageName, String installToRepo, String ballerinaCentralURL) {
        compileResult = compileBalFile("ballerina.push");
        String accessToken = getAccessTokenOfCLI() != null ? removeQuotationsFromValue(getAccessTokenOfCLI()) : null;
        if (accessToken != null) {
            // Get the org-name and version by reading Ballerina.toml inside the project
            Manifest manifest = readManifestConfigurations();
            if (manifest.getName() != null && manifest.getVersion() != null) {
                String orgName = removeQuotationsFromValue(manifest.getName());
                String version = removeQuotationsFromValue(manifest.getVersion());
                String resourcePath = ballerinaCentralURL + Paths.get(orgName).resolve(packageName)
                        .resolve(version);

                Path currentDirPath = Paths.get(".").toAbsolutePath().normalize();
                // Construct the package name along with the version and check if it exists
                Path pkgPath = currentDirPath.resolve(".ballerina").resolve("repo").resolve(orgName)
                        .resolve(packageName).resolve(version);
                Path baloFilePath = pkgPath.resolve(packageName + "-" + version + ".balo");
                if (Files.notExists(pkgPath)) {
                    outStream.println("Package " + packageName + " with version " + version + " does not exist");
                } else {
                    // Default push to the remote repository
                    if (installToRepo == null) {
                        String[] proxyConfigs = readProxyConfigurations();
                        String[] arguments = new String[]{accessToken, resourcePath, baloFilePath.toString()};
                        arguments = Stream.concat(Arrays.stream(arguments), Arrays.stream(proxyConfigs))
                                .toArray(String[]::new);
                        BRunUtil.invoke(compileResult, "push", new BValue[]{buildStringArray(arguments)});
                        Runtime.getRuntime().exit(0);
                    } else {
                        // Check if the package should be installed into the home repository
                        if (installToRepo.equals("home")) {
                            // Directory path to .ballerina/artifacts
                            Path targetDirectoryPath = UserRepositoryUtils.initializeUserRepository()
                                    .resolve(BLangConstants.USER_REPO_ARTIFACTS_DIRNAME).resolve(orgName)
                                    .resolve(packageName).resolve(version);
                            if (!Files.exists(targetDirectoryPath)) {
                                try {
                                    Files.createDirectories(targetDirectoryPath);
                                } catch (IOException e) {
                                    outStream.println("Error when occured when creating directories in " +
                                            "./ballerina/artifacts/ to install the package locally");
                                }
                            }
                            //copy balo file to the user repository
                            try {
                                Path dstBaloFile = targetDirectoryPath.resolve(baloFilePath.getFileName());
                                Files.copy(baloFilePath, dstBaloFile, StandardCopyOption.REPLACE_EXISTING);
                                outStream.println("Ballerina package pushed successfully to the user repository");
                            } catch (IOException e) {
                                outStream.println("Error occured when copying the balo file to the user repository");
                            }
                        }
                    }
                }
            } else {
                outStream.println("An org-name and package version is required when pushing. This is not specified " +
                        "in Ballerina.toml inside the project");
            }
        } else {
            outStream.println("You have not specified an access-token for the central in your Settings.toml\n" +
                    "Please login to central if you are already registered using 'central.ballerina.io/login' to get" +
                    " a valid access-token. \nIf you are new to the site please register using " +
                    "'central.ballerina.io/register'");
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
     * Read the manifest.
     *
     * @return manifest configuration object
     */
    private static Manifest readManifestConfigurations() {
        String tomlFilePath = Paths.get(".").toAbsolutePath().normalize().resolve("Ballerina.toml").toString();
        try {
            return ManifestProcessor.parseTomlContentFromFile(tomlFilePath);
        } catch (IOException e) {
            return new Manifest();
        }
    }

    /**
     * Read Settings.toml to populate the configurations.
     *
     * @return settings object
     */
    private static Settings readSettings() {
        String tomlFilePath = UserRepositoryUtils.initializeUserRepository().resolve("Settings.toml").toString();
        try {
            return SettingsProcessor.parseTomlContentFromFile(tomlFilePath);
        } catch (IOException e) {
            return new Settings();
        }
    }

    /**
     * Read proxy configurations from the SettingHeaders.toml file.
     *
     * @return array with proxy configurations
     */
    private static String[] readProxyConfigurations() {
        String host = "", port = "", username = "", password = "";
        String proxyConfigArr[] = new String[]{host, port, username, password};
        Settings settings = readSettings();
        if (settings.getProxy() != null) {
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
     * Read the access token generated for the CLI.
     *
     * @return access token for generated for the CLI
     */
    private static String getAccessTokenOfCLI() {
        Settings settings = readSettings();
        if (settings != null) {
            if (settings.getCentral() != null) {
                return settings.getCentral().getAccessToken();
            }
        }
        return null;
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
