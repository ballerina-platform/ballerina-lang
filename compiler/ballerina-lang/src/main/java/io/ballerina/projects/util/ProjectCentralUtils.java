package io.ballerina.projects.util;

import io.ballerina.projects.PackageExistsException;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.Settings;
import io.ballerina.projects.TomlDocument;
import io.ballerina.projects.internal.SettingsBuilder;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.exceptions.PackageAlreadyExistsException;

import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

import static io.ballerina.projects.util.ProjectUtils.createAndGetHomeReposPath;


/**
 * Utilities related to Central functionality.
 *
 * @since 2.0.0
 */
public class ProjectCentralUtils {

    private static final String SUPPORTED_PLATFORM = "java11";

    public static void pullPackage(String orgName, String packageName) throws ProjectException {
        pullPackage(orgName, packageName, "");
    }

    public static void pullPackage(String orgName, String packageName, String version) throws ProjectException {
        Settings settings = readSettings();
        Proxy proxy = ProjectUtils.initializeProxy(settings.getProxy());
        String accessToken = ProjectUtils.getAccessTokenOfCLI(settings);

        CentralAPIClient centralAPIClient = new CentralAPIClient(RepoUtils.getRemoteRepoURL(), proxy, accessToken);

        Path packagePathInBalaCache =
                ProjectUtils.createAndGetHomeReposPath()
                        .resolve(ProjectConstants.REPOSITORIES_DIR)
                        .resolve(ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME)
                        .resolve(ProjectConstants.BALA_DIR_NAME)
                        .resolve(orgName)
                        .resolve(packageName);

        try {
            centralAPIClient.pullPackage(orgName, packageName, version, packagePathInBalaCache, SUPPORTED_PLATFORM,
                        ProjectUtils.getBallerinaVersion(), false);
        } catch (PackageAlreadyExistsException e) {
            throw new PackageExistsException(e.getMessage());
        } catch (CentralClientException e) {
            throw new ProjectException(e.getMessage());
        }

    }

    /**
     * Read Settings.toml to populate the configurations.
     *
     * @return {@link Settings} settings object
     */
    private static Settings readSettings() {
        Path settingsFilePath = createAndGetHomeReposPath().resolve(ProjectConstants.SETTINGS_FILE_NAME);
        try {
            TomlDocument settingsTomlDocument = TomlDocument
                    .from(String.valueOf(settingsFilePath.getFileName()), Files.readString(settingsFilePath));
            SettingsBuilder settingsBuilder = SettingsBuilder.from(settingsTomlDocument);
            return settingsBuilder.settings();
        } catch (IOException e) {
            // If Settings.toml not exists return empty Settings object
            return Settings.from();
        }
    }

}
