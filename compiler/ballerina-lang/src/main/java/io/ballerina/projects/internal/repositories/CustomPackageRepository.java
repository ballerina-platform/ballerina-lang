package io.ballerina.projects.internal.repositories;

import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.PackageMetadataResponse;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.internal.ImportModuleRequest;
import io.ballerina.projects.internal.ImportModuleResponse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class CustomPackageRepository extends FileSystemRepository{
    protected final String repoId;


    //TODO: Overide the super methods if needed Eg: getPackage
    public CustomPackageRepository(Environment environment, Path cacheDirectory, String distributionVersion,
                                   String repoId) {
        super(environment, cacheDirectory, distributionVersion);
        this.repoId = repoId;
    }

    @Override
    public Optional<Package> getPackage(ResolutionRequest request, ResolutionOptions options) {
        String packageName = request.packageName().value();
        String orgName = request.orgName().value();
        String version = request.version().isPresent() ?
                request.version().get().toString() : "0.0.0";
        Path balaPath = super.getPackagePath(orgName, packageName, version);
        if (!Files.exists(balaPath)) {
            getPackageFromRemoteRepo(orgName, packageName, version);
        }
        return super.getPackage(request, options);
    }



    @Override
    public boolean isPackageExists(PackageOrg org,
                                   PackageName name,
                                   PackageVersion version) {
        boolean isPackageExist = super.isPackageExists(org, name, version);
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

        Path balaPath = getPackagePath(org.toString(), name.toString(), version.toString());
        if (Files.exists(balaPath)) {
            return Collections.singletonList(version);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    protected DependencyGraph<PackageDescriptor> getDependencyGraph(PackageOrg org, PackageName name,
                                                                    PackageVersion version) {
        isPackageExists(org, name, version);
        return super.getDependencyGraph(org, name, version);
    }

    @Override
    public Collection<ModuleDescriptor> getModules(PackageOrg org, PackageName name, PackageVersion version) {
        isPackageExists(org, name, version);
        return super.getModules(org, name, version);
    }

//    @Override
//    public Collection<PackageMetadataResponse> getPackageMetadata(Collection<ResolutionRequest> requests,
//                                                           ResolutionOptions options) {
//
//    }
//
//    @Override
//    public Collection<ImportModuleResponse> getPackageNames(Collection<ImportModuleRequest> requests,
//                                                     ResolutionOptions options) {
//
//    }


    protected abstract boolean getPackageFromRemoteRepo(String org, String name, String version);
}
