/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.projects.test;

import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageResolution;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.projects.environment.PackageLockingMode;
import io.ballerina.projects.environment.PackageMetadataResponse;
import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.environment.ResolutionResponse;
import io.ballerina.projects.util.ProjectConstants;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static io.ballerina.projects.util.ProjectConstants.BALLERINA_TOML;
import static io.ballerina.projects.util.ProjectConstants.DIST_CACHE_DIRECTORY;

public class CustomRepoResolutionTests extends BaseTest {
    ProjectEnvironmentBuilder projectEnvironmentBuilder;
    private static final Path TMP_RESOURCES_DIR = Path.of("build", "custom-repo-resources");
    private static final Path CUSTOM_USER_HOME = Path.of("build", "user-home");
    private static final Path CUSTOM_FS_REPO_PATH = CUSTOM_USER_HOME.resolve("repositories/customRepo");

    @BeforeTest
    public void setup() throws IOException {
        Path resourceDir = Paths.get("src/test/resources/custom-repo-resources")
                .toAbsolutePath();
        FileUtils.copyDirectory(resourceDir.toFile(), TMP_RESOURCES_DIR.toFile());
        Path mavenRepoPath = CUSTOM_USER_HOME.resolve("repositories/maven-repo");
        Path settingsTomlPath = TMP_RESOURCES_DIR.resolve("Settings.toml");
        Files.copy(settingsTomlPath, CUSTOM_USER_HOME.resolve("Settings.toml"),
                StandardCopyOption.REPLACE_EXISTING);

        Environment environment = EnvironmentBuilder.getBuilder().setUserHome(CUSTOM_USER_HOME).build();
        this.projectEnvironmentBuilder = ProjectEnvironmentBuilder.getBuilder(environment);

        // Cache multiple versions of dep1 in the custom repo
        Path dep1Path = TMP_RESOURCES_DIR.resolve("dep1");
        BCompileUtil.compileAndCacheBala(dep1Path, mavenRepoPath, projectEnvironmentBuilder);

        Files.writeString(dep1Path.resolve(BALLERINA_TOML), Files.readString(dep1Path.resolve(BALLERINA_TOML))
                .replace("0.1.0", "0.1.1"));
        BCompileUtil.compileAndCacheBala(dep1Path, mavenRepoPath, projectEnvironmentBuilder);

        Files.writeString(dep1Path.resolve(BALLERINA_TOML), Files.readString(dep1Path.resolve(BALLERINA_TOML))
                .replace("0.1.1", "0.2.0"));
        BCompileUtil.compileAndCacheBala(dep1Path, mavenRepoPath, projectEnvironmentBuilder);

        // Cache multiple versions of dep2
        Path dep2Path = TMP_RESOURCES_DIR.resolve("dep2");
        BCompileUtil.compileAndCacheBala(dep2Path, mavenRepoPath, projectEnvironmentBuilder);

        Files.writeString(dep2Path.resolve(BALLERINA_TOML), Files.readString(dep2Path.resolve(BALLERINA_TOML))
                .replace("1.0.0", "1.0.1"));
        BCompileUtil.compileAndCacheBala(dep2Path, mavenRepoPath, projectEnvironmentBuilder);

        Files.writeString(dep2Path.resolve(BALLERINA_TOML), Files.readString(dep2Path.resolve(BALLERINA_TOML))
                .replace("1.0.1", "1.1.0"));
        BCompileUtil.compileAndCacheBala(dep2Path, mavenRepoPath, projectEnvironmentBuilder);

        Files.writeString(dep2Path.resolve(BALLERINA_TOML), Files.readString(dep2Path.resolve(BALLERINA_TOML))
                .replace("1.1.0", "2.0.0"));
        BCompileUtil.compileAndCacheBala(dep2Path, mavenRepoPath, projectEnvironmentBuilder);

        // Cache multiple versions of dep3
        Path dep3Path = TMP_RESOURCES_DIR.resolve("test.dep3");
        BCompileUtil.compileAndCacheBala(dep3Path, mavenRepoPath, projectEnvironmentBuilder);

        Files.writeString(dep3Path.resolve(BALLERINA_TOML), Files.readString(dep3Path.resolve(BALLERINA_TOML))
                .replace("1.0.0", "1.0.1"));
        BCompileUtil.compileAndCacheBala(dep3Path, mavenRepoPath, projectEnvironmentBuilder);

        Files.writeString(dep3Path.resolve(BALLERINA_TOML), Files.readString(dep3Path.resolve(BALLERINA_TOML))
                .replace("1.0.1", "1.1.0"));
        BCompileUtil.compileAndCacheBala(dep3Path, mavenRepoPath, projectEnvironmentBuilder);

        Files.writeString(dep3Path.resolve(BALLERINA_TOML), Files.readString(dep3Path.resolve(BALLERINA_TOML))
                .replace("1.1.0", "2.0.0"));
        BCompileUtil.compileAndCacheBala(dep3Path, mavenRepoPath, projectEnvironmentBuilder);

        Path centralRepoPath = CUSTOM_USER_HOME.resolve("repositories/central.ballerina.io");

        // Cache dependencies
        Path fooPath = TMP_RESOURCES_DIR.resolve("pkg-customfs-foo");
        BCompileUtil.compileAndCacheBala(fooPath, CUSTOM_FS_REPO_PATH, projectEnvironmentBuilder);

        Files.writeString(dep3Path.resolve(BALLERINA_TOML), Files.readString(dep3Path.resolve(BALLERINA_TOML))
                .replace("1.0.0", "1.3.0"));
        BCompileUtil.compileAndCacheBala(fooPath, centralRepoPath, projectEnvironmentBuilder);

        Path barPath = TMP_RESOURCES_DIR.resolve("pkg-customfs-bar");
        BCompileUtil.compileAndCacheBala(barPath, CUSTOM_FS_REPO_PATH, projectEnvironmentBuilder);

        Files.writeString(dep3Path.resolve(BALLERINA_TOML), Files.readString(dep3Path.resolve(BALLERINA_TOML))
                .replace("1.0.0", "1.1.0"));
        BCompileUtil.compileAndCacheBala(barPath, centralRepoPath, projectEnvironmentBuilder);

        Path bazzPath = TMP_RESOURCES_DIR.resolve("pkg-customfs-bazz");
        BCompileUtil.compileAndCacheBala(bazzPath, centralRepoPath, projectEnvironmentBuilder);

        Path pkgAPath = TMP_RESOURCES_DIR.resolve("packageA");
        BCompileUtil.compileAndCacheBala(pkgAPath, Paths.get("build").resolve(DIST_CACHE_DIRECTORY),
                projectEnvironmentBuilder);
    }

