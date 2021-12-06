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

import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.directory.SingleFileProject;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import static io.ballerina.projects.test.TestUtils.isWindows;
import static io.ballerina.projects.test.TestUtils.resetPermissions;

/**
 * Contains cases to test the basic package structure.
 *
 * @since 2.0.0
 */
public class TestSingleFileProject {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/");

    @Test (description = "tests loading a valid standalone Ballerina file")
    public void testLoadSingleFile() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("single_file").resolve("main.bal");
        SingleFileProject project = null;
        try {
            project = TestUtils.loadSingleFileProject(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        // 2) Load the package
        Package currentPackage = project.currentPackage();
        // 3) Load the default module
        Module defaultModule = currentPackage.getDefaultModule();
        Assert.assertEquals(defaultModule.documentIds().size(), 1);

        Collection<ModuleId> moduleIds = currentPackage.moduleIds();
        Assert.assertEquals(moduleIds.size(), 1);
        Assert.assertEquals(moduleIds.iterator().next(), currentPackage.getDefaultModule().moduleId());

    }

    @Test (description = "tests loading a valid standalone Ballerina file")
    public void testLoadSingleFileInProject() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject").resolve("util").resolve("file-util.bal");
        SingleFileProject project = null;
        try {
            project = TestUtils.loadSingleFileProject(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        // 2) Load the package
        Package currentPackage = project.currentPackage();
        // 3) Load the default module
        Module defaultModule = currentPackage.getDefaultModule();
        Assert.assertEquals(defaultModule.documentIds().size(), 1);

        Collection<ModuleId> moduleIds = currentPackage.moduleIds();
        Assert.assertEquals(moduleIds.size(), 1);
        Assert.assertEquals(moduleIds.iterator().next(), currentPackage.getDefaultModule().moduleId());

    }

    @Test (description = "tests loading an invalid standalone Ballerina file")
    public void testLoadSingleFileNegative() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject").resolve("modules").resolve("services")
                .resolve("svc.bal");
        try {
            TestUtils.loadSingleFileProject(projectPath);
            Assert.fail("expected an invalid project exception");
        } catch (ProjectException e) {
            Assert.assertTrue(e.getMessage().contains("The source file '" + projectPath +
                    "' belongs to a Ballerina package."));
        }

        projectPath = RESOURCE_DIRECTORY.resolve("myproject").resolve("main.bal");
        try {
            TestUtils.loadSingleFileProject(projectPath);
            Assert.fail("expected an invalid project exception");
        } catch (ProjectException e) {
            Assert.assertTrue(e.getMessage().contains("The source file '" + projectPath +
                    "' belongs to a Ballerina package."));
        }
    }

    @Test(description = "tests setting build options to the project")
    public void testDefaultBuildOptions() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("single_file").resolve("main.bal");
        SingleFileProject project = null;
        try {
            project = SingleFileProject.load(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        // Verify expected default buildOptions
        Assert.assertTrue(project.buildOptions().skipTests());
        Assert.assertFalse(project.buildOptions().observabilityIncluded());
        Assert.assertFalse(project.buildOptions().codeCoverage());
        Assert.assertFalse(project.buildOptions().offlineBuild());
        Assert.assertFalse(project.buildOptions().experimental());
        Assert.assertFalse(project.buildOptions().testReport());
    }

    @Test(description = "tests setting build options to the project")
    public void testOverrideBuildOptions() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("single_file").resolve("main.bal");
        SingleFileProject project = null;
        BuildOptions buildOptions = BuildOptions.builder()
                .setSkipTests(true)
                .setObservabilityIncluded(true)
                .build();
        try {
            project = TestUtils.loadSingleFileProject(projectPath, buildOptions);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        // Verify expected overridden buildOptions
        Assert.assertTrue(project.buildOptions().skipTests());
        Assert.assertTrue(project.buildOptions().observabilityIncluded());
        Assert.assertFalse(project.buildOptions().codeCoverage());
        Assert.assertFalse(project.buildOptions().experimental());
        Assert.assertFalse(project.buildOptions().testReport());
    }

    @Test
    public void testUpdateDocument() {
        // Inputs from langserver
        Path filePath = RESOURCE_DIRECTORY.resolve("single_file").resolve("main.bal");
        String newContent = "import ballerina/io;\n";

        // Load the project from document filepath
        SingleFileProject singleFileProject = TestUtils.loadSingleFileProject(filePath);

        // get the
        // document ID
        Module oldModule = singleFileProject.currentPackage().module(
                singleFileProject.currentPackage().moduleIds().iterator().next());
        DocumentId oldDocumentId = oldModule.documentIds().iterator().next();
        Document oldDocument = oldModule.document(oldDocumentId);

        // Update the document
        Document updatedDoc = oldDocument.modify().withContent(newContent).apply();

        Assert.assertEquals(oldDocument.module().documentIds().size(), updatedDoc.module().documentIds().size());
        Assert.assertEquals(oldDocument.module().testDocumentIds().size(),
                updatedDoc.module().testDocumentIds().size());
        for (DocumentId documentId : oldDocument.module().documentIds()) {
            Assert.assertTrue(updatedDoc.module().documentIds().contains(documentId));
            Assert.assertFalse(updatedDoc.module().testDocumentIds().contains(documentId));
        }

        Assert.assertNotEquals(oldDocument, updatedDoc);
        Assert.assertNotEquals(oldDocument.syntaxTree().textDocument().toString(), newContent);
        Assert.assertEquals(updatedDoc.syntaxTree().textDocument().toString(), newContent);

        Package updatedPackage = singleFileProject.currentPackage();
        Assert.assertNotEquals(oldModule.packageInstance(), singleFileProject.currentPackage());
        Assert.assertEquals(updatedPackage.module(oldDocument.module().moduleId()).document(oldDocumentId), updatedDoc);
        Assert.assertEquals(updatedPackage, updatedDoc.module().packageInstance());
    }

    @Test (description = "tests loading a single file with no read permission")
    public void testSingleFileWithNoReadPermission() {
        // Skip test in windows due to file permission setting issue
        if (isWindows()) {
            throw new SkipException("Skipping tests on Windows");
        }
        Path projectPath = RESOURCE_DIRECTORY.resolve("single_file_no_permission").resolve("main.bal");

        // 1) Remove write permission
        boolean readable = projectPath.toFile().setReadable(false, false);
        if (!readable) {
            Assert.fail("could not remove read permission of file");
        }

        // 2) Initialize the project instance
        SingleFileProject project = null;
        try {
            project = TestUtils.loadSingleFileProject(projectPath);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("does not have read permissions"));
        }

        resetPermissions(projectPath);
    }

    @Test (description = "tests diagnostics of a single file")
    public void testDiagnostics() {

        Path filePath = RESOURCE_DIRECTORY.resolve("single_file").resolve("main_with_error.bal");

        // Load the project from document filepath
        SingleFileProject project = TestUtils.loadSingleFileProject(filePath);
        // 2) Load the package
        Package currentPackage = project.currentPackage();

        // 3) Compile the current package
        PackageCompilation compilation = currentPackage.getCompilation();

        Assert.assertEquals(compilation.diagnosticResult().diagnosticCount(), 1);
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_11);
        Assert.assertEquals(jBallerinaBackend.diagnosticResult().diagnosticCount(), 1);

        Assert.assertEquals(
                compilation.diagnosticResult().diagnostics().stream().findFirst().get().location().lineRange()
                        .filePath(), "main_with_error.bal");
    }

