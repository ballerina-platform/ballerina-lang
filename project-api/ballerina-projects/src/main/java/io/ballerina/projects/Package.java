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

    static Package from(PackageConfig packageConfig) {
        // TODO create package context here by giving the package config
        // do the same for modules and documents
        // il. package context creates modules contexts and modules context create document contexts

        // contexts need to hold onto the configs. Should we decouple config from tree information as follows.
        // package config has the tree information like modules.
        PackageContext packageContext = PackageContext.from(packageConfig);
        return new Package(packageContext);
    }

    public PackageId packageId() {
        return this.packageContext.packageId();
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

    public boolean containsModule(ModuleId moduleId) {
        return this.moduleMap.containsKey(moduleId);
    }
}
