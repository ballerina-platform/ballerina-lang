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

import org.ballerinalang.launcher.LauncherUtils;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.spi.EmbeddedExecutor;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.model.Proxy;
import org.ballerinalang.toml.model.Settings;
import org.ballerinalang.util.EmbeddedExecutorProvider;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.repo.RemoteRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.Repo;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import org.wso2.ballerinalang.programfile.ProgramFileConstants;
import org.wso2.ballerinalang.util.RepoUtils;
import org.wso2.ballerinalang.util.TomlParserUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.ballerinalang.launcher.LauncherUtils.createLauncherException;
import static org.ballerinalang.util.BLangConstants.MAIN_FUNCTION_NAME;

/**
 * This class provides util methods when pushing Ballerina modules to central and home repository.
 *
 * @since 0.95.2
 */
public class PushUtils {

    private static final String BALLERINA_CENTRAL_CLI_TOKEN = "https://central.ballerina.io/cli-token";
    private static final PrintStream SYS_ERR = System.err;
    private static final Path BALLERINA_HOME_PATH = RepoUtils.createAndGetHomeReposPath();
    private static final Path SETTINGS_TOML_FILE_PATH = BALLERINA_HOME_PATH.resolve(
            ProjectDirConstants.SETTINGS_FILE_NAME);
    private static PrintStream outStream = System.out;
    private static EmbeddedExecutor executor = EmbeddedExecutorProvider.getInstance().getExecutor();
    private static Settings settings;

