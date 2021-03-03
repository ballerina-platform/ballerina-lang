/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects.test;

import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.repos.TempDirCompilationCache;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Contains cases to test dependency file path resolution logic.
 *
 * @since 2.0.0
 */
public class DependencyFilePathTests extends BaseTest {
    private static final Path RESOLUTION_PROJECTS_DIR = Paths.get(
            "src/test/resources/projects_for_resolution_tests").toAbsolutePath();
    private static final Path DEPENDENCY_FILEPATH_PROJECTS_DIR = Paths.get(
            "src/test/resources/projects_for_dependency_file_path").toAbsolutePath();

    @BeforeTest
    public void setup() {
        // Compile and cache dependency for dependency path tests
        BCompileUtil.compileAndCacheBala("projects_for_resolution_tests/package_a");
        BCompileUtil.compileAndCacheBala("projects_for_dependency_file_path/package_a.b");
    }

    @Test
    public void testGetBuildProjectDependencyFilePath() {
        // package_a --> package_b
        Path projectDirPath = RESOLUTION_PROJECTS_DIR.resolve("package_a");
        Project project = ProjectLoader.loadProject(projectDirPath);
        Path dependencyFilePath = project.dependencyFilePath("samjs", "package_b", "main.bal");
        Path expectedPath = getBalaPath("samjs", "package_b", "0.1.0")
                .resolve("modules/package_b/main.bal");
        Assert.assertEquals(dependencyFilePath.toString(), expectedPath.toString());

        dependencyFilePath = project.dependencyFilePath("samjs", "package_b.mod_b1", "mod1.bal");
        expectedPath = getBalaPath("samjs", "package_b", "0.1.0")
                .resolve("modules/package_b.mod_b1/mod1.bal");
        Assert.assertEquals(dependencyFilePath.toString(), expectedPath.toString());
    }

    @Test
    public void testGetBuildProjectTransitiveDependencyFilePath() {
        // package_a --> package_b --> package_c
        Path projectDirPath = RESOLUTION_PROJECTS_DIR.resolve("package_a");
        BuildProject buildProject = BuildProject.load(projectDirPath);

        Path dependencyFilePath = buildProject.dependencyFilePath("samjs", "package_c.mod_c2", "mod2.bal");
        Path expectedPath = getBalaPath("samjs", "package_c", "0.1.0")
                .resolve("modules/package_c.mod_c2/mod2.bal");
        Assert.assertEquals(dependencyFilePath.toString(), expectedPath.toString());

        dependencyFilePath = buildProject.dependencyFilePath("samjs", "package_c", "main.bal");
        expectedPath = getBalaPath("samjs", "package_c", "0.1.0")
                .resolve("modules/package_c/main.bal");
        Assert.assertEquals(dependencyFilePath.toString(), expectedPath.toString());
    }

    @Test
    public void testGetBalaProjectDependencyFilePath() {
        // package_a --> package_b --> package_c
        Path balaPath = getBalaPath("samjs", "package_a", "0.1.0");
        ProjectEnvironmentBuilder defaultBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
        defaultBuilder.addCompilationCacheFactory(TempDirCompilationCache::from);
        Project balaProject = ProjectLoader.loadProject(balaPath, defaultBuilder);

        Path dependencyFilePath = balaProject.dependencyFilePath("samjs", "package_c.mod_c2", "mod2.bal");
        Path expectedPath = getBalaPath("samjs", "package_c", "0.1.0")
                .resolve("modules/package_c.mod_c2/mod2.bal");
        Assert.assertEquals(dependencyFilePath.toString(), expectedPath.toString());

        dependencyFilePath = balaProject.dependencyFilePath("samjs", "package_c", "main.bal");
        expectedPath = getBalaPath("samjs", "package_c", "0.1.0")
                .resolve("modules/package_c/main.bal");
        Assert.assertEquals(dependencyFilePath.toString(), expectedPath.toString());
    }

