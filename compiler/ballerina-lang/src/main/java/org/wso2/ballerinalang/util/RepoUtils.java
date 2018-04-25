package org.wso2.ballerinalang.util;

import org.ballerinalang.compiler.BLangCompilerException;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Home repository util methods.
 */
public class RepoUtils {
    private static final String USER_HOME = "user.home";
    private static final String PRODUCTION_URL = "https://api.central.ballerina.io/packages/";
    private static final String STAGING_URL = "https://api.staging-central.ballerina.io/packages/";
    private static final boolean BALLERINA_DEV_STAGE_CENTRAL = Boolean.parseBoolean(
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
}
