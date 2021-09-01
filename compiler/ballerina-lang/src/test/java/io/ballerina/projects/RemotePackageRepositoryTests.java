package io.ballerina.projects;

import io.ballerina.projects.environment.PackageMetadataResponse;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.environment.ResolutionResponse;
import io.ballerina.projects.internal.ImportModuleRequest;
import io.ballerina.projects.internal.ImportModuleResponse;
import io.ballerina.projects.internal.repositories.FileSystemRepository;
import io.ballerina.projects.internal.repositories.RemotePackageRepository;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.exceptions.ConnectionErrorException;
import org.ballerinalang.central.client.model.PackageNameResolutionRequest;
import org.ballerinalang.central.client.model.PackageNameResolutionResponse;
import org.ballerinalang.central.client.model.PackageResolutionRequest;
import org.ballerinalang.central.client.model.PackageResolutionResponse;
import org.ballerinalang.central.client.model.PackageResolutionResponse.Dependency;
import org.ballerinalang.central.client.model.PackageResolutionResponse.Package;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test remote package repository.
 *
 */
public class RemotePackageRepositoryTests {
    private RemotePackageRepository remotePackageRepository;
    CentralAPIClient centralAPIClient = mock(CentralAPIClient.class);
    FileSystemRepository fileSystemRepository = mock(FileSystemRepository.class);

    // Mock data
    // - Package descriptors
    PackageDescriptor http120 = PackageDescriptor.from(
            PackageOrg.from("ballerina"), PackageName.from("http"), PackageVersion.from("1.2.0"));
    PackageDescriptor http121 = PackageDescriptor.from(
            PackageOrg.from("ballerina"), PackageName.from("http"), PackageVersion.from("1.2.1"));
    PackageDescriptor covid156 = PackageDescriptor.from(
            PackageOrg.from("ballerinax"), PackageName.from("covid"), PackageVersion.from("1.5.6"));
    PackageDescriptor covid159 = PackageDescriptor.from(
            PackageOrg.from("ballerinax"), PackageName.from("covid"), PackageVersion.from("1.5.9"));
    PackageDescriptor smtp130 = PackageDescriptor.from(
            PackageOrg.from("ballerinax"), PackageName.from("smtp"), PackageVersion.from("1.3.0"));
    // - Resolution requests
    ResolutionRequest resHttp120 = ResolutionRequest.from(http120 , PackageDependencyScope.DEFAULT, false);
    ResolutionRequest resCovid156 = ResolutionRequest.from(covid156 , PackageDependencyScope.DEFAULT, false);
    ResolutionRequest resSmtp130 = ResolutionRequest.from(smtp130 , PackageDependencyScope.DEFAULT, false);

    // - Data returned from central client
    // -- dependency package
    Dependency io132 = new Dependency("ballerina", "io", "1.3.2", Arrays.asList());
    Dependency http122d = new Dependency("ballerina", "http", "1.2.2", Arrays.asList(io132));
    // -- direct imports
    Package http122 = new Package("ballerina", "http", "1.2.2", Arrays.asList(io132));
    Package covid154 = new Package("ballerinax", "covid", "1.5.7", Arrays.asList(io132, http122d));
    Package smtp = new Package("ballerinax", "smtp", "1.0.0", Arrays.asList());
    // File system responses
    PackageMetadataResponse fileHttp120 = PackageMetadataResponse
            .from(resHttp120, http121, DependencyGraph.emptyGraph());
    PackageMetadataResponse fileCovid159 = PackageMetadataResponse
            .from(resCovid156, covid159, DependencyGraph.emptyGraph());
    PackageMetadataResponse fileSmtp130 = PackageMetadataResponse.createUnresolvedResponse(resSmtp130);

    @BeforeSuite
    public void setup() {
        remotePackageRepository = new RemotePackageRepository(fileSystemRepository, centralAPIClient);
    }


