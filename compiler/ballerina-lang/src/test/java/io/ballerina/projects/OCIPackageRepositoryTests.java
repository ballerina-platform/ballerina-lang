/*
 * Copyright (c) 2026, WSO2 LLC. (http://wso2.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.projects;

import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.PackageLockingMode;
import io.ballerina.projects.environment.PackageMetadataResponse;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.environment.ResolutionResponse;
import io.ballerina.projects.internal.repositories.OCIPackageRepository;
import org.ballerinalang.oci.OciClient;
import org.ballerinalang.oci.OciClientException;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test OCI backed package repository.
 *
 * @since 2201.14.0
 */
public class OCIPackageRepositoryTests {

    private static class MockOciPackageRepository extends OCIPackageRepository {

        public MockOciPackageRepository(Environment environment, Path cacheDirectory, String distributionVersion) {
            super(environment, cacheDirectory, distributionVersion, null);
        }

        @Override
        public boolean getFromOci(PackageOrg org, PackageName name, PackageVersion version) {
            return false;
        }
    }

    private static final Path RESOURCE_DIRECTORY = Path.of("src/test/resources");
    private static final Path TEST_REPO = RESOURCE_DIRECTORY.resolve("custom-repo-resources/local-custom-repo");
    private OCIPackageRepository ociPackageRepository;

    @BeforeClass
    public void setup() {
        ociPackageRepository = new MockOciPackageRepository(new Environment() {
            @Override
            public <T> T getService(Class<T> clazz) {
                return null;
            }
        }, TEST_REPO, "1.2.3");
    }

    @Test(description = "Test package existence in OCI repository")
    public void testIsPackageExist() {
        boolean isPackageExists = ociPackageRepository.isPackageExists(
                PackageOrg.from("testorg"), PackageName.from("packA"),
                PackageVersion.from("0.1.0"));
        Assert.assertTrue(isPackageExists);
    }

    @Test(description = "Test non-existing package in OCI repository - online")
    public void testNonExistingPkg() {
        boolean isPackageExists = ociPackageRepository.isPackageExists(
                PackageOrg.from("testorg"),
                PackageName.from("packC"), PackageVersion.from("0.1.0"));
        Assert.assertFalse(isPackageExists);
    }

