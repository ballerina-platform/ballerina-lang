package org.ballerinalang.moduleloader;

import org.ballerinalang.moduleloader.model.Module;
import org.ballerinalang.moduleloader.model.ModuleId;
import org.ballerinalang.moduleloader.model.ModuleResolution;
import org.ballerinalang.moduleloader.model.Project;
import org.ballerinalang.toml.model.Dependency;
import org.ballerinalang.toml.model.LockFileImport;
import org.ballerinalang.toml.model.Manifest;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class ModuleLoaderImpl implements ModuleLoader {

    private Project project;
    private List<Repo> repos;

    public ModuleLoaderImpl(Project project, List<Repo> repos) {
        this.project = project;
        this.repos = repos;
    }

    @Override
    public ModuleId resolveVersion(ModuleId moduleId, ModuleId enclModuleId) throws IOException {
        // return if version exists in moduleId
        if (moduleId.version != null && !"".equals(moduleId.version.trim())) {
            return moduleId;
        }

        // if not transitive and lock file exists
        if (this.project.hasLockFile()) { // enclModuleId != null &&
            // not a top level module or bal
            String lockFileVersion = resolveVersionFromLockFile(moduleId, enclModuleId);
            if (lockFileVersion != null) {
                 moduleId.version = lockFileVersion;
                return moduleId;
            }
        }

        // If module is in the current project
        if (this.repos.get(0).isModuleExists(moduleId)) {
            moduleId.version = this.project.manifest.getProject().getVersion();
            return moduleId;
        }

        // If it is an immediate import check the Ballerina.toml
        // Set version from the Ballerina.toml of the current project
        if (enclModuleId != null && this.project.manifest != null) {
            // If exact version return
            String versionFromManifest = resolveVersionFromManifest(moduleId, this.project.manifest);
            if (isExactVersion(versionFromManifest)) {
                moduleId.version = versionFromManifest;
                return moduleId;
            }
            // If not exact version, look in caches and repos
            String moduleIdFromRepos = resolveModuleVersion(this.repos, moduleId, versionFromManifest);
            if (moduleIdFromRepos != null) {
                moduleId.version = moduleIdFromRepos;
                return moduleId;
            }
        }

        // If enclosing module is not a project module, module is transitive
        // If it is a transitive we need to check the toml file of the dependent module
        if (!this.repos.get(0).isModuleExists(enclModuleId)) {
            BaloCache homeBaloCache = (BaloCache) this.repos.get(4);
            Module parentModule = homeBaloCache.getModule(enclModuleId);
            Manifest parentManifest = RepoUtils.getManifestFromBalo(parentModule.getSourcePath());
            String versionFromManifest = resolveVersionFromManifest(moduleId, parentManifest);
            // If exact version
            if (isExactVersion(versionFromManifest)) {
                moduleId.version = versionFromManifest;
                return moduleId;
            }
            // If not exact version, look in caches and repos
            String moduleIdFromRepos = resolveModuleVersion(this.repos, moduleId, versionFromManifest);
            if (moduleIdFromRepos != null) {
                moduleId.version = moduleIdFromRepos;
                return moduleId;
            }
        }

        // find latest in repos
        // pull if the module is in a remote repo.
        return moduleId;
    }

    @Override
    public ModuleResolution resolveModule(ModuleId moduleId) {
        return null;
    }

    private String resolveVersionFromLockFile(ModuleId moduleId, ModuleId enclModuleId) {
        if (this.project.getLockFile().getImports().containsKey(enclModuleId.toString())) {
            List<LockFileImport> foundBaseImport = this.project.getLockFile().getImports().get(enclModuleId.toString());

            for (LockFileImport nestedImport : foundBaseImport) {
                if (moduleId.orgName.equals(nestedImport.getOrgName()) && moduleId.moduleName
                        .equals(nestedImport.getName())) {
                    return nestedImport.getVersion();
                }
            }
        }
        return null;
    }

    private String resolveVersionFromManifest(ModuleId moduleId, Manifest manifest) {
        for (Dependency dependency : manifest.getDependencies()) {
            if (dependency.getModuleName().equals(moduleId.moduleName)
                    && dependency.getOrgName().equals(moduleId.orgName)
                    && dependency.getMetadata().getVersion() != null
                    && !"*".equals(dependency.getMetadata().getVersion())) {
                return dependency.getMetadata().getVersion();
            }
        }
        return null;
    }

    public void generateRepoHierarchy() {
        // 1. product modules
        ProjectModules projectModules = new ProjectModules(Paths.get(System.getProperty("user.dir")),
                this.project.manifest.getProject().getOrgName(), this.project.manifest.getProject().getVersion());
        repos.add(projectModules);

        // 2. project bir cache
        BirCache projectBirCache = new BirCache(Paths.get(String.valueOf(Paths.get(System.getProperty("user.dir"))),
                ProjectDirConstants.TARGET_DIR_NAME, ProjectDirConstants.CACHES_DIR_NAME,
                ProjectDirConstants.BIR_CACHE_DIR_NAME));
        repos.add(projectBirCache);

        // 3. distribution bir cache
        BirCache distBirCache = new BirCache(
                Paths.get(System.getProperty(ProjectDirConstants.BALLERINA_HOME), "bir-cache"));
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

    private String resolveModuleVersion(List<Repo> repos, ModuleId moduleId, String filter) throws IOException {
        TreeSet<String> versions = new TreeSet<>();
        for (Repo repo : repos) {
            versions.addAll(repo.resolveVersions(moduleId, filter));
        }
        return versions.last();
    }

    private boolean isExactVersion(String version) {
        final Pattern semVerPattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
        return semVerPattern.matcher(version).matches();
    }
}
