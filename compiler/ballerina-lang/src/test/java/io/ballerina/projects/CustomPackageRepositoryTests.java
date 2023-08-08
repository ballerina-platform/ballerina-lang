/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.projects;

import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.internal.repositories.CustomPackageRepository;
import io.ballerina.projects.internal.repositories.CustomRepositoryHelper;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Test file system repository.
 *
 * @since 2.0.0
 */
public class CustomPackageRepositoryTests {

    class MockCustomPackageRepository extends CustomPackageRepository implements CustomRepositoryHelper {

        public MockCustomPackageRepository(Environment environment, Path cacheDirectory, String distributionVersion,
                                            String repoId) {
            super(environment, cacheDirectory, distributionVersion, repoId);
        }


        @Override
        public boolean getPackageFromRemoteRepo(String org, String name, String version) {
            Path sourceFolderPath = RESOURCE_DIRECTORY.resolve("custom-repo-resources").resolve("remote-custom-repo").resolve(name);
            Path destinationFolderPath = RESOURCE_DIRECTORY.resolve("custom-repo-resources").resolve("local-custom-repo")
                    .resolve("bala").resolve(org).resolve(name);

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
                        Files.copy(file, destinationFolderPath.resolve(sourceFolderPath.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                        return FileVisitResult.CONTINUE;
                    }
                });

            } catch (IOException e) {
                return false;
            }
            return true;
        }
    }

    private static final Path RESOURCE_DIRECTORY = Paths.get("src", "test", "resources");
    private static final Path TEST_REPO = RESOURCE_DIRECTORY.resolve("custom-repo-resources").resolve("local-custom-repo");
    private CustomPackageRepository customPackageRepository;

    @BeforeSuite
    public void setup() {
        customPackageRepository = new MockCustomPackageRepository(new Environment() {
            @Override
            public <T> T getService(Class<T> clazz) {
                return null;
            }
        }, TEST_REPO, "1.2.3", "local-custom-repo");
    }

    @Test(description = "Test package existence in custom repository")
    public void testIsPackageExist() throws IOException {
        boolean isPackageExists = customPackageRepository.isPackageExists(PackageOrg.from("luheerathan"), PackageName.from("pact"), PackageVersion.from("0.1.0"));
        Assert.assertTrue(isPackageExists);
        deleteRemotePackage();
    }

    @Test(description = "Test non-existing package in custom repository")
    public void testNonExistingPkg()  {
        boolean isPackageExists = customPackageRepository.isPackageExists(PackageOrg.from("luheerathan"), PackageName.from("pact1"), PackageVersion.from("0.1.0"));
        Assert.assertTrue(!isPackageExists);
    }

    @Test(description = "Test package version existence in custom repository")
    public void testGetPackageVersions() throws IOException {
        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("luheerathan"), PackageName.from("pact"), PackageVersion.from("0.1.0")),
                PackageDependencyScope.DEFAULT);
        Collection<PackageVersion> versions = customPackageRepository.getPackageVersions(resolutionRequest,
                ResolutionOptions.builder().setOffline(true).build());
        Assert.assertEquals(versions.size(), 1);
        Assert.assertTrue(versions.contains(PackageVersion.from("0.1.0")));
        deleteRemotePackage();
    }

    @Test(description = "Test getPackage (existing package) in custom repository")
    public void testGetPackage() throws IOException {
        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("luheerathan"), PackageName.from("pact"), PackageVersion.from("0.1.0")),
                PackageDependencyScope.DEFAULT);
        Optional<Package> package_ = customPackageRepository.getPackage(resolutionRequest,
                ResolutionOptions.builder().setOffline(true).build());
        Assert.assertFalse(package_.isEmpty());
        Assert.assertEquals(PackageOrg.from("luheerathan"), package_.get().packageOrg());
        deleteRemotePackage();
    }

    @Test(description = "Test getPackage (non existing package) in custom repository")
    public void testGetPackageNonExisting() throws IOException {
        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("luheerathan"), PackageName.from("pact1"), PackageVersion.from("0.1.0")),
                PackageDependencyScope.DEFAULT);
        Optional<Package> package_ = customPackageRepository.getPackage(resolutionRequest,
                ResolutionOptions.builder().setOffline(true).build());
        Assert.assertTrue(package_.isEmpty());
        deleteRemotePackage();
    }

    @Test(description = "Test getPackages")
    public void testGetPackages() throws IOException {
        Map<String, List<String>> package_ = customPackageRepository.getPackages();
        Assert.assertEquals(package_.keySet().size(), 1);
        Assert.assertTrue(package_.containsKey("luheerathan"));
        Assert.assertEquals(package_.get("luheerathan").size(), 2);
        deleteRemotePackage();
    }

    @Test(description = "Test package version existence in custom repository")
    public void testGetNonExistingPackageVersions1() throws IOException {
        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("luheerathan"), PackageName.from("pact"), PackageVersion.from("0.2.0")),
                PackageDependencyScope.DEFAULT);
        Collection<PackageVersion> versions = customPackageRepository.getPackageVersions(resolutionRequest,
                ResolutionOptions.builder().setOffline(true).build());
        Assert.assertEquals(versions.size(), 0);
        deleteRemotePackage();
    }

    @Test(description = "Test non-existing package modules in custom repository")
    public void testExistingPkgModules() throws IOException {
        Collection<ModuleDescriptor> modules = customPackageRepository.getModules(PackageOrg.from("luheerathan"), PackageName.from("pact1"), PackageVersion.from("0.1.0"));
        Assert.assertTrue(modules.isEmpty());
        deleteRemotePackage();
    }


    @Test(description = "Test existing package modules in custom repository")
    public void testNonExistingPkgModules() throws IOException {
        Collection<ModuleDescriptor> modules = customPackageRepository.getModules(PackageOrg.from("luheerathan"), PackageName.from("pact"), PackageVersion.from("0.1.0"));
        Assert.assertFalse(modules.isEmpty());
        deleteRemotePackage();
    }

    @Test(description = "Test package version existence in custom repository")
    public void testGetNonExistingPackageVersions2() throws IOException {
        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("luheerathan"), PackageName.from("pact1"), PackageVersion.from("0.2.0")),
                PackageDependencyScope.DEFAULT);
        Collection<PackageVersion> versions = customPackageRepository.getPackageVersions(resolutionRequest,
                ResolutionOptions.builder().setOffline(true).build());
        Assert.assertEquals(versions.size(), 0);
        deleteRemotePackage();
    }


    private static void deleteRemotePackage() throws IOException {
        Path destinationFolderPath = RESOURCE_DIRECTORY.resolve("custom-repo-resources").resolve("local-custom-repo")
                .resolve("bala").resolve("luheerathan").resolve("pact");
        if (Files.exists(destinationFolderPath)) {
            Files.walk(destinationFolderPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }
}
