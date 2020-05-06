package org.ballerinalang.moduleloader;

import org.ballerinalang.moduleloader.model.ModuleId;
import org.ballerinalang.moduleloader.model.ModuleResolution;
import org.ballerinalang.moduleloader.model.Project;
import org.ballerinalang.toml.model.Dependency;
import org.ballerinalang.toml.model.LockFileImport;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.TreeSet;

public class ModuleLoaderImpl implements ModuleLoader {

    private Project project;

    public ModuleLoaderImpl(Project project) {
        // define repo list

        this.project = project;
    }

    @Override
    public ModuleId resolveVersion(ModuleId moduleId, ModuleId enclModuleId) throws IOException {
        // return if version exists in moduleId
        if (moduleId.version != null && !"".equals(moduleId.version.trim())) {
            return moduleId;
        }

        // if not transitive and lock file exists
        if (enclModuleId != null && this.project.hasLockFile()) {
            // not a top level module or bal
            String lockFileVersion = resolveVersionFromLockFile(moduleId, enclModuleId);
            if (lockFileVersion != null) {
                moduleId.version = resolveModuleVersion(moduleId, lockFileVersion, this.project);
                return moduleId;
            }
        }

        // Set version from the Ballerina.toml of the current project.
        if (enclModuleId != null && this.project.manifest != null) {
            moduleId.version = resolveVersionFromManifest(moduleId);
            if (moduleId.version != null) {
                return moduleId;
            }
        }

        // Set the version from Ballerina.toml found in dependent balos.
        //        if (enclModuleId != null && this.dependencyManifests.size() > 0
        //                && this.dependencyManifests.containsKey(enclModuleId)) {
        //
        //            for (Dependency manifestDependency : this.dependencyManifests.get(enclModuleId).getDependencies()) {
        //                if (manifestDependency.getOrgName().equals(moduleId.orgName.value) &&
        //                        manifestDependency.getModuleName().equals(moduleId.name.value) &&
        //                        manifestDependency.getMetadata().getVersion() != null &&
        //                        !"*".equals(manifestDependency.getMetadata().getVersion())) {
        //                    moduleId.version = new Name(manifestDependency.getMetadata().getVersion());
        //                }
        //            }
        //        }
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

    private String resolveVersionFromManifest(ModuleId moduleId) {
        for (Dependency dependency : this.project.manifest.getDependencies()) {
            if (dependency.getModuleName().equals(moduleId.moduleName) && dependency.getOrgName()
                    .equals(moduleId.orgName) && dependency.getMetadata().getVersion() != null && !"*"
                    .equals(dependency.getMetadata().getVersion())) {
                return dependency.getMetadata().getVersion();
            }
        }
        return null;
    }

    private String resolveModuleVersion(ModuleId moduleId, String filter, Project project) throws IOException {
        TreeSet<String> versions = new TreeSet<>();

        // 1. project modules
        ProjectModules projectModules = new ProjectModules(Paths.get(System.getProperty("user.dir")),
                project.manifest.getProject().getOrgName(), project.manifest.getProject().getVersion());
        versions.addAll(projectModules.resolveVersions(moduleId, filter));

        // 2. project bir cache
        BirCache projectBirCache = new BirCache(Paths.get(String.valueOf(Paths.get(System.getProperty("user.dir"))),
                ProjectDirConstants.TARGET_DIR_NAME, ProjectDirConstants.CACHES_DIR_NAME,
                ProjectDirConstants.BIR_CACHE_DIR_NAME));
        versions.addAll(projectBirCache.resolveVersions(moduleId, filter));

        // 3. distribution bir cache
        BirCache distBirCache = new BirCache(
                Paths.get(System.getProperty(ProjectDirConstants.BALLERINA_HOME), "bir-cache"));
        versions.addAll(distBirCache.resolveVersions(moduleId, filter));

        // 4. home bir cache
        BirCache homeBirCache = new BirCache(
                RepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.BIR_CACHE_DIR_NAME));
        versions.addAll(homeBirCache.resolveVersions(moduleId, filter));

        // 5. home balo cache
        BaloCache homeBaloCache = new BaloCache(
                RepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.BALO_CACHE_DIR_NAME));
        versions.addAll(homeBaloCache.resolveVersions(moduleId, filter));

        // 6. central repo
        Central central = new Central();
        versions.addAll(central.resolveVersions(moduleId, filter));

        return versions.last();
    }
}
