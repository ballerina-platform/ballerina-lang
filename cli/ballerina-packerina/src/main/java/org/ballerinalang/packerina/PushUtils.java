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

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.spi.EmbeddedExecutor;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.model.Settings;
import org.ballerinalang.toml.parser.ManifestProcessor;
import org.ballerinalang.toml.parser.SettingsProcessor;
import org.ballerinalang.util.EmbeddedExecutorProvider;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.repo.RemoteRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.Repo;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Files;
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

/**
 * This class provides util methods when pushing Ballerina packages to central and home repository.
 *
 * @since 0.95.2
 */
public class PushUtils {

    private static final Path BALLERINA_HOME_PATH = RepoUtils.createAndGetHomeReposPath();
    private static final Path SETTINGS_TOML_FILE_PATH = BALLERINA_HOME_PATH.resolve(
            ProjectDirConstants.SETTINGS_FILE_NAME);
    private static PrintStream outStream = System.err;
    private static EmbeddedExecutor executor = EmbeddedExecutorProvider.getInstance().getExecutor();

    /**
     * Push/Uploads packages to the central repository.
     *
     * @param packageName   path of the package folder to be pushed
     * @param installToRepo if it should be pushed to central or home
     */
    public static void pushPackages(String packageName, String installToRepo) {
        Manifest manifest = readManifestConfigurations();
        if (manifest.getName().isEmpty()) {
            throw new BLangCompilerException("An org-name is required when pushing. This is not specified in " +
                                                     "Ballerina.toml inside the project");
        }

        if (manifest.getVersion().isEmpty()) {
            throw new BLangCompilerException("A package version is required when pushing. This is not specified " +
                                                     "in Ballerina.toml inside the project");
        }

        String orgName = manifest.getName();
        String version = manifest.getVersion();

        PackageID packageID = new PackageID(new Name(orgName), new Name(packageName), new Name(version));

        Path prjDirPath = Paths.get(".").toAbsolutePath().normalize();

        // Get package path from project directory path
        Path pkgPathFromPrjtDir = Paths.get(prjDirPath.toString(), ProjectDirConstants.DOT_BALLERINA_DIR_NAME,
                                            ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME, orgName,
                                            packageName, version, packageName + ".zip");
        if (Files.notExists(pkgPathFromPrjtDir)) {
            throw new BLangCompilerException("package does not exist");
        }

        if (installToRepo == null) {
            // Get access token
            String accessToken = checkAccessToken();

            // Read the Package.md file content from the artifact
            String mdFileContent = getPackageMDFileContent(pkgPathFromPrjtDir.toString(), packageName);
            if (mdFileContent == null) {
                throw new BLangCompilerException("Cannot find Package.md file in the artifact");
            }

            String description = readSummary(mdFileContent);
            String homepageURL = manifest.getHomepageURL();
            String repositoryURL = manifest.getRepositoryURL();
            String apiDocURL = manifest.getDocumentationURL();
            String authors = String.join(",", manifest.getAuthors());
            String keywords = String.join(",", manifest.getKeywords());
            String license = manifest.getLicense();

            // Push package to central
            String resourcePath = resolvePkgPathInRemoteRepo(packageID);
            String msg = orgName + "/" + packageName + ":" + version + " [project repo -> central]";
            executor.execute("packaging_push/packaging_push.balx", true, accessToken, mdFileContent,
                             description, homepageURL, repositoryURL, apiDocURL, authors, keywords, license,
                             resourcePath, pkgPathFromPrjtDir.toString(), msg);

        } else {
            if (!installToRepo.equals("home")) {
                throw new BLangCompilerException("Unknown repository provided to push the package");
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
            long modifiedTimeOfFileAtStart = getLastModifiedTimeOfFile(SETTINGS_TOML_FILE_PATH);
            executor.execute("packaging_token_updater/packaging_token_updater.balx", false);

            boolean waitForToken = true;
            while (waitForToken) {
                pause();
                long modifiedTimeOfFileAfter = getLastModifiedTimeOfFile(SETTINGS_TOML_FILE_PATH);
                if (modifiedTimeOfFileAtStart != modifiedTimeOfFileAfter) {
                    accessToken = getAccessTokenOfCLI();
                    if (accessToken.isEmpty()) {
                        throw new BLangCompilerException("Access token is missing in " +
                                                                 SETTINGS_TOML_FILE_PATH.toString() + " Please " +
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
            throw new BLangCompilerException("Error occurred when getting the access token");
        }
    }

    /**
     * Get last modified time of file.
     *
     * @param path file path
     * @return last modified time in milliseconds
     */
    private static long getLastModifiedTimeOfFile(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException ex) {
            throw new BLangCompilerException("Error occurred when reading file for token " +
                                                     SETTINGS_TOML_FILE_PATH.toString());
        }
    }

    /**
     * Install the package artifact to the home repository.
     *
     * @param packageID          packageID of the package
     * @param pkgPathFromPrjtDir package path from the project directory
     */
    private static void installToHomeRepo(PackageID packageID, Path pkgPathFromPrjtDir) {
        Path targetDirectoryPath = Paths.get(BALLERINA_HOME_PATH.toString(),
                                             ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME,
                                             packageID.orgName.getValue(),
                                             packageID.name.getValue(),
                                             packageID.version.getValue(),
                                             packageID.name.getValue() + ".zip");
        if (Files.exists(targetDirectoryPath)) {
            throw new BLangCompilerException("Ballerina package exists in the home repository");
        } else {
            try {
                Files.createDirectories(targetDirectoryPath);
                Files.copy(pkgPathFromPrjtDir, targetDirectoryPath, StandardCopyOption.REPLACE_EXISTING);
                outStream.println(packageID.orgName.getValue() + "/" + packageID.name.getValue() + ":" +
                                          packageID.version.getValue() + " [project repo -> home repo]");
            } catch (IOException e) {
                throw new BLangCompilerException("Error occurred when creating directories in the home repository");
            }
        }
    }

    /**
     * Get URI of the package from the remote repo.
     *
     * @param packageID packageID object
     * @return full URI path of the package relative to the remote repo
     */
    private static String resolvePkgPathInRemoteRepo(PackageID packageID) {
        Repo<URI> remoteRepo = new RemoteRepo(URI.create(RepoUtils.getRemoteRepoURL()));
        Patten patten = remoteRepo.calculate(packageID);
        if (patten == Patten.NULL) {
            throw new BLangCompilerException("Couldn't find package " + packageID.toString());
        }
        Converter<URI> converter = remoteRepo.getConverterInstance();
        List<URI> uris = patten.convert(converter).collect(Collectors.toList());
        if (uris.isEmpty()) {
            throw new BLangCompilerException("Couldn't find package " + packageID.toString());
        }
        return uris.get(0).toString();
    }

    /**
     * Read the manifest.
     *
     * @return manifest configuration object
     */
    private static Manifest readManifestConfigurations() {
        String tomlFilePath = Paths.get(".").toAbsolutePath().normalize().resolve
                (ProjectDirConstants.MANIFEST_FILE_NAME).toString();
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
        String tomlFilePath = SETTINGS_TOML_FILE_PATH.toString();
        try {
            return SettingsProcessor.parseTomlContentFromFile(tomlFilePath);
        } catch (IOException e) {
            return new Settings();
        }
    }

    /**
     * Read the access token generated for the CLI.
     *
     * @return access token for generated for the CLI
     */
    private static String getAccessTokenOfCLI() {
        Settings settings = readSettings();
        if (settings.getCentral() != null) {
            return settings.getCentral().getAccessToken();
        }
        return "";
    }

    /**
     * Reads the content of Package.md inside the archived balo.
     *
     * @param archivedFilePath balo file path of the package
     * @return content of Package.md as a string
     */
    private static String getPackageMDFileContent(String archivedFilePath, String packageName) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(archivedFilePath);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().equalsIgnoreCase(packageName + "/" + "Package.md")) {
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
     * Read summary of the package from Package.md file.
     *
     * @param mdFileContent full content of Package.md
     * @return summary of the package
     */
    private static String readSummary(String mdFileContent) {
        if (mdFileContent.isEmpty()) {
            throw new BLangCompilerException("Package.md in the artifact is empty");
        }

        Optional<String> result = Arrays.asList(mdFileContent.split("\n")).stream()
                                        .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                                        .findFirst();

        if (!result.isPresent()) {
            throw new BLangCompilerException("Cannot find package summary");
        }

        String firstLine = result.get();
        if (firstLine.length() > 50) {
            throw new BLangCompilerException("Summary of the package exceeds 50 characters");
        }
        return firstLine;
    }
}
