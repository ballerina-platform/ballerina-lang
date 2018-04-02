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
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.HomeRepoUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class provides util methods when pushing Ballerina packages to central and home repository.
 *
 * @since 0.95.2
 */
public class PushUtils {
    private static PrintStream outStream = System.err;

    /**
     * Push/Uploads packages to the central repository.
     *
     * @param packageName   path of the package folder to be pushed
     * @param installToRepo if it should be pushed to central or home
     */
    public static void pushPackages(String packageName, String installToRepo) {
        String accessToken = getAccessTokenOfCLI();
        Manifest manifest = readManifestConfigurations();
        if (manifest.getName() == null && manifest.getVersion() == null) {
            throw new BLangCompilerException("An org-name and package version is required when pushing. " +
                                                     "This is not specified in Ballerina.toml inside the project");
        }
        String orgName = manifest.getName();
        String version = manifest.getVersion();

        PackageID packageID = new PackageID(new Name(orgName), new Name(packageName), new Name(version));
        Path prjDirPath = Paths.get(".").toAbsolutePath().normalize().resolve(ProjectDirConstants
                                                                                      .DOT_BALLERINA_DIR_NAME);

        // Get package path from project directory path
        Path pkgPathFromPrjtDir = Paths.get(prjDirPath.toString(), "repo", Names.ANON_ORG.getValue(),
                                            packageName, Names.DEFAULT_VERSION.getValue(), packageName + ".zip");
        if (installToRepo == null) {
            if (accessToken == null) {
                // TODO: get bal home location dynamically
                throw new BLangCompilerException("Access token is missing in ~/ballerina_home/Settings.toml file.\n" +
                                                         "Please visit https://central.ballerina.io/cli-token");
            }
            // Push package to central
            String resourcePath = resolvePkgPathInRemoteRepo(packageID);
            String msg = orgName + "/" + packageName + ":" + version + " [project repo -> central]";
            EmbeddedExecutor executor = EmbeddedExecutorProvider.getInstance().getExecutor();
            executor.execute("packaging.push/ballerina.push.balx", accessToken, resourcePath,
                             pkgPathFromPrjtDir.toString(), msg);
        } else {
            if (!installToRepo.equals("home")) {
                throw new BLangCompilerException("Unknown repository provided to push the package");
            }
            Path balHomeDir = HomeRepoUtils.createAndGetHomeReposPath();
            Path targetDirectoryPath = Paths.get(balHomeDir.toString(), "repo", orgName, packageName, version,
                                                 packageName + ".zip");
            if (Files.exists(targetDirectoryPath)) {
                throw new BLangCompilerException("Ballerina package exists in the home repository");
            } else {
                try {
                    Files.createDirectories(targetDirectoryPath);
                    Files.copy(pkgPathFromPrjtDir, targetDirectoryPath, StandardCopyOption.REPLACE_EXISTING);
                    outStream.println(orgName + "/" + packageName + ":" + version + " [project repo -> home repo]");
                } catch (IOException e) {
                    throw new BLangCompilerException("Error occurred when creating directories in the home repository");
                }
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
        Repo<URI> remoteRepo = new RemoteRepo(URI.create("https://staging.central.ballerina.io:9090/"));
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
        String tomlFilePath = HomeRepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.SETTINGS_FILE_NAME)
                                           .toString();
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
        return null;
    }
}
