package org.ballerinalang.moduleloader;

import org.ballerinalang.moduleloader.model.Module;
import org.ballerinalang.moduleloader.model.ModuleId;
import org.ballerinalang.moduleloader.model.Project;
import org.ballerinalang.toml.model.Dependency;
import org.ballerinalang.toml.model.LockFileImport;
import org.ballerinalang.toml.model.Manifest;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.ballerinalang.moduleloader.Util.isGreaterVersion;

public class ModuleLoaderImpl implements ModuleLoader {

    private Project project;
    private RepoHierarchy repoHierarchy;
    // store moduleId and Repo which module exists
    Map<ModuleId,Repo> resolvedModules = new HashMap<>();

    public ModuleLoaderImpl(Project project, RepoHierarchy repoHierarchy) {
        this.project = project;
        this.repoHierarchy = repoHierarchy;
    }

    @Override
    public ModuleId resolveVersion(ModuleId moduleId, ModuleId enclModuleId) throws IOException {
        // if version already exists in moduleId
        if (isVersionExists(moduleId)) {
            ModuleId resolvedModuleId = resolveModuleVersionFromRepos(this.repoHierarchy.getRepoList(), moduleId,
                    moduleId.getVersion());
            if (resolvedModuleId.getVersion() != null) {
                return resolvedModuleId;
            }
        }

        // if module is in the current project
        if (this.project.isModuleExists(moduleId)) {
            moduleId.setVersion(this.project.getManifest().getProject().getVersion());
            resolvedModules.put(moduleId, repoHierarchy.getProjectModules());
            return moduleId;
        }

        // if lock file exists
        if (enclModuleId != null && this.project.hasLockFile()) {
            // not a top level module or bal
            String versionFromLockfile = resolveVersionFromLockFile(moduleId, enclModuleId);
            // version in lock can be null if the import is added after creating the lock
            if (versionFromLockfile != null) {
                ModuleId resolvedModuleId = resolveModuleVersionFromRepos(this.repoHierarchy.getRepoList(), moduleId,
                        versionFromLockfile);
                if (resolvedModuleId.getVersion() != null) {
                    return resolvedModuleId;
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
                ModuleId resolvedModuleId = resolveModuleVersionFromRepos(this.repoHierarchy.getRepoList(), moduleId,
                        versionFromManifest);
                if (resolvedModuleId.getVersion() != null) {
                    return resolvedModuleId;
                }
            }
        }

        // if enclosing module is not a project module, module is transitive
        // if it is a transitive we need to check the toml file of the dependent module
        if (enclModuleId != null && !this.project.isModuleExists(enclModuleId)) {
            BaloCache homeBaloCache = this.repoHierarchy.getHomeBaloCache();
            Module parentModule = homeBaloCache.getModule(enclModuleId);
            Manifest parentManifest = RepoUtils.getManifestFromBalo(parentModule.getSourcePath());
            versionFromManifest = resolveVersionFromManifest(moduleId, parentManifest);
            if (versionFromManifest != null) {
                ModuleId resolvedModuleId = resolveModuleVersionFromRepos(this.repoHierarchy.getRepoList(), moduleId,
                        versionFromManifest);
                if (resolvedModuleId.getVersion() != null) {
                    return resolvedModuleId;
                }
            }
        }

        return moduleId;
    }

    @Override
    public Module resolveModule(ModuleId moduleId) {
        // if moduleId exists in `resolvedModules` map
        if (resolvedModules.containsKey(moduleId)) {
            Cache cache = (Cache) resolvedModules.get(moduleId);
            return cache.getModule(moduleId);
        }

//        for (Repo repo : this.repoHierarchy.getRepoList()) {
//            if (repo.isModuleExists(moduleId)) {
//                if (repo instanceof Cache) {
//                    return ((Cache) repo).getModule(moduleId);
//                }
//            }
//        }
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

    private ModuleId resolveModuleVersionFromRepos(List<Repo> repos, ModuleId moduleId, String filter) throws IOException {
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

                // when pulled, module is in the Balo cache
                moduleId.setVersion(moduleVersions.lastEntry().getKey());
                resolvedModules.put(moduleId, this.repoHierarchy.getHomeBaloCache());
            } else {
                moduleId.setVersion(moduleVersions.lastEntry().getKey());
                resolvedModules.put(moduleId, moduleVersions.lastEntry().getValue());
            }
        }
        return moduleId;
    }

    private boolean isVersionExists(ModuleId moduleId) {
        return moduleId.getVersion() != null && !"".equals(moduleId.getVersion().trim());
    }
}