    @Test
    public void testGetHierarchicalPackageDependencyFilePath() {
        // package_x -> package_a.b
        Path projectDirPath = DEPENDENCY_FILEPATH_PROJECTS_DIR.resolve("package_x");
        Project project = ProjectLoader.loadProject(projectDirPath);
        Path dependencyFilePath = project.dependencyFilePath("samjs", "package_a.b", "main.bal");

        Path expectedPath = getBalaPath("samjs", "package_a.b", "0.1.0")
                .resolve("modules/package_a.b/main.bal");
        Assert.assertEquals(dependencyFilePath.toString(), expectedPath.toString());

        dependencyFilePath = project.dependencyFilePath("samjs", "package_a.b.mod_ab1", "mod1.bal");
        expectedPath = getBalaPath("samjs", "package_a.b", "0.1.0")
                .resolve("modules/package_a.b.mod_ab1/mod1.bal");
        Assert.assertEquals(dependencyFilePath.toString(), expectedPath.toString());
    }

    @Test
    public void testSingleFileDependencyFilePath() {
        // standalone file -> package_a
        Path projectDirPath = DEPENDENCY_FILEPATH_PROJECTS_DIR.resolve("standalone-main.bal");
        Project project = ProjectLoader.loadProject(projectDirPath);
        Path dependencyFilePath = project.dependencyFilePath("samjs", "package_b", "main.bal");

        Path expectedPath = getBalaPath("samjs", "package_b", "0.1.0")
                .resolve("modules/package_b/main.bal");
        Assert.assertEquals(dependencyFilePath.toString(), expectedPath.toString());

        dependencyFilePath = project.dependencyFilePath("samjs", "package_a.mod_a1", "mod1.bal");
        expectedPath = getBalaPath("samjs", "package_a", "0.1.0")
                .resolve("modules/package_a.mod_a1/mod1.bal");
        Assert.assertEquals(dependencyFilePath.toString(), expectedPath.toString());
    }

    @Test (expectedExceptions = ProjectException.class)
    public void testGetDependencyFilePathInvalidFile() {
        Path projectDirPath = RESOLUTION_PROJECTS_DIR.resolve("package_a");
        BuildProject buildProject = BuildProject.load(projectDirPath);
        Path dependencyFilePath = buildProject.dependencyFilePath("samjs", "package_b", "mod1.bal");
    }

    @Test (expectedExceptions = ProjectException.class)
    public void testGetDependencyFilePathInvalidModule() {
        Path projectDirPath = RESOLUTION_PROJECTS_DIR.resolve("package_a");
        BuildProject buildProject = BuildProject.load(projectDirPath);
        Path dependencyFilePath = buildProject.dependencyFilePath("samjs", "package_b1", "main.bal");
    }

    @Test (expectedExceptions = ProjectException.class)
    public void testGetDependencyFilePathInvalidHierarchicalModule() {
        Path projectDirPath = DEPENDENCY_FILEPATH_PROJECTS_DIR.resolve("standalone-main.bal");
        Project project = ProjectLoader.loadProject(projectDirPath);
        Path dependencyFilePath = project.dependencyFilePath("samjs", "package_a.b", "main.bal");
    }

    @Test (expectedExceptions = ProjectException.class)
    public void testGetDependencyFilePathInvalidOrg() {
        Path projectDirPath = RESOLUTION_PROJECTS_DIR.resolve("package_a");
        BuildProject buildProject = BuildProject.load(projectDirPath);
        Path dependencyFilePath = buildProject.dependencyFilePath("sam", "package_b1", "main.bal");
    }

    @Test (expectedExceptions = ProjectException.class)
    public void testGetDependencyFilePathEmptyValue() {
        Path projectDirPath = RESOLUTION_PROJECTS_DIR.resolve("package_a");
        BuildProject buildProject = BuildProject.load(projectDirPath);
        Path dependencyFilePath = buildProject.dependencyFilePath("samjs", "", "main.bal");
    }
}