    @BeforeMethod
    public void cleanProject() throws IOException {
        Path projectPath = TMP_RESOURCES_DIR.resolve("myproject");
        Files.deleteIfExists(projectPath.resolve("Dependencies.toml"));
        if (Files.exists(projectPath.resolve("target"))) {
            FileUtils.deleteDirectory(projectPath.resolve("target").toFile());
        }
    }

    @Test
    public void testCustomRepoResolutionNewProject() {
        Path projectPath = TMP_RESOURCES_DIR.resolve("myproject");
        BuildProject buildProject = TestUtils.loadBuildProject(projectEnvironmentBuilder, projectPath);
        PackageResolution resolution = buildProject.currentPackage().getResolution();
        Assert.assertEquals(resolution.diagnosticResult().diagnosticCount(), 0);
        ResolvedPackageDependency root = resolution.dependencyGraph().getRoot();
        for (ResolvedPackageDependency packageDependency : resolution.dependencyGraph().getDirectDependencies(root)) {
            if (packageDependency.packageInstance().descriptor().name().toString().equals("dep1")) {
                Assert.assertEquals(packageDependency.packageInstance().descriptor().version().toString(), "0.2.0");
            } else {
                Assert.assertEquals(packageDependency.packageInstance().descriptor().version().toString(), "2.0.0");
            }
        }
    }

