package io.ballerina.projects;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * {@code Package} represents a Ballerina Package.
 *
 * @since 2.0.0
 */
public class Package {
    private final PackageContext packageContext;
    private final Map<ModuleId, Module> moduleMap;
    private final Function<ModuleId, Module> populateModuleFunc;

    private Package(PackageContext packageContext) {
        this.packageContext = packageContext;
        this.moduleMap = new ConcurrentHashMap<>();
        this.populateModuleFunc = moduleId -> Module.from(
                this.packageContext.moduleContext(moduleId), this);
    }

    static Package from(Project project, PackageConfig packageConfig) {
        // TODO create package context here by giving the package config
        // do the same for modules and documents
        // il. package context creates modules contexts and modules context create document contexts

        // contexts need to hold onto the configs. Should we decouple config from tree information as follows.
        // package config has the tree information like modules.
        PackageContext packageContext = PackageContext.from(project, packageConfig);
        return new Package(packageContext);
    }

    PackageContext packageContext() {
        return this.packageContext;
    }

    public PackageId packageId() {
        return this.packageContext.packageId();
    }

    public PackageName packageName() {
        return packageContext.packageName();
    }

    public Collection<ModuleId> moduleIds() {
        return this.packageContext.moduleIds();
    }

    public Iterable<Module> modules() {
        return null;
    }

    public Module module(ModuleId moduleId) {
        // TODO Should we throw an error if the moduleId is not present
        return this.moduleMap.computeIfAbsent(moduleId, this.populateModuleFunc);
    }

    public Module module(ModuleName moduleName) {
        for (Module module : this.moduleMap.values()) {
            if (module.moduleName().equals(moduleName)) {
                return module;
            }
        }

        ModuleContext moduleContext = this.packageContext.moduleContext(moduleName);
        if (moduleContext != null) {
            return module(moduleContext.moduleId());
        }

        return null;
    }

    public boolean containsModule(ModuleId moduleId) {
        return this.moduleMap.containsKey(moduleId);
    }

    public Module getDefaultModule() {
        return module(this.packageContext.defaultModuleContext().moduleId());
    }

    // PackageCompilation has the emit method
//    public PackageCompilation getCompilation() {
//        return this.packageContext.getCompilation();
//    }

    public DependencyGraph<ModuleId> moduleDependencyGraph() {
        // Each Package should know the packages that it depends on and packages that depends on it
        // Each Module should know the modules that it depends on and modules that depends on it
        return this.packageContext.moduleDependencyGraph();
    }

    public void resolveDependencies() {
        packageContext.resolveDependencies();
    }

    public Collection<PackageDependency> packageDependencies() {
        return packageContext.packageDependencies();
    }

//    public BallerinaToml ballerinaToml() {
//        return this.packageContext.ballerinaToml();
//    }
}
