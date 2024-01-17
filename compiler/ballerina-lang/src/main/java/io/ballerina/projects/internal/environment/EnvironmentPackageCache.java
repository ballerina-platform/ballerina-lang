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

    private final Map<PackageId, Project> projectsById = new HashMap<>();
    private final Map<PackageOrg, Map<PackageName, Map<PackageVersion, Project>>>
            projectsByOrgNameVersion = new HashMap<>();

    public void cache(Package pkg) {
        projectsById.put(pkg.packageId(), pkg.project());
        projectsByOrgNameVersion.computeIfAbsent(pkg.packageOrg(), k -> new HashMap<>())
                .computeIfAbsent(pkg.packageName(), k -> new HashMap<>())
                .put(pkg.packageVersion(), pkg.project());
    }

    @Override
    public Optional<Package> getPackage(PackageId packageId) {
        Project project = projectsById.get(packageId);
        if (project == null) {
            return Optional.empty();
        }

        return Optional.of(project.currentPackage());
    }

    @Override
    public Package getPackageOrThrow(PackageId packageId) {
        Project project = projectsById.get(packageId);
        if (project == null) {
            throw new IllegalStateException("Cannot find a Package for the given PackageId: " + packageId);
        }
        return project.currentPackage();
    }

    @Override
    public Optional<Package> getPackage(PackageOrg packageOrg,
                                        PackageName packageName,
                                        PackageVersion version) {
        return Optional.ofNullable(projectsByOrgNameVersion.getOrDefault(packageOrg, new HashMap<>())
                .getOrDefault(packageName, new HashMap<>())
                .get(version)).map(Project::currentPackage);
    }

    @Override
    public List<Package> getPackages(PackageOrg packageOrg, PackageName packageName) {
        List<Package> foundList = new ArrayList<>();
        for (Project project : projectsById.values()) {
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
        Optional<Project> project = Optional.ofNullable(projectsById.get(packageId));
        if (project.isEmpty()) {
            return;
        }
        PackageDescriptor pkgDesc = project.get().currentPackage().descriptor();
        projectsByOrgNameVersion.get(pkgDesc.org())
                .get(pkgDesc.name())
                .remove(pkgDesc.version());
        projectsById.remove(packageId);
    }
}
