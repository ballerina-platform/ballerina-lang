package org.wso2.ballerinalang.compiler.packaging.module.resolver;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageEntity;
import org.ballerinalang.toml.model.Dependency;
import org.ballerinalang.toml.model.LockFileImport;
import org.ballerinalang.toml.model.Manifest;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.model.ModuleResolveException;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.model.PackageBalo;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.model.Project;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.RepoUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.wso2.ballerinalang.compiler.packaging.module.resolver.Util.isAbsoluteVersion;
import static org.wso2.ballerinalang.compiler.packaging.module.resolver.Util.isGreaterVersion;

public class ModuleResolverImpl implements ModuleResolver {

    private Project project;
    private RepoHierarchy repoHierarchy;
    private boolean isLockEnabled;

    // store moduleId and Repo which module exists
    private Map<PackageID, Cache> resolvedModules = new HashMap<>();

    private static final String DEFAULT_VERSION = "0.0.0";

    public ModuleResolverImpl(Project project, RepoHierarchy repoHierarchy, boolean isLockEnabled) {
        this.project = project;
        this.repoHierarchy = repoHierarchy;
        this.isLockEnabled = isLockEnabled;
    }

    @Override
    public PackageID resolveVersion(PackageID packageID, PackageID enclPackageID) {
        // if version already exists in moduleId
        if (isVersionExists(packageID)) {
            PackageID resolvedModuleId = resolveModuleVersionFromRepos(this.repoHierarchy.getRepoList(), packageID,
                    packageID.version.getValue());
            if (isVersionExists(resolvedModuleId)) {
                return resolvedModuleId;
            }
        }

        // if `ballerina` org module, directly resolve from distribution BIR cache
        if (packageID.getOrgName().getValue().equals("ballerina")) {
            List<String> resolvedVersions = this.repoHierarchy.getDistributionBirCache().resolveVersions(packageID, "*");
            if (!resolvedVersions.isEmpty()) {
                packageID.version = new Name(resolvedVersions.get(0));
                resolvedModules.put(packageID, this.repoHierarchy.getDistributionBirCache());
                return packageID;
            }
//            else {
//                packageID.version = new Name(DEFAULT_VERSION);
//            }
        }

        // if module is in the current project
        if (this.project.isModuleExists(packageID)) {
            packageID.version = new Name(this.project.getManifest().getProject().getVersion());
            resolvedModules.put(packageID, repoHierarchy.getProjectModules());
            return packageID;
        }

        // if lock file exists
        if (enclPackageID != null && this.project.hasLockFile() && this.isLockEnabled) {
            // not a top level module or bal
            String versionFromLockfile = resolveVersionFromLockFile(packageID, enclPackageID);
            // version in lock can be null if the import is added after creating the lock
            if (versionFromLockfile != null) {
                PackageID resolvedModuleId = resolveModuleVersionFromRepos(this.repoHierarchy.getRepoList(), packageID,
                        versionFromLockfile);
                if (isVersionExists(resolvedModuleId)) {
                    return resolvedModuleId;
                }
            }
        }

        String versionFromManifest;
        // if it is an immediate import check the Ballerina.toml
        // Set version from the Ballerina.toml of the current project
        if (enclPackageID != null && this.project.getManifest() != null && this.project.isModuleExists(enclPackageID)) {
            // If exact version return
            versionFromManifest = resolveVersionFromManifest(packageID, this.project.getManifest());
            if (versionFromManifest != null) {
                PackageID resolvedModuleId = resolveModuleVersionFromRepos(this.repoHierarchy.getRepoList(), packageID,
                        versionFromManifest);
                if (isVersionExists(resolvedModuleId)) {
                    return resolvedModuleId;
                }
            }
        }

        // if enclosing module is not a project module, module is transitive
        // if it is a transitive we need to check the toml file of the dependent module
        if (enclPackageID != null && !this.project.isModuleExists(enclPackageID)) {
            BaloCache homeBaloCache = this.repoHierarchy.getHomeBaloCache();
            PackageBalo parentModule = (PackageBalo) homeBaloCache.getModule(enclPackageID);
            Manifest parentManifest = RepoUtils.getManifestFromBalo(parentModule.getSourcePath());
            versionFromManifest = resolveVersionFromManifest(packageID, parentManifest);
            if (versionFromManifest != null) {
                PackageID resolvedModuleId = resolveModuleVersionFromRepos(this.repoHierarchy.getRepoList(), packageID,
                        versionFromManifest);
                if (isVersionExists(resolvedModuleId)) {
                    return resolvedModuleId;
                }
            }
        }

        // version is not resolved
        if (packageID.version.getValue() == null || "".equals(packageID.version.getValue().trim())) {
            throw new ModuleResolveException(
                    "module not found: " + packageID.getOrgName().getValue() + "/" + packageID.getName().getValue());
        }

        return packageID;
    }