    @Test
    public void testCustomRepoResolutionExistingProject() throws IOException {
        Path projectPath = TMP_RESOURCES_DIR.resolve("myproject");
        Files.copy(TMP_RESOURCES_DIR.resolve("dependency-tomls/Dependencies.toml"),
                projectPath.resolve("Dependencies.toml"), StandardCopyOption.REPLACE_EXISTING);

        BuildProject buildProject = TestUtils.loadBuildProject(projectEnvironmentBuilder, projectPath);
        PackageResolution resolution = buildProject.currentPackage().getResolution();
        Assert.assertEquals(resolution.diagnosticResult().diagnosticCount(), 0);
        ResolvedPackageDependency root = resolution.dependencyGraph().getRoot();
        for (ResolvedPackageDependency packageDependency : resolution.dependencyGraph().getDirectDependencies(root)) {
            if (packageDependency.packageInstance().descriptor().name().toString().equals("dep1")) {
                Assert.assertEquals(packageDependency.packageInstance().descriptor().version().toString(), "0.1.1");
            } else {
                Assert.assertEquals(packageDependency.packageInstance().descriptor().version().toString(), "1.0.1");
            }
        }
    }

    @Test
    public void testCustomRepoResolutionExistingProjectSoftLock() throws IOException {
        Path projectPath = TMP_RESOURCES_DIR.resolve("myproject");
        Files.copy(TMP_RESOURCES_DIR.resolve("dependency-tomls/Dependencies.toml"),
                projectPath.resolve("Dependencies.toml"), StandardCopyOption.REPLACE_EXISTING);

        BuildOptions buildOptions = BuildOptions.builder().setLockingMode(PackageLockingMode.SOFT).build();
        BuildProject buildProject = TestUtils.loadBuildProject(projectEnvironmentBuilder, projectPath, buildOptions);
        PackageResolution resolution = buildProject.currentPackage().getResolution();
        Assert.assertEquals(resolution.diagnosticResult().diagnosticCount(), 0);
        ResolvedPackageDependency root = resolution.dependencyGraph().getRoot();
        for (ResolvedPackageDependency packageDependency : resolution.dependencyGraph().getDirectDependencies(root)) {
            if (packageDependency.packageInstance().descriptor().name().toString().equals("dep1")) {
                Assert.assertEquals(packageDependency.packageInstance().descriptor().version().toString(), "0.1.1");
            } else {
                Assert.assertEquals(packageDependency.packageInstance().descriptor().version().toString(), "1.1.0");
            }
        }
    }

    @Test
    public void testCustomRepoResolutionExistingProjectSticky() throws IOException {
        Path projectPath = TMP_RESOURCES_DIR.resolve("myproject");
        Files.copy(TMP_RESOURCES_DIR.resolve("dependency-tomls/Dependencies.toml"),
                projectPath.resolve("Dependencies.toml"), StandardCopyOption.REPLACE_EXISTING);

        BuildOptions buildOptions = BuildOptions.builder().setSticky(true).build();
        BuildProject buildProject = TestUtils.loadBuildProject(projectEnvironmentBuilder, projectPath, buildOptions);
        PackageResolution resolution = buildProject.currentPackage().getResolution();
        Assert.assertEquals(resolution.diagnosticResult().diagnosticCount(), 0);
        ResolvedPackageDependency root = resolution.dependencyGraph().getRoot();
        for (ResolvedPackageDependency packageDependency : resolution.dependencyGraph().getDirectDependencies(root)) {
            if (packageDependency.packageInstance().descriptor().name().toString().equals("dep1")) {
                Assert.assertEquals(packageDependency.packageInstance().descriptor().version().toString(), "0.1.0");
            } else {
                Assert.assertEquals(packageDependency.packageInstance().descriptor().version().toString(), "1.0.0");
            }
        }
    }

    @Test
    public void testCustomRepoResolutionExistingProjectHardLock() throws IOException {
        Path projectPath = TMP_RESOURCES_DIR.resolve("myproject");
        Files.copy(TMP_RESOURCES_DIR.resolve("dependency-tomls/Dependencies.toml"),
                projectPath.resolve("Dependencies.toml"), StandardCopyOption.REPLACE_EXISTING);

        BuildOptions buildOptions = BuildOptions.builder().setLockingMode(PackageLockingMode.HARD).build();
        BuildProject buildProject = TestUtils.loadBuildProject(projectEnvironmentBuilder, projectPath, buildOptions);
        PackageResolution resolution = buildProject.currentPackage().getResolution();
        Assert.assertEquals(resolution.diagnosticResult().diagnosticCount(), 0);
        ResolvedPackageDependency root = resolution.dependencyGraph().getRoot();
        for (ResolvedPackageDependency packageDependency : resolution.dependencyGraph().getDirectDependencies(root)) {
            if (packageDependency.packageInstance().descriptor().name().toString().equals("dep1")) {
                Assert.assertEquals(packageDependency.packageInstance().descriptor().version().toString(), "0.1.0");
            } else {
                Assert.assertEquals(packageDependency.packageInstance().descriptor().version().toString(), "1.0.0");
            }
        }
    }

