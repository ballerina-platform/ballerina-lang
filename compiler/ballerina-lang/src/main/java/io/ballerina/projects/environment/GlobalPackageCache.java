package io.ballerina.projects.environment;

import io.ballerina.projects.Package;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageId;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.Project;
import io.ballerina.projects.SemanticVersion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Global Package instance cache.
 *
 * @since 2.0.0
 */
public class GlobalPackageCache {

    private final Map<PackageId, Project> projects = new HashMap<>();

    public void cache(Package pkg) {
        projects.put(pkg.packageId(), pkg.project());
    }

    /**
     * Returns the package with the given {@code PackageId}.
     *
     * @param id the packageId
     * @return the package with the given {@code PackageId}.
     */
    public Optional<Package> getPackage(PackageId id) {
        Project project = projects.get(id);
        if (project == null) {
            return Optional.empty();
        }

        return Optional.of(project.currentPackage());
    }

    /**
     * Returns the package with the given organization, name and version.
     *
     * @param packageOrg      organization name
     * @param packageName     package name
     * @param semanticVersion package version
     * @return the package with given organization, name and version
     */
    public Optional<Package> getPackage(PackageOrg packageOrg,
                                        PackageName packageName,
                                        SemanticVersion semanticVersion) {
        // Do we have a need to improve this logic?
        for (Project project : projects.values()) {
            PackageDescriptor pkgDesc = project.currentPackage().packageDescriptor();
            if (pkgDesc.org().equals(packageOrg) &&
                    pkgDesc.name().equals(packageName) &&
                    pkgDesc.version().value().equals(semanticVersion)) {
                return Optional.of(project.currentPackage());
            }
        }
        return Optional.empty();
    }

    /**
     * Returns all the package versions with the given org and name.
     *
     * @param packageOrg  organization name
     * @param packageName package name
     * @return all the package versions with the given org and name
     */
    public List<Package> getPackages(PackageOrg packageOrg, PackageName packageName) {
        // Do we have a need to improve this logic?
        List<Package> foundList = new ArrayList<>();
        for (Project project : projects.values()) {
            PackageDescriptor pkgDesc = project.currentPackage().packageDescriptor();
            if (pkgDesc.org().equals(packageOrg) &&
                    pkgDesc.name().equals(packageName)) {
                foundList.add(project.currentPackage());
            }
        }
        return foundList;
    }
}
