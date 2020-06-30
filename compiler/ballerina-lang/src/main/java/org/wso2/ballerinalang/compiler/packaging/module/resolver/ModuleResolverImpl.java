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

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageEntity;
import org.ballerinalang.toml.model.Dependency;
import org.ballerinalang.toml.model.LockFileImport;
import org.ballerinalang.toml.model.Manifest;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.model.ModuleResolveException;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.model.PackageBalo;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.model.Project;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.model.ProjectModuleEntity;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.model.SingleBalEntity;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.wso2.ballerinalang.compiler.packaging.module.resolver.Util.isAbsoluteVersion;
import static org.wso2.ballerinalang.compiler.packaging.module.resolver.Util.isGreaterVersion;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_PKG_BINARY_EXT;

/**
 * Module resolver implementation.
 */
public class ModuleResolverImpl implements ModuleResolver {

    private Project project;
    private RepoHierarchy repoHierarchy;
    private boolean isLockEnabled;
    private boolean testsEnabled;
    private SourceDirectory sourceDirectory;

    // store moduleId and Repo which module exists
    public Map<PackageID, Cache> resolvedModules = new HashMap<>();

    private static final String DEFAULT_VERSION = "0.0.0";

    public ModuleResolverImpl(Project project, RepoHierarchy repoHierarchy, boolean isLockEnabled, boolean testsEnabled,
            SourceDirectory sourceDirectory) {
        this.project = project;
        this.repoHierarchy = repoHierarchy;
        this.isLockEnabled = isLockEnabled;
        this.testsEnabled = testsEnabled;
        this.sourceDirectory = sourceDirectory;
    }

