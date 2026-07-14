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
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.internal.repositories.OCIPackageRepository;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
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

/**
 * Test OCI backed package repository.
 *
 * @since 2201.14.0
 */
public class OCIPackageRepositoryTests {

    private static class MockOciPackageRepository extends OCIPackageRepository {

        public MockOciPackageRepository(Environment environment, Path cacheDirectory, String distributionVersion) {
            super(environment, cacheDirectory, distributionVersion, null, true);
        }

        @Override
        public boolean getFromOci(PackageOrg org, PackageName name, PackageVersion version) {
            Path sourceFolderPath =
                    RESOURCE_DIRECTORY.resolve("custom-repo-resources/remote-custom-repo").resolve(name.toString());
            Path destinationFolderPath =
                    RESOURCE_DIRECTORY.resolve("custom-repo-resources/local-oci-repo/bala").resolve(org.toString())
                            .resolve(name.toString());

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
    private static final Path TEST_REPO = RESOURCE_DIRECTORY.resolve("custom-repo-resources/local-oci-repo");
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
                PackageOrg.from("ballina_test"), PackageName.from("oci1"),
                PackageVersion.from("0.1.0"));
        Assert.assertTrue(isPackageExists);
    }

    @Test(description = "Test non-existing package in OCI repository - online")
    public void testNonExistingPkg() {
        boolean isPackageExists = ociPackageRepository.isPackageExists(
                PackageOrg.from("ballina_test"),
                PackageName.from("oci3"), PackageVersion.from("0.1.0"));
        Assert.assertFalse(isPackageExists);
    }

    @Test(description = "Test package version existence in OCI repository")
    public void testGetPackageVersions() {
        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("ballina_test"),
                        PackageName.from("oci1"), PackageVersion.from("0.1.0")),
                PackageDependencyScope.DEFAULT);
        Collection<PackageVersion> versions = ociPackageRepository.getPackageVersions(resolutionRequest,
                ResolutionOptions.builder().setOffline(true).build());
        Assert.assertEquals(versions.size(), 1);
        Assert.assertTrue(versions.contains(PackageVersion.from("0.1.0")));
    }

    @Test(description = "Test getPackage (non existing package) in OCI repository - offline")
    public void testGetPackageNonExistingOffline() {
        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("ballina_test"),
                        PackageName.from("oci3"), PackageVersion.from("0.1.0")),
                PackageDependencyScope.DEFAULT);
        Optional<Package> repositoryPackage = ociPackageRepository.getPackage(resolutionRequest,
                ResolutionOptions.builder().setOffline(true).build());
        Assert.assertTrue(repositoryPackage.isEmpty());
    }

    @Test(description = "Test getPackage (non existing package) in OCI repository - online")
    public void testGetPackageNonExistingOnline() {
        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("ballina_test"),
                        PackageName.from("oci3"), PackageVersion.from("0.1.0")),
                PackageDependencyScope.DEFAULT);
        Optional<Package> repositoryPackage = ociPackageRepository.getPackage(resolutionRequest,
                ResolutionOptions.builder().setOffline(false).build());
        Assert.assertTrue(repositoryPackage.isEmpty());
    }

    @Test(description = "Test getPackage (existing package) in OCI repository - online")
    public void testGetPackageExistingOnline() {
        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("ballina_test"),
                        PackageName.from("oci1"), PackageVersion.from("0.1.0")),
                PackageDependencyScope.DEFAULT);
        Optional<Package> repositoryPackage = ociPackageRepository.getPackage(resolutionRequest,
                ResolutionOptions.builder().setOffline(false).build());
        Assert.assertFalse(repositoryPackage.isEmpty());
    }

    @Test(description = "Test getPackage (existing package) in OCI repository - offline")
    public void testGetPackageExistingOffline() {
        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("ballina_test"),
                        PackageName.from("oci1"), PackageVersion.from("0.1.0")),
                PackageDependencyScope.DEFAULT);
        Optional<Package> repositoryPackage = ociPackageRepository.getPackage(resolutionRequest,
                ResolutionOptions.builder().setOffline(true).build());
        Assert.assertTrue(repositoryPackage.isPresent());
        Assert.assertEquals(repositoryPackage.get().descriptor().toString(), "ballina_test/oci1:0.1.0");
    }

    @Test(description = "Test getPackages")
    public void testGetPackages() {
        Map<String, List<String>> repositoryPackages = ociPackageRepository.getPackages();
        Assert.assertEquals(repositoryPackages.keySet().size(), 1);
        Assert.assertTrue(repositoryPackages.containsKey("ballina_test"));
        Assert.assertEquals(repositoryPackages.get("ballina_test").size(), 2);
    }

    @Test(description = "Test non-existing package version in OCI repository")
    public void testGetNonExistingPackageVersions1() {
        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("ballina_test"),
                        PackageName.from("oci1"), PackageVersion.from("0.2.0")),
                PackageDependencyScope.DEFAULT);
        Collection<PackageVersion> versions = ociPackageRepository.getPackageVersions(resolutionRequest,
                ResolutionOptions.builder().setOffline(true).build());
        Assert.assertEquals(versions.size(), 0);
        Assert.assertFalse(versions.contains(PackageVersion.from("0.2.0")));
    }

    @Test(description = "Test non-existing package modules in OCI repository")
    public void testNonExistingPkgModules() {
        Collection<ModuleDescriptor> modules = ociPackageRepository.getModules(
                PackageOrg.from("ballina_test"),
                PackageName.from("oci3"), PackageVersion.from("0.1.0"));
        Assert.assertTrue(modules.isEmpty());
    }

    @Test(description = "Test non-existing package version of a non-existing package in OCI repository")
    public void testGetNonExistingPackageVersions2() {
        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("ballina_test"),
                        PackageName.from("oci4"), PackageVersion.from("0.2.0")),
                PackageDependencyScope.DEFAULT);
        Collection<PackageVersion> versions = ociPackageRepository.getPackageVersions(resolutionRequest,
                ResolutionOptions.builder().setOffline(true).build());
        Assert.assertEquals(versions.size(), 0);
    }
}