    @Test
    public void testCustomRepoResolutionExistingProjectWithMinVersion() throws IOException {
        Path projectPath = TMP_RESOURCES_DIR.resolve("myproject");
        Files.copy(TMP_RESOURCES_DIR.resolve("dependency-tomls/Dependencies.toml"),
                projectPath.resolve("Dependencies.toml"), StandardCopyOption.REPLACE_EXISTING);

        Files.writeString(projectPath.resolve(BALLERINA_TOML), Files.readString(projectPath.resolve(BALLERINA_TOML))
                + "[[dependency]]\n" +
                "org=\"testorg\"\n" +
                "name=\"dep1\"\n" +
                "version=\"0.2.0\"\n");

        BuildProject buildProject = TestUtils.loadBuildProject(projectEnvironmentBuilder, projectPath);
        PackageResolution resolution = buildProject.currentPackage().getResolution();
        Assert.assertEquals(resolution.diagnosticResult().diagnosticCount(), 0);
        ResolvedPackageDependency root = resolution.dependencyGraph().getRoot();
        for (ResolvedPackageDependency packageDependency : resolution.dependencyGraph().getDirectDependencies(root)) {
            if (packageDependency.packageInstance().descriptor().name().toString().equals("dep1")) {
                Assert.assertEquals(packageDependency.packageInstance().descriptor().version().toString(), "0.2.0");
            } else {
                Assert.assertEquals(packageDependency.packageInstance().descriptor().version().toString(), "1.0.1");
            }
        }
    }

    @Test
    public void testCustomRepoResolutionHierarchicalDependency() {
        Path projectPath = TMP_RESOURCES_DIR.resolve("myproject2");
        BuildProject buildProject = TestUtils.loadBuildProject(projectEnvironmentBuilder, projectPath);
        PackageResolution resolution = buildProject.currentPackage().getResolution();
        Assert.assertEquals(resolution.diagnosticResult().diagnosticCount(), 0);
        ResolvedPackageDependency root = resolution.dependencyGraph().getRoot();
        ResolvedPackageDependency dependency = resolution.dependencyGraph().getDirectDependencies(root)
                .iterator().next();
        Assert.assertEquals(dependency.packageInstance().descriptor().version().toString(), "2.0.0");
    }

    @Test
    public void testCustomRepoResolutionHierarchicalDependencyMediumLock() throws IOException {
        Path projectPath = TMP_RESOURCES_DIR.resolve("myproject2");
        Files.copy(TMP_RESOURCES_DIR.resolve("dependency-tomls/Hierarchical-dependencies.toml"),
                projectPath.resolve("Dependencies.toml"), StandardCopyOption.REPLACE_EXISTING);

        BuildProject buildProject = TestUtils.loadBuildProject(projectEnvironmentBuilder, projectPath);
        PackageResolution resolution = buildProject.currentPackage().getResolution();
        Assert.assertEquals(resolution.diagnosticResult().diagnosticCount(), 0);
        ResolvedPackageDependency root = resolution.dependencyGraph().getRoot();
        ResolvedPackageDependency dependency = resolution.dependencyGraph().getDirectDependencies(root)
                .iterator().next();
        Assert.assertEquals(dependency.packageInstance().descriptor().version().toString(), "1.0.1");
    }

    @Test
    public void testCustomRepoResolutionHierarchicalDependencyHardLock() throws IOException {
        Path projectPath = TMP_RESOURCES_DIR.resolve("myproject2");
        Files.copy(TMP_RESOURCES_DIR.resolve("dependency-tomls/Hierarchical-dependencies.toml"),
                projectPath.resolve("Dependencies.toml"), StandardCopyOption.REPLACE_EXISTING);

        BuildOptions buildOptions = BuildOptions.builder().setLockingMode(PackageLockingMode.HARD).build();
        BuildProject buildProject = TestUtils.loadBuildProject(projectEnvironmentBuilder, projectPath, buildOptions);
        PackageResolution resolution = buildProject.currentPackage().getResolution();
        Assert.assertEquals(resolution.diagnosticResult().diagnosticCount(), 0);
        ResolvedPackageDependency root = resolution.dependencyGraph().getRoot();
        ResolvedPackageDependency dependency = resolution.dependencyGraph().getDirectDependencies(root)
                .iterator().next();
        Assert.assertEquals(dependency.packageInstance().descriptor().version().toString(), "1.0.0");
    }

