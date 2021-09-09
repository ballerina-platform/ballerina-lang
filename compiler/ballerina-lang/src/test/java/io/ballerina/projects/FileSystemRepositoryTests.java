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
import io.ballerina.projects.internal.repositories.FileSystemRepository;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Test file system repository.
 *
 * @since 2.0.0
 */
public class FileSystemRepositoryTests {

    class MockFileSystemRepository extends FileSystemRepository {

        public MockFileSystemRepository(Environment environment, Path cacheDirectory) {
            super(environment, cacheDirectory);
        }

        @Override
        protected List<Path> getIncompatibleVer(List<Path> versions, PackageOrg org, PackageName name) {
            return new ArrayList<>();
        }
    }

    private static final Path RESOURCE_DIRECTORY = Paths.get("src", "test", "resources");
    private static final Path TEST_REPO = RESOURCE_DIRECTORY.resolve("test-repo");
    private FileSystemRepository fileSystemRepository;

    @BeforeSuite
    public void setup() {
        fileSystemRepository = new MockFileSystemRepository(new Environment() {
            @Override
            public <T> T getService(Class<T> clazz) {
                return null;
            }
        }, TEST_REPO);
    }

    @Test
    public void testGetPackageVersions() {
        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("hevayo"), PackageName.from("package_a"), null),
                PackageDependencyScope.DEFAULT);
        Collection<PackageVersion> versions = fileSystemRepository.getPackageVersions(resolutionRequest,
                ResolutionOptions.builder().setOffline(true).build());
        Assert.assertEquals(versions.size(), 2);
        Assert.assertTrue(versions.contains(PackageVersion.from("0.1.2")));
        Assert.assertTrue(versions.contains(PackageVersion.from("0.1.5")));
    }

}