    @Test
    public void testProjectRefresh() {
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("projects_for_refresh_tests").resolve("single-file")
                .resolve("main.bal");
        SingleFileProject singleFileProject = TestUtils.loadSingleFileProject(projectDirPath);
        PackageCompilation compilation = singleFileProject.currentPackage().getCompilation();
        int errorCount = compilation.diagnosticResult().errorCount();
        Assert.assertEquals(errorCount, 3);

        BCompileUtil.compileAndCacheBala("projects_for_refresh_tests/package_refresh_two_v2");
        int errorCount2 = singleFileProject.currentPackage().getCompilation().diagnosticResult().errorCount();
        Assert.assertEquals(errorCount2, 3);

        singleFileProject.clearCaches();
        int errorCount3 = singleFileProject.currentPackage().getCompilation().diagnosticResult().errorCount();
        Assert.assertEquals(errorCount3, 0);
    }

    @Test
    public void testProjectDuplicate() {
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("projects_for_refresh_tests").resolve("single-file")
                .resolve("main.bal");
        SingleFileProject project = TestUtils.loadSingleFileProject(projectDirPath);
        Project duplicate = project.duplicate();
        Assert.assertEquals(duplicate.kind(), ProjectKind.SINGLE_FILE_PROJECT);

        Assert.assertNotSame(project, duplicate);
        Assert.assertNotSame(project.currentPackage().project(), duplicate.currentPackage().project());
        Assert.assertNotSame(
                project.currentPackage().project().buildOptions(), duplicate.currentPackage().project().buildOptions());
        Assert.assertNotSame(project.projectEnvironmentContext(),
                duplicate.projectEnvironmentContext());
        Assert.assertNotSame(project.projectEnvironmentContext().getService(CompilerContext.class),
                duplicate.projectEnvironmentContext().getService(CompilerContext.class));
        Assert.assertNotSame(
                PackageCache.getInstance(project.projectEnvironmentContext().getService(CompilerContext.class)),
                PackageCache.getInstance(duplicate.projectEnvironmentContext().getService(CompilerContext.class)));

        Assert.assertNotSame(project.currentPackage(), duplicate.currentPackage());
        Assert.assertEquals(project.currentPackage().packageId(), duplicate.currentPackage().packageId());
        Assert.assertEquals(project.currentPackage().getDefaultModule().moduleId(),
                duplicate.currentPackage().getDefaultModule().moduleId());

        Assert.assertNotSame(project.currentPackage().getDefaultModule(),
                duplicate.currentPackage().getDefaultModule());
        Assert.assertNotSame(project.currentPackage().getDefaultModule().project(),
                duplicate.currentPackage().getDefaultModule().project());
        Assert.assertNotSame(project.currentPackage().getDefaultModule().packageInstance(),
                duplicate.currentPackage().getDefaultModule().packageInstance());

        Assert.assertEquals(project.currentPackage().getDefaultModule().descriptor(),
                duplicate.currentPackage().getDefaultModule().descriptor());
        Assert.assertEquals(project.currentPackage().getDefaultModule().moduleMd().isPresent(),
                duplicate.currentPackage().getDefaultModule().moduleMd().isPresent());

        DocumentId documentId = project.currentPackage().getDefaultModule().documentIds().stream().findFirst().get();
        Assert.assertEquals(documentId,
                duplicate.currentPackage().getDefaultModule().documentIds().stream().findFirst().get());

        Assert.assertNotSame(project.currentPackage().getDefaultModule().document(documentId),
                duplicate.currentPackage().getDefaultModule().document(documentId));
        Assert.assertNotSame(project.currentPackage().getDefaultModule().document(documentId).module(),
                duplicate.currentPackage().getDefaultModule().document(documentId).module());
        Assert.assertNotSame(project.currentPackage().getDefaultModule().document(documentId).syntaxTree(),
                duplicate.currentPackage().getDefaultModule().document(documentId).syntaxTree());

        Assert.assertEquals(project.currentPackage().getDefaultModule().document(documentId).name(),
                duplicate.currentPackage().getDefaultModule().document(documentId).name());
        Assert.assertEquals(
                project.currentPackage().getDefaultModule().document(documentId).syntaxTree().toSourceCode(),
                duplicate.currentPackage().getDefaultModule().document(documentId).syntaxTree().toSourceCode());

        project.currentPackage().getCompilation();
        duplicate.currentPackage().getCompilation();
    }

    @AfterClass(alwaysRun = true)
    public void reset() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("single_file_no_permission");
        resetPermissions(projectPath);
    }
}
