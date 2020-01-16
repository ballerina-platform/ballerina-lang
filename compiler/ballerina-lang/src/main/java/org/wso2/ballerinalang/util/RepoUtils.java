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

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.toml.exceptions.TomlException;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.parser.ManifestProcessor;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * Home repository util methods.
 */
public class RepoUtils {

    public static final String BALLERINA_INSTALL_DIR_PROP = "ballerina.home";
    public static final String COMPILE_BALLERINA_ORG_PROP = "BALLERINA_DEV_COMPILE_BALLERINA_ORG";
    public static final String LOAD_BUILTIN_FROM_SOURCE_PROP = "BALLERINA_DEV_LOAD_BUILTIN_FROM_SOURCE";
    public static final boolean COMPILE_BALLERINA_ORG = getBooleanProp(COMPILE_BALLERINA_ORG_PROP);
    public static final boolean LOAD_BUILTIN_FROM_SOURCE = getBooleanProp(LOAD_BUILTIN_FROM_SOURCE_PROP);

    private static final String USER_HOME = "user.home";
    private static final String DEFAULT_TERMINAL_SIZE = "80";
    private static final String BALLERINA_CLI_WIDTH = "BALLERINA_CLI_WIDTH";
    private static final String PRODUCTION_URL = "https://api.central.ballerina.io/1.0";
    private static final String STAGING_URL = "https://api.staging-central.ballerina.io";

    private static final String BALLERINA_ORG = "ballerina";
    private static final String BALLERINAX_ORG = "ballerinax";

    public static final boolean BALLERINA_DEV_STAGE_CENTRAL = Boolean.parseBoolean(
            System.getenv("BALLERINA_DEV_STAGE_CENTRAL"));
    
    /**
     * Create and get the home repository path.
     *
     * @return home repository path
     */
    public static Path createAndGetHomeReposPath() {
        Path homeRepoPath;
        String homeRepoDir = System.getenv(ProjectDirConstants.HOME_REPO_ENV_KEY);
        if (homeRepoDir == null || homeRepoDir.isEmpty()) {
            String userHomeDir = System.getProperty(USER_HOME);
            if (userHomeDir == null || userHomeDir.isEmpty()) {
                throw new BLangCompilerException("Error creating home repository: unable to get user home directory");
            }
            homeRepoPath = Paths.get(userHomeDir, ProjectDirConstants.HOME_REPO_DEFAULT_DIRNAME);
        } else {
            // User has specified the home repo path with env variable.
            homeRepoPath = Paths.get(homeRepoDir);
        }

        homeRepoPath = homeRepoPath.toAbsolutePath();
        if (Files.exists(homeRepoPath) && !Files.isDirectory(homeRepoPath, LinkOption.NOFOLLOW_LINKS)) {
            throw new BLangCompilerException("Home repository is not a directory: " + homeRepoPath.toString());
        }
        return homeRepoPath;
    }

    /**
     * Checks if the path is a project.
     *
     * @param sourceRoot source root of the project.
     * @return true if the directory is a project repo, false if its the home repo
     */
    public static boolean isBallerinaProject(Path sourceRoot) {
        Path manifest = sourceRoot.resolve(ProjectDirConstants.MANIFEST_FILE_NAME);
        return Files.isDirectory(sourceRoot) && Files.exists(manifest) && Files.isRegularFile(manifest);
    }

    /**
     * Checks if the path is a standalone file.
     *
     * @param file path to bal file
     * @return true if the file is a standalone bal file
     */
    public static boolean isBallerinaStandaloneFile(Path file) {
        // Check if the file is a regular file
        if (!Files.isRegularFile(file)) {
            return false;
        }
        // Check if it is a file with bal extention.
        if (!file.toString().endsWith(ProjectDirConstants.BLANG_SOURCE_EXT)) {
            return false;
        }
        // Check if it is inside a project
        Path projectRoot = ProjectDirs.findProjectRoot(file.getParent());
        if (null != projectRoot) {
            // Check if it is inside a module
            Path src = projectRoot.resolve(ProjectDirConstants.SOURCE_DIR_NAME);
            Path parent = file.getParent();
            while (parent != null) {
                if (src.equals(parent)) {
                    return false;
                }
                parent = parent.getParent();
            }
            return true;
        } else {
            return true;
        }
    }


