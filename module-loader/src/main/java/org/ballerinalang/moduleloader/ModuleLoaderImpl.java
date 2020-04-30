package org.ballerinalang.moduleloader;

import org.ballerinalang.moduleloader.model.ModuleId;
import org.ballerinalang.moduleloader.model.ModuleResolution;
import org.ballerinalang.moduleloader.model.Project;
import org.ballerinalang.toml.model.Dependency;
import org.ballerinalang.toml.model.LockFileImport;

import java.util.List;

public class ModuleLoaderImpl implements ModuleLoader {

    private Project project;

    public ModuleLoaderImpl(Project project) {
        // define repo list

        this.project = project;
    }

    @Override
    public ModuleId resolveVersion(ModuleId moduleId, ModuleId enclModuleId) {
        // return if version exists in moduleId
        if (moduleId.version != null && !"".equals(moduleId.version.trim())) {
            return moduleId;
        }

        // if not transitive and lock file exists
        if (enclModuleId != null && this.project.hasLockFile()) {
            // not a top level module or bal
            moduleId.version = resolveVersionFromLockFile(moduleId, enclModuleId);
            if (moduleId.version != null) {
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
                if (moduleId.orgName.equals(nestedImport.getOrgName()) && moduleId.moduleName.equals(nestedImport.getName())) {
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
}
