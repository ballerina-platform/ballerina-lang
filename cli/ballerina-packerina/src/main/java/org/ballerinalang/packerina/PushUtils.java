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
package org.ballerinalang.packerina;

import org.ballerinalang.spi.EmbeddedExecutor;
import org.ballerinalang.toml.model.Dependency;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.model.Proxy;
import org.ballerinalang.toml.model.Settings;
import org.ballerinalang.util.EmbeddedExecutorProvider;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import org.wso2.ballerinalang.util.RepoUtils;
import org.wso2.ballerinalang.util.TomlParserUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.ballerinalang.tool.LauncherUtils.createLauncherException;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.IMPLEMENTATION_VERSION;
import static org.wso2.ballerinalang.util.RepoUtils.BALLERINA_DEV_STAGE_CENTRAL;

/**
 * This class provides util methods when pushing Ballerina modules to central and home repository.
 *
 * @since 0.95.2
 */
public class PushUtils {

    private static final String BALLERINA_CENTRAL_CLI_TOKEN = BALLERINA_DEV_STAGE_CENTRAL ?
                                                              "https://staging-central.ballerina.io/cli-token" :
                                                              "https://central.ballerina.io/cli-token";
    private static final Path BALLERINA_HOME_PATH = RepoUtils.createAndGetHomeReposPath();
    private static final Path SETTINGS_TOML_FILE_PATH = BALLERINA_HOME_PATH.resolve(
            ProjectDirConstants.SETTINGS_FILE_NAME);

    private static EmbeddedExecutor executor = EmbeddedExecutorProvider.getInstance().getExecutor();
    private static Settings settings;

    private static final PrintStream SYS_ERR = System.err;
    private static final PrintStream SYS_OUT = System.out;
    /**
     * Push/Uploads modules to the central repository.
     *
     * @param moduleName path of the module folder to be pushed
     * @param prjDirPath path to the directory containing source files and modules
     * @return status of the module pushed
     */
    public static boolean pushPackages(String moduleName, Path prjDirPath) {
        try {
            // Get balo output path
            Path baloOutputDir = Paths.get(prjDirPath.toString(), ProjectDirConstants.TARGET_DIR_NAME,
                    ProjectDirConstants.TARGET_BALO_DIRECTORY);
        
            if (Files.notExists(baloOutputDir)) {
                throw createLauncherException("cannot find balo file for the module: " + moduleName + ". Run " +
                                              "'ballerina build -c <module_name>' to compile and generate the balo.");
            }
        
            Optional<Path> moduleBaloFile = Files.list(baloOutputDir)
                    .filter(baloFile -> null != baloFile.getFileName() &&
                                        baloFile.getFileName().toString().startsWith(moduleName + "-" +
                                                                                     IMPLEMENTATION_VERSION))
                    .findFirst();
        
            if (!moduleBaloFile.isPresent()) {
                throw createLauncherException("cannot find balo file for the module: " + moduleName + ". Run " +
                                              "'ballerina build -c <module_name>' to compile and generate the balo.");
            }
        
            // get the manifest from balo file
            Path baloFilePath = moduleBaloFile.get();
            Manifest manifest = RepoUtils.getManifestFromBalo(baloFilePath.toAbsolutePath());
            String orgName = manifest.getProject().getOrgName();
            
            // Validate the org-name
            if (!RepoUtils.validateOrg(manifest.getProject().getOrgName())) {
                throw createLauncherException("invalid organization name provided \'" +
                                              manifest.getProject().getOrgName() + "\'. Only lowercase alphanumerics " +
                                              "and underscores are allowed in an organization name and the maximum " +
                                              "length is 256 characters");
            }
        
            // Validate the module-name
            if (!RepoUtils.validatePkg(moduleName)) {
                throw createLauncherException("invalid module name provided \'" + moduleName + "\'. Only " +
                                              "alphanumerics, underscores and periods are allowed in a module name " +
                                              "and the maximum length is 256 characters");
            }
        
            if (!RepoUtils.validatePkg(moduleName)) {
                throw createLauncherException("invalid module name provided \'" + moduleName + "\'. Only " +
                                              "alphanumerics, underscores and periods are allowed in a module name " +
                                              "and the maximum length is 256 characters");
            }
        
            // check if there are any dependencies with balo path
            List<String> dependenciesWithBaloPath =
                    manifest.getDependencies().stream()
                            .filter(dep -> dep.getMetadata().getPath() != null)
                            .map(Dependency::getModuleID)
                            .collect(Collectors.toList());
        
            if (dependenciesWithBaloPath.size() > 0) {
                throw createLauncherException("dependencies cannot be given by path when pushing module(s) to " +
                                              "remote. check dependencies in Ballerina.toml: [" +
                                              String.join(", ", dependenciesWithBaloPath) + "]");
            }
        
            String version = manifest.getProject().getVersion();
        
            // Get access token
            String accessToken = checkAccessToken();
            Proxy proxy = settings.getProxy();
            String proxyPortAsString = proxy.getPort() == 0 ? "" : Integer.toString(proxy.getPort());
        
            // Push module to central
            String urlWithModulePath = URI.create(RepoUtils.getRemoteRepoURL()).resolve("/modules/").toString();
            String outputLogMessage = orgName + "/" + moduleName + ":" + version + " [project repo -> central]";
        
            Optional<RuntimeException> exception = executor.executeMainFunction("module_push", urlWithModulePath,
                    proxy.getHost(), proxyPortAsString, proxy.getUserName(), proxy.getPassword(), accessToken,
                    orgName, moduleBaloFile.get().toAbsolutePath().toString(),
                    outputLogMessage);
            if (exception.isPresent()) {
                String errorMessage = exception.get().getMessage();
                if (null != errorMessage && !"".equals(errorMessage.trim())) {
                    // removing the error stack
                    if (errorMessage.contains("\n\tat")) {
                        errorMessage = errorMessage.substring(0, errorMessage.indexOf("\n\tat"));
                    }
    
                    SYS_ERR.println(errorMessage);
                    return false;
                }
            } else {
                return true;
            }
        } catch (IOException e) {
            throw createLauncherException("file error occurred in finding balo file for the module '" + moduleName +
                                          "'");
        }
    
        return false;
    }