    @Test
    public void testCustomRepoResolutionHierarchicalDependencySoftLock() throws IOException {
        Path projectPath = TMP_RESOURCES_DIR.resolve("myproject2");
        Files.copy(TMP_RESOURCES_DIR.resolve("dependency-tomls/Hierarchical-dependencies.toml"),
                projectPath.resolve("Dependencies.toml"), StandardCopyOption.REPLACE_EXISTING);
        BuildOptions buildOptions = BuildOptions.builder().setLockingMode(PackageLockingMode.SOFT).build();

        BuildProject buildProject = TestUtils.loadBuildProject(projectEnvironmentBuilder, projectPath, buildOptions);
        PackageResolution resolution = buildProject.currentPackage().getResolution();
        Assert.assertEquals(resolution.diagnosticResult().diagnosticCount(), 0);
        ResolvedPackageDependency root = resolution.dependencyGraph().getRoot();
        ResolvedPackageDependency dependency = resolution.dependencyGraph().getDirectDependencies(root)
                .iterator().next();
        Assert.assertEquals(dependency.packageInstance().descriptor().version().toString(), "1.1.0");
    }

    @Test
    public void testCustomFSRepository() throws IOException {
        Path settingsToml = CUSTOM_USER_HOME.resolve(ProjectConstants.SETTINGS_FILE_NAME);
        // Copy Settings.toml
        Files.copy(TMP_RESOURCES_DIR.resolve("Settings-FS.toml"),
                settingsToml,
                StandardCopyOption.REPLACE_EXISTING);
        String replaced = Files.readString(settingsToml).replace("<custom-repo-path>",
                CUSTOM_FS_REPO_PATH.toString());
        Files.writeString(settingsToml, replaced);

        PackageDescriptor packageDescriptor1 = PackageDescriptor.from(PackageOrg.from("testorg"),
                PackageName.from("foo"));
        PackageDescriptor packageDescriptor2 = PackageDescriptor.from(PackageOrg.from("testorg"),
                PackageName.from("bar.winery"));
        PackageDescriptor packageDescriptor3 = PackageDescriptor.from(PackageOrg.from("testorg2"),
                PackageName.from("bazz"));
        PackageDescriptor packageDescriptor4 = PackageDescriptor.from(PackageOrg.from("testorg"),
                PackageName.from("pkgA"));
        List<ResolutionRequest> requests = new ArrayList<>();
        requests.add(ResolutionRequest.from(packageDescriptor1));
        requests.add(ResolutionRequest.from(packageDescriptor2));
        requests.add(ResolutionRequest.from(packageDescriptor3));
        requests.add(ResolutionRequest.from(packageDescriptor4));

        Environment environment = EnvironmentBuilder.getBuilder().setUserHome(CUSTOM_USER_HOME).build();
        PackageResolver packageResolver = environment.getService(PackageResolver.class);
        Collection<PackageMetadataResponse> packageMetadataResponses = packageResolver
                .resolvePackageMetadata(requests, ResolutionOptions.builder().build());
        Assert.assertEquals(packageMetadataResponses.size(), 4);

        // Both the dependencies should be resolved successfully since the custom repo contains both the dependencies
        Assert.assertTrue(packageMetadataResponses.stream().noneMatch(response ->
                response.resolutionStatus() == ResolutionResponse.ResolutionStatus.UNRESOLVED));
        packageMetadataResponses.forEach(response -> {
            Assert.assertSame(response.resolutionStatus(), ResolutionResponse.ResolutionStatus.RESOLVED);
            Assert.assertTrue("testorg/foo:1.0.0".equals(response.resolvedDescriptor().toString()) ||
                    "testorg/bar.winery:1.0.0".equals(response.resolvedDescriptor().toString()) ||
                    "testorg2/bazz:1.0.0".equals(response.resolvedDescriptor().toString()) ||
                    "testorg/pkgA:1.0.0".equals(response.resolvedDescriptor().toString()));
        });
    }
}
