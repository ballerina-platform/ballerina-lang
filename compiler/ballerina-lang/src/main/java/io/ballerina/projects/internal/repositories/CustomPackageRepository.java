package io.ballerina.projects.internal.repositories;

import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.environment.Environment;

import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.environment.ResolutionRequest;


import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class CustomPackageRepository extends AbstractPackageRepository implements CustomRepositoryHelper {
    private final FileSystemRepository fileSystemRepository;
    protected final String repoId;

    public CustomPackageRepository(Environment environment, Path cacheDirectory, String distributionVersion,
                                   String repoId) {
        this.fileSystemRepository = new FileSystemRepository(environment, cacheDirectory, distributionVersion);
        this.repoId = repoId;
    }

    @Override
    public Optional<Package> getPackage(ResolutionRequest request, ResolutionOptions options) {
        String packageName = request.packageName().value();
        String orgName = request.orgName().value();
        String version = request.version().isPresent() ?
                request.version().get().toString() : "0.0.0";
        Path balaPath = this.fileSystemRepository.getPackagePath(orgName, packageName, version);
        if (!Files.exists(balaPath)) {
            getPackageFromRemoteRepo(orgName, packageName, version);
        }
        return this.fileSystemRepository.getPackage(request, options);
    }

    @Override
    public Collection<PackageVersion> getPackageVersions(ResolutionRequest request, ResolutionOptions options) {
        return getPackageVersions(request.orgName(), request.packageName(),
                request.version().orElse(null));
    }

    @Override
    public Map<String, List<String>> getPackages() {
        return this.fileSystemRepository.getPackages();
    }


    @Override
    public boolean isPackageExists(PackageOrg org,
                                   PackageName name,
                                   PackageVersion version) {
        boolean isPackageExist = this.fileSystemRepository.isPackageExists(org, name, version);
        if (!isPackageExist) {
            return getPackageFromRemoteRepo(org.value(), name.value(), version.value().toString());
        }
        return true;
    }

    @Override
    protected List<PackageVersion> getPackageVersions(PackageOrg org, PackageName name, PackageVersion version) {
        if (version == null) {
            return Collections.emptyList();
        }

        isPackageExists(org, name, version);

        Path balaPath = this.fileSystemRepository.getPackagePath(org.toString(), name.toString(), version.toString());
        if (Files.exists(balaPath)) {
            return Collections.singletonList(version);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    protected DependencyGraph<PackageDescriptor> getDependencyGraph(PackageOrg org, PackageName name,
                                                                    PackageVersion version) {
        boolean packageExists = isPackageExists(org, name, version);
        if (!packageExists) {
            return DependencyGraph.emptyGraph();
        }
        return this.fileSystemRepository.getDependencyGraph(org, name, version);
    }

    @Override
    public Collection<ModuleDescriptor> getModules(PackageOrg org, PackageName name, PackageVersion version) {
        boolean packageExists = isPackageExists(org, name, version);
        if (!packageExists) {
            return Collections.emptyList();
        }
        return this.fileSystemRepository.getModules(org, name, version);
    }
}
