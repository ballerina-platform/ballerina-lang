package io.ballerina.projects.environment;

import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageId;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GlobalPackageCache {
    private final Map<PackageId, Package> packages = new HashMap<>();

    public void put(Package pkg) {
        packages.put(pkg.packageId(), pkg);
    }

    public Package get(PackageId id) {
        return packages.get(id);
    }

    public Collection<Package> values() {
        return packages.values();
    }

    public Module loadFromCache(ModuleLoadRequest modLoadRequest) {
        // TODO improve the logic
        for (Package pkg : packages.values()) {
            // TODO this logic is wrong. We need to take org name into the equation
            if (pkg.packageName().equals(modLoadRequest.packageName())) {
                return pkg.module(modLoadRequest.moduleName());
            }
        }
        return null;
    }
}