    /**
     * Push/Uploads modules to the central repository.
     *
     * @param packageName   path of the module folder to be pushed
     * @param sourceRoot    path to the directory containing source files and modules
     * @param installToRepo repo the module should be pushed to central or the home repository
     * @param noBuild       do not build sources before pushing
     */
    public static void pushPackages(String packageName, String sourceRoot, String installToRepo, boolean noBuild) {
        Path prjDirPath = LauncherUtils.getSourceRootPath(sourceRoot);
        // Check if the Ballerina.toml exists
        if (Files.notExists(prjDirPath.resolve(ProjectDirConstants.MANIFEST_FILE_NAME))) {
            throw createLauncherException("Couldn't locate Ballerina.toml in the project directory. Run " +
                                                     "'ballerina init' to create the Ballerina.toml file " +
                                                     "automatically and re-run the 'ballerina push' command");
        }
        Manifest manifest = TomlParserUtils.getManifest(prjDirPath);
        if (manifest.getName().isEmpty()) {
            throw createLauncherException("An org-name is required when pushing. This is not specified in " +
                                                     "Ballerina.toml inside the project");
        }

        if (manifest.getVersion().isEmpty()) {
            throw createLauncherException("A module version is required when pushing. This is not specified " +
                                                     "in Ballerina.toml inside the project");
        }

        String orgName = manifest.getName();
        // Validate the org-name
        if (!RepoUtils.validateOrg(orgName)) {
            throw createLauncherException("invalid organization name provided \'" + orgName + "\'. Only " +
                                          "lowercase alphanumerics and underscores are allowed in an organization " +
                                          "name and the maximum length is 256 characters");
        }
        // Validate the module-name
        if (!RepoUtils.validatePkg(packageName)) {
            throw createLauncherException("invalid module name provided \'" + packageName + "\'. Only " +
                                          "alphanumerics, underscores and periods are allowed in a module name and " +
                                          "the maximum length is 256 characters");
        }
        String version = manifest.getVersion();
        String ballerinaVersion = RepoUtils.getBallerinaVersion();
        PackageID packageID = new PackageID(new Name(orgName), new Name(packageName), new Name(version));

        // Get module path from project directory path
        Path pkgPathFromPrjtDir = Paths.get(prjDirPath.toString(), ProjectDirConstants.DOT_BALLERINA_DIR_NAME,
                                            ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME, orgName,
                                            packageName, version, packageName + ".zip");

        // Always build if the flag is not given
        if (!noBuild) {
            BuilderUtils.compileWithTestsAndWrite(prjDirPath, packageName, packageName, false, false, false, false);
        } else if (Files.notExists(pkgPathFromPrjtDir)) {
            // If --no-build is given, first check if the module artifact exists. If it does not exist prompt the user
            // to run "ballerina push" without the --no-build flag
            throw createLauncherException("Couldn't locate the module artifact to be pushed. Run 'ballerina " +
                                                     "push' without the --no-build flag");
        }
        
        if (installToRepo == null) {
            // Get access token
            String accessToken = checkAccessToken();

            // Read the Module.md file content from the artifact
            String mdFileContent = getModuleMDFileContent(pkgPathFromPrjtDir.toString(), packageName);
            if (mdFileContent == null) {
                throw createLauncherException("Cannot find Module.md file in the artifact");
            }

            String description = readSummary(mdFileContent);
            String homepageURL = manifest.getHomepageURL();
            String repositoryURL = manifest.getRepositoryURL();
            String apiDocURL = manifest.getDocumentationURL();
            String authors = String.join(",", manifest.getAuthors());
            String keywords = String.join(",", manifest.getKeywords());
            String license = manifest.getLicense();

            // Push module to central
            String resourcePath = resolvePkgPathInRemoteRepo(packageID);
            String msg = orgName + "/" + packageName + ":" + version + " [project repo -> central]";
            Proxy proxy = settings.getProxy();
            String baloVersionOfPkg = String.valueOf(ProgramFileConstants.VERSION_NUMBER);
            executor.executeFunction("packaging_push/packaging_push.balx", MAIN_FUNCTION_NAME, accessToken,
                    mdFileContent, description, homepageURL, repositoryURL, apiDocURL, authors, keywords, license,
                    resourcePath, pkgPathFromPrjtDir.toString(), msg, ballerinaVersion, proxy.getHost(),
                    proxy.getPort(), proxy.getUserName(), proxy.getPassword(), baloVersionOfPkg);

        } else {
            if (!installToRepo.equals("home")) {
                throw createLauncherException("Unknown repository provided to push the module");
            }
            installToHomeRepo(packageID, pkgPathFromPrjtDir);
        }
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
                SYS_ERR.println("Opening the web browser to " +
                                        BALLERINA_CENTRAL_CLI_TOKEN +
                                        " for auto token update ...");

                BrowserLauncher.startInDefaultBrowser(BALLERINA_CENTRAL_CLI_TOKEN);
            } catch (IOException e) {
                throw createLauncherException("Access token is missing in " + SETTINGS_TOML_FILE_PATH.toString() +
                                                 "\nAuto update failed. Please visit https://central.ballerina.io");
            }
            long modifiedTimeOfFileAtStart = getLastModifiedTimeOfFile(SETTINGS_TOML_FILE_PATH);
            executor.executeFunction("packaging_token_updater/packaging_token_updater.balx", MAIN_FUNCTION_NAME);

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
     * Install the module artifact to the home repository.
     *
     * @param packageID          packageID of the module
     * @param pkgPathFromPrjtDir module path from the project directory
     */
    private static void installToHomeRepo(PackageID packageID, Path pkgPathFromPrjtDir) {
        Path targetDirectoryPath = Paths.get(BALLERINA_HOME_PATH.toString(),
                                             ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME,
                                             packageID.orgName.getValue(),
                                             packageID.name.getValue(),
                                             packageID.version.getValue(),
                                             packageID.name.getValue() + ".zip");
        if (Files.exists(targetDirectoryPath)) {
            throw createLauncherException("Ballerina module exists in the home repository");
        } else {
            try {
                Files.createDirectories(targetDirectoryPath);
                Files.copy(pkgPathFromPrjtDir, targetDirectoryPath, StandardCopyOption.REPLACE_EXISTING);
                outStream.println(packageID.orgName.getValue() + "/" + packageID.name.getValue() + ":" +
                                          packageID.version.getValue() + " [project repo -> home repo]");
            } catch (IOException e) {
                throw createLauncherException("Error occurred when creating directories in the home repository");
            }
        }
    }