    @Override
    public PackageEntity resolveModule(PackageID moduleId) {
        // if moduleId exists in `resolvedModules` map
        if (resolvedModules.containsKey(moduleId)) {
            Cache cache = resolvedModules.get(moduleId);
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

    private String resolveVersionFromLockFile(PackageID moduleId, PackageID enclModuleId) {
        if (this.project.getLockFile().getImports().containsKey(enclModuleId.toString())) {
            List<LockFileImport> foundBaseImport = this.project.getLockFile().getImports().get(enclModuleId.toString());

            for (LockFileImport nestedImport : foundBaseImport) {
                if (moduleId.getOrgName().getValue().equals(nestedImport.getOrgName())
                        && moduleId.getName().getValue().equals(nestedImport.getName())) {
                    return nestedImport.getVersion();
                }
            }
        }
        return null;
    }

    private String resolveVersionFromManifest(PackageID moduleId, Manifest manifest) {
        for (Dependency dependency : manifest.getDependencies()) {
            if (dependency.getModuleName().equals(moduleId.getName().getValue())
                    && dependency.getOrgName().equals(moduleId.getOrgName().getValue())
                    && dependency.getMetadata().getVersion() != null
                    && !"*".equals(dependency.getMetadata().getVersion())) {
                return dependency.getMetadata().getVersion();
            }
        }
        return null;
    }

    private PackageID resolveModuleVersionFromRepos(List<Repo> repos, PackageID moduleId, String filter) {
        TreeMap<String, Repo> moduleVersions = new TreeMap<>();

        for (Repo repo : repos) {
            List<String> resolvedVersions = repo.resolveVersions(moduleId, filter);

            // if absolute version
            if (isAbsoluteVersion(filter) && repo instanceof Cache) {
                moduleId.version = new Name(resolvedVersions.get(resolvedVersions.size() - 1));
                resolvedModules.put(moduleId, (Cache) repo);
                return moduleId;
            }

            for (String versionStr : resolvedVersions) {
                if (moduleVersions.isEmpty() || isGreaterVersion(moduleVersions.lastKey(), versionStr)) {
                    moduleVersions.put(versionStr, repo);
                }
            }
        }

        if (!moduleVersions.isEmpty()) {
            // pull if the module is in a remote repo.
            Repo versionResolvedRepo = moduleVersions.lastEntry().getValue();
            if (versionResolvedRepo instanceof Central) {
                versionResolvedRepo.pullModule(moduleId);

                // when pulled, module is in the Balo cache
                if (!moduleVersions.lastEntry().getKey().equals(DEFAULT_VERSION)) {
                    moduleId.version = new Name(moduleVersions.lastEntry().getKey());
                    resolvedModules.put(moduleId, this.repoHierarchy.getHomeBaloCache());
                }
            } else { // if not Central, repo is a cache
                if (!moduleVersions.lastEntry().getKey().equals(DEFAULT_VERSION)) {
                    moduleId.version = new Name(moduleVersions.lastEntry().getKey());
                    resolvedModules.put(moduleId, (Cache) moduleVersions.lastEntry().getValue());
                }
            }
        }
        return moduleId;
    }

    private boolean isVersionExists(PackageID packageID) {
        return packageID.version != null && !"".equals(packageID.version.getValue()) && packageID.version != Names.DEFAULT_VERSION
                && !packageID.version.getValue().equals(DEFAULT_VERSION);
    }
}
