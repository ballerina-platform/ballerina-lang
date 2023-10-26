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
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Environment-level Package cache.
 *
 * @since 2.0.0
 */
public class EnvironmentPackageCache implements WritablePackageCache {

    private final Map<PackageId, Project> projects = new HashMap<>();
    private final Map<PackageOrg, Map<PackageName, Map<PackageVersion, Project>>> projectCache = new HashMap<>();

    public void cache(Package pkg) {
        projects.put(pkg.packageId(), pkg.project());
        projectCache
            .computeIfAbsent(pkg.packageOrg(), org -> new HashMap<>())
            .computeIfAbsent(pkg.packageName(), name -> new HashMap<>())
            .put(pkg.packageVersion(), pkg.project());
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
        return Optional.ofNullable(
            projectCache
                .getOrDefault(packageOrg, Collections.emptyMap())
                .getOrDefault(packageName, Collections.emptyMap())
                .getOrDefault(version, Collections.emptyMap())
        ).map(Project::currentPackage);
    }

    @Override
    public List<Package> getPackages(PackageOrg packageOrg, PackageName packageName) {
        // Improved logic: Use streams to simplify the code
        return projectCache
                .getOrDefault(packageOrg, Collections.emptyMap())
                .getOrDefault(packageName, Collections.emptyMap())
                .values()
                .stream()
                .map(Project::currentPackage)
                .collect(Collectors.toList());
    }

    @Override
    public void removePackage(PackageId packageId) {
        Project project = projects.remove(packageId);
        if (project != null) {
            Package pkg = project.currentPackage();
            projectCache
                .getOrDefault(pkg.packageOrg(), Collections.emptyMap())
                .getOrDefault(pkg.packageName(), Collections.emptyMap())
                .remove(pkg.packageVersion());
        }
    }
}
