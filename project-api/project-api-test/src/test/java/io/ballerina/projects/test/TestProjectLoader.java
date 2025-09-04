/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.ProjectLoadResult;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.util.ProjectConstants;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;

public class TestProjectLoader {
    private static final Path RESOURCE_DIRECTORY = Path.of("src/test/resources/");

    @Test
    public void testLoadWorkspaceProjectFromRoot() {
        Path workspacePath = RESOURCE_DIRECTORY.resolve("workspaces/wp-multiple-roots");
        ProjectLoadResult loadResult = ProjectLoader.load(workspacePath);
        Assert.assertEquals(loadResult.project().kind(), ProjectKind.WORKSPACE_PROJECT);
        Assert.assertEquals(loadResult.project().sourceRoot(), workspacePath.toAbsolutePath().normalize());
    }

    @Test
    public void testLoadWorkspaceProjectFromWorkspaceBalToml() {
        Path workspacePath = RESOURCE_DIRECTORY.resolve("workspaces/wp-multiple-roots");
        ProjectLoadResult loadResult = ProjectLoader.load(workspacePath.resolve(ProjectConstants.BALLERINA_TOML));
        Assert.assertEquals(loadResult.project().kind(), ProjectKind.WORKSPACE_PROJECT);
        Assert.assertEquals(loadResult.project().sourceRoot(), workspacePath.toAbsolutePath().normalize());
    }

    @Test
    public void testLoadWorkspaceProjectFromPackageFile() {
        Path workspacePath = RESOURCE_DIRECTORY.resolve("workspaces/wp-multiple-roots");

        Path packageFilePath = workspacePath.resolve("bye-app/Ballerina.toml");
        ProjectLoadResult loadResult = ProjectLoader.load(packageFilePath);
        Assert.assertEquals(loadResult.project().kind(), ProjectKind.WORKSPACE_PROJECT);
        Assert.assertEquals(loadResult.project().sourceRoot(), workspacePath.toAbsolutePath().normalize());

        packageFilePath = workspacePath.resolve("depA/Ballerina.toml");
        loadResult = ProjectLoader.load(packageFilePath);
        Assert.assertEquals(loadResult.project().kind(), ProjectKind.WORKSPACE_PROJECT);
        Assert.assertEquals(loadResult.project().sourceRoot(), workspacePath.toAbsolutePath().normalize());
    }

    @Test
    public void testLoadWorkspaceProjectFromModuleFiles() {
        Path workspacePath = RESOURCE_DIRECTORY.resolve("workspaces/wp-multiple-roots");

        Path packageFilePath = workspacePath.resolve("bye-app/main.bal");
        ProjectLoadResult loadResult = ProjectLoader.load(packageFilePath);
        Assert.assertEquals(loadResult.project().kind(), ProjectKind.WORKSPACE_PROJECT);
        Assert.assertEquals(loadResult.project().sourceRoot(), workspacePath.toAbsolutePath().normalize());

        packageFilePath = workspacePath.resolve("depA/depA.bal");
        loadResult = ProjectLoader.load(packageFilePath);
        Assert.assertEquals(loadResult.project().kind(), ProjectKind.WORKSPACE_PROJECT);
        Assert.assertEquals(loadResult.project().sourceRoot(), workspacePath.toAbsolutePath().normalize());

        packageFilePath = workspacePath.resolve("depA/modules/storage/db.bal");
        loadResult = ProjectLoader.load(packageFilePath);
        Assert.assertEquals(loadResult.project().kind(), ProjectKind.WORKSPACE_PROJECT);
        Assert.assertEquals(loadResult.project().sourceRoot(), workspacePath.toAbsolutePath().normalize());

        packageFilePath = workspacePath.resolve("depA/modules/storage/tests/db_tests.bal");
        loadResult = ProjectLoader.load(packageFilePath);
        Assert.assertEquals(loadResult.project().kind(), ProjectKind.WORKSPACE_PROJECT);
        Assert.assertEquals(loadResult.project().sourceRoot(), workspacePath.toAbsolutePath().normalize());
    }

    @Test
    public void testLoadBuildProjectFromRoot() {
        Path packagePath = RESOURCE_DIRECTORY.resolve("myproject");
        ProjectLoadResult loadResult = ProjectLoader.load(packagePath);
        Assert.assertEquals(loadResult.project().kind(), ProjectKind.BUILD_PROJECT);
        Assert.assertEquals(loadResult.project().sourceRoot(), packagePath.toAbsolutePath().normalize());
    }