    @Override
    public PackageID resolveVersion(PackageID packageID, PackageID enclPackageID) {
        // if version already exists in moduleId
        if (isVersionExists(packageID)) {
            PackageID resolvedModuleId = resolveModuleVersionFromRepos(this.repoHierarchy.getRepoList(), packageID,
                    packageID.version.getValue());
            if (isVersionExists(resolvedModuleId) && resolvedModules.containsKey(packageID)) {
                return resolvedModuleId;
            }
        }

        // if module is in the current project
        if (this.project.isModuleExists(packageID)) {
            packageID.version = new Name(this.project.getManifest().getProject().getVersion());
            resolvedModules.put(packageID, repoHierarchy.getProjectModules());
            return packageID;
        }

        // if module exists in project build repo, This applies in the build
        if (this.repoHierarchy.getProjectBuildRepo().isModuleExists(packageID)) {
            List<String> resolvedVersions = this.repoHierarchy.getProjectBuildRepo().resolveVersions(packageID, "*");
            packageID.version = new Name(resolvedVersions.get(resolvedVersions.size() - 1));
            resolvedModules.put(packageID, this.repoHierarchy.getProjectBuildRepo());
            return packageID;
        }

        // if it is an immediate import and path balo in the Ballerina.toml
        // Set version from the path balo
        if (enclPackageID != null && this.project.getManifest() != null) { // this.project.isModuleExists(enclPackageID)
            // If path balo dependency
            String pathBaloPath = resolvePathBaloFromManifest(packageID, this.project.getManifest());
            if (pathBaloPath != null) {
                // get version from balo path. This is always an absolute version
                String versionFromBaloPath = getVersionFromPathBalo(pathBaloPath);
                packageID.version = new Name(versionFromBaloPath);
                // add path balo to `resolvedModules` map
                PathBalo pathBalo = new PathBalo(Paths.get(pathBaloPath));
                resolvedModules.put(packageID, pathBalo);
                // return path balo version
                return packageID;
            }
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
            PackageID resolvedModuleId = getPackageIDFromRepos(packageID, versionFromManifest);
            if (resolvedModuleId != null) {
                return resolvedModuleId;
            }
        }
        // if a single bal file import
        if (enclPackageID != null && enclPackageID.toString().equals(".")) {
            // set newest version from repos
            PackageID resolvedModuleId = resolveModuleVersionFromRepos(this.repoHierarchy.getRepoList(), packageID,
                    "*");
            if (isVersionExists(resolvedModuleId)) {
                return resolvedModuleId;
            }
        }

        // if enclosing module is not a project module, module is transitive
        // if it is a transitive we need to check the toml file of the dependent module
        if (enclPackageID != null && !this.project.isModuleExists(enclPackageID)) {
            BaloCache homeBaloCache = this.repoHierarchy.getHomeBaloCache();
            PackageBalo parentModule = (PackageBalo) homeBaloCache.getModule(enclPackageID);
            if (parentModule != null) {
                Manifest parentManifest = RepoUtils.getManifestFromBalo(getBaloPath(parentModule.getSourcePath()));
                versionFromManifest = resolveVersionFromManifest(packageID, parentManifest);
                PackageID resolvedModuleId = getPackageIDFromRepos(packageID, versionFromManifest);
                if (resolvedModuleId != null) {
                    return resolvedModuleId;
                }
            }
        }

        // version is not resolved
        if (packageID.version.getValue() == null || "".equals(packageID.version.getValue().trim())) {
//            throw new ModuleResolveException(
//                    "module not found: " + packageID.getOrgName().getValue() + "/" + packageID.getName().getValue());
            return null;
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
        return null;
    }

    @Override
    public PackageEntity loadModule(PackageID moduleId) {
        Path srcPath = this.sourceDirectory.getPath();
        if (".".equals(moduleId.toString())) {
            // load single bal file
            return new SingleBalEntity(moduleId, srcPath); // resolve(moduleId.sourceFileName.getValue())
        } else {
            // load module
            return new ProjectModuleEntity(moduleId,
                    srcPath.resolve(ProjectDirConstants.SOURCE_DIR_NAME).resolve(moduleId.getName().getValue()),
                    this.testsEnabled);
        }
    }

    private Path getBaloPath(Path srcPath) {
        if (srcPath.toString().endsWith(BLANG_COMPILED_PKG_BINARY_EXT)) {
            return srcPath;
        } else {
            File[] files = new File(String.valueOf(srcPath)).listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.getPath().endsWith(BLANG_COMPILED_PKG_BINARY_EXT)) {
                        return Paths.get(file.getPath());
                    }
                }
            }
        }
        throw new ModuleResolveException("cannot find balo in the path: " + srcPath);
    }

    private PackageID getPackageIDFromRepos(PackageID packageID, String versionFromManifest) {
        if (versionFromManifest != null) {
            PackageID resolvedModuleId = resolveModuleVersionFromRepos(this.repoHierarchy.getRepoList(), packageID,
                    versionFromManifest);
            if (isVersionExists(resolvedModuleId)) {
                return resolvedModuleId;
            }
        } else {
            // If cannot resolve from Ballerina.toml, we will fetch the latest package from the repos
            PackageID resolvedModuleId = resolveModuleVersionFromRepos(this.repoHierarchy.getRepoList(), packageID,
                    "*");
            if (isVersionExists(resolvedModuleId)) {
                return resolvedModuleId;
            }
        }
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

    private String resolvePathBaloFromManifest(PackageID moduleId, Manifest manifest) {
        for (Dependency dependency : manifest.getDependencies()) {
            if (dependency.getModuleName().equals(moduleId.getName().getValue())
            && dependency.getOrgName().equals(moduleId.getOrgName().getValue())
            && dependency.getMetadata().getPath() != null) {
                return String.valueOf(dependency.getMetadata().getPath());
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
            if (isAbsoluteVersion(filter) && repo instanceof Cache && !resolvedVersions.isEmpty()) {
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
//                if (!moduleVersions.lastEntry().getKey().equals(DEFAULT_VERSION)) {
                    moduleId.version = new Name(moduleVersions.lastEntry().getKey());
                    resolvedModules.put(moduleId, (Cache) moduleVersions.lastEntry().getValue());
//                }
            }
        }
        return moduleId;
    }

    private boolean isVersionExists(PackageID packageID) {
        return packageID.version != null && !"".equals(packageID.version.getValue());
//       && packageID.version != Names.DEFAULT_VERSION && !packageID.version.getValue().equals(DEFAULT_VERSION);
    }

    private String getVersionFromPathBalo(String baloPath) {
        String version = baloPath.substring(baloPath.lastIndexOf('.') - 5, baloPath.lastIndexOf('.'));
        if (!isAbsoluteVersion(version)) {
            throw new ModuleResolveException(
                    "retrieving version from balo path failed, balo path: " + baloPath + ", version: " + version);
        }
        return version;
    }
}
