/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects.internal.repositories;

import io.ballerina.projects.CompilationCache;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.internal.environment.FingerprintCompilationCache;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.wso2.ballerinalang.util.RepoUtils;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This class represents the package respository available in the Ballerina distribution.
 *
 * @since 2.0.0
 */
public class BallerinaDistributionRepository extends FileSystemRepository {

    /** Repository directory in the user home that caches distribution packages compiled against other versions. */
    public static final String HOME_CACHE_REPOSITORY = "distribution";

    private Path userHome;

    private BallerinaDistributionRepository(Environment environment, Path distributionRepoPath) {
        super(environment, distributionRepoPath);
    }

    public static BallerinaDistributionRepository from(Environment environment, Path distributionPath) {
        Path distributionRepoPath = distributionPath.resolve(ProjectConstants.DIST_CACHE_DIRECTORY);
        if (Files.notExists(distributionPath)) {
            throw new ProjectException("Ballerina distribution repository does not exists: " +
                    distributionRepoPath);
        }
        return new BallerinaDistributionRepository(environment, distributionRepoPath);
    }

    /**
     * Sets the Ballerina user home in which distribution packages compiled against other dependency versions are
     * cached.
     *
     * @param userHome Ballerina user home directory
     */
    public void setUserHome(Path userHome) {
        this.userHome = userHome;
    }

    /**
     * Returns the cache directory in the user home for distribution packages that are compiled against dependency
     * versions other than the ones the distribution cache is compiled against.
     *
     * @param userHome Ballerina user home directory
     * @return cache directory in the user home
     */
    public static Path homeCacheDir(Path userHome) {
        return userHome.resolve(ProjectConstants.REPOSITORIES_DIR)
                .resolve(HOME_CACHE_REPOSITORY)
                .resolve(ProjectConstants.CACHES_DIR_NAME + "-" + RepoUtils.getBallerinaShortVersion());
    }

    @Override
    protected CompilationCache createCompilationCache(Project project) {
        if (!cachesPerFingerprint()) {
            return super.createCompilationCache(project);
        }
        // The distribution cache is read in place when it matches. A distribution may be installed in a location
        // that is not writable, so other variants are cached in the user home.
        Path home = userHome != null ? userHome : ProjectUtils.createAndGetHomeReposPath();
        return new FingerprintCompilationCache(project, homeCacheDir(home), cacheDir());
    }
}
