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
import java.util.TreeMap;

import static org.ballerinalang.moduleloader.Util.isGreaterVersion;

public class ModuleLoaderImpl implements ModuleLoader {

    private Project project;
    private List<Repo> repos;

    public ModuleLoaderImpl(Project project, List<Repo> repos) {
        this.project = project;
        this.repos = repos;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<Repo> getRepos() {
        return repos;
    }

    public void setRepos(List<Repo> repos) {
        this.repos = repos;
    }

    @Override
    public ModuleId resolveVersion(ModuleId moduleId, ModuleId enclModuleId) throws IOException {
        // if version already exists in moduleId
        if (moduleId.getVersion() != null && !"".equals(moduleId.getVersion().trim())) {
            return moduleId;
        }

        // if module is in the current project
        if (this.project.isModuleExists(moduleId)) {
            moduleId.setVersion(this.project.getManifest().getProject().getVersion());
            return moduleId;
        }

        // if lock file exists
        if (enclModuleId != null && this.project.hasLockFile()) {
            // not a top level module or bal
            String versionFromLockfile = resolveVersionFromLockFile(moduleId, enclModuleId);
            // version in lock can be null if the import is added after creating the lock
            if (versionFromLockfile != null) {
                String resolvedVersion =  resolveModuleVersionFromRepos(this.repos, moduleId, versionFromLockfile);
                moduleId.setVersion(resolvedVersion);
                if (moduleId.getVersion() != null) {
                    return moduleId;
                }
            }
        }

        String versionFromManifest;
        // if it is an immediate import check the Ballerina.toml
        // Set version from the Ballerina.toml of the current project
        if (enclModuleId != null && this.project.getManifest() != null && this.project.isModuleExists(enclModuleId)) {
            // If exact version return
            versionFromManifest = resolveVersionFromManifest(moduleId, this.project.getManifest());
            if (versionFromManifest != null) {
                moduleId.setVersion(resolveModuleVersionFromRepos(this.repos, moduleId, versionFromManifest));
                if (moduleId.getVersion() != null) {
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
            if (versionFromManifest != null) {
                moduleId.setVersion(resolveModuleVersionFromRepos(this.repos, moduleId, versionFromManifest));
                if (moduleId.getVersion() != null) {
                    return moduleId;
                }
            }
        }

        return moduleId;
    }

    @Override
    public ModuleResolution resolveModule(ModuleId moduleId) {
        return null;
    }

    String resolveVersionFromLockFile(ModuleId moduleId, ModuleId enclModuleId) {
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

    public String resolveVersionFromManifest(ModuleId moduleId, Manifest manifest) {
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
        ProjectModules projectModules = new ProjectModules(Paths.get(System.getProperty("user.dir")),
                this.project.getManifest().getProject().getOrgName(),
                this.project.getManifest().getProject().getVersion());
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

    private String resolveModuleVersionFromRepos(List<Repo> repos, ModuleId moduleId, String filter) throws IOException {
        TreeMap<String, Repo> moduleVersions = new TreeMap<>();
        for (Repo repo : repos) {
            for (String versionStr : repo.resolveVersions(moduleId, filter)) {
                if (moduleVersions.isEmpty() || isGreaterVersion(moduleVersions.lastKey(), versionStr)) {
                    moduleVersions.put(versionStr, repo);
                }
            }
        }
        if (!moduleVersions.isEmpty()) {
            // pull if the module is in a remote repo.
            Repo versionResolvedRepo = moduleVersions.lastEntry().getValue();
            if (versionResolvedRepo instanceof Central) {
                ((Central) versionResolvedRepo).pullModule(moduleId);
            }
            return moduleVersions.lastEntry().getKey();
        }
        return null;
    }
}
