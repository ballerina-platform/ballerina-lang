/*
 * Copyright (c) 2023, WSO2 LLC. (http://wso2.com).
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
import io.ballerina.projects.internal.ImportModuleRequest;
import io.ballerina.projects.internal.ImportModuleResponse;
import io.ballerina.projects.internal.repositories.MavenPackageRepository;
import org.ballerinalang.maven.bala.client.MavenResolverClient;
import org.ballerinalang.maven.bala.client.MavenResolverClientException;
import org.ballerinalang.maven.bala.client.model.PackageResolutionResponse;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Test maven package repository.
 *
 * @since 2.0.0
 */
public class MavenPackageRepositoryTests {


    private static class MockMavenPackageRepository extends MavenPackageRepository {

        public MockMavenPackageRepository(Environment environment, Path cacheDirectory, String distributionVersion) {
            super(environment, cacheDirectory, distributionVersion, null, null, false);
        }


        @Override
        public boolean getPackageFromRemoteRepo(String org, String name, String version) {
            Path sourceFolderPath =
                    RESOURCE_DIRECTORY.resolve("custom-repo-resources/remote-custom-repo").resolve(name);
            Path destinationFolderPath =
                    RESOURCE_DIRECTORY.resolve("custom-repo-resources/local-custom-repo/bala").resolve(org)
                            .resolve(name);

            try {
                Files.walkFileTree(sourceFolderPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        Path targetDir = destinationFolderPath.resolve(sourceFolderPath.relativize(dir));
                        Files.createDirectories(targetDir);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.copy(file, destinationFolderPath.resolve(sourceFolderPath.relativize(file)),
                                StandardCopyOption.REPLACE_EXISTING);
                        return FileVisitResult.CONTINUE;
                    }
                });

            } catch (IOException e) {
                return false;
            }
            return true;
        }
    }

    private static final Path RESOURCE_DIRECTORY = Path.of("src/test/resources");
    private static final Path TEST_REPO = RESOURCE_DIRECTORY.resolve("custom-repo-resources/local-custom-repo");
    private MavenPackageRepository customPackageRepository;

    @BeforeSuite
    public void setup() {
        customPackageRepository = new MockMavenPackageRepository(new Environment() {
            @Override
            public <T> T getService(Class<T> clazz) {
                return null;
            }
        }, TEST_REPO, "1.2.3");
    }

    @Test(description = "Test package existence in custom repository")
    public void testIsPackageExist() {
        boolean isPackageExists = customPackageRepository.isPackageExists(
                PackageOrg.from("testorg"), PackageName.from("packA"),
                PackageVersion.from("0.1.0"));
        Assert.assertTrue(isPackageExists);
    }

    @Test(description = "Test non-existing package in custom repository - online")
    public void testNonExistingPkg()  {
        boolean isPackageExists = customPackageRepository.isPackageExists(
                PackageOrg.from("testorg"),
                PackageName.from("packC"), PackageVersion.from("0.1.0"));
        Assert.assertFalse(isPackageExists);
    }

    @Test(description = "Test package version existence in custom repository")
    public void testGetPackageVersionsOnline() {
        customPackageRepository.isPackageExists(
                PackageOrg.from("testorg"), PackageName.from("packA"),
                PackageVersion.from("0.1.0"));
        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("testorg"),
                        PackageName.from("packA"), PackageVersion.from("0.1.0")),
                PackageDependencyScope.DEFAULT);
        Collection<PackageVersion> versions = customPackageRepository.getPackageVersions(resolutionRequest,
                ResolutionOptions.builder().setOffline(true).build());
        Assert.assertEquals(versions.size(), 1);
        Assert.assertTrue(versions.contains(PackageVersion.from("0.1.0")));
    }

    @Test(description = "Test getPackage (non existing package) in custom repository - offline")
    public void testGetPackageNonExistingOffline() {
        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("testorg"),
                        PackageName.from("packC"), PackageVersion.from("0.1.0")),
                PackageDependencyScope.DEFAULT);
        Optional<Package> repositoryPackage = customPackageRepository.getPackage(resolutionRequest,
                ResolutionOptions.builder().setOffline(true).build());
        Assert.assertTrue(repositoryPackage.isEmpty());
    }

    @Test(description = "Test getPackage (non existing package) in custom repository - online")
    public void testGetPackageNonExistingOnline() {
        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("testorg"),
                        PackageName.from("packC"), PackageVersion.from("0.1.0")),
                PackageDependencyScope.DEFAULT);
        Optional<Package> repositoryPackage = customPackageRepository.getPackage(resolutionRequest,
                ResolutionOptions.builder().setOffline(false).build());
        Assert.assertTrue(repositoryPackage.isEmpty());
    }

    @Test(description = "Test getPackage (existing package) in custom repository - online")
    public void testGetPackageExistingOnline() {
        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("testorg"),
                        PackageName.from("packA"), PackageVersion.from("0.1.0")),
                PackageDependencyScope.DEFAULT);
        Optional<Package> repositoryPackage = customPackageRepository.getPackage(resolutionRequest,
                ResolutionOptions.builder().setOffline(false).build());
        Assert.assertFalse(repositoryPackage.isEmpty());
    }

    @Test(description = "Test getPackage (existing package) in custom repository - offline")
    public void testGetPackageExistingOffline() {
        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("testorg"),
                        PackageName.from("packA"), PackageVersion.from("0.1.0")),
                PackageDependencyScope.DEFAULT);
        Optional<Package> repositoryPackage = customPackageRepository.getPackage(resolutionRequest,
                ResolutionOptions.builder().setOffline(true).build());
        Assert.assertTrue(repositoryPackage.isPresent());
        Assert.assertEquals(repositoryPackage.get().descriptor().toString(), "testorg/packA:0.1.0");
    }

    @Test(description = "Test getPackages")
    public void testGetPackages() {
        Map<String, List<String>> repositoryPackages = customPackageRepository.getPackages();
        Assert.assertEquals(repositoryPackages.keySet().size(), 1);
        Assert.assertTrue(repositoryPackages.containsKey("testorg"));
        Assert.assertEquals(repositoryPackages.get("testorg").size(), 2);
    }

    @Test(description = "Test package version existence in custom repository")
    public void testGetNonExistingPackageVersions1() {
        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("testorg"),
                        PackageName.from("packA"), PackageVersion.from("0.2.0")),
                PackageDependencyScope.DEFAULT);
        Collection<PackageVersion> versions = customPackageRepository.getPackageVersions(resolutionRequest,
                ResolutionOptions.builder().setOffline(true).build());
        Assert.assertEquals(versions.size(), 0);
        Assert.assertFalse(versions.contains(PackageVersion.from("0.2.0")));
    }

    @Test(description = "Test non-existing package modules in custom repository")
    public void testNonExistingPkgModules() {
        Collection<ModuleDescriptor> modules = customPackageRepository.getModules(
                PackageOrg.from("testorg"),
                PackageName.from("packC"), PackageVersion.from("0.1.0"));
        Assert.assertTrue(modules.isEmpty());
    }

    @Test(description = "Test package version existence in custom repository")
    public void testGetNonExistingPackageVersions2() {
        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("testorg"),
                        PackageName.from("pact1"), PackageVersion.from("0.2.0")),
                PackageDependencyScope.DEFAULT);
        Collection<PackageVersion> versions = customPackageRepository.getPackageVersions(resolutionRequest,
                ResolutionOptions.builder().setOffline(true).build());
        Assert.assertEquals(versions.size(), 0);
    }

    private static final Environment PROXY_ENV = new Environment() {
        @Override
        public <T> T getService(Class<T> clazz) {
            return null;
        }
    };

    private static final String PROXY_REPO_LOCATION =
            TEST_REPO.resolve("bala").toAbsolutePath().toString();

    private static class MockProxyMavenPackageRepository extends MavenPackageRepository {
        public MockProxyMavenPackageRepository(Environment environment, Path cacheDirectory,
                                               String distributionVersion, MavenResolverClient client,
                                               String repoLocation) {
            super(environment, cacheDirectory, distributionVersion, client, repoLocation, true);
        }
    }

    private MavenPackageRepository proxyRepo(MavenResolverClient client) {
        return new MockProxyMavenPackageRepository(PROXY_ENV, TEST_REPO, "1.2.3", client, PROXY_REPO_LOCATION);
    }

    private PackageResolutionResponse buildResolutionResponse(String org, String name, String version) {
        PackageResolutionResponse.Package pkg =
                new PackageResolutionResponse.Package(org, name, version, List.of());
        return PackageResolutionResponse.from(List.of(pkg), List.of());
    }


    @Test(description = "Proxy: getPackageVersions returns remote versions via getPackageVersionsInCentralProxy",
            groups = {"proxy"})
    public void testGetPackageVersionsProxyCentralSuccess() throws MavenResolverClientException {
        MavenResolverClient mockClient = Mockito.mock(MavenResolverClient.class);
        Mockito.when(mockClient.getPackageVersionsInCentralProxy(anyString(), anyString(), anyString(), any()))
                .thenReturn(List.of("0.1.0", "0.2.0"));

        MavenPackageRepository repo = proxyRepo(mockClient);
        ResolutionRequest request = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("testorg"), PackageName.from("packA")),
                PackageDependencyScope.DEFAULT);

        Collection<PackageVersion> versions = repo.getPackageVersions(request,
                ResolutionOptions.builder().setOffline(false).build());

        Assert.assertTrue(versions.contains(PackageVersion.from("0.1.0")));
        Assert.assertTrue(versions.contains(PackageVersion.from("0.2.0")));
    }

    @Test(description = "Proxy: getPackageVersions skips remote call when offline", groups = {"proxy"})
    public void testGetPackageVersionsProxyCentralOffline() throws MavenResolverClientException {
        MavenResolverClient mockClient = Mockito.mock(MavenResolverClient.class);
        MavenPackageRepository repo = proxyRepo(mockClient);

        ResolutionRequest request = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("testorg"), PackageName.from("packA"),
                        PackageVersion.from("0.1.0")),
                PackageDependencyScope.DEFAULT);

        Collection<PackageVersion> versions = repo.getPackageVersions(request,
                ResolutionOptions.builder().setOffline(true).build());

        verify(mockClient, never()).getPackageVersionsInCentralProxy(anyString(), anyString(), anyString(), any());
        Assert.assertTrue(versions.contains(PackageVersion.from("0.1.0")));
    }

    @Test(description = "Proxy: getPackageVersions falls back to FS cache when client throws",
            groups = {"proxy"})
    public void testGetPackageVersionsProxyCentralClientThrows() throws MavenResolverClientException {
        MavenResolverClient mockClient = Mockito.mock(MavenResolverClient.class);
        Mockito.when(mockClient.getPackageVersionsInCentralProxy(anyString(), anyString(), anyString(), any()))
                .thenThrow(new MavenResolverClientException("network error"));

        MavenPackageRepository repo = proxyRepo(mockClient);
        ResolutionRequest request = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("testorg"), PackageName.from("packA"),
                        PackageVersion.from("0.1.0")),
                PackageDependencyScope.DEFAULT);

        Collection<PackageVersion> versions = repo.getPackageVersions(request,
                ResolutionOptions.builder().setOffline(false).build());

        // No exception propagated; FS cache version is returned
        Assert.assertTrue(versions.contains(PackageVersion.from("0.1.0")));
    }

    @Test(description = "Proxy: getPackageNames resolves package via getPackageVersionsInCentralProxy",
            groups = {"proxy"})
    public void testGetPackageNamesProxyCentralSuccess() throws MavenResolverClientException {
        MavenResolverClient mockClient = Mockito.mock(MavenResolverClient.class);
        Mockito.when(mockClient.getPackageVersionsInCentralProxy(anyString(), anyString(), anyString(), any()))
                .thenReturn(List.of("0.1.0"));

        MavenPackageRepository repo = proxyRepo(mockClient);
        ImportModuleRequest importRequest = new ImportModuleRequest(PackageOrg.from("testorg"), "packA", List.of());

        Collection<ImportModuleResponse> responses = repo.getPackageNames(
                List.of(importRequest), ResolutionOptions.builder().setOffline(false).build());

        Assert.assertFalse(responses.isEmpty());
    }

    @Test(description = "Proxy: getPackageNames skips remote call when offline", groups = {"proxy"})
    public void testGetPackageNamesProxyCentralOffline() throws MavenResolverClientException {
        MavenResolverClient mockClient = Mockito.mock(MavenResolverClient.class);
        MavenPackageRepository repo = proxyRepo(mockClient);

        ImportModuleRequest importRequest = new ImportModuleRequest(PackageOrg.from("testorg"), "packA", List.of());

        repo.getPackageNames(List.of(importRequest), ResolutionOptions.builder().setOffline(true).build());

        verify(mockClient, never()).getPackageVersionsInCentralProxy(anyString(), anyString(), anyString(), any());
    }

    @Test(description = "Proxy: getPackageNames falls back to FS when client throws", groups = {"proxy"})
    public void testGetPackageNamesProxyCentralClientThrows() throws MavenResolverClientException {
        MavenResolverClient mockClient = Mockito.mock(MavenResolverClient.class);
        Mockito.when(mockClient.getPackageVersionsInCentralProxy(anyString(), anyString(), anyString(), any()))
                .thenThrow(new MavenResolverClientException("network error"));

        MavenPackageRepository repo = proxyRepo(mockClient);
        ImportModuleRequest importRequest = new ImportModuleRequest(
                PackageOrg.from("testorg"), "unknownPkg", List.of());

        // Must not throw; result may be empty (nothing in FS either)
        Collection<ImportModuleResponse> responses = repo.getPackageNames(
                List.of(importRequest), ResolutionOptions.builder().setOffline(false).build());

        Assert.assertNotNull(responses);
    }

    @Test(description = "Proxy: getPackageMetadata resolves package via central proxy", groups = {"proxy"})
    public void testGetPackageMetadataProxyCentralResolved() throws MavenResolverClientException {
        MavenResolverClient mockClient = Mockito.mock(MavenResolverClient.class);
        Mockito.when(mockClient.getPackageVersionsInCentralProxy(anyString(), anyString(), anyString(), any()))
                .thenReturn(List.of("0.1.0"));
        Mockito.when(mockClient.resolveDependency(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(buildResolutionResponse("testorg", "packA", "0.1.0"));

        MavenPackageRepository repo = proxyRepo(mockClient);
        ResolutionRequest request = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("testorg"), PackageName.from("packA")),
                PackageDependencyScope.DEFAULT);

        Collection<PackageMetadataResponse> responses = repo.getPackageMetadata(
                List.of(request), ResolutionOptions.builder().setOffline(false).build());

        Assert.assertFalse(responses.isEmpty());
        PackageMetadataResponse response = responses.iterator().next();
        Assert.assertEquals(response.resolutionStatus(), ResolutionResponse.ResolutionStatus.RESOLVED);
        Assert.assertEquals(response.resolvedDescriptor().version(), PackageVersion.from("0.1.0"));
    }

    @Test(description = "Proxy: getPackageMetadata skips remote when offline", groups = {"proxy"})
    public void testGetPackageMetadataProxyCentralOffline() throws MavenResolverClientException {
        MavenResolverClient mockClient = Mockito.mock(MavenResolverClient.class);
        MavenPackageRepository repo = proxyRepo(mockClient);

        ResolutionRequest request = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("testorg"), PackageName.from("packA"),
                        PackageVersion.from("0.1.0")),
                PackageDependencyScope.DEFAULT);

        Collection<PackageMetadataResponse> responses = repo.getPackageMetadata(
                List.of(request), ResolutionOptions.builder().setOffline(true).build());

        verify(mockClient, never()).getPackageVersionsInCentralProxy(anyString(), anyString(), anyString(), any());
        verify(mockClient, never()).resolveDependency(anyString(), anyString(), anyString(), anyString(), anyString());
        Assert.assertFalse(responses.isEmpty());
    }

    @Test(description = "Proxy: getPackageMetadata skips remote when version is HARD-locked and locally resolved",
            groups = {"proxy"})
    public void testGetPackageMetadataProxyCentralHardLockResolved() throws MavenResolverClientException {
        MavenResolverClient mockClient = Mockito.mock(MavenResolverClient.class);
        MavenPackageRepository repo = proxyRepo(mockClient);

        // packA:0.1.0 is already in FS cache; HARD locking should skip remote
        ResolutionRequest request = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("testorg"), PackageName.from("packA"),
                        PackageVersion.from("0.1.0")),
                PackageDependencyScope.DEFAULT,
                DependencyResolutionType.SOURCE,
                PackageLockingMode.HARD);

        Collection<PackageMetadataResponse> responses = repo.getPackageMetadata(
                List.of(request), ResolutionOptions.builder().setOffline(false).build());

        verify(mockClient, never()).getPackageVersionsInCentralProxy(anyString(), anyString(), anyString(), any());
        Assert.assertFalse(responses.isEmpty());
        PackageMetadataResponse response = responses.iterator().next();
        Assert.assertEquals(response.resolutionStatus(), ResolutionResponse.ResolutionStatus.RESOLVED);
    }

    @Test(description = "Proxy: getPackageMetadata returns UNRESOLVED when client throws and package absent from FS",
            groups = {"proxy"})
    public void testGetPackageMetadataProxyCentralClientThrows() throws MavenResolverClientException {
        MavenResolverClient mockClient = Mockito.mock(MavenResolverClient.class);
        Mockito.when(mockClient.getPackageVersionsInCentralProxy(anyString(), anyString(), anyString(), any()))
                .thenThrow(new MavenResolverClientException("network error"));

        MavenPackageRepository repo = proxyRepo(mockClient);
        ResolutionRequest request = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("testorg"), PackageName.from("unknownPkg")),
                PackageDependencyScope.DEFAULT);

        Collection<PackageMetadataResponse> responses = repo.getPackageMetadata(
                List.of(request), ResolutionOptions.builder().setOffline(false).build());

        Assert.assertFalse(responses.isEmpty());
        PackageMetadataResponse response = responses.iterator().next();
        Assert.assertEquals(response.resolutionStatus(), ResolutionResponse.ResolutionStatus.UNRESOLVED);
    }


    @Test(description = "Proxy: getDependencyGraph is built from resolveDependency response",
            groups = {"proxy"})
    public void testGetDependencyGraphProxyCentral() throws MavenResolverClientException {
        MavenResolverClient mockClient = Mockito.mock(MavenResolverClient.class);

        // Remote returns packB:0.1.0 which depends on packC:0.2.0
        Mockito.when(mockClient.getPackageVersionsInCentralProxy(anyString(), anyString(), anyString(), any()))
                .thenReturn(List.of("0.1.0"));

        PackageResolutionResponse.Dependency depC =
                new PackageResolutionResponse.Dependency("testorg", "packC", "0.2.0", List.of());
        PackageResolutionResponse.Package pkgB =
                new PackageResolutionResponse.Package("testorg", "packB", "0.1.0", List.of(depC));
        PackageResolutionResponse resolutionResp =
                PackageResolutionResponse.from(List.of(pkgB), List.of());

        Mockito.when(mockClient.resolveDependency(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(resolutionResp);

        MavenPackageRepository repo = proxyRepo(mockClient);
        // packB is NOT in FS cache – forces the remote response to win in merge
        ResolutionRequest request = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("testorg"), PackageName.from("packB")),
                PackageDependencyScope.DEFAULT);

        Collection<PackageMetadataResponse> responses = repo.getPackageMetadata(
                List.of(request), ResolutionOptions.builder().setOffline(false).build());

        Assert.assertFalse(responses.isEmpty());
        PackageMetadataResponse response = responses.iterator().next();
        Assert.assertEquals(response.resolutionStatus(), ResolutionResponse.ResolutionStatus.RESOLVED);
        Assert.assertTrue(response.dependencyGraph().isPresent());

        DependencyGraph<PackageDescriptor> graph = response.dependencyGraph().get();
        PackageDescriptor packC = PackageDescriptor.from(
                PackageOrg.from("testorg"), PackageName.from("packC"), PackageVersion.from("0.2.0"));
        Assert.assertTrue(graph.getNodes().contains(packC));

        verify(mockClient).resolveDependency(anyString(), anyString(), anyString(), anyString(), anyString());
    }
}
