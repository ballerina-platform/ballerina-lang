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
package io.ballerina.projects.test;

import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.directory.BuildProject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * Contains cases to test the basic package structure.
 *
 * @since 2.0.0
 */
public class TestBuildProject {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/");

    @Test (description = "tests loading a valid build project")
    public void testBuildProjectAPI() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject");
        // 1) Initialize the project instance
        BuildProject project = null;
        try {
            project = BuildProject.loadProject(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        // 2) Load the package
        Package currentPackage = project.currentPackage();
        // 3) Load the default module
        Module defaultModule = currentPackage.getDefaultModule();
        Assert.assertEquals(defaultModule.documentIds().size(), 2);

        // TODO find an easy way to test the project structure. e.g. serialize the structure in a json file.
        int noOfSrcDocuments = 0;
        int noOfTestDocuments = 0;
        final Collection<ModuleId> moduleIds = currentPackage.moduleIds();
        Assert.assertEquals(moduleIds.size(), 3);
        for (ModuleId moduleId : moduleIds) {
            Module module = currentPackage.module(moduleId);
            for (DocumentId documentId : module.documentIds()) {
                noOfSrcDocuments++;
            }
            for (DocumentId testDocumentId : module.testDocumentIds()) {
                noOfTestDocuments++;
            }
            for (Document doc : module.documents()) {
                Assert.assertNotNull(doc.syntaxTree());
            }
        }

        Assert.assertEquals(noOfSrcDocuments, 4);
        Assert.assertEquals(noOfTestDocuments, 3);

    }

    @Test (description = "tests loading an invalid Ballerina project")
    public void testLoadBallerinaProjectNegative() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject").resolve("modules").resolve("services")
                .resolve("svc.bal");
        try {
            BuildProject.loadProject(projectPath);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("provided path is not a valid Ballerina project"));
        }

        projectPath = RESOURCE_DIRECTORY.resolve("myproject").resolve("modules").resolve("services");
        try {
            BuildProject.loadProject(projectPath);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("provided path is not a valid Ballerina project"));
        }

        projectPath = RESOURCE_DIRECTORY.resolve("single-file");
        try {
            BuildProject.loadProject(projectPath);
            Assert.fail("expected an invalid project exception");
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("provided path is not a valid Ballerina project"));
        }
    }

    @Test (description = "tests loading another invalid Ballerina project")
    public void testLoadBallerinaProjectInProject() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject").resolve("modules").resolve("services")
                .resolve("resources").resolve("invalidProject");
        try {
            BuildProject.loadProject(projectPath);
            Assert.fail("expected an invalid project exception");
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("provided path is not a valid Ballerina project"));
        }
    }

    @Test (description = "tests loading a valid build project and set build options")
    public void testSetBuildOptions() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject");
        // 1) Initialize the project instance
        BuildProject project = null;
        try {
            project = BuildProject.loadProject(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        BuildProject.BuildOptions buildOptions = project.getBuildOptions();

        // Verify expected default buildOptions
        Assert.assertFalse(buildOptions.isObservabilityIncluded());
        Assert.assertFalse(buildOptions.isSkipTests());
        Assert.assertFalse(buildOptions.isOffline());
        Assert.assertFalse(buildOptions.isTestReport());
        Assert.assertFalse(buildOptions.isCodeCoverage());
        Assert.assertFalse(buildOptions.isSkipLock());
        Assert.assertFalse(buildOptions.isExperimental());
        Assert.assertNull(buildOptions.getB7aConfigFile());

        buildOptions.setObservabilityEnabled(false);
        buildOptions.setSkipLock(true);
        buildOptions.setSkipTests(true);
        buildOptions.setCodeCoverage(true);

        // Update and verify buildOptions
        Assert.assertFalse(project.getBuildOptions().isObservabilityIncluded());
        Assert.assertTrue(project.getBuildOptions().isSkipTests());
        Assert.assertFalse(project.getBuildOptions().isOffline());
        Assert.assertFalse(project.getBuildOptions().isTestReport());
        Assert.assertTrue(project.getBuildOptions().isCodeCoverage());
        Assert.assertTrue(project.getBuildOptions().isSkipLock());
        Assert.assertFalse(project.getBuildOptions().isExperimental());
    }

    @Test (description = "tests loading a valid build project with build options from toml")
    public void testSetBuildOptionsFromToml() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("projectWithBuildOptions");
        // 1) Initialize the project instance
        BuildProject project = null;
        try {
            project = BuildProject.loadProject(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        BuildProject.BuildOptions buildOptions = project.getBuildOptions();

        // Verify expected default buildOptions
        Assert.assertTrue(buildOptions.isObservabilityIncluded());
        Assert.assertTrue(buildOptions.isSkipTests());
        Assert.assertTrue(buildOptions.isExperimental());
        Assert.assertFalse(buildOptions.isOffline());
        Assert.assertFalse(buildOptions.isTestReport());
        Assert.assertFalse(buildOptions.isCodeCoverage());
        Assert.assertFalse(buildOptions.isSkipLock());
        Assert.assertEquals(buildOptions.getB7aConfigFile(), "/tmp/ballerina.conf");

        buildOptions.setObservabilityEnabled(false);
        buildOptions.setSkipLock(true);
        buildOptions.setSkipTests(true);
        buildOptions.setCodeCoverage(true);

        // Update and verify buildOptions
        Assert.assertTrue(project.getBuildOptions().isExperimental());
        Assert.assertFalse(project.getBuildOptions().isObservabilityIncluded());
        Assert.assertTrue(project.getBuildOptions().isSkipTests());
        Assert.assertFalse(project.getBuildOptions().isOffline());
        Assert.assertFalse(project.getBuildOptions().isTestReport());
        Assert.assertTrue(project.getBuildOptions().isCodeCoverage());
        Assert.assertTrue(project.getBuildOptions().isSkipLock());
    }

}
