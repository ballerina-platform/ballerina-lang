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

package org.wso2.ballerinalang.compiler.packaging.module.resolver;

import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.ballerinalang.util.RepoUtils.getLibDir;

/**
 * Repo hierarchy which resolves modules.
 */
public class RepoHierarchy {

    private List<Repo> repoList = new ArrayList<>();
    private static final String USER_DIR = "user.dir";
    private static final String BIR_CACHE = "bir-cache";

    public RepoHierarchy(String projectOrgName, String projectVersion, boolean offline, boolean testsEnabled,
            SourceDirectory sourceDirectory) {
        generateRepoHierarchy(this.repoList, projectOrgName, projectVersion, offline, testsEnabled, sourceDirectory);
    }

    // if online -> no central repo in the hierarchy
    private void generateRepoHierarchy(List<Repo> repos, String projectOrgName, String projectVersion, boolean offline,
            boolean testsEnabled, SourceDirectory sourceDirectory) {
        // 1. product modules
        ProjectModules projectModules = new ProjectModules(
                sourceDirectory.getPath().resolve(ProjectDirConstants.SOURCE_DIR_NAME), projectOrgName, projectVersion,
                testsEnabled);
        repos.add(projectModules);

        // 2. Project build repo
        Path systemZipRepo = getLibDir().resolve("repo");
        ProjectBuildRepo projectBuildRepo = new ProjectBuildRepo(systemZipRepo);
        repos.add(projectBuildRepo);

        // 3. project bir cache
        BirCache projectBirCache = new BirCache(
                Paths.get(String.valueOf(Paths.get(System.getProperty(USER_DIR))), ProjectDirConstants.TARGET_DIR_NAME,
                        ProjectDirConstants.CACHES_DIR_NAME, ProjectDirConstants.BIR_CACHE_DIR_NAME));
        repos.add(projectBirCache);

        // 4. distribution bir cache
        BirCache distBirCache = new BirCache(
                Paths.get(System.getProperty(ProjectDirConstants.BALLERINA_HOME), BIR_CACHE));
        repos.add(distBirCache);

        // 5. home bir cache
        BirCache homeBirCache = new BirCache(
                RepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.BIR_CACHE_DIR_NAME));
        repos.add(homeBirCache);

        // 6. home balo cache
        BaloCache homeBaloCache = new BaloCache(
                RepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.BALO_CACHE_DIR_NAME));
        repos.add(homeBaloCache);

        if (!offline) {
            // 7. central repo
            Central central = new Central();
            repos.add(central);
        }
    }

    public List<Repo> getRepoList() {
        return repoList;
    }

    public ProjectModules getProjectModules() {
        return (ProjectModules) this.repoList.get(0);
    }

    public ProjectBuildRepo getProjectBuildRepo() {
        return (ProjectBuildRepo) this.repoList.get(1);
    }

    public BirCache getProjectBirCache() {
        return (BirCache) this.repoList.get(2);
    }

    public BirCache getDistributionBirCache() {
        return (BirCache) this.repoList.get(3);
    }

    public BirCache getHomeBirCache() {
        return (BirCache) this.repoList.get(4);
    }

    public BaloCache getHomeBaloCache() {
        return (BaloCache) this.repoList.get(5);
    }

    public Central getCentralRepo() {
        return (Central) this.repoList.get(6);
    }
}
