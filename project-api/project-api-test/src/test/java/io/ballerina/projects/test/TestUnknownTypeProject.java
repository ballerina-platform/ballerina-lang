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

import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
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
public class TestUnknownTypeProject {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/");

    @Test (description = "tests loading a file from the default module")
    public void testLoadProjectByDefaultModuleFile() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject").resolve("main.bal");
        Project project = null;
        try {
            project = TestUtils.loadProject(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertTrue(project instanceof BuildProject);
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
        }

        Assert.assertEquals(noOfSrcDocuments, 4);
        Assert.assertEquals(noOfTestDocuments, 3);

    }

    @Test (description = "tests loading a test file from the default module")
    public void testLoadProjectByDefaultModuleTestFile() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject").resolve("tests").resolve("main_tests.bal");
        Project project = null;
        try {
            project = TestUtils.loadProject(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertTrue(project instanceof BuildProject);
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
        }

        Assert.assertEquals(noOfSrcDocuments, 4);
        Assert.assertEquals(noOfTestDocuments, 3);

    }

    @Test (description = "tests loading a file from a non-default module")
    public void testLoadProjectByOtherModulesFile() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject").resolve("modules").resolve("services")
                .resolve("svc.bal");
        Project project = null;
        try {
            project = TestUtils.loadProject(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertTrue(project instanceof BuildProject);
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
        }

        Assert.assertEquals(noOfSrcDocuments, 4);
        Assert.assertEquals(noOfTestDocuments, 3);
    }

    @Test (description = "tests loading a test file from a non-default module")
    public void testLoadProjectByNonDefaultModuleTestFile() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject").resolve("modules").resolve("services")
                .resolve("tests").resolve("svc_tests.bal");
        Project project = null;
        try {
            project = TestUtils.loadProject(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertTrue(project instanceof BuildProject);
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
        }

        Assert.assertEquals(noOfSrcDocuments, 4);
        Assert.assertEquals(noOfTestDocuments, 3);
    }

    @Test (description = "tests loading a Ballerina project")
    public void testLoadProject() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject");
        Project project = null;
        try {
            project = TestUtils.loadProject(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertTrue(project instanceof BuildProject);
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
        }

        Assert.assertEquals(noOfSrcDocuments, 4);
        Assert.assertEquals(noOfTestDocuments, 3);

    }
    @Test (description = "tests loading a valid standalone Ballerina file")
    public void testLoadSingleFile() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("single_file").resolve("main.bal");
        Project project = null;
        try {
            project = TestUtils.loadProject(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertTrue(project.kind() == ProjectKind.SINGLE_FILE_PROJECT);
        project.documentId(projectPath);
        // 2) Load the package
        Package currentPackage = project.currentPackage();
        // 3) Load the default module
        Module defaultModule = currentPackage.getDefaultModule();
        Assert.assertEquals(defaultModule.documentIds().size(), 1);

        Collection<ModuleId> moduleIds = currentPackage.moduleIds();
        Assert.assertEquals(moduleIds.size(), 1);
        Assert.assertEquals(moduleIds.iterator().next(), currentPackage.getDefaultModule().moduleId());

    }

    @Test (description = "tests loading a valid bala project by a default module file")
    public void testLoadBalaProjectByDefaultModuleFile() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("extracted-bala-project/package_c/0.1.0/any")
                .resolve("modules/package_c/main.bal");
        Project project = null;
        try {
            project = TestUtils.loadProject(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertSame(project.kind(), ProjectKind.BALA_PROJECT);
        // 2) Load the package
        Package currentPackage = project.currentPackage();
        // 3) Load the default module
        Module defaultModule = currentPackage.getDefaultModule();
        Assert.assertEquals(defaultModule.documentIds().size(), 1);

        Collection<ModuleId> moduleIds = currentPackage.moduleIds();
        Assert.assertEquals(moduleIds.size(), 3);

    }

    @Test (description = "tests loading a valid bala project by a non-default module file")
    public void testLoadBalaProjectByNonDefaultModuleFile() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("extracted-bala-project/package_c/0.1.0/any")
                .resolve("modules/package_c.mod_c1/mod1.bal");
        Project project = null;
        try {
            project = TestUtils.loadProject(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertSame(project.kind(), ProjectKind.BALA_PROJECT);
        // 2) Load the package
        Package currentPackage = project.currentPackage();
        // 3) Load the default module
        Module defaultModule = currentPackage.getDefaultModule();
        Assert.assertEquals(defaultModule.documentIds().size(), 1);

        Collection<ModuleId> moduleIds = currentPackage.moduleIds();
        Assert.assertEquals(moduleIds.size(), 3);

    }

    @Test (description = "tests loading a valid bala project by the project root directory")
    public void testLoadBalaProjectByBalaRoot() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("extracted-bala-project/package_c/0.1.0/any");
        Project project = null;
        try {
            project = TestUtils.loadProject(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertSame(project.kind(), ProjectKind.BALA_PROJECT);
        // 2) Load the package
        Package currentPackage = project.currentPackage();
        // 3) Load the default module
        Module defaultModule = currentPackage.getDefaultModule();
        Assert.assertEquals(defaultModule.documentIds().size(), 1);

        Collection<ModuleId> moduleIds = currentPackage.moduleIds();
        Assert.assertEquals(moduleIds.size(), 3);
    }

    @Test (description = "tests loading a valid bala project by the modules root directory")
    public void testLoadBalaProjectByBalaModulesRoot() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("extracted-bala-project/package_c/0.1.0/any/modules/package_c");
        Project project = null;
        try {
            project = TestUtils.loadProject(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertSame(project.kind(), ProjectKind.BALA_PROJECT);
        // 2) Load the package
        Package currentPackage = project.currentPackage();
        // 3) Load the default module
        Module defaultModule = currentPackage.getDefaultModule();
        Assert.assertEquals(defaultModule.documentIds().size(), 1);

        Collection<ModuleId> moduleIds = currentPackage.moduleIds();
        Assert.assertEquals(moduleIds.size(), 3);
    }

    @Test (description = "tests loading a valid bala project by non-bal file")
    public void testLoadBalaProjectByNonBalFile() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("extracted-bala-project/package_c/0.1.0/any/package.json");
        Project project = null;
        try {
            project = TestUtils.loadProject(projectPath);
            Assert.fail("project loading with a non-bal file is expected to fail");
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("provided path is not a valid Ballerina source file"));
        }
    }

    @Test (description = "tests loading a valid bala project by non-bal file")
    public void testLoadBalaFile() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("balaloader").resolve("foo-winery-any-0.1.0.bala");
        Project project = null;
        try {
            project = TestUtils.loadProject(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertSame(project.kind(), ProjectKind.BALA_PROJECT);
        // 2) Load the package
        Package currentPackage = project.currentPackage();
        // 3) Load the default module
        Module defaultModule = currentPackage.getDefaultModule();
        Assert.assertEquals(defaultModule.documentIds().size(), 2);

        Collection<ModuleId> moduleIds = currentPackage.moduleIds();
        Assert.assertEquals(moduleIds.size(), 3);
    }
}
