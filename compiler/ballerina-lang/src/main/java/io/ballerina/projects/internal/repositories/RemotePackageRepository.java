package io.ballerina.projects.internal.repositories;

import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.Settings;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.PackageLockingMode;
import io.ballerina.projects.environment.PackageRepository;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.environment.ResolutionResponseDescriptor;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.exceptions.ConnectionErrorException;
import org.wso2.ballerinalang.util.RepoUtils;

import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;
import static io.ballerina.projects.util.ProjectUtils.initializeProxy;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.SUPPORTED_PLATFORMS;

/**
 * This class represents the remote package repository.
 *
 * @since 2.0.0
 */
public class RemotePackageRepository implements PackageRepository {

    private FileSystemRepository fileSystemRepo;
    private CentralAPIClient client;

    private RemotePackageRepository(FileSystemRepository fileSystemRepo, CentralAPIClient client) {
        this.fileSystemRepo = fileSystemRepo;
        this.client = client;
    }

    public static RemotePackageRepository from(Environment environment, Path cacheDirectory, String repoUrl,
            Settings settings) {
        if (Files.notExists(cacheDirectory)) {
            throw new ProjectException("cache directory does not exists: " + cacheDirectory);
        }
        String ballerinaShortVersion = RepoUtils.getBallerinaShortVersion();
        FileSystemRepository fileSystemRepository = new FileSystemRepository(
                environment, cacheDirectory, ballerinaShortVersion);
        Proxy proxy = initializeProxy(settings.getProxy());
        CentralAPIClient client = new CentralAPIClient(repoUrl, proxy, getAccessTokenOfCLI(settings));

        return new RemotePackageRepository(fileSystemRepository, client);
    }

    public static RemotePackageRepository from(Environment environment, Path cacheDirectory, Settings settings) {
        String repoUrl = RepoUtils.getRemoteRepoURL();
        if ("".equals(repoUrl)) {
            throw new ProjectException("remote repo url is empty");
        }

        return from(environment, cacheDirectory, repoUrl, settings);
    }

    @Override
    public Optional<Package> getPackage(ResolutionRequest resolutionRequest) {
        // Avoid resolving from remote repository for lang repo tests
        String langRepoBuild = System.getProperty("LANG_REPO_BUILD");
        if (langRepoBuild != null) {
            return Optional.empty();
        }

        // Check if the package is in cache
        Optional<Package> cachedPackage = this.fileSystemRepo.getPackage(resolutionRequest);
        if (cachedPackage.isPresent()) {
            return cachedPackage;
        }

        String packageName = resolutionRequest.packageName().value();
        String orgName = resolutionRequest.orgName().value();
        String version = resolutionRequest.version().isPresent() ? resolutionRequest.version().get().toString() : null;

        Path packagePathInBalaCache = this.fileSystemRepo.bala.resolve(orgName).resolve(packageName);

        // If environment is online pull from central
        if (!resolutionRequest.offline()) {
            for (String supportedPlatform : SUPPORTED_PLATFORMS) {
                try {
                    this.client.pullPackage(orgName, packageName, version, packagePathInBalaCache, supportedPlatform,
                                            RepoUtils.getBallerinaVersion(), true);
                } catch (CentralClientException e) {
                    // ignore when get package fail
                }
            }
        }

        return this.fileSystemRepo.getPackage(resolutionRequest);
    }

    @Override
    public List<PackageVersion> getPackageVersions(ResolutionRequest resolutionRequest) {
        String langRepoBuild = System.getProperty("LANG_REPO_BUILD");
        if (langRepoBuild != null) {
            return Collections.emptyList();
        }
        String orgName = resolutionRequest.orgName().value();
        String packageName = resolutionRequest.packageName().value();

        // First, Get local versions
        Set<PackageVersion> packageVersions = new HashSet<>(fileSystemRepo.getPackageVersions(resolutionRequest));

        // If the resolution request specifies to resolve offline, we return the local version
        if (resolutionRequest.offline()) {
            return new ArrayList<>(packageVersions);
        }

        try {
            for (String version : this.client.getPackageVersions(orgName, packageName, JvmTarget.JAVA_11.code(),
                                                                 RepoUtils.getBallerinaVersion())) {
                packageVersions.add(PackageVersion.from(version));
            }
        } catch (ConnectionErrorException e) {
            // ignore connect to remote repo failure
            return new ArrayList<>(packageVersions);
        } catch (CentralClientException e) {
            throw new ProjectException(e.getMessage());
        }
        return new ArrayList<>(packageVersions);
    }

    @Override
    public Map<String, List<String>> getPackages() {
        // We only return locally cached packages
        return fileSystemRepo.getPackages();
    }

    @Override
    public List<ResolutionResponseDescriptor> resolveDependencyVersions(
            List<ResolutionRequest> packageLoadRequests, PackageLockingMode packageLockingMode) {
        return fileSystemRepo.resolveDependencyVersions(packageLoadRequests, packageLockingMode);
    }
}
