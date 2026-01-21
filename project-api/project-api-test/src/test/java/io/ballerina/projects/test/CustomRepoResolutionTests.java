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
import io.ballerina.projects.PackageResolution;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.projects.environment.PackageLockingMode;
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

import static io.ballerina.projects.util.ProjectConstants.BALLERINA_TOML;


public class CustomRepoResolutionTests extends BaseTest {
    ProjectEnvironmentBuilder projectEnvironmentBuilder;
    private static final Path TMP_RESOURCES_DIR = Path.of("build", "custom-repo-resources");

    @BeforeTest
    public void setup() throws IOException {
        Path resourceDir = Paths.get("src/test/resources/custom-repo-resources")
                .toAbsolutePath();
        FileUtils.copyDirectory(resourceDir.toFile(), TMP_RESOURCES_DIR.toFile());
        Path customUserHome = Path.of("build", "user-home");
        Path mavenRepoPath = customUserHome.resolve("repositories/maven-repo");
        Path settingsTomlPath = TMP_RESOURCES_DIR.resolve("Settings.toml");
        Files.copy(settingsTomlPath, customUserHome.resolve("Settings.toml"), StandardCopyOption.REPLACE_EXISTING);

        Environment environment = EnvironmentBuilder.getBuilder().setUserHome(customUserHome).build();
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
    public void testCustomRepoResolutionNewProject() throws IOException {
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
}
