package org.ballerinalang.moduleloader;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.toml.model.Dependency;
import org.ballerinalang.toml.model.LockFileImport;
import org.ballerinalang.toml.model.Manifest;

import java.util.List;
import java.util.Map;

public class VersionResolver {

    private final Project project;
//    private final Map<PackageID, Manifest> dependencyManifests;

    public VersionResolver(Project project) { // Map<PackageID, Manifest> dependencyManifests
        this.project = project;
//        this.dependencyManifests = dependencyManifests;
    }

    public String resolve(ModuleId moduleId, ModuleId enclModuleId) {
        // Check if module is an absolute version

        // return if version exists in moduleId
        if (moduleId.version != null) {
            return moduleId.version;
        }

        // check if lockfile exits
        if (enclModuleId != null && this.project.hasLockFile()) {
            // not a top level module or bal
            if (this.project.getLockFile().getImports().containsKey(enclModuleId.toString())) {
                List<LockFileImport> foundBaseImport = this.project.getLockFile().getImports().get(enclModuleId.toString());

                for (LockFileImport nestedImport : foundBaseImport) {
                    if (moduleId.orgName.equals(nestedImport.getOrgName()) && moduleId.moduleName.equals(nestedImport.getName())) {
                        return nestedImport.getVersion();
                    }
                }
            }
        }

        // Set version from the Ballerina.toml of the current project.
        if (enclModuleId != null && this.project.manifest != null) {
            for (Dependency dependency : this.project.manifest.getDependencies()) {
                if (dependency.getModuleName().equals(moduleId.moduleName) && dependency.getOrgName()
                        .equals(moduleId.orgName) && dependency.getMetadata().getVersion() != null && !"*"
                        .equals(dependency.getMetadata().getVersion())) {
                    return dependency.getMetadata().getVersion();
                }
            }
        }

        // Set the version from Ballerina.toml found in dependent balos.
//        if (enclModuleId != null && this.dependencyManifests.size() > 0 && this.dependencyManifests
//                .containsKey(enclModuleId)) {
//            for (Dependency manifestDependency : this.dependencyManifests.get(enclModuleId).getDependencies()) {
//                if (manifestDependency.getOrgName().equals(moduleId.orgName)
//                        && manifestDependency.getModuleName().equals(moduleId.moduleName)
//                        && manifestDependency.getMetadata().getVersion() != null
//                        && !"*".equals(manifestDependency.getMetadata().getVersion())) {
//                    return manifestDependency.getMetadata().getVersion();
//                }
//            }
//        }

        // If it is an absolute version return
        // If it is a range check central
        // return version returned from central
        return null;
    }
}
