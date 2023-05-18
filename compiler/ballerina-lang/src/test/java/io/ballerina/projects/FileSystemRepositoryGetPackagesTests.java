/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.projects.internal.repositories.AbstractPackageRepository;
import io.ballerina.projects.internal.repositories.FileSystemRepository;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Contains FileSystemRepository test cases.
 *
 * @since 2201.2.1
 */
public class FileSystemRepositoryGetPackagesTests {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src", "test", "resources", "repository-resources");
    private Map<String, List<String>> nonEmptyRepoPackages;
    private Map<String, List<String>> emptyRepoPackages;
    private Environment environment;

    @BeforeClass
    public void setupEnvironment() {
        environment = new Environment() {
            @Override
            public <T> T getService(Class<T> clazz) {
                return null;
            }
        };
    }

    @BeforeGroups("nonEmptyBalaDirGetPackage")
    public void setupNonEmptyRepository() {
        Path balaDirParent = RESOURCE_DIRECTORY.resolve("non-empty-repo");
        AbstractPackageRepository packageRepository = new FileSystemRepository(environment, balaDirParent);
        nonEmptyRepoPackages = packageRepository.getPackages();
    }

    @BeforeGroups("emptyBalaDirGetPackage")
    public void setupEmptyRepository() {
        Path balaDirParent = RESOURCE_DIRECTORY.resolve("empty-repo");
        AbstractPackageRepository packageRepository = new FileSystemRepository(environment, balaDirParent);
        emptyRepoPackages = packageRepository.getPackages();
    }

    @Test(description = "Test getPackages with correctly created packages", groups = {"nonEmptyBalaDirGetPackage"})
    public void testGetPackagesForCorrectPackages() {
        Assert.assertTrue(nonEmptyRepoPackages.containsKey("foo"));
        Assert.assertTrue(nonEmptyRepoPackages.get("foo").contains("package_a:1.1.0"));
        Assert.assertTrue(nonEmptyRepoPackages.get("foo").contains("package_b:6.7.8"));

        Assert.assertTrue(nonEmptyRepoPackages.containsKey("bar"));
        Assert.assertTrue(nonEmptyRepoPackages.get("bar").contains("package_i:1.1.1"));
    }

    @Test(description = "Test getPackages for an empty organization", groups = {"nonEmptyBalaDirGetPackage"})
    public void testGetPackagesForAnEmptyOrg() {
        // empty organization should be in the hashmap
        Assert.assertTrue(nonEmptyRepoPackages.containsKey("empty-org"));
    }

    @Test(description = "Test getPackages with a file in org list", groups = {"nonEmptyBalaDirGetPackage"})
    public void testGetPackagesWithAFileInOrgList() {
        // a file in the org list should be ignored
        Assert.assertFalse(nonEmptyRepoPackages.containsKey("file-in-org"));
    }

    @Test(description = "Test getPackages with a hidden org", groups = {"nonEmptyBalaDirGetPackage"})
    public void testGetPackagesWithAHiddenOrg() {
        // orgs cannot have "." since it is not alphanumeric. Hence ignored
        Assert.assertFalse(nonEmptyRepoPackages.containsKey(".hidden-org"));
    }

    @Test(description = "Test getPackages with a file in package directory list",
            groups = {"nonEmptyBalaDirGetPackage"})
    public void testGetPackagesWithAFileInPkgDirList() {
        // a file in the package dir list should be ignored
        for (String pkg : nonEmptyRepoPackages.get("foo")) {
            Assert.assertFalse(pkg.startsWith("package_g"));
        }
    }

    @Test(description = "Test getPackages with a hidden package directory", groups = {"nonEmptyBalaDirGetPackage"})
    public void testGetPackagesWithAHiddenPkgDir() {
        // packages are allowed to have "."
        Assert.assertTrue(nonEmptyRepoPackages.get("foo").contains(".package_h:1.1.1"));
    }

    @Test(description = "Test getPackages with a file as the package", groups = {"nonEmptyBalaDirGetPackage"})
    public void testGetPackagesWithAFileAsPackage() {
        // files are ignored as packages.
        Assert.assertFalse(nonEmptyRepoPackages.get("foo").contains("package_e:5.4.3"));
    }

    @Test(description = "Test getPackages with a hidden package", groups = {"nonEmptyBalaDirGetPackage"})
    public void testGetPackagesWithAHiddenPackage() {
        // packages cannot contain a leading ".". It goes against SemVer
        Assert.assertFalse(nonEmptyRepoPackages.get("foo").contains("package_f:.1.2.3"));
    }

    @Test(description = "Test getPackages with incorrect versions", groups = {"nonEmptyBalaDirGetPackage"})
    public void testGetPackagesWithIncorrectVersions() {
        // package names are ignored if not according to SemVer.
        Assert.assertFalse(nonEmptyRepoPackages.get("foo").contains("package_c:.2.3.0_temp"));
        Assert.assertFalse(nonEmptyRepoPackages.get("foo").contains("package_d:.5.5.5.5"));
    }

    @Test(description = "Test getPackages for an empty repo", groups = {"emptyBalaDirGetPackage"})
    public void testGetPackagesForAnEmptyBalaDir() {
        // the hashmap should be empty
        Assert.assertTrue(emptyRepoPackages.isEmpty());
    }
}