    @Test
    public void testLoadBuildProjectFromFile() {
        Path packagePath = RESOURCE_DIRECTORY.resolve("myproject");
        Path packageFilePath = packagePath.resolve(ProjectConstants.BALLERINA_TOML);
        ProjectLoadResult loadResult = ProjectLoader.load(packageFilePath);
        Assert.assertEquals(loadResult.project().kind(), ProjectKind.BUILD_PROJECT);
        Assert.assertEquals(loadResult.project().sourceRoot(), packagePath.toAbsolutePath().normalize());

        packageFilePath = packagePath.resolve("main.bal");
        loadResult = ProjectLoader.load(packageFilePath);
        Assert.assertEquals(loadResult.project().kind(), ProjectKind.BUILD_PROJECT);
        Assert.assertEquals(loadResult.project().sourceRoot(), packagePath.toAbsolutePath().normalize());

        packageFilePath = packagePath.resolve(ProjectConstants.DEPENDENCIES_TOML);
        loadResult = ProjectLoader.load(packageFilePath);
        Assert.assertEquals(loadResult.project().kind(), ProjectKind.BUILD_PROJECT);
        Assert.assertEquals(loadResult.project().sourceRoot(), packagePath.toAbsolutePath().normalize());

        packageFilePath = packagePath.resolve("modules/storage/db.bal");
        loadResult = ProjectLoader.load(packageFilePath);
        Assert.assertEquals(loadResult.project().kind(), ProjectKind.BUILD_PROJECT);
        Assert.assertEquals(loadResult.project().sourceRoot(), packagePath.toAbsolutePath().normalize());

        packageFilePath = packagePath.resolve("modules/storage/tests/db_tests.bal");
        loadResult = ProjectLoader.load(packageFilePath);
        Assert.assertEquals(loadResult.project().kind(), ProjectKind.BUILD_PROJECT);
        Assert.assertEquals(loadResult.project().sourceRoot(), packagePath.toAbsolutePath().normalize());
    }

    @Test
    public void testLoadSingleFile() {
        Path filePath = RESOURCE_DIRECTORY.resolve("single_file/main.bal");
        ProjectLoadResult loadResult = ProjectLoader.load(filePath);
        Assert.assertEquals(loadResult.project().kind(), ProjectKind.SINGLE_FILE_PROJECT);
        Assert.assertEquals(loadResult.project().sourceRoot(), filePath.toAbsolutePath().normalize());
    }

    @Test
    public void testLoadBalaFileFromRoot() {
        Path workspacePath = RESOURCE_DIRECTORY.resolve("extracted-bala-project/package_c/0.1.0/any");
        ProjectLoadResult loadResult = ProjectLoader.load(workspacePath);
        Assert.assertEquals(loadResult.project().kind(), ProjectKind.BALA_PROJECT);
        Assert.assertEquals(loadResult.project().sourceRoot(), workspacePath.toAbsolutePath().normalize());
    }

    @Test
    public void testLoadBalaProjectFromFile() {
        Path balaProjectPath = RESOURCE_DIRECTORY.resolve("extracted-bala-project/package_c/0.1.0/any");
        Path packageFilePath = balaProjectPath.resolve("modules/package_c/main.bal");
        ProjectLoadResult loadResult = ProjectLoader.load(packageFilePath);
        Assert.assertEquals(loadResult.project().kind(), ProjectKind.BALA_PROJECT);
        Assert.assertEquals(loadResult.project().sourceRoot(), balaProjectPath.toAbsolutePath().normalize());

        packageFilePath = balaProjectPath.resolve("modules/package_c.mod_c1/mod1.bal");
        loadResult = ProjectLoader.load(packageFilePath);
        Assert.assertEquals(loadResult.project().kind(), ProjectKind.BALA_PROJECT);
        Assert.assertEquals(loadResult.project().sourceRoot(), balaProjectPath.toAbsolutePath().normalize());
    }

    @Test
    public void testBuildProjectInWorkspace() {
        Path packagePath = RESOURCE_DIRECTORY.resolve("workspaces/wp-wrong-path/other/pkgB");
        Path packageFilePath = packagePath.resolve("main.bal");
        ProjectLoadResult loadResult = ProjectLoader.load(packageFilePath);
        Assert.assertEquals(loadResult.project().kind(), ProjectKind.BUILD_PROJECT);
        Assert.assertEquals(loadResult.project().sourceRoot(), packagePath.toAbsolutePath().normalize());
    }

    @Test
    public void testLoadNonExistentPath() {
        try {
            Path filePath = RESOURCE_DIRECTORY.resolve("non-existent.bal");
            ProjectLoader.load(filePath);
        } catch (ProjectException e) {
            Assert.assertTrue(e.getMessage().contains("provided file path does not exist"));
        }

    }
}
