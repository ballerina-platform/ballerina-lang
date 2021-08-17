package io.ballerina.projects.internal.environment;

import io.ballerina.projects.ProjectException;
import io.ballerina.projects.Settings;
import io.ballerina.projects.TomlDocument;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.PackageRepository;
import io.ballerina.projects.internal.SettingsBuilder;
import io.ballerina.projects.internal.repositories.LocalPackageRepository;
import io.ballerina.projects.internal.repositories.RemotePackageRepository;
import io.ballerina.projects.util.ProjectConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static io.ballerina.runtime.api.constants.RuntimeConstants.USER_HOME;

/**
 * Represents the Ballerina user home and responsible for resolving cached packages.
 *
 * @since 2.0.0
 */
public final class BallerinaUserHome {

    private final Path ballerinaUserHomeDirPath;
    private final RemotePackageRepository remotePackageRepository;
    private final LocalPackageRepository localPackageRepository;
    private final Map<String, PackageRepository> customRepositories;

    private BallerinaUserHome(Environment environment, Path ballerinaUserHomeDirPath) {
        this.ballerinaUserHomeDirPath = ballerinaUserHomeDirPath;
        Path repositoryPath = ballerinaUserHomeDirPath.resolve(ProjectConstants.REPOSITORIES_DIR);
        Path remotePackageRepositoryPath = ballerinaUserHomeDirPath.resolve(ProjectConstants.REPOSITORIES_DIR)
                .resolve(ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME);
        try {
            Files.createDirectories(remotePackageRepositoryPath);
        } catch (AccessDeniedException ae) {
            throw new ProjectException("permission denied to create the directory: " + repositoryPath);
        } catch (IOException exception) {
            throw new ProjectException("unable to create the file system cache of Ballerina Central repository: " +
                    remotePackageRepositoryPath);
        }

        this.remotePackageRepository = RemotePackageRepository
                .from(environment, remotePackageRepositoryPath, readSettings());
        this.localPackageRepository = createLocalRepository(environment);
        this.customRepositories = Map.of(ProjectConstants.LOCAL_REPOSITORY_NAME, localPackageRepository);
    }

    public static BallerinaUserHome from(Environment environment, Path ballerinaUserHomeDirPath) {
        validateBallerinaUserHomeDir(ballerinaUserHomeDirPath);
        return new BallerinaUserHome(environment, ballerinaUserHomeDirPath);
    }

    public static BallerinaUserHome from(Environment environment) {
        String userHomeDir = System.getProperty(USER_HOME);
        if (userHomeDir == null || userHomeDir.isEmpty()) {
            throw new ProjectException("unable to get user home directory");
        }

        Path homeRepoPath = Paths.get(userHomeDir, ProjectConstants.HOME_REPO_DEFAULT_DIRNAME);
        return from(environment, homeRepoPath);
    }

    public RemotePackageRepository remotePackageRepository() {
        return this.remotePackageRepository;
    }

    public Map<String, PackageRepository> customRepositories() {
        return this.customRepositories;
    }

    public LocalPackageRepository localPackageRepository() {
        return localPackageRepository;
    }

    /**
     * Read Settings.toml to populate the configurations.
     *
     * @return {@link Settings} settings object
     */
    private Settings readSettings() {
        Path settingsFilePath = this.ballerinaUserHomeDirPath.resolve(ProjectConstants.SETTINGS_FILE_NAME);
        if (Files.notExists(settingsFilePath)) {
            try {
                Files.createFile(settingsFilePath);
            } catch (AccessDeniedException ae) {
                throw new ProjectException("permission denied to create the file: "
                        + ProjectConstants.SETTINGS_FILE_NAME + " in " + this.ballerinaUserHomeDirPath);
            } catch (IOException e) {
                throw new ProjectException("failed to create file: " +  ProjectConstants.SETTINGS_FILE_NAME + " in "
                        + this.ballerinaUserHomeDirPath + " " + e.getMessage());
            }
        }
        try {
            TomlDocument settingsTomlDocument = TomlDocument
                    .from(String.valueOf(settingsFilePath.getFileName()), Files.readString(settingsFilePath));
            SettingsBuilder settingsBuilder = SettingsBuilder.from(settingsTomlDocument);
            return settingsBuilder.settings();
        } catch (IOException e) {
            // Ignore 'Settings.toml' reading and parsing errors and return empty Settings object
            return Settings.from();
        }
    }

    private static void validateBallerinaUserHomeDir(Path ballerinaUserHomeDirPath) {
        // If directory does not exists, create it
        if (Files.notExists(ballerinaUserHomeDirPath) || !Files.isDirectory(ballerinaUserHomeDirPath)) {
            try {
                Files.createDirectories(ballerinaUserHomeDirPath);
            } catch (IOException e) {
                throw new ProjectException(
                        "Ballerina user home directory does not exists in '" + ballerinaUserHomeDirPath
                                + "', Directory creation also failed");
            }
        }
    }

    private LocalPackageRepository createLocalRepository(Environment environment) {
        Path repositoryPath = ballerinaUserHomeDirPath.resolve(ProjectConstants.REPOSITORIES_DIR)
                .resolve(ProjectConstants.LOCAL_REPOSITORY_NAME);
        try {
            Files.createDirectories(repositoryPath);
        } catch (IOException exception) {
            throw new ProjectException("unable to create repository: " + ProjectConstants.LOCAL_REPOSITORY_NAME);
        }
        String ballerinaShortVersion = RepoUtils.getBallerinaShortVersion();
        return new LocalPackageRepository(environment, repositoryPath, ballerinaShortVersion);
    }
}
