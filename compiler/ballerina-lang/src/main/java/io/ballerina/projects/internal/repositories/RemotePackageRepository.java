package io.ballerina.projects.internal.repositories;

import io.ballerina.projects.Package;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.PackageRepository;
import io.ballerina.projects.environment.ResolutionRequest;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.exceptions.ConnectionErrorException;
import org.ballerinalang.central.client.exceptions.PackageAlreadyExistsException;
import org.ballerinalang.toml.model.Settings;
import org.wso2.ballerinalang.util.RepoUtils;

import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        FileSystemRepository fileSystemRepository = new FileSystemRepository(environment, cacheDirectory);

        Proxy proxy = initializeProxy(settings.getProxy());
        CentralAPIClient client = new CentralAPIClient(repoUrl, proxy);

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
        String packageName = resolutionRequest.packageName().value();
        String orgName = resolutionRequest.orgName().value();
        String version = resolutionRequest.version().isPresent() ? resolutionRequest.version().get().toString() : null;

        Path packagePathInBaloCache = this.fileSystemRepo.balo.resolve(orgName).resolve(packageName);

        for (String supportedPlatform : SUPPORTED_PLATFORMS) {
            try {
                this.client.pullPackage(orgName, packageName, version, packagePathInBaloCache, supportedPlatform,
                                        RepoUtils.getBallerinaVersion(), true);
            } catch (CentralClientException | PackageAlreadyExistsException | ConnectionErrorException e) {
                // ignore when get package fail
            }
        }

        return this.fileSystemRepo.getPackage(resolutionRequest);
    }

    @Override
    public List<PackageVersion> getPackageVersions(ResolutionRequest resolutionRequest) {
        String orgName = resolutionRequest.orgName().value();
        String packageName = resolutionRequest.packageName().value();

        List<PackageVersion> packageVersions = new ArrayList<>();

        try {
            for (String version : this.client.getPackageVersions(orgName, packageName)) {
                packageVersions.add(PackageVersion.from(version));
            }
        } catch (CentralClientException e) {
            throw new ProjectException(e.getMessage());
        } catch (ConnectionErrorException e) {
            // ignore connect to remote repo failure
        }
        return packageVersions;
    }

    @Override
    public Map<String, List<String>> getPackages() {
        throw new UnsupportedOperationException();
    }
}