    /**
     * Checks if the access token is available in Settings.toml or not.
     *
     * @return access token if its present
     */
    private static String checkAccessToken() {
        String accessToken = getAccessTokenOfCLI();

        if (accessToken.isEmpty()) {
            try {
                SYS_ERR.println("Opening the web browser to " + BALLERINA_CENTRAL_CLI_TOKEN +
                                        " for auto token update ...");

                BrowserLauncher.startInDefaultBrowser(BALLERINA_CENTRAL_CLI_TOKEN);
            } catch (IOException e) {
                throw createLauncherException("Access token is missing in " + SETTINGS_TOML_FILE_PATH.toString() +
                                                 "\nAuto update failed. Please visit https://central.ballerina.io");
            }
            long modifiedTimeOfFileAtStart = getLastModifiedTimeOfFile(SETTINGS_TOML_FILE_PATH);
            executor.executeService("module_cli_token_updater");

            boolean waitForToken = true;
            while (waitForToken) {
                pause();
                long modifiedTimeOfFileAfter = getLastModifiedTimeOfFile(SETTINGS_TOML_FILE_PATH);
                if (modifiedTimeOfFileAtStart != modifiedTimeOfFileAfter) {
                    accessToken = getAccessTokenOfCLI();
                    if (accessToken.isEmpty()) {
                        throw createLauncherException("Access token is missing in " +
                                                                 SETTINGS_TOML_FILE_PATH.toString() + "\nPlease " +
                                                                 "visit https://central.ballerina.io");
                    } else {
                        waitForToken = false;
                    }
                }
            }
        }
        return accessToken;
    }

    /**
     * Pause for 3s to check if the access token is received.
     */
    private static void pause() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            throw createLauncherException("Error occurred while retrieving the access token");
        }
    }

    /**
     * Get last modified time of file.
     *
     * @param path file path
     * @return last modified time in milliseconds
     */
    private static long getLastModifiedTimeOfFile(Path path) {
        if (!Files.isRegularFile(path)) {
            return -1;
        }
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException ex) {
            throw createLauncherException("Error occurred when reading file for token " +
                                             SETTINGS_TOML_FILE_PATH.toString());
        }
    }

    /**
     * Read the access token generated for the CLI.
     *
     * @return access token for generated for the CLI
     */
    private static String getAccessTokenOfCLI() {
        settings = TomlParserUtils.readSettings();
        // The access token can be specified as an environment variable or in 'Settings.toml'. First we would check if
        // the access token was specified as an environment variable. If not we would read it from 'Settings.toml'
        String tokenAsEnvVar = System.getenv(ProjectDirConstants.BALLERINA_CENTRAL_ACCESS_TOKEN);
        if (tokenAsEnvVar != null) {
            return tokenAsEnvVar;
        }
        if (settings.getCentral() != null) {
            return settings.getCentral().getAccessToken();
        }
        return "";
    }

    /**
     * Push all modules to central.
     *
     * @param sourceRootPath source root or project root
     * @return status of the modules pushed
     */
    public static boolean pushAllPackages(Path sourceRootPath) {
        try {
            List<String> fileList = Files.list(sourceRootPath.resolve(ProjectDirConstants.SOURCE_DIR_NAME))
                                         .filter(path -> Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS))
                                         .map(ProjectDirs::getLastComp)
                                         .filter(dirName -> !isSpecialDirectory(dirName))
                                         .map(Path::toString).collect(Collectors.toList());
            if (fileList.size() == 0) {
                throw createLauncherException("no modules found to push in " + sourceRootPath.toString());
            }
            for (String path : fileList) {
                boolean statusOfModulePush = pushPackages(path, sourceRootPath);
                if (!statusOfModulePush) {
                    return false;
                }
            }
        } catch (IOException ex) {
            throw createLauncherException("error occurred while pushing modules from " + sourceRootPath.toString()
                                                     + " " + ex.getMessage());
        }
        return true;
    }

    /**
     * Checks if the directory is a special directory that is not a module.
     *
     * @param dirName directory name
     * @return if the directory is a special directory or not
     */
    private static boolean isSpecialDirectory(Path dirName) {
        List<String> ignoreDirs = Arrays.asList(ProjectDirConstants.TARGET_DIR_NAME,
                                                ProjectDirConstants.RESOURCE_DIR_NAME);
        String dirNameStr = dirName.toString();
        return dirNameStr.startsWith(".") || dirName.toFile().isHidden() || ignoreDirs.contains(dirNameStr);
    }
}
