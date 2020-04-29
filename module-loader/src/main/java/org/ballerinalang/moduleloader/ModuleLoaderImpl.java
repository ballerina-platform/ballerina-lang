package org.ballerinalang.moduleloader;

import org.ballerinalang.moduleloader.model.ModuleId;
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
            resolveVersionFromLockFile(moduleId, enclModuleId);
        }

        // Set version from the Ballerina.toml of the current project.
        if (enclModuleId != null && this.project.manifest != null) {
            resolveVersionFromManifest(moduleId);
        }
        return moduleId;
    }

    private void resolveVersionFromLockFile(ModuleId moduleId, ModuleId enclModuleId) {
        if (this.project.getLockFile().getImports().containsKey(enclModuleId.toString())) {
            List<LockFileImport> foundBaseImport = this.project.getLockFile().getImports().get(enclModuleId.toString());

            for (LockFileImport nestedImport : foundBaseImport) {
                if (moduleId.orgName.equals(nestedImport.getOrgName()) && moduleId.moduleName.equals(nestedImport.getName())) {
                    moduleId.version = nestedImport.getVersion();
                }
            }
        }
    }

    private void resolveVersionFromManifest(ModuleId moduleId) {
        for (Dependency dependency : this.project.manifest.getDependencies()) {
            if (dependency.getModuleName().equals(moduleId.moduleName) && dependency.getOrgName()
                    .equals(moduleId.orgName) && dependency.getMetadata().getVersion() != null && !"*"
                    .equals(dependency.getMetadata().getVersion())) {
                moduleId.version = dependency.getMetadata().getVersion();
            }
        }
    }

    @Override
    public ModuleResolution resolveModule(ModuleId moduleId) {
        return null;
    }

}
