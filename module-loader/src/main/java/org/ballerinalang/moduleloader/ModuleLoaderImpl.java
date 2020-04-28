package org.ballerinalang.moduleloader;

public class ModuleLoaderImpl implements ModuleLoader {

    Project project;

    public ModuleLoaderImpl(Project project) {
        // define repo list

        this.project = project;
    }

    @Override
    public ModuleId resolveVersion(ModuleId moduleId, ModuleId enclModuleId) {
        VersionResolver versionResolver = new VersionResolver(project);
        moduleId.version = versionResolver.resolve(moduleId, enclModuleId);
        return moduleId;
    }

    @Override
    public ModuleResolution resolveModule(ModuleId moduleId) {
        return null;
    }

}