    @Test(description = "Test remote repo responses in valid cases")
    public void testHappyPath() throws CentralClientException {
        // Mock API Calls
        // Mock CentralClient call
        PackageResolutionResponse response = PackageResolutionResponse.from(Arrays.asList(http122, covid154),
                Arrays.asList());
        when(centralAPIClient.resolveDependencies(any(PackageResolutionRequest.class),
                anyString(), anyString(), anyBoolean())).thenReturn(response);
        // Mock response from file system
        when(fileSystemRepository.resolvePackageMetadata(anyList()))
                .thenReturn(Arrays.asList(fileHttp120, fileCovid159));

        // Test call to remote repository
        List<PackageMetadataResponse> resolutionResponseDescriptors =
                remotePackageRepository.resolvePackageMetadata(Arrays.asList(resHttp120, resCovid156));

        // response should return resolution for same number of requests
        Assert.assertEquals(resolutionResponseDescriptors.size(), 2);

        // If the remote repository version is higher than filesystem it should return remote version
        PackageMetadataResponse httpResult =  resolutionResponseDescriptors.get(0);
        Assert.assertEquals(httpResult.resolvedDescriptor().version().toString(), "1.2.2");
        Assert.assertEquals(httpResult.resolutionStatus(), ResolutionResponse.ResolutionStatus.RESOLVED);

        // If the remote repository version is lower than filesystem it should return filesystem version
        PackageMetadataResponse covidResult =  resolutionResponseDescriptors.get(1);
        Assert.assertEquals(covidResult.resolvedDescriptor().version().toString(), "1.5.9");
        Assert.assertEquals(covidResult.resolutionStatus(), ResolutionResponse.ResolutionStatus.RESOLVED);

        // Check if dependencies are populated for two sub levels


    }

    @Test(description = "Test remote repo responses in unresolved cases")
    public void testUnresolvedCases() throws CentralClientException {
        // Mock API Calls
        // Mock CentralClient call
        PackageResolutionResponse response = PackageResolutionResponse.from(Arrays.asList(http122),
                Arrays.asList(covid154, smtp));
        when(centralAPIClient.resolveDependencies(any(PackageResolutionRequest.class),
                anyString(), anyString(), anyBoolean())).thenReturn(response);
        // Mock response from file system
        when(fileSystemRepository.resolvePackageMetadata(anyList()))
                .thenReturn(Arrays.asList(PackageMetadataResponse.createUnresolvedResponse(resHttp120),
                        fileCovid159, fileSmtp130));

        // Test call to remote repository
        List<PackageMetadataResponse> resolutionResponseDescriptors =
                remotePackageRepository.resolvePackageMetadata(Arrays.asList(resHttp120, resCovid156, resSmtp130));

        // response should return resolution for same number of requests
        Assert.assertEquals(resolutionResponseDescriptors.size(), 3);

        // if local is unresolved it should return remote
        PackageMetadataResponse httpResult =  resolutionResponseDescriptors.get(0);
        Assert.assertEquals(httpResult.resolvedDescriptor().version().toString(), "1.2.2");
        Assert.assertEquals(httpResult.resolutionStatus(), ResolutionResponse.ResolutionStatus.RESOLVED);

        // If remote is unresolved it should return local
        PackageMetadataResponse covidResult =  resolutionResponseDescriptors.get(1);
        Assert.assertEquals(covidResult.resolvedDescriptor().version().toString(), "1.5.9");
        Assert.assertEquals(covidResult.resolutionStatus(), ResolutionResponse.ResolutionStatus.RESOLVED);

        // If both do not have a resolution it should return as unresolved
        PackageMetadataResponse smtpResult =  resolutionResponseDescriptors.get(2);
        Assert.assertEquals(smtpResult.resolutionStatus(), ResolutionResponse.ResolutionStatus.UNRESOLVED);
    }

    // Test request in offline
    @Test(description = "Test offline requests")
    public void testOfflineRequests() throws CentralClientException {
        // Mock API Calls
        // Mock CentralClient call
        PackageResolutionResponse response = PackageResolutionResponse.from(Arrays.asList(http122, covid154),
                Arrays.asList());
        when(centralAPIClient.resolveDependencies(any(PackageResolutionRequest.class),
                anyString(), anyString(), anyBoolean()))
                .thenThrow(new AssertionError("Client get called in offline mode"));
        // Mock response from file system
        when(fileSystemRepository.resolvePackageMetadata(anyList()))
                .thenReturn(Arrays.asList(fileHttp120, fileCovid159));

        // Test call to remote repository
        ResolutionRequest resHttp120Offline = ResolutionRequest.from(http120 , PackageDependencyScope.DEFAULT, true);
        ResolutionRequest resCovid156Offline = ResolutionRequest.from(covid156 , PackageDependencyScope.DEFAULT, true);
        List<PackageMetadataResponse> resolutionResponseDescriptors =
                remotePackageRepository.resolvePackageMetadata(Arrays.asList(resHttp120Offline, resCovid156Offline));

        // response should return resolution for same number of requests
        Assert.assertEquals(resolutionResponseDescriptors.size(), 2);

        // If the remote repository version is higher than filesystem it should return remote version
        PackageMetadataResponse httpResult =  resolutionResponseDescriptors.get(0);
        Assert.assertEquals(httpResult.resolvedDescriptor().version().toString(), "1.2.1");
        Assert.assertEquals(httpResult.resolutionStatus(), ResolutionResponse.ResolutionStatus.RESOLVED);

        // If the remote repository version is lower than filesystem it should return filesystem version
        PackageMetadataResponse covidResult =  resolutionResponseDescriptors.get(1);
        Assert.assertEquals(covidResult.resolvedDescriptor().version().toString(), "1.5.9");
        Assert.assertEquals(covidResult.resolutionStatus(), ResolutionResponse.ResolutionStatus.RESOLVED);
    }

