package org.ballerinalang.moduleloader;

import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class RepoHierarchy {

    private List<Repo> repoList = new ArrayList<>();
    private static final String USER_DIR = "user.dir";
    private static final String BIR_CACHE = "bir-cache";

    public RepoHierarchy(String projectOrgName, String projectVersion) {
        generateRepoHierarchy(this.repoList, projectOrgName, projectVersion);
    }

    private void generateRepoHierarchy(List<Repo> repos, String projectOrgName, String projectVersion) {
        // 1. product modules
        ProjectModules projectModules = new ProjectModules(Paths.get(System.getProperty(USER_DIR)), projectOrgName,
                // this.project.getManifest().getProject().getOrgName()
                projectVersion);
        repos.add(projectModules);

        // 2. project bir cache
        BirCache projectBirCache = new BirCache(
                Paths.get(String.valueOf(Paths.get(System.getProperty(USER_DIR))), ProjectDirConstants.TARGET_DIR_NAME,
                        ProjectDirConstants.CACHES_DIR_NAME, ProjectDirConstants.BIR_CACHE_DIR_NAME));
        repos.add(projectBirCache);

        // 3. distribution bir cache
        BirCache distBirCache = new BirCache(
                Paths.get(System.getProperty(ProjectDirConstants.BALLERINA_HOME), BIR_CACHE));
        repos.add(distBirCache);

        // 4. home bir cache
        BirCache homeBirCache = new BirCache(
                RepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.BIR_CACHE_DIR_NAME));
        repos.add(homeBirCache);

        // 5. home balo cache
        BaloCache homeBaloCache = new BaloCache(
                RepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.BALO_CACHE_DIR_NAME));
        repos.add(homeBaloCache);

        // 6. central repo
        Central central = new Central();
        repos.add(central);
    }

    List<Repo> getRepoList() {
        return repoList;
    }

    public ProjectModules getProjectModules() {
        return (ProjectModules) this.repoList.get(0);
    }

    public BirCache getProjectBirCache() {
        return (BirCache) this.repoList.get(1);
    }

    public BirCache getDistributionBirCache() {
        return (BirCache) this.repoList.get(2);
    }

    public BirCache getHomeBirCache() {
        return (BirCache) this.repoList.get(3);
    }

    BaloCache getHomeBaloCache() {
        return (BaloCache) this.repoList.get(4);
    }

    public Central getCentralRepo() {
        return (Central) this.repoList.get(5);
    }
}
