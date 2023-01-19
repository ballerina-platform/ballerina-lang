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
package io.ballerina.projects.test;

import io.ballerina.projects.BallerinaToml;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.util.ProjectConstants;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;

import static io.ballerina.projects.test.TestUtils.isWindows;
import static io.ballerina.projects.test.TestUtils.resetPermissions;

/**
 * Contains cases to test the basic package structure when the project includes generated directory.
 *
 * @since 2201.4.0
 */
public class TestBuildProjectWithGeneratedSources extends BaseTest {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/generated-sources-tests/");

    @Test (description = "tests loading a valid build project with generated sources")
    public void testBuildProjectAPI() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("my_generated_project");

        // 1) Initialize the project instance
        BuildProject project = loadBuildProject(projectPath);
        // 2) Load the package
        Package currentPackage = project.currentPackage();
        // 3) Load the default module
        Module defaultModule = currentPackage.getDefaultModule();
        Assert.assertEquals(defaultModule.documentIds().size(), 4);

        // 4) Get Ballerina.toml file
        Optional<BallerinaToml> ballerinaTomlOptional = currentPackage.ballerinaToml();
        Assert.assertTrue(ballerinaTomlOptional.isPresent());

        int noOfSrcDocuments = 0;
        int noOfTestDocuments = 0;
        final Collection<ModuleId> moduleIds = currentPackage.moduleIds();
        Assert.assertEquals(moduleIds.size(), 4);
        for (ModuleId moduleId : moduleIds) {
            Module module = currentPackage.module(moduleId);
            noOfSrcDocuments += module.documentIds().size();
            noOfTestDocuments += module.testDocumentIds().size();
            for (DocumentId docId : module.documentIds()) {
                Assert.assertNotNull(module.document(docId).syntaxTree());
            }
        }

        Assert.assertEquals(noOfSrcDocuments, 9);
        Assert.assertEquals(noOfTestDocuments, 3);

    }

    @Test (description = "tests loading a build project with generated sources with no read permission")
    public void testBuildProjectWithNoReadPermission() {
        // Skip test in windows due to file permission setting issue
        if (isWindows()) {
            throw new SkipException("Skipping tests on Windows");
        }
        Path projectPath = RESOURCE_DIRECTORY.resolve("project_no_permission");
        Path generatedDirPath = projectPath.resolve(ProjectConstants.GENERATED_MODULES_ROOT);
        // 1) Remove read permission
        boolean readable = generatedDirPath.toFile().setReadable(false, true);
        if (!readable) {
            Assert.fail("could not remove read permission");
        }

        // 2) Initialize the project instance
        BuildProject project = null;
        try {
            project = TestUtils.loadBuildProject(projectPath);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("does not have read permissions"));
        }

        resetPermissions(generatedDirPath);
    }

    @Test (description = "tests loading a valid build project with a new generated module")
    public void testProjectWithNewGeneratedModule() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("new_generated_mod");

        // 1) Initialize the project instance
        BuildProject project = loadBuildProject(projectPath);
        // 2) Load the package
        Package currentPackage = project.currentPackage();
        // 3) Load the default module
        Module defaultModule = currentPackage.getDefaultModule();
        Assert.assertEquals(defaultModule.documentIds().size(), 1);

        // 4) Get Ballerina.toml file
        Optional<BallerinaToml> ballerinaTomlOptional = currentPackage.ballerinaToml();
        Assert.assertTrue(ballerinaTomlOptional.isPresent());

        int noOfSrcDocuments = 0;
        int noOfTestDocuments = 0;
        final Collection<ModuleId> moduleIds = currentPackage.moduleIds();
        Assert.assertEquals(moduleIds.size(), 2);
        for (ModuleId moduleId : moduleIds) {
            Module module = currentPackage.module(moduleId);
            noOfSrcDocuments += module.documentIds().size();
            noOfTestDocuments += module.testDocumentIds().size();
            for (DocumentId docId : module.documentIds()) {
                Assert.assertNotNull(module.document(docId).syntaxTree());
            }
        }
        Assert.assertEquals(noOfSrcDocuments, 2);
        Assert.assertEquals(noOfTestDocuments, 0);
    }
    @AfterClass (alwaysRun = true)
    public void reset() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("project_no_permission");
        TestUtils.resetPermissions(projectPath);
    }

    private static BuildProject loadBuildProject(Path projectPath) {
        return loadBuildProject(projectPath, null);
    }

    private static BuildProject loadBuildProject(Path projectPath, BuildOptions buildOptions) {
        BuildProject buildProject = null;
        try {
            if (buildOptions == null) {
                buildProject = TestUtils.loadBuildProject(projectPath);
            } else {
                buildProject = TestUtils.loadBuildProject(projectPath, buildOptions);
            }
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        return buildProject;
    }
}