    @Test(description = "Test client send an exception")
    public void testClientException() throws CentralClientException {
        // Mock API Calls
        // Mock CentralClient call
        PackageResolutionResponse response = PackageResolutionResponse.from(Arrays.asList(http122, covid154),
                Arrays.asList());
        when(centralAPIClient.resolveDependencies(any(PackageResolutionRequest.class),
                anyString(), anyString(), anyBoolean())).thenThrow(new ConnectionErrorException("500 Error"));
        // Mock response from file system
        when(fileSystemRepository.resolvePackageMetadata(anyList()))
                .thenReturn(Arrays.asList(fileHttp120, fileCovid159));

        // Test call to remote repository
        List<PackageMetadataResponse> resolutionResponseDescriptors =
                remotePackageRepository.resolvePackageMetadata(Arrays.asList(resHttp120, resCovid156));

        // response should return resolution for same number of requests
        Assert.assertEquals(resolutionResponseDescriptors.size(), 2);

        // If the remote repository version is higher than filesystem it should return remote version
        PackageMetadataResponse httpResult =  resolutionResponseDescriptors.get(0);
        Assert.assertEquals(httpResult.resolvedDescriptor().version().toString(), "1.2.1");
        Assert.assertEquals(httpResult.resolutionStatus(), ResolutionResponse.ResolutionStatus.RESOLVED);

        // If the remote repository version is lower than filesystem it should return filesystem version
        PackageMetadataResponse covidResult =  resolutionResponseDescriptors.get(1);
        Assert.assertEquals(covidResult.resolvedDescriptor().version().toString(), "1.5.9");
        Assert.assertEquals(covidResult.resolutionStatus(), ResolutionResponse.ResolutionStatus.RESOLVED);
    }

    //Package name resolution data
    ImportModuleRequest javaArrayReq = new ImportModuleRequest(PackageOrg.from("ballerina"), "java.array");
    ImportModuleRequest covidReq = new ImportModuleRequest(PackageOrg.from("ballerina"), "covid.client");
    ImportModuleRequest unknownReq = new ImportModuleRequest(PackageOrg.from("ballerina"), "unknown");

    PackageDescriptor descJavaArray = PackageDescriptor
            .from(PackageOrg.from("ballerina"), PackageName.from("java.array"));

    ImportModuleResponse fileJavaArray = new ImportModuleResponse(descJavaArray, javaArrayReq);
    ImportModuleResponse fileCovid = new ImportModuleResponse(covidReq);
    ImportModuleResponse fileUnresolved = new ImportModuleResponse(unknownReq);

    @Test(description = "Test package name resolution")
    public void testPackageNameResolution() throws CentralClientException {
        // Mock central name resolution
        PackageNameResolutionResponse centralResponse = new PackageNameResolutionResponse(Arrays.asList(
                new PackageNameResolutionResponse
                        .Module("ballerina", "covid.client", "1.2.3", "covid", null)),
                Arrays.asList(
                new PackageNameResolutionResponse
                        .Module("ballerina", "unknown", "1.4.5", null, "module not found"))
        );
        when(centralAPIClient.resolvePackageNames(any(PackageNameResolutionRequest.class),
                anyString(), anyString(), anyBoolean())).thenReturn(centralResponse);

        // Mock response from file system
        when(fileSystemRepository.resolvePackageNames(anyList()))
                .thenReturn(Arrays.asList(fileJavaArray, fileCovid, fileUnresolved));


        List<ImportModuleResponse> response = this.remotePackageRepository
                .resolvePackageNames(Arrays.asList(javaArrayReq, covidReq, unknownReq));

        Assert.assertEquals(response.size(), 3);
        ImportModuleResponse javaArray = response.get(0);
        Assert.assertEquals(javaArray.packageDescriptor().name().value(), "java.array");

        ImportModuleResponse covid = response.get(1);
        Assert.assertEquals(covid.packageDescriptor().name().value(), "covid");

        ImportModuleResponse unknown = response.get(2);
        Assert.assertEquals(unknown.resolutionStatus(), ResolutionResponse.ResolutionStatus.UNRESOLVED);
    }
}
