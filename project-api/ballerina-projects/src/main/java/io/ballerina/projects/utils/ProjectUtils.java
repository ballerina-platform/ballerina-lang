/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.projects.utils;

import io.ballerina.projects.Package;
import org.wso2.ballerinalang.util.RepoUtils;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Project related util methods.
 *
 * @since 2.0.0
 */
public class ProjectUtils {
    private static final String USER_HOME = "user.home";

    /**
     * Validates the org-name.
     *
     * @param orgName The org-name
     * @return True if valid org-name or package name, else false.
     */
    public static boolean validateOrgName(String orgName) {
        String validRegex = "^[a-z0-9_]*$";
        return Pattern.matches(validRegex, orgName);
    }

    /**
     * Validates the package name.
     *
     * @param packageName The package name.
     * @return True if valid package name, else false.
     */
    public static boolean validatePkgName(String packageName) {
        String validLanglib = "^[lang.a-z0-9_]*$";
        String validRegex = "^[a-z0-9_]*$";
        // We have special case for lang. packages
        // todo consider orgname when checking is it is a lang lib
        return Pattern.matches(validRegex, packageName) || Pattern.matches(validLanglib, packageName);
    }

    /**
     * Validates the module name.
     *
     * @param moduleName The module name.
     * @return True if valid module name, else false.
     */
    public static boolean validateModuleName(String moduleName) {
        String validRegex = "^[a-zA-Z0-9_.]*$";
        return Pattern.matches(validRegex, moduleName);
    }

    /**
     * Find the project root by recursively up to the root.
     *
     * @param filePath project path
     * @return project root
     */
    public static Path findProjectRoot(Path filePath) {
        if (filePath != null) {
            if (filePath.toFile().isDirectory()) {
                if (Files.exists(filePath.resolve(ProjectConstants.BALLERINA_TOML))) {
                    return filePath;
                }
            }
            return findProjectRoot(filePath.getParent());
        }
        return null;
    }

    /**
     * Checks if the path is a Ballerina project.
     *
     * @param sourceRoot source root of the project.
     * @return true if the directory is a project repo, false if its the home repo
     */
    public static boolean isBallerinaProject(Path sourceRoot) {
        Path ballerinaToml = sourceRoot.resolve(ProjectConstants.BALLERINA_TOML);
        return Files.isDirectory(sourceRoot)
                && Files.exists(ballerinaToml)
                && Files.isRegularFile(ballerinaToml);
    }

    /**
     * Guess organization name based on user name in system.
     *
     * @return organization name
     */
    public static String guessOrgName() {
        String guessOrgName = System.getProperty(ProjectConstants.USER_NAME);
        if (guessOrgName == null) {
            guessOrgName = "my_org";
        } else {
            guessOrgName = guessOrgName.toLowerCase(Locale.getDefault());
        }
        return guessOrgName;
    }

    /**
     * Guess package name with valid pattern.
     *
     * @param packageName package name
     * @return package name
     */
    public static String guessPkgName (String packageName) {
        if (!validatePkgName(packageName)) {
            return packageName.replaceAll("[^a-z0-9_]", "_");
        }
        return packageName;
    }

    public static String getBaloName(Package pkg) {
        return ProjectUtils.getBaloName(pkg.packageOrg().toString(),
                pkg.packageName().toString(),
                pkg.packageVersion().toString(),
                null
        );
    }

    public static String getBaloName(String org, String pkgName, String version, String platform) {
        // <orgname>-<packagename>-<platform>-<version>.balo
        if (platform == null || "".equals(platform)) {
            platform = "any";
        }
        return org + "-" + pkgName + "-" + platform + "-" + version + ProjectConstants.BLANG_COMPILED_PKG_BINARY_EXT;
    }

    public static String getJarName(Package pkg) {
        // <orgname>-<packagename>-<version>.jar
        return pkg.packageOrg().toString() + "-" + pkg.packageName().toString()
                + "-" + pkg.packageVersion() + ProjectConstants.BLANG_COMPILED_JAR_EXT;
    }

    public static String getExecutableName(Package pkg) {
        // <packagename>.jar
        return pkg.packageName().toString() + ProjectConstants.BLANG_COMPILED_JAR_EXT;
    }


    public static Path getBirCacheFromHome() {
        return createAndGetHomeReposPath().resolve(ProjectConstants.BIR_CACHE_DIR_NAME + "-" +
                RepoUtils.getBallerinaVersion());
    }

    public static Path getJarCacheFromHome() {
        return createAndGetHomeReposPath().resolve(ProjectConstants.JAR_CACHE_DIR_NAME + "-" +
                RepoUtils.getBallerinaVersion());
    }

    public static Path getBaloCacheFromHome() {
        return createAndGetHomeReposPath().resolve(ProjectConstants.BALO_CACHE_DIR_NAME);
    }

    /**
     * Create and get the home repository path.
     *
     * @return home repository path
     */
    public static Path createAndGetHomeReposPath() {
        Path homeRepoPath;
        String homeRepoDir = System.getenv(ProjectConstants.HOME_REPO_ENV_KEY);
        if (homeRepoDir == null || homeRepoDir.isEmpty()) {
            String userHomeDir = System.getProperty(USER_HOME);
            if (userHomeDir == null || userHomeDir.isEmpty()) {
                throw new RuntimeException("Error creating home repository: unable to get user home directory");
            }
            homeRepoPath = Paths.get(userHomeDir, ProjectConstants.HOME_REPO_DEFAULT_DIRNAME);
        } else {
            // User has specified the home repo path with env variable.
            homeRepoPath = Paths.get(homeRepoDir);
        }

        homeRepoPath = homeRepoPath.toAbsolutePath();
        if (Files.exists(homeRepoPath) && !Files.isDirectory(homeRepoPath, LinkOption.NOFOLLOW_LINKS)) {
            throw new RuntimeException("Home repository is not a directory: " + homeRepoPath.toString());
        }
        return homeRepoPath;
    }

    public static String getOrgFromBaloName(String baloName) {
        return baloName.split("-")[0];
    }

    public static String getPackageNameFromBaloName(String baloName) {
        return baloName.split("-")[1];
    }

    public static String getVersionFromBaloName(String baloName) {
        // TODO validate this method of getting the version of the balo
        String versionAndExtension = baloName.split("-")[3];
        int extensionIndex = versionAndExtension.indexOf(ProjectConstants.BLANG_COMPILED_PKG_BINARY_EXT);
        return versionAndExtension.substring(0, extensionIndex);
    }
}