    /**
     * Get the remote repo URL.
     *
     * @return URL of the remote repository
     */
    public static String getRemoteRepoURL() {
        if (BALLERINA_DEV_STAGE_CENTRAL) {
            return STAGING_URL;
        }
        return PRODUCTION_URL;
    }

    /**
     * Get the staging URL.
     *
     * @return URL of the remote repository
     */
    public static String getStagingURL() {
            return STAGING_URL;
    }

    public static Path getLibDir() {
        return Paths.get(System.getProperty(BALLERINA_INSTALL_DIR_PROP, ".")).resolve("lib");
    }

    /**
     * Get the terminal width.
     *
     * @return terminal width as a string
     */
    public static String getTerminalWidth() {
        Map<String, String> envVariableMap = System.getenv();
        if (envVariableMap.containsKey(BALLERINA_CLI_WIDTH)) {
            return envVariableMap.get(BALLERINA_CLI_WIDTH);
        }
        return DEFAULT_TERMINAL_SIZE;
    }

    public static Path createAndGetLibsRepoPath() {
        String ballerinaHome = System.getProperty(ProjectDirConstants.BALLERINA_HOME);
        if (ballerinaHome == null || ballerinaHome.isEmpty()) {
            return null;
        }

        return Paths.get(ballerinaHome).resolve(ProjectDirConstants.BALLERINA_HOME_LIB);
    }

    private static boolean getBooleanProp(String key) {
        return Boolean.parseBoolean(System.getProperty(key));
    }


    /**
     * Get the ballerina version the package is built with.
     *
     * @return ballerina version
     */
    public static String getBallerinaVersion() {
        try (InputStream inputStream = RepoUtils.class.getResourceAsStream(ProjectDirConstants.PROPERTIES_FILE)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties.getProperty(ProjectDirConstants.BALLERINA_VERSION);
        } catch (Throwable ignore) {
        }
        return "unknown";
    }


    /**
     * Validates the org-name and package name.
     *
     * @param orgName The org-name
     * @return True if valid org-name or package name, else false.
     */
    public static boolean validateOrg(String orgName) {
        String validRegex = "^[a-z0-9_]*$";
        return Pattern.matches(validRegex, orgName);
    }

    /**
     * Validates the org-name and package name.
     *
     * @param pkgName The package name.
     * @return True if valid package name, else false.
     */
    public static boolean validatePkg(String pkgName) {
        String validRegex = "^[a-zA-Z0-9_.]*$";
        return Pattern.matches(validRegex, pkgName);
    }

    /**
     * Check if the org-name is a reserved org-name in ballerina.
     *
     * @param orgName The org-name
     * @return True if the org-name is reserved, else false.
     */
    public static boolean isReservedOrgName(String orgName) {
        return orgName.equals(BALLERINA_ORG) || orgName.equals(BALLERINAX_ORG);
    }

    /**
     * Check if ballerina version is from a stable release or a nightly build.
     *
     * @return True if ballerina version is from a nightly build, else false.
     */
    public static boolean isANightlyBuild() {
        return getBallerinaVersion().contains("SNAPSHOT");
    }
    
    /**
     * Get the Ballerina.toml from a balo file.
     *
     * @param baloPath The path to balo file.
     * @return Ballerina.toml contents.
     */
    public static Manifest getManifestFromBalo(Path baloPath) {
        try (JarFile jar = new JarFile(baloPath.toString())) {
            java.util.Enumeration enumEntries = jar.entries();
            while (enumEntries.hasMoreElements()) {
                JarEntry file = (JarEntry) enumEntries.nextElement();
                if (file.getName().contains(ProjectDirConstants.MANIFEST_FILE_NAME)) {
                    if (file.isDirectory()) { // if its a directory, ignore
                        continue;
                    }
                    // get the input stream
                    Manifest manifest;
                    try (InputStream is = jar.getInputStream(file)) {
                        manifest = ManifestProcessor.parseTomlContentAsStream(is);
                    }
                    
                    return manifest;
                }
            }
        } catch (IOException e) {
            throw new BLangCompilerException("unable to read balo file: " + baloPath +
                                             ". balo file seems to be corrupted.");
        } catch (TomlException e) {
            throw new BLangCompilerException("unable to read balo file: " + baloPath +
                                             ". balo file seems to be corrupted: " + e.getMessage());
        }
    
        throw new BLangCompilerException("unable to find '/metadata/Ballerina.toml' file in balo file: " +
                                         baloPath + "");
    }
    
}
