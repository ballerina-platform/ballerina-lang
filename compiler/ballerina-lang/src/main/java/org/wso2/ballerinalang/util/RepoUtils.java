package org.wso2.ballerinalang.util;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.toml.model.Settings;
import org.ballerinalang.toml.parser.SettingsProcessor;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Home repository util methods.
 */
public class RepoUtils {
    private static final String USER_HOME = "user.home";
    private static final String DEFAULT_TERMINAL_SIZE = "80";
    private static final String BALLERINA_CLI_WIDTH = "BALLERINA_CLI_WIDTH";
    private static final String PRODUCTION_URL = "https://api.central.ballerina.io/packages/";
    private static final String STAGING_URL = "https://api.staging-central.ballerina.io/packages/";
    private static final boolean BALLERINA_DEV_STAGE_CENTRAL = Boolean.parseBoolean(
            System.getenv("BALLERINA_DEV_STAGE_CENTRAL"));
    private static Settings settings = null;

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
     * Checks if the path is a project repo.
     *
     * @param path dir path
     * @return true if the directory is a project repo, false if its the home repo
     */
    public static boolean hasProjectRepo(Path path) {
        path = path.resolve(ProjectDirConstants.DOT_BALLERINA_DIR_NAME);
        return !path.equals(createAndGetHomeReposPath()) && Files.exists(path, LinkOption.NOFOLLOW_LINKS) &&
                Files.isDirectory(path);
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
     * Read Settings.toml to populate the configurations.
     *
     * @return settings object
     */
    public static Settings readSettings() {
        if (settings == null) {
            String tomlFilePath = RepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.SETTINGS_FILE_NAME)
                                           .toString();
            try {
                settings = SettingsProcessor.parseTomlContentFromFile(tomlFilePath);
            } catch (IOException e) {
                settings = new Settings();
            }
        }
        return settings;
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
}
