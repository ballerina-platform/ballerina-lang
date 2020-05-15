package org.ballerinalang.moduleloader;

import org.ballerinalang.moduleloader.model.Module;
import org.ballerinalang.moduleloader.model.ModuleId;
import org.ballerinalang.moduleloader.model.ModuleResolution;
import org.ballerinalang.moduleloader.model.ModuleVersion;
import org.ballerinalang.moduleloader.model.Project;
import org.ballerinalang.toml.model.Dependency;
import org.ballerinalang.toml.model.LockFileImport;
import org.ballerinalang.toml.model.Manifest;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class ModuleLoaderImpl implements ModuleLoader {

    private Project project;
    List<Repo> repos;

    public ModuleLoaderImpl(Project project, List<Repo> repos) {
        this.project = project;
        this.repos = repos;
    }

    @Override
    public ModuleId resolveVersion(ModuleId moduleId, ModuleId enclModuleId) throws IOException {
        // if version already exists in moduleId
        if (moduleId.getVersion() != null && moduleId.getVersion().getVersion() != null
                && !"".equals(moduleId.getVersion().getVersion().trim())) {
            return moduleId;
        }

        // if module is in the current project
        if (this.project.isModuleExists(moduleId)) {
            moduleId.setVersion(
                    new ModuleVersion(this.repos.get(0), this.project.getManifest().getProject().getVersion()));
            return moduleId;
        }

        // if lock file exists
        if (enclModuleId != null && this.project.hasLockFile()) {
            // not a top level module or bal
            String versionFromLockfile = resolveVersionFromLockFile(moduleId, enclModuleId);
            // version in lock can be null if the import is added after creating the lock
            if (versionFromLockfile != null) {
                ModuleVersion moduleVersion = resolveModuleVersion(this.repos, moduleId, versionFromLockfile);
                if (moduleVersion != null) {
                    moduleId.setVersion(moduleVersion);
                    return moduleId;
                }
            }
        }

        String versionFromManifest = null;
        // if it is an immediate import check the Ballerina.toml
        // Set version from the Ballerina.toml of the current project
        if (enclModuleId != null && this.project.getManifest() != null && this.project.isModuleExists(enclModuleId)) {
            // If exact version return
            versionFromManifest = resolveVersionFromManifest(moduleId, this.project.getManifest());
            if (versionFromManifest != null) {
                ModuleVersion moduleVersion = resolveModuleVersion(this.repos, moduleId, versionFromManifest);
                if (moduleVersion != null) {
                    moduleId.setVersion(moduleVersion);
                    return moduleId;
                }
            }
        }

        // if enclosing module is not a project module, module is transitive
        // if it is a transitive we need to check the toml file of the dependent module
        if (enclModuleId != null && !this.project.isModuleExists(enclModuleId)) {
            BaloCache homeBaloCache = (BaloCache) this.repos.get(4);
            Module parentModule = homeBaloCache.getModule(enclModuleId);
            Manifest parentManifest = RepoUtils.getManifestFromBalo(parentModule.getSourcePath());
            versionFromManifest = resolveVersionFromManifest(moduleId, parentManifest);
            // if exact version
            if (versionFromManifest != null) {
                ModuleVersion moduleVersion = resolveModuleVersion(this.repos, moduleId, versionFromManifest);
                if (moduleVersion != null) {
                    moduleId.setVersion(moduleVersion);
                    return moduleId;
                }
            }
        }

        // find latest in repos
        // if not exact version, look in caches and repos
        ModuleVersion moduleVersionFromRepos = resolveModuleVersion(this.repos, moduleId, versionFromManifest);
        if (moduleVersionFromRepos != null) {
            moduleId.setVersion(moduleVersionFromRepos);
            return moduleId;
        }

        // pull if the module is in a remote repo.
//        this.pullFromRemoteRepos(moduleId);
        // if remote repo pull fail we can do a build failure.

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
                if (moduleId.getOrgName().equals(nestedImport.getOrgName()) && moduleId.getModuleName()
                        .equals(nestedImport.getName())) {
                    return nestedImport.getVersion();
                }
            }
        }
        return null;
    }

    private String resolveVersionFromManifest(ModuleId moduleId, Manifest manifest) {
        for (Dependency dependency : manifest.getDependencies()) {
            if (dependency.getModuleName().equals(moduleId.getModuleName())
                    && dependency.getOrgName().equals(moduleId.getOrgName())
                    && dependency.getMetadata().getVersion() != null
                    && !"*".equals(dependency.getMetadata().getVersion())) {
                return dependency.getMetadata().getVersion();
            }
        }
        return null;
    }

    public void generateRepoHierarchy(List<Repo> repos) {
        // 1. product modules
        addProjectModules(repos, Paths.get(System.getProperty("user.dir")));

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

    public void addProjectModules(List<Repo> repos, Path projectPath) {
        ProjectModules projectModules = new ProjectModules(projectPath, this.project.getManifest().getProject().getOrgName(),
                this.project.getManifest().getProject().getVersion());
        repos.add(projectModules);
    }

    private ModuleVersion resolveModuleVersion(List<Repo> repos, ModuleId moduleId, String filter) throws IOException {
        TreeMap<String, Repo> moduleVersions = new TreeMap<>();
        for (Repo repo : repos) {
            for (String versionStr : repo.resolveVersions(moduleId, filter)) {
                moduleVersions.put(versionStr, repo);
            }
        }
        if (moduleVersions.isEmpty()) {
            return null;
        } else {
            return new ModuleVersion(moduleVersions.lastEntry().getValue(), moduleVersions.lastKey());
        }
    }

    private boolean isExactVersion(String version) {
        final Pattern semVerPattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
        return semVerPattern.matcher(version).matches();
    }
}