    @Test(description = "Test package version existence in OCI repository")
    public void testGetPackageVersions() {
        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("testorg"),
                        PackageName.from("packA"), PackageVersion.from("0.1.0")),
                PackageDependencyScope.DEFAULT);
        Collection<PackageVersion> versions = ociPackageRepository.getPackageVersions(resolutionRequest,
                ResolutionOptions.builder().setOffline(true).build());
        Assert.assertEquals(versions.size(), 1);
        Assert.assertTrue(versions.contains(PackageVersion.from("0.1.0")));
    }

    @Test(description = "Test getPackage (non existing package) in OCI repository - offline")
    public void testGetPackageNonExistingOffline() {
        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("testorg"),
                        PackageName.from("packC"), PackageVersion.from("0.1.0")),
                PackageDependencyScope.DEFAULT);
        Optional<Package> repositoryPackage = ociPackageRepository.getPackage(resolutionRequest,
                ResolutionOptions.builder().setOffline(true).build());
        Assert.assertTrue(repositoryPackage.isEmpty());
    }

    @Test(description = "Test getPackage (non existing package) in OCI repository - online")
    public void testGetPackageNonExistingOnline() {
        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("testorg"),
                        PackageName.from("packC"), PackageVersion.from("0.1.0")),
                PackageDependencyScope.DEFAULT);
        Optional<Package> repositoryPackage = ociPackageRepository.getPackage(resolutionRequest,
                ResolutionOptions.builder().setOffline(false).build());
        Assert.assertTrue(repositoryPackage.isEmpty());
    }

    @Test(description = "Test getPackage (existing package) in OCI repository - online")
    public void testGetPackageExistingOnline() {
        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("testorg"),
                        PackageName.from("packA"), PackageVersion.from("0.1.0")),
                PackageDependencyScope.DEFAULT);
        Optional<Package> repositoryPackage = ociPackageRepository.getPackage(resolutionRequest,
                ResolutionOptions.builder().setOffline(false).build());
        Assert.assertFalse(repositoryPackage.isEmpty());
    }

    @Test(description = "Test getPackage (existing package) in OCI repository - offline")
    public void testGetPackageExistingOffline() {
        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("testorg"),
                        PackageName.from("packA"), PackageVersion.from("0.1.0")),
                PackageDependencyScope.DEFAULT);
        Optional<Package> repositoryPackage = ociPackageRepository.getPackage(resolutionRequest,
                ResolutionOptions.builder().setOffline(true).build());
        Assert.assertTrue(repositoryPackage.isPresent());
        Assert.assertEquals(repositoryPackage.get().descriptor().toString(), "testorg/packA:0.1.0");
    }

    @Test(description = "Test getPackages")
    public void testGetPackages() {
        Map<String, List<String>> repositoryPackages = ociPackageRepository.getPackages();
        Assert.assertEquals(repositoryPackages.keySet().size(), 1);
        Assert.assertTrue(repositoryPackages.containsKey("testorg"));
        Assert.assertEquals(repositoryPackages.get("testorg").size(), 2);
    }

    @Test(description = "Test non-existing package version in OCI repository")
    public void testGetNonExistingPackageVersions1() {
        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("testorg"),
                        PackageName.from("packA"), PackageVersion.from("0.2.0")),
                PackageDependencyScope.DEFAULT);
        Collection<PackageVersion> versions = ociPackageRepository.getPackageVersions(resolutionRequest,
                ResolutionOptions.builder().setOffline(true).build());
        Assert.assertEquals(versions.size(), 0);
        Assert.assertFalse(versions.contains(PackageVersion.from("0.2.0")));
    }

    @Test(description = "Test non-existing package modules in OCI repository")
    public void testNonExistingPkgModules() {
        Collection<ModuleDescriptor> modules = ociPackageRepository.getModules(
                PackageOrg.from("testorg"),
                PackageName.from("packC"), PackageVersion.from("0.1.0"));
        Assert.assertTrue(modules.isEmpty());
    }

    @Test(description = "Test non-existing package version of a non-existing package in OCI repository")
    public void testGetNonExistingPackageVersions2() {
        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("testorg"),
                        PackageName.from("packE"), PackageVersion.from("0.2.0")),
                PackageDependencyScope.DEFAULT);
        Collection<PackageVersion> versions = ociPackageRepository.getPackageVersions(resolutionRequest,
                ResolutionOptions.builder().setOffline(true).build());
        Assert.assertEquals(versions.size(), 0);
    }


    private static final Path PROXY_TEST_REPO =
            RESOURCE_DIRECTORY.resolve("custom-repo-resources/local-custom-repo");

    private static final Environment PROXY_ENV = new Environment() {
        @Override
        public <T> T getService(Class<T> clazz) {
            return null;
        }
    };

    private OCIPackageRepository proxyRepo(OciClient client) {
        return new OCIPackageRepository(PROXY_ENV, PROXY_TEST_REPO, "1.2.3", client, true);
    }

    @Test(description = "Proxy: version discovery reads the distribution-scoped index instead of listing tags",
            groups = {"proxy"})
    public void testGetPackageVersionsProxyCentralUsesIndex() {
        OciClient mockClient = Mockito.mock(OciClient.class);
        Mockito.when(mockClient.pullMetadata(anyString(), anyString(), anyString()))
                .thenReturn(List.of("0.1.0", "0.2.0"));

        OCIPackageRepository repo = proxyRepo(mockClient);
        ResolutionRequest request = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("testorg"), PackageName.from("packA")),
                PackageDependencyScope.DEFAULT);

        Collection<PackageVersion> versions = repo.getPackageVersions(request,
                ResolutionOptions.builder().setOffline(false).build());

        Assert.assertTrue(versions.contains(PackageVersion.from("0.2.0")));
        verify(mockClient).pullMetadata("testorg", "packA", "1.2.3");
        verify(mockClient, never()).listTags(anyString(), anyString());
        // The index is already filtered for the distribution, so no per-version label lookups
        verify(mockClient, never()).pullLabels(anyString(), anyString(), anyString());
    }

    @Test(description = "Hosted: versions are filtered by the Maven proxy's distribution rule, patch ignored")
    public void testGetPackageVersionsHostedFiltersByDistribution() {
        OciClient mockClient = Mockito.mock(OciClient.class);
        Mockito.when(mockClient.listTags("testorg", "remotepkg")).thenReturn(List.of("1.0.0", "1.1.0", "1.2.0"));
        mockLabels(mockClient, "1.0.0", "2201.12.0");  // older update
        mockLabels(mockClient, "1.1.0", "2201.13.6");  // same update, newer patch
        mockLabels(mockClient, "1.2.0", "2201.14.0");  // newer update

        Collection<PackageVersion> versions = hostedRepo(mockClient, "2201.13.0").getPackageVersions(
                remotePkgRequest(), ResolutionOptions.builder().setOffline(false).build());

        Assert.assertTrue(versions.contains(PackageVersion.from("1.0.0")));
        Assert.assertTrue(versions.contains(PackageVersion.from("1.1.0")));
        Assert.assertFalse(versions.contains(PackageVersion.from("1.2.0")));
    }

    @Test(description = "Hosted: a SNAPSHOT distribution accepts packages built with its release")
    public void testGetPackageVersionsHostedSnapshotDistribution() {
        OciClient mockClient = Mockito.mock(OciClient.class);
        Mockito.when(mockClient.listTags("testorg", "remotepkg")).thenReturn(List.of("1.0.0"));
        mockLabels(mockClient, "1.0.0", "2201.14.0");

        Collection<PackageVersion> versions = hostedRepo(mockClient, "2201.14.0-SNAPSHOT").getPackageVersions(
                remotePkgRequest(), ResolutionOptions.builder().setOffline(false).build());

        Assert.assertTrue(versions.contains(PackageVersion.from("1.0.0")));
    }

    @Test(description = "Hosted: versions without compatibility labels are not filtered out, as in a Maven repository")
    public void testGetPackageVersionsHostedUnlabelledVersion() {
        OciClient mockClient = Mockito.mock(OciClient.class);
        Mockito.when(mockClient.listTags("testorg", "remotepkg")).thenReturn(List.of("1.0.0", "1.1.0"));
        Mockito.when(mockClient.pullLabels("testorg", "remotepkg", "1.0.0")).thenReturn(Map.of());
        Mockito.when(mockClient.pullLabels("testorg", "remotepkg", "1.1.0")).thenReturn(Map.of(
                OciClient.PLATFORM_LABEL, "unknownplatform", OciClient.DISTRIBUTION_LABEL, "2201.13.0"));

        Collection<PackageVersion> versions = hostedRepo(mockClient, "2201.13.0").getPackageVersions(
                remotePkgRequest(), ResolutionOptions.builder().setOffline(false).build());

        Assert.assertTrue(versions.contains(PackageVersion.from("1.0.0")));
        // A label that is present is still enforced
        Assert.assertFalse(versions.contains(PackageVersion.from("1.1.0")));
    }

    @Test(description = "A pull the registry fails at first is retried, and succeeds once the registry serves it")
    public void testGetFromOciRetriesRegistryFailures() throws IOException {
        OciClient mockClient = Mockito.mock(OciClient.class);
        Mockito.doThrow(new OciClientException("upstream not ready"))
                .doAnswer(invocation -> {
                    writePackABala(Path.of(invocation.getArgument(3, String.class)));
                    return null;
                })
                .when(mockClient).pullMetadata(eq("testorg"), eq("packA"), eq("0.1.0"), anyString(), anyString());
        Path cache = Files.createTempDirectory("oci-retry-cache");
        OCIPackageRepository repo = retryingRepo(cache, mockClient);

        boolean pulled = repo.getFromOci(PackageOrg.from("testorg"), PackageName.from("packA"),
                PackageVersion.from("0.1.0"));

        Assert.assertTrue(pulled);
        verify(mockClient, times(2)).pullMetadata(eq("testorg"), eq("packA"), eq("0.1.0"), anyString(), anyString());
        Assert.assertTrue(repo.isPackageExists(PackageOrg.from("testorg"), PackageName.from("packA"),
                PackageVersion.from("0.1.0")));
    }

    @Test(description = "A pull the registry keeps failing gives up after the last attempt")
    public void testGetFromOciGivesUpAfterLastAttempt() throws IOException {
        OciClient mockClient = Mockito.mock(OciClient.class);
        Mockito.doThrow(new OciClientException("upstream down"))
                .when(mockClient).pullMetadata(anyString(), anyString(), anyString(), anyString(), anyString());
        OCIPackageRepository repo = retryingRepo(Files.createTempDirectory("oci-retry-cache"), mockClient);

        boolean pulled = repo.getFromOci(PackageOrg.from("testorg"), PackageName.from("packA"),
                PackageVersion.from("0.1.0"));

        Assert.assertFalse(pulled);
        verify(mockClient, times(3)).pullMetadata(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    // Retries without waiting, so the tests don't sleep
    private static OCIPackageRepository retryingRepo(Path cache, OciClient client) {
        return new OCIPackageRepository(PROXY_ENV, cache, "2201.13.0", client) {
            @Override
            protected boolean waitBeforeRetry(int attempt) {
                return true;
            }
        };
    }

    // Stands in for the registry download: zips the packA fixture where the client would write its bala
    private static void writePackABala(Path downloadDirectory) throws IOException {
        Path source = TEST_REPO.resolve("bala/testorg/packA/0.1.0/any");
        Path bala = downloadDirectory.resolve("testorg/packA/0.1.0/packA-0.1.0.bala");
        Files.createDirectories(bala.getParent());
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(bala));
             Stream<Path> files = Files.walk(source)) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {
                zip.putNextEntry(new ZipEntry(source.relativize(file).toString().replace('\\', '/')));
                Files.copy(file, zip);
                zip.closeEntry();
            }
        }
    }

    private OCIPackageRepository hostedRepo(OciClient client, String distributionVersion) {
        return new OCIPackageRepository(PROXY_ENV, PROXY_TEST_REPO, distributionVersion, client);
    }

    private static ResolutionRequest remotePkgRequest() {
        return ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("testorg"), PackageName.from("remotepkg")),
                PackageDependencyScope.DEFAULT);
    }

    private static void mockLabels(OciClient client, String version, String distributionVersion) {
        Mockito.when(client.pullLabels("testorg", "remotepkg", version)).thenReturn(Map.of(
                OciClient.PLATFORM_LABEL, "java21", OciClient.DISTRIBUTION_LABEL, distributionVersion));
    }

    @Test(description = "Proxy: offline resolution never calls the registry", groups = {"proxy"})
    public void testGetPackageVersionsProxyCentralOffline() {
        OciClient mockClient = Mockito.mock(OciClient.class);
        OCIPackageRepository repo = proxyRepo(mockClient);
        ResolutionRequest request = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("testorg"),
                        PackageName.from("packA"), PackageVersion.from("0.1.0")),
                PackageDependencyScope.DEFAULT);

        Collection<PackageVersion> versions = repo.getPackageVersions(request,
                ResolutionOptions.builder().setOffline(true).build());

        verify(mockClient, never()).pullMetadata(anyString(), anyString(), anyString());
        Assert.assertTrue(versions.contains(PackageVersion.from("0.1.0")));
    }

    @Test(description = "Proxy: a HARD-locked version already resolved locally skips the registry round trip",
            groups = {"proxy"})
    public void testGetPackageMetadataProxyCentralHardLockSkipsRegistry() {
        OciClient mockClient = Mockito.mock(OciClient.class);
        OCIPackageRepository repo = proxyRepo(mockClient);
        ResolutionRequest request = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("testorg"),
                        PackageName.from("packA"), PackageVersion.from("0.1.0")),
                PackageDependencyScope.DEFAULT,
                DependencyResolutionType.SOURCE,
                PackageLockingMode.HARD);

        Collection<PackageMetadataResponse> responses = repo.getPackageMetadata(
                List.of(request), ResolutionOptions.builder().setOffline(false).build());

        verify(mockClient, never()).pullMetadata(anyString(), anyString(), anyString());
        Assert.assertFalse(responses.isEmpty());
        Assert.assertEquals(responses.iterator().next().resolutionStatus(),
                ResolutionResponse.ResolutionStatus.RESOLVED);
    }
}
