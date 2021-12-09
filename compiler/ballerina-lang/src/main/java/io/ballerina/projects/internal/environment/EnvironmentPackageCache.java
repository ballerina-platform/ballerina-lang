package io.ballerina.projects.internal.environment;

import io.ballerina.projects.Package;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageId;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.Project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Environment-level Package cache.
 *
 * @since 2.0.0
 */
public class EnvironmentPackageCache implements WritablePackageCache {

    private final Map<PackageId, Project> projects = new HashMap<>();

    public void cache(Package pkg) {
        projects.put(pkg.packageId(), pkg.project());
    }

    @Override
    public Optional<Package> getPackage(PackageId packageId) {
        Project project = projects.get(packageId);
        if (project == null) {
            return Optional.empty();
        }

        return Optional.of(project.currentPackage());
    }

    @Override
    public Package getPackageOrThrow(PackageId packageId) {
        Project project = projects.get(packageId);
        if (project == null) {
            throw new IllegalStateException("Cannot find a Package for the given PackageId: " + packageId);
        }
        return project.currentPackage();
    }

    @Override
    public Optional<Package> getPackage(PackageOrg packageOrg,
                                        PackageName packageName,
                                        PackageVersion version) {
        // Do we have a need to improve this logic?
        for (Project project : projects.values()) {
            PackageDescriptor pkgDesc = project.currentPackage().descriptor();
            if (pkgDesc.org().equals(packageOrg) && pkgDesc.name().equals(packageName) &&
                    pkgDesc.version().equals(version)) {
                return Optional.of(project.currentPackage());
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Package> getPackages(PackageOrg packageOrg, PackageName packageName) {
        // Do we have a need to improve this logic?
        // TODO Optimize this logic
        List<Package> foundList = new ArrayList<>();
        for (Project project : projects.values()) {
            PackageManifest pkgDesc = project.currentPackage().manifest();
            if (pkgDesc.org().equals(packageOrg) &&
                    pkgDesc.name().equals(packageName)) {
                foundList.add(project.currentPackage());
            }
        }
        return foundList;
    }

    @Override
    public void removePackage(PackageId packageId) {
        projects.remove(packageId);
    }
}