    /**
     * Get URI of the module from the remote repo.
     *
     * @param packageID packageID object
     * @return full URI path of the module relative to the remote repo
     */
    private static String resolvePkgPathInRemoteRepo(PackageID packageID) {
        Repo<URI> remoteRepo = new RemoteRepo(URI.create(RepoUtils.getRemoteRepoURL()));
        Patten patten = remoteRepo.calculate(packageID);
        if (patten == Patten.NULL) {
            throw createLauncherException("Couldn't find module " + packageID.toString());
        }
        Converter<URI> converter = remoteRepo.getConverterInstance();
        List<URI> uris = patten.convert(converter, packageID).collect(Collectors.toList());
        if (uris.isEmpty()) {
            throw createLauncherException("Couldn't find module " + packageID.toString());
        }
        return uris.get(0).toString();
    }

    /**
     * Read the access token generated for the CLI.
     *
     * @return access token for generated for the CLI
     */
    private static String getAccessTokenOfCLI() {
        settings = TomlParserUtils.readSettings();
        if (settings.getCentral() != null) {
            return settings.getCentral().getAccessToken();
        }
        return "";
    }

    /**
     * Reads the content of Module.md inside the archived balo.
     *
     * @param archivedFilePath balo file path of the module
     * @return content of Module.md as a string
     */
    private static String getModuleMDFileContent(String archivedFilePath, String packageName) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(archivedFilePath);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().equalsIgnoreCase(packageName + "/" + "Module.md")) {
                    InputStream stream = zipFile.getInputStream(entry);
                    Scanner scanner = new Scanner(stream, "UTF-8").useDelimiter("\\A");
                    return scanner.hasNext() ? scanner.next() : "";
                }
            }
        } catch (IOException ignore) {
        } finally {
            try {
                if (zipFile != null) {
                    zipFile.close();
                }
            } catch (IOException ignore) {
            }
        }
        return null;
    }

    /**
     * Read summary of the module from Module.md file.
     *
     * @param mdFileContent full content of Module.md
     * @return summary of the module
     */
    private static String readSummary(String mdFileContent) {
        if (mdFileContent.isEmpty()) {
            throw createLauncherException("Module.md in the artifact is empty");
        }

        Optional<String> result = Arrays.stream(mdFileContent.split("\n"))
                                        .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                                        .findFirst();

        if (!result.isPresent()) {
            throw createLauncherException("Cannot find module summary");
        }

        String firstLine = result.get();
        if (firstLine.length() > 50) {
            throw createLauncherException("Summary of the module exceeds 50 characters");
        }
        return firstLine;
    }

    /**
     * Push all modules to central.
     *
     * @param sourceRoot source root or project root
     * @param installToRepo repo the module should be pushed to central or the home repository
     * @param noBuild    do not build sources before pushing
     */
    public static void pushAllPackages(String sourceRoot, String installToRepo, boolean noBuild) {
        Path sourceRootPath = LauncherUtils.getSourceRootPath(sourceRoot);
        try {
            List<String> fileList = Files.list(sourceRootPath)
                                         .filter(path -> Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS))
                                         .map(ProjectDirs::getLastComp)
                                         .filter(dirName -> !isSpecialDirectory(dirName))
                                         .map(Path::toString).collect(Collectors.toList());
            if (fileList.size() == 0) {
                throw createLauncherException("no modules found to push in " + sourceRootPath.toString());
            }
            fileList.forEach(path -> pushPackages(path, sourceRoot, installToRepo, noBuild));
        } catch (IOException ex) {
            throw createLauncherException("error occurred while pushing modules from " + sourceRootPath.toString()
                                                     + " " + ex.getMessage());
        }
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
