package io.ballerina.projects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * {@code Package} represents a Ballerina Package.
 *
 * @since 2.0.0
 */
public class Package {
    private final Project project;
    private final PackageContext packageContext;
    private final Map<ModuleId, Module> moduleMap;
    private final Function<ModuleId, Module> populateModuleFunc;

    private Package(PackageContext packageContext, Project project) {
        this.packageContext = packageContext;
        this.project = project;
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
        return new Package(packageContext, project);
    }

    PackageContext packageContext() {
        return this.packageContext;
    }

    public Project project() {
        return this.project;
    }

    public PackageId packageId() {
        return this.packageContext.packageId();
    }

    public PackageName packageName() {
        return packageContext.packageName();
    }

    public PackageOrg packageOrg() {
        return packageContext.packageOrg();
    }

    public PackageVersion packageVersion() {
        return packageContext.packageVersion();
    }

    public PackageDescriptor descriptor() {
        return packageContext.descriptor();
    }

    public PackageManifest manifest() {
        return packageContext.manifest();
    }

    public Collection<ModuleId> moduleIds() {
        return this.packageContext.moduleIds();
    }

    public Iterable<Module> modules() {
        List<Module> moduleList = new ArrayList<>();
        for (ModuleId moduleId : this.packageContext.moduleIds()) {
            moduleList.add(module(moduleId));
        }
        return new ModuleIterable(moduleList);
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

    public PackageCompilation getCompilation() {
        return this.packageContext.getPackageCompilation();
    }

    public PackageResolution getResolution() {
        return this.packageContext.getResolution();
    }

    public DependencyGraph<ModuleId> moduleDependencyGraph() {
        // Each Package should know the packages that it depends on and packages that depends on it
        // Each Module should know the modules that it depends on and modules that depends on it
        return this.packageContext.moduleDependencyGraph();
    }

    public Collection<PackageDependency> packageDependencies() {
        return packageContext.packageDependencies();
    }

    public CompilationOptions compilationOptions() {
        return packageContext.compilationOptions();
    }

    public Optional<BallerinaToml> ballerinaToml() {
        return packageContext.ballerinaToml();
    }

    /**
     * Returns an instance of the Package.Modifier.
     *
     * @return module modifier
     */
    public Modifier modify() {
        return new Modifier(this);
    }

    private static class ModuleIterable implements Iterable {

        private final Collection<Module> moduleList;

        public ModuleIterable(Collection<Module> moduleList) {
            this.moduleList = moduleList;
        }

        @Override
        public Iterator<Module> iterator() {
            return this.moduleList.iterator();
        }

        @Override
        public Spliterator spliterator() {
            return this.moduleList.spliterator();
        }
    }

    /**
     * Inner class that handles package modifications.
     */
    public static class Modifier {
        private PackageId packageId;
        private PackageManifest packageManifest;
        private BallerinaToml ballerinaToml;
        private Map<ModuleId, ModuleContext> moduleContextMap;
        private Project project;
        private final DependencyGraph<PackageDescriptor> pkgDescDependencyGraph;

        public Modifier(Package oldPackage) {
            this.packageId = oldPackage.packageId();
            this.packageManifest = oldPackage.manifest();
            this.ballerinaToml = oldPackage.ballerinaToml().orElse(null);
            this.moduleContextMap = copyModules(oldPackage);
            this.project = oldPackage.project;
            this.pkgDescDependencyGraph = oldPackage.packageContext().dependencyGraph();
        }

        Modifier updateModule(ModuleContext newModuleContext) {
            this.moduleContextMap.put(newModuleContext.moduleId(), newModuleContext);
            return this;
        }

        /**
         * Adds a new module in a new package that is copied from the existing.
         *
         * @param moduleConfig configuration of the module to add
         * @return Package.Modifier which contains the updated package
         */
        public Modifier addModule(ModuleConfig moduleConfig) {
            ModuleContext newModuleContext = ModuleContext.from(this.project, moduleConfig);
            this.moduleContextMap.put(newModuleContext.moduleId(), newModuleContext);
            return this;
        }

        /**
         * Creates a copy of the existing package and removes the module from the new package.
         *
         * @param moduleId moduleId of the module to remove
         * @return Package.Modifier which contains the updated package
         */
        public Modifier removeModule(ModuleId moduleId) {
            moduleContextMap.remove(moduleId);
            return this;
        }

        /**
         * Returns the updated package created by a module add/remove/update operation.
         *
         * @return updated package
         */
        public Package apply() {
            return createNewPackage();
        }

        private Map<ModuleId, ModuleContext> copyModules(Package oldPackage) {
            Map<ModuleId, ModuleContext> moduleContextMap = new HashMap<>();

            for (ModuleId moduleId : oldPackage.packageContext.moduleIds()) {
                moduleContextMap.put(moduleId, oldPackage.packageContext.moduleContext(moduleId));
            }
            return moduleContextMap;
        }

        private Package createNewPackage() {
            PackageContext newPackageContext = new PackageContext(this.project, this.packageId, this.packageManifest,
                    this.ballerinaToml, this.moduleContextMap, this.pkgDescDependencyGraph);
            this.project.setCurrentPackage(new Package(newPackageContext, this.project));
            return this.project.currentPackage();
        }
    }
}
