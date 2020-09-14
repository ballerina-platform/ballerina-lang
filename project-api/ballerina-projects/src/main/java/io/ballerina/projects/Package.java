package io.ballerina.projects;

import io.ballerina.projects.model.BallerinaToml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * {@code Package} represents a Ballerina Package.
 *
 * @since 2.0.0
 */
public class Package {
    private final PackageContext packageContext;
    private Project project;
    private final Map<ModuleId, Module> moduleMap;
    private final Function<ModuleId, Module> populateModuleFunc;

    private Package(PackageContext packageContext, Project project) {
        this.packageContext = packageContext;
        this.project = project;
        this.moduleMap = new ConcurrentHashMap<>();
        this.populateModuleFunc = moduleId -> Module.from(
                this.packageContext.moduleContext(moduleId), this);
    }

    static Package from(PackageConfig packageConfig, Project project) {
        // TODO create package context here by giving the package config
        // do the same for modules and documents
        // il. package context creates modules contexts and modules context create document contexts

        // contexts need to hold onto the configs. Should we decouple config from tree information as follows.
        // package config has the tree information like modules.
        PackageContext packageContext = PackageContext.from(packageConfig);
        return new Package(packageContext, project);
    }

    public PackageId packageId() {
        return this.packageContext.packageId();
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

    public boolean containsModule(ModuleId moduleId) {
        return this.moduleMap.containsKey(moduleId);
    }

    public Module getDefaultModule() {
        return module(this.packageContext.defaultModuleContext().moduleId());
    }

    public BallerinaToml ballerinaToml() {
        return this.packageContext.ballerinaToml();
    }

    /** Returns an instance of the Package.Modifier.
     *
     * @return  module modifier
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
        private Package oldPackage;
        private ModuleContext newModuleContext = null;
        private Package newPackage;

        public Modifier(Package oldPackage) {
            this.oldPackage = oldPackage;
        }

        Modifier updateModule(ModuleContext newModuleContext) {
            this.newModuleContext = newModuleContext;
            Map<ModuleId, ModuleContext> moduleContextMap = copyModulesfromOld();
            moduleContextMap.put(newModuleContext.moduleId(), newModuleContext);
            createNewPackage(moduleContextMap);

            return this;
        }

        /**
         * Adds a new module in a new package that is copied from the existing.
         *
         * @param moduleConfig configuration of the module to add
         * @return Package.Modifier which contains the updated package
         */
        public Modifier addModule(ModuleConfig moduleConfig) {
            this.newModuleContext = ModuleContext.from(moduleConfig);
            Map<ModuleId, ModuleContext> moduleContextMap = copyModulesfromOld();
            moduleContextMap.put(newModuleContext.moduleId(), newModuleContext);
            createNewPackage(moduleContextMap);

            return this;
        }

        /**
         * Creates a copy of the existing package and removes the module from the new package.
         *
         * @param moduleId moduleId of the module to remove
         * @return Package.Modifier which contains the updated package
         */
        public Modifier removeModule(ModuleId moduleId) {
            Map<ModuleId, ModuleContext> moduleContextMap = copyModulesfromOld();
            moduleContextMap.remove(moduleId);
            PackageContext newPackageContext = new PackageContext(
                    oldPackage.packageId(),
                    oldPackage.packageContext.packagePath(),
                    oldPackage.packageContext.defaultModuleContext(),
                    moduleContextMap);
            oldPackage.project.context.setCurrentPackage(new Package(newPackageContext, oldPackage.project));
            this.newPackage = oldPackage.project.currentPackage();

            return this;
        }

        /**
         * Returns the updated package created by a module add/remove/update operation.
         *
         * @return updated package
         */
        public Package apply() {
            return this.newPackage;
        }

        private Map<ModuleId, ModuleContext> copyModulesfromOld() {
            Map<ModuleId, ModuleContext> moduleContextMap = new HashMap<>();

            for (ModuleId moduleId : oldPackage.packageContext.moduleIds()) {
                moduleContextMap.put(moduleId, oldPackage.packageContext.moduleContext(moduleId));
            }
            return moduleContextMap;
        }

        private void createNewPackage(Map<ModuleId, ModuleContext> moduleContextMap) {
            ModuleContext defaultModuleContext;
            if (newModuleContext.moduleId() == oldPackage.getDefaultModule().moduleId()) {
                defaultModuleContext = newModuleContext;
            } else {
                defaultModuleContext = oldPackage.packageContext.defaultModuleContext();
            }

            PackageContext newPackageContext = new PackageContext(oldPackage.packageId(),
                    oldPackage.packageContext.packagePath(), defaultModuleContext, moduleContextMap);
            oldPackage.project.context.setCurrentPackage(new Package(newPackageContext, oldPackage.project));
            this.newPackage = oldPackage.project.currentPackage();
        }
    }
}
